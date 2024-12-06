package com.example.studeezy.library;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studeezy.R;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder> {

    private List<LibraryFile> fileList;
    private Context context;

    public LibraryAdapter(Context context, List<LibraryFile> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library, parent, false);
        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        LibraryFile file = fileList.get(position);

        holder.filenameTextView.setText(file.getFilename());
        holder.uploaderTextView.setText("Uploader: " + file.getUploaderName());

        holder.filenameTextView.setOnClickListener(v -> {
            // Decode the base64 string to byte array
            byte[] decodedBytes = Base64.decode(file.getBase64(), Base64.NO_WRAP);

            ((Library) context).showPdfPreviewDialog(decodedBytes);
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class LibraryViewHolder extends RecyclerView.ViewHolder {

        TextView filenameTextView;
        TextView uploaderTextView;

        public LibraryViewHolder(View itemView) {
            super(itemView);
            filenameTextView = itemView.findViewById(R.id.filenameTextView);
            uploaderTextView = itemView.findViewById(R.id.uploaderTextView);
        }
    }
}