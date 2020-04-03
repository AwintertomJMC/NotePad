package com.supersmart.notepad;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static com.supersmart.notepad.CharsetNames.standardCharsetsNames;

public class MainActivity extends AppCompatActivity {
    private File file;
    private EditText editText;
    private TextView mainFileNameTextView;
    private TextView mainCharsetNameTextView;
    private PopupWindow popupWindow;
    private boolean popupShowing;
    private static final int REQUESTPERMISSIONCODE = 0x001;
    private static final int OPENFILEBYORIGINAL = 0x002;
    private static final int OPENFILEBYMYSELF = 0x003;
    private static final int SAVEAS = 0x004;
    private static final String[] TXTFILELISTS = {"asp","bat","cmd","com","c","cpp","git","gitignore","htm","html","log","text","txt","sh",
    "clj","cljs","cljc","cljx","clojure","edn",
    "coffee","cson","iced",
    "json","code-workspace","code-snippets","hintrc","babelrc","jsonc","eslintrc"};
    private String charset = "utf-8";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
        mainFileNameTextView = findViewById(R.id.mainFileNameTextView);
        mainCharsetNameTextView = findViewById(R.id.mainCharsetNameTextView);
        mainCharsetNameTextView.setText(charset);
        newFile();
        popupWindow = new PopupWindow(null, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(LayoutInflater.from(this).inflate(R.layout.main_menu,null));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupShowing = false;
            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }
    public void showMainMenu(View view)
    {
        if(popupShowing)
        {
            popupWindow.dismiss();
            popupShowing = false;
        }
        else
        {
            popupWindow.showAsDropDown(view,0,0);
            popupShowing = true;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void newFile(final View view)
    {
        if(askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.save_file_dialog_title1);
            if(file == null)
            {
                builder.setMessage(getString(R.string.save_file_dialog_message1,mainFileNameTextView.getText().toString()));
            }
            else
            {
                builder.setMessage(getString(R.string.save_file_dialog_message2,file.getAbsolutePath()));
            }
            builder.setPositiveButton(R.string.save_file_dialog_pos_button1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if(file == null)
                    {
                        saveAs(view);
                    }
                    else
                    {
                        try {
                            if(checkIfTXTFile(file))
                            {
                                if(saveFile(file))
                                {
                                    newFile();
                                }
                            }
                            else
                            {
                                saveNonTXTFileInfo();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            builder.setNegativeButton(R.string.save_file_dialog_neg_button1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    newFile();
                }
            });
            builder.setNeutralButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }
    public void quitApp(View view)
    {
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void saveFile(View view) throws IOException {
        if (askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            if(file == null)
            {
                //新文件，调用保存到目录的方法。
                saveAs(view);
            }
            else
            {
                //打开的文件，调用自身的保存方法。
//                System.out.println(file.getName());
                //判断文件后缀名
                if(checkIfTXTFile(file))
                {
                    saveAlertInfo();
                }
                else
                {
                    saveNonTXTFileInfo();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void openMyFile(View view)
    {
        if(askPermission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            Intent intent = new Intent(MainActivity.this, MyFileExplorerActivity.class);
            intent.putExtra("openMode",0);//0为读取，1为保存
            intent.putExtra("encoding",charset);
            startActivityForResult(intent,OPENFILEBYMYSELF);
        }
    }
    public void saveAs(View view)
    {
        Intent intent = new Intent(MainActivity.this,MyFileExplorerActivity.class);
        intent.putExtra("openMode",1);//0为读取，1为保存
        intent.putExtra("openedFile",file);
        intent.putExtra("encoding",charset);
        startActivityForResult(intent,SAVEAS);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void chooseCharsetInMain(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_encoding_title));
        builder.setItems(standardCharsetsNames, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                encodingTextView.setText(standardCharsetsNames[which]);
                charset = standardCharsetsNames[which];
                mainCharsetNameTextView.setText(charset);
                if(file!=null)
                {
                    readFile(file);
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //如果返回的是来自系统自带方式读取的路径
        if (requestCode==OPENFILEBYORIGINAL&&resultCode == Activity.RESULT_OK)
        {
            assert data != null;
            Uri uri = data.getData();
            String halfPath = uri.getPath().split(":")[0];
            String[] resolvedHalfPathStrings = halfPath.split("/");
            String pathType = resolvedHalfPathStrings[resolvedHalfPathStrings.length-1];
            if (pathType.equals("primary"))
            {
                file = new File(Environment.getExternalStorageDirectory()+"/"+ uri.getPath().split(":")[1]);
            }
            else
            {
                file = new File("/storage/"+pathType+"/"+ uri.getPath().split(":")[1]);
                Toast.makeText(this, "目前只能读取模式", Toast.LENGTH_SHORT).show();
            }
            readFile(file);
        }
        //如果路径来自于自己写的文件目录
        else if (requestCode == OPENFILEBYMYSELF && resultCode == RESULT_OK)
        {
            assert data != null;
            charset = data.getStringExtra("encoding");
            mainCharsetNameTextView.setText(charset);
            String filePath = data.getStringExtra("filePath");
            try {
                assert filePath != null;
                File tempFile = new File(filePath);
                if (tempFile.exists())
                {
                    file = new File(filePath);
                    readFile(file);
                    mainFileNameTextView.setText(file.getName());
                }
                else
                {
                    Toast.makeText(this, getString(R.string.cannot_open_file_toast1,filePath), Toast.LENGTH_SHORT).show();
                }
            }catch (NullPointerException e)
            {
                Toast.makeText(this, getString(R.string.cannot_open_file_toast1,filePath), Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == SAVEAS && resultCode == RESULT_OK)
        {
            assert data != null;
            String filePath = data.getStringExtra("filePath");
            assert filePath != null;
            file = new File(filePath);
            try {
                saveFile(file);
                mainFileNameTextView.setText(file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean askPermission(String... permissionName)
    {
        boolean flag = true;
        if (Build.VERSION.SDK_INT >= 23)
        {
            final Context context = getApplicationContext();
            requestPermissions(permissionName,REQUESTPERMISSIONCODE);
            for (String permission : permissionName) {
                int i = ContextCompat.checkSelfPermission(context,permission);
                if (i != PackageManager.PERMISSION_GRANTED)
                {
                    flag = false;
                }
            }
            if(!flag)
            {
                ActivityCompat.requestPermissions(this,permissionName,REQUESTPERMISSIONCODE);
            }
        }
        return flag;
    }
    private void readFile(final File file)
    {
        try {
            //TODO:查一下如何复制粘贴文件和fileInputStream.read();
            editText.getText().clear();
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);
            int i=0;
            char[] chars = new char[1024];
            while ((i = inputStreamReader.read(chars))!=-1)
            {
                editText.append(new String(chars),0,i);
            }
            editText.setSelection(0);
        } catch (FileNotFoundException e) {
            Toast.makeText(MainActivity.this, getString(R.string.cannot_open_file_toast1,file.getAbsolutePath()), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, getString(R.string.cannot_open_file_toast1,file.getAbsolutePath()), Toast.LENGTH_SHORT).show();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean saveFile(File file) throws IOException {
        if (askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, charset);
                outputStreamWriter.write(editText.getText().toString());
                outputStreamWriter.flush();
                outputStreamWriter.close();
                fileOutputStream.close();
                Toast.makeText(this,getString(R.string.save_file_success_toast1,file.getAbsolutePath()),Toast.LENGTH_SHORT).show();
                return true;
            } catch (FileNotFoundException e) {
                Toast.makeText(MainActivity.this, getString(R.string.cannot_open_file_toast1,file.getAbsolutePath()), Toast.LENGTH_SHORT).show();
                return false;
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "IO EXCEPTION:保存失败", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }
    private void newFile()
    {
        if(file!=null) file = null;
        mainFileNameTextView.setText(getString(R.string.new_file_name));
        editText.getText().clear();
    }
    private void saveAlertInfo()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.save_file_dialog_title2));
        builder.setMessage(getString(R.string.save_file_dialog_message2,file.getAbsolutePath()));
        builder.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    saveFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void saveNonTXTFileInfo()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_file_dialog_title2);
        builder.setMessage(R.string.save_file_dialog_warning);
        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private boolean checkIfTXTFile(File file)
    {
        String fileName = file.getName();
        for (String suffix:TXTFILELISTS)
        {
            if (fileName.endsWith(suffix))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(popupWindow != null)
        {
            popupWindow.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
