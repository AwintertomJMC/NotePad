package com.supersmart.notepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RootExternalStorageDirectoryRecyclerViewAdapter extends RecyclerView.Adapter<RootExternalStorageDirectoryRecyclerViewAdapter.RootExternalStorageDirectoryRecyclerViewHolder> {

    private MyFileExplorerActivity myFileExplorerActivity;
    private LayoutInflater layoutInflater;
    List<File> directoryList = new ArrayList<>();
    File chosenDirectory;
    RootExternalStorageDirectoryRecyclerViewAdapter(MyFileExplorerActivity myFileExplorerActivity) {
        this.myFileExplorerActivity = myFileExplorerActivity;
        layoutInflater = LayoutInflater.from(myFileExplorerActivity);
    }
    @NonNull
    @Override
    public RootExternalStorageDirectoryRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //把布局文件给到一个view里，再返回holder
        return new RootExternalStorageDirectoryRecyclerViewHolder(layoutInflater.inflate(R.layout.layout_filelist, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RootExternalStorageDirectoryRecyclerViewHolder holder, int position) {
        File directory = directoryList.get(position);
        holder.file = directory;
        holder.textView.setText(directory.getAbsolutePath());
        if (directory.isDirectory())//如果是目录
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
                chosenDirectory = holder.file;
                myFileExplorerActivity.switchToFileExplorerLayout();
            }
        });
    }

    @Override
    public int getItemCount() {
        return directoryList.size();
    }

    class RootExternalStorageDirectoryRecyclerViewHolder extends RecyclerView.ViewHolder {

        private File file;//设置文件
        private TextView textView;//设置对应的textView
        private ImageView imageView;

        public RootExternalStorageDirectoryRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.fileName);
            imageView = itemView.findViewById(R.id.fileIcon);
        }
    }
}
