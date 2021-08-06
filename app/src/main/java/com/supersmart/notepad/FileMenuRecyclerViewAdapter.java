package com.supersmart.notepad;

import android.content.Context;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.supersmart.notepad.data.FileInfo;
import com.supersmart.notepad.util.FileInfoUtil;

import java.io.File;
import java.util.Arrays;

public class FileMenuRecyclerViewAdapter extends RecyclerView.Adapter<FileMenuRecyclerViewAdapter.FileDirectoryViewHolder> {
    private LayoutInflater layoutInflater;
    File externalStorageRootDirectory;
    File currentDirectory, fileList[];//获取该目录下的文件列表
    EditText fileNameEditText;//用来和MyFileExplorerActivity联动
    TextView filePathTextView;
    EditText fileTypeEditText;
    boolean sorted;

    FileMenuRecyclerViewAdapter(Context context, File externalStorageRootDirectory) {
        layoutInflater = LayoutInflater.from(context);
        this.currentDirectory = this.externalStorageRootDirectory = externalStorageRootDirectory;
        fileList = currentDirectory.listFiles();
        if (fileList != null && fileList.length > 0) {
            Arrays.sort(fileList, FileComparator.fileComparatorByName);
        }
        sorted = true;
    }

    class FileDirectoryViewHolder extends RecyclerView.ViewHolder {//view所有器
        private File file;//设置文件
        private TextView textView;//设置对应的textView
        private ImageView imageView;

        FileDirectoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.fileName);
            imageView = itemView.findViewById(R.id.fileIcon);
        }
    }

    @NonNull
    @Override
    public FileDirectoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//创建View时的操作
        View view = layoutInflater.inflate(R.layout.layout_filelist, null);//把布局文件给到一个view里，再返回holder
        return new FileDirectoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FileDirectoryViewHolder holder, final int position) {//设置view内容时的操作
        final File file = fileList[position];
        holder.textView.setText(file.getName());
        holder.file = file;
        if (file.isDirectory())//如果是目录
        {
            holder.imageView.setImageResource(R.mipmap.folder);//设置成这个图标
        } else //如果是文件
        {
            holder.imageView.setImageResource(R.mipmap.file);
        }
        //设置事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.file.isDirectory()) {
                    currentDirectory = holder.file;
                    fileList = currentDirectory.listFiles();
                    notifyDataSetChanged();//提醒数据集改变
                    filePathTextView.setText(currentDirectory.getAbsolutePath());
                } else {
                    //TODO:文件读取的问题
                    //TODO:排序修改
                    FileInfo fileInfoFromFile = FileInfoUtil.getFileInfoFromFile(holder.file);
                    fileNameEditText.setText(fileInfoFromFile.getFileName());
                    fileTypeEditText.setText(fileInfoFromFile.getFileType());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList != null ? fileList.length : 0;
    }
}
