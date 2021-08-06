package com.supersmart.notepad;

import static com.supersmart.notepad.CharsetNames.standardCharsetsNames;
import static com.supersmart.notepad.data.AppConsts.NEWFILECODE;
import static com.supersmart.notepad.data.AppConsts.OPENFILECODE;
import static com.supersmart.notepad.data.AppConsts.READDATAFROMFILECODE;
import static com.supersmart.notepad.data.AppConsts.SAVEASCODE;
import static com.supersmart.notepad.data.AppConsts.SAVEFILECODE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


public class MainActivity extends AppCompatActivity {
    private File currentFile;
    private EditText editText;
    private TextView mainFileNameTextView;
    private TextView mainCharsetNameTextView;
    private String currentCharset = "utf-8";
    private char[] chars = new char[1024];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
        mainFileNameTextView = findViewById(R.id.mainFileNameTextView);
        mainCharsetNameTextView = findViewById(R.id.mainCharsetNameTextView);
        mainCharsetNameTextView.setText(currentCharset);
        createNewFile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_popup_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //如果路径来自于自己写的文件目录
        //TODO:编码问题
        if (requestCode == OPENFILECODE && resultCode == RESULT_OK)
        {
            assert data != null;
            currentCharset = data.getStringExtra("encoding");
            mainCharsetNameTextView.setText(currentCharset);
            String filePath = data.getStringExtra("filePath");
            try {
                assert filePath != null;
                File tempFile = new File(filePath);
                if (tempFile.exists())
                {
                    currentFile = new File(filePath);
                    readDataFromFile(currentFile);
                    mainFileNameTextView.setText(currentFile.getName());
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
        else if(requestCode == SAVEASCODE && resultCode == RESULT_OK)
        {
            assert data != null;
            String filePath = data.getStringExtra("filePath");
            currentCharset = data.getStringExtra("encoding");
            assert filePath != null;
            File newFile = new File(filePath);
            saveDataToFile(newFile);
            mainFileNameTextView.setText(currentFile.getName());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case NEWFILECODE:
                    newFileCode();break;
                case OPENFILECODE:
                    openFileCode();break;
                case SAVEFILECODE:
                    saveFileCode();break;
                case SAVEASCODE:
                    saveAsCode();break;
                case READDATAFROMFILECODE:
                    readDataFromFile(currentFile);break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void newFile(MenuItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, NEWFILECODE);
        }
        else {
            newFileCode();
        }
    }

    private void newFileCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_file_dialog_title1);
        if(currentFile == null)
        {
            builder.setMessage(getString(R.string.save_file_dialog_message1,mainFileNameTextView.getText().toString()));
        }
        else
        {
            builder.setMessage(getString(R.string.save_file_dialog_message2,currentFile.getAbsolutePath()));
        }
        builder.setPositiveButton(R.string.save_file_dialog_pos_button1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(currentFile == null)
                {
                    saveAsCode();
                }
                else
                {
                    createNewFile();
                }
            }
        });
        builder.setNegativeButton(R.string.save_file_dialog_neg_button1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                createNewFile();
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

    private void createNewFile() {
        if (currentFile != null) {
            currentFile = null;
        }
        mainFileNameTextView.setText(getString(R.string.new_file_name));
        editText.getText().clear();
    }

    public void openFile(MenuItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, OPENFILECODE);
        }
        else {
            openFileCode();
        }
    }

    private void openFileCode() {
        Intent intent = new Intent(MainActivity.this, MyFileExplorerActivity.class);
        intent.putExtra("openMode",0);//0为读取，1为保存
        intent.putExtra("encoding",currentCharset);
        startActivityForResult(intent,OPENFILECODE);
    }

    public void charsetSelect(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_encoding_title));
        builder.setItems(standardCharsetsNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentCharset = standardCharsetsNames[which];
                mainCharsetNameTextView.setText(standardCharsetsNames[which]);
                if(currentFile!=null)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, READDATAFROMFILECODE);
                    }
                    else {
                        readDataFromFile(currentFile);
                    }
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void saveFile(MenuItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, SAVEFILECODE);
        }
        else {
            saveFileCode();
        }
    }

    private void saveFileCode() {
        if (currentFile == null) {
            saveAsCode();
            return;
        }
        saveDataToFile(currentFile);
    }

    public void saveAs(MenuItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, SAVEASCODE);
        }
        else {
            saveAsCode();
        }
    }

    private void saveAsCode() {
        Intent intent = new Intent(MainActivity.this,MyFileExplorerActivity.class);
        intent.putExtra("openMode",1);//0为读取，1为保存
        intent.putExtra("openedFile",currentFile);
        intent.putExtra("encoding",currentCharset);
        startActivityForResult(intent,SAVEASCODE);
    }

    public void quitApp(MenuItem item) {
        finish();
    }

    private void readDataFromFile(final File file)
    {
        try {
            //TODO:输入文件优化;
            editText.getText().clear();
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,currentCharset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            int i=0;
            while ((i = bufferedReader.read(chars))!=-1)
            {
                editText.append(new String(chars),0,i);
            }
            editText.setSelection(0);
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, getString(R.string.cannot_open_file_toast1,file.getAbsolutePath()), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDataToFile(File file) {
        try {
            OutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, currentCharset);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            int i = 0, lastCharLength = editText.getText().length() % 1024;
            while (i < editText.getText().length() / 1024) {
                editText.getText().getChars(0, 1024, chars, i*1024);
                bufferedWriter.write(chars, i*1024, 1024);
                i += 1;
            }
            editText.getText().getChars(0, lastCharLength, chars, i);
            bufferedWriter.write(chars, i, lastCharLength);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStreamWriter.close();
            fileOutputStream.close();
            Toast.makeText(this,getString(R.string.save_file_success_toast1,file.getAbsolutePath()),Toast.LENGTH_SHORT).show();
            currentFile = file;
            mainCharsetNameTextView.setText(currentCharset);
        } catch (FileNotFoundException e) {
            Toast.makeText(MainActivity.this, getString(R.string.cannot_open_file_toast1,file.getAbsolutePath()), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "IO EXCEPTION:保存失败", Toast.LENGTH_SHORT).show();
        }
    }
}
