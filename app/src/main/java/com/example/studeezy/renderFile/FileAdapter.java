package com.example.studeezy.renderFile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studeezy.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private Context context;
    private List<FileModel> fileList;
    private OnFileClickListener onFileClickListener;

    public FileAdapter(Context context, List<FileModel> fileList, OnFileClickListener onFileClickListener) {
        this.context = context;
        this.fileList = fileList;
        this.onFileClickListener = onFileClickListener;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        FileModel fileModel = fileList.get(position);
        holder.textViewFileName.setText(fileModel.getFileName());

        holder.buttonDownload.setOnClickListener(v -> onFileClickListener.onFileClick(fileModel));

        holder.textViewFileName.setOnClickListener(v -> onFileClickListener.onFileNameClick(fileModel));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFileName;
        Button buttonDownload;

        public FileViewHolder(View itemView) {
            super(itemView);
            textViewFileName = itemView.findViewById(R.id.textViewFileName);
            buttonDownload = itemView.findViewById(R.id.buttonDownload);
        }
    }

    public interface OnFileClickListener {
        void onFileClick(FileModel fileModel);
        void onFileNameClick(FileModel fileModel);
    }
}