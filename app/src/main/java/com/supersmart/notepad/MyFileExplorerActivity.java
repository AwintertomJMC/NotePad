package com.supersmart.notepad;

import androidx.annotation.LayoutRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;

import static com.supersmart.notepad.CharsetNames.standardCharsetsNames;

import com.supersmart.notepad.data.FileInfo;
import com.supersmart.notepad.util.FileInfoUtil;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class MyFileExplorerActivity extends AppCompatActivity {
    private FileMenuRecyclerViewAdapter fileMenuRecyclerViewAdapter;
    private RootExternalStorageDirectoryRecyclerViewAdapter rootExternalStorageDirectoryRecyclerViewAdapter;
    private int mode;
    private String[] sizes = {"B","KB","MB","GB","TB"};
    private TextView encodingTextView;
    private boolean isSelectRootDirectoryLayout = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchToRootExternalStorageDirectoryLayout();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void switchToRootExternalStorageDirectoryLayout() {
        setContentView(R.layout.layout_select_directory);
        rootExternalStorageDirectoryRecyclerViewAdapter = new RootExternalStorageDirectoryRecyclerViewAdapter(this);
        rootExternalStorageDirectoryRecyclerViewAdapter.directoryList.add(Environment.getExternalStorageDirectory());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rootExternalStorageDirectoryRecyclerViewAdapter.directoryList.add(Environment.getStorageDirectory());
        }
        RecyclerView recyclerView = findViewById(R.id.rootExternalStorageDirectoryListRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(rootExternalStorageDirectoryRecyclerViewAdapter);
        isSelectRootDirectoryLayout = true;
    }

    public void switchToFileExplorerLayout() {
        //切换并显示界面
        this.setContentView(R.layout.activity_my_file_explorer);
        fileMenuRecyclerViewAdapter = new FileMenuRecyclerViewAdapter(this,rootExternalStorageDirectoryRecyclerViewAdapter.chosenDirectory);
        fileMenuRecyclerViewAdapter.fileNameEditText = findViewById(R.id.fileNameEditText);
        fileMenuRecyclerViewAdapter.filePathTextView = findViewById(R.id.currentDirTextView);
        fileMenuRecyclerViewAdapter.fileTypeEditText = findViewById(R.id.fileTypeEditText);
        RecyclerView fileMenuRecyclerView = findViewById(R.id.fileMenuRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        fileMenuRecyclerView.setLayoutManager(linearLayoutManager);
        fileMenuRecyclerView.setAdapter(fileMenuRecyclerViewAdapter);

        mode = getIntent().getIntExtra("openMode",0);
        if (mode == 0)
        {
            ((TextView) findViewById(R.id.fileExplorerTitle)).setText(R.string.file_explorer_title_read);
        }
        else
        {
            ((TextView) findViewById(R.id.fileExplorerTitle)).setText(R.string.file_explorer_title_save);
            File file = (File) getIntent().getSerializableExtra("openedFile");
            if(file!=null)
            {
                FileInfo fileInfoFromFile = FileInfoUtil.getFileInfoFromFile(file);
                fileMenuRecyclerViewAdapter.fileNameEditText.setText(fileInfoFromFile.getFileName());
                fileMenuRecyclerViewAdapter.fileTypeEditText.setText(fileInfoFromFile.getFileType());
            }
        }

        ((TextView) findViewById(R.id.currentDirTextView)).setText(fileMenuRecyclerViewAdapter.currentDirectory.getAbsolutePath());
        encodingTextView = findViewById(R.id.encodingTextView);
        encodingTextView.setText(getIntent().getStringExtra("encoding"));
        isSelectRootDirectoryLayout = false;
    }

    public void goBack(View view)
    {
        //如果到根目录
        if (isSelectRootDirectoryLayout) {
            finish();
        } else {
            if (fileMenuRecyclerViewAdapter.currentDirectory.equals(fileMenuRecyclerViewAdapter.externalStorageRootDirectory)||fileMenuRecyclerViewAdapter.currentDirectory.getParentFile() == null)
            {
//            Toast.makeText(this, R.string.file_explorer_root_dir_toast, Toast.LENGTH_SHORT).show();
                switchToRootExternalStorageDirectoryLayout();
            }
            else
            {
                fileMenuRecyclerViewAdapter.currentDirectory = fileMenuRecyclerViewAdapter.currentDirectory.getParentFile();
                fileMenuRecyclerViewAdapter.fileList = fileMenuRecyclerViewAdapter.currentDirectory.listFiles();
                fileMenuRecyclerViewAdapter.notifyDataSetChanged();
                ((TextView) findViewById(R.id.currentDirTextView)).setText(fileMenuRecyclerViewAdapter.currentDirectory.getAbsolutePath());
            }
        }
    }

    public void okToOperateFile(View view)
    {
        String fullFileName = FileInfoUtil.getFullPathOfFile(fileMenuRecyclerViewAdapter.currentDirectory.getAbsolutePath(),
                fileMenuRecyclerViewAdapter.fileNameEditText.getText().toString(),
                fileMenuRecyclerViewAdapter.fileTypeEditText.getText().toString());
        File file = new File(fullFileName);
        switch (mode) {
            //读取模式
            case 0: {
                if(file.isDirectory())
                {
                    return;
                }
                if (!file.exists())
                {
                    Toast.makeText(this, getString(R.string.file_explorer_file_cannot_found_toast,
                            fullFileName),
                            Toast.LENGTH_SHORT).show();
                }else
                {
                    if (file.length() >= 102400)//返回的是字节多少，超出100K即102400
                    {
                        showReadWarningDialog(fullFileName);
                    } else//文件太大则提示用户，如果用户同意，则继续执行
                    {
                        returnResult(fullFileName);
                    }
                }
                break;
            }
            //保存模式
            case 1: {
                if (file.exists()) {
                    //另存为提示文件是否覆盖
                    showWriteWarningDialog(fullFileName);
                }
                else
                {
                    returnResult(fullFileName);
                }
                break;
            }
        }
    }
    public void cancel(View view)
    {
        finish();
    }
    //创建文件夹时弹出输入框
    public void createFolder(MenuItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setFocusable(true);
        builder.setView(editText);
        builder.setTitle(R.string.file_explorer_dialog_title1);
        builder.setMessage(R.string.file_explorer_dialog_message1);
        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(fileMenuRecyclerViewAdapter.currentDirectory.getAbsolutePath()+"/"+editText.getText().toString());
                if(file.exists())
                {
                    Toast.makeText(MyFileExplorerActivity.this,getString(R.string.file_explorer_dir_already_exists_toast,file.getName()),Toast.LENGTH_SHORT).show();
                    fileMenuRecyclerViewAdapter.fileList = fileMenuRecyclerViewAdapter.currentDirectory.listFiles();
                    fileMenuRecyclerViewAdapter.notifyDataSetChanged();
                }
                else
                {
                    if (file.mkdir())
                    {
                        Toast.makeText(MyFileExplorerActivity.this,
                                getString(R.string.file_explorer_dir_create_success_toast,file.getName()),Toast.LENGTH_SHORT)
                                .show();
                        fileMenuRecyclerViewAdapter.fileList = fileMenuRecyclerViewAdapter.currentDirectory.listFiles();
                        fileMenuRecyclerViewAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        Toast.makeText(MyFileExplorerActivity.this,getString(R.string.file_explorer_dir_create_failed_toast,file.getName()),Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void sort(MenuItem item) {
        if(!fileMenuRecyclerViewAdapter.sorted)
        {
            Arrays.sort(fileMenuRecyclerViewAdapter.fileList,FileComparator.fileComparatorByName);
        }
        else
        {
            if (fileMenuRecyclerViewAdapter.fileList != null) {
                for (int i=0;i<fileMenuRecyclerViewAdapter.fileList.length/2;++i)
                {
                    File temp1 = fileMenuRecyclerViewAdapter.fileList[i];
                    fileMenuRecyclerViewAdapter.fileList[i] = fileMenuRecyclerViewAdapter.fileList[fileMenuRecyclerViewAdapter.fileList.length-1-i];
                    fileMenuRecyclerViewAdapter.fileList[fileMenuRecyclerViewAdapter.fileList.length-1-i] = temp1;
                }
            }
        }
        fileMenuRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void showPopUpMenuInFileExplorer(View view)
    {
        PopupMenu popupMenu = new PopupMenu(this,view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.file_explorer_popup_menu, popupMenu.getMenu());
        popupMenu.show();
    }
    public void chooseCharset(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_encoding_title);
        builder.setItems(standardCharsetsNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                encodingTextView.setText(standardCharsetsNames[which]);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void showReadWarningDialog(final String filePath)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.file_explorer_dialog_title2);
        builder.setMessage(getString(R.string.file_explorer_dialog_message2,showFileLength(filePath)));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                returnResult(filePath);
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void showWriteWarningDialog(final String filePath)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.file_explorer_dialog_title3);
        builder.setMessage(getString(R.string.file_explorer_dialog_message3,new File(filePath).getName()));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                returnResult(filePath);
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private String showFileLength(String filePath)
    {
        File file = new File(filePath);
        //如果是文件则计算大小
        if(file.isFile())
        {
            int i=0;
            float length = (float)file.length();
            while (length>=1024)
            {
                length = length/1024;
                i++;
            }
            if(i>sizes.length) i = sizes.length;
            return length+sizes[i];
        }
        return "NOT A FILE";
    }
    private void returnResult(String filePath)
    {
        Intent intent = new Intent();
        intent.putExtra("filePath",filePath);
        intent.putExtra("encoding",encodingTextView.getText().toString());
        setResult(RESULT_OK,intent);
        finish();
    }
}
