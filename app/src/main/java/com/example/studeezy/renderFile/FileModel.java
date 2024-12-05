package com.example.studeezy.renderFile;

public class FileModel {
    private String fileName;
    private String fileBase64;

    public FileModel(String fileName, String fileBase64) {
        this.fileName = fileName;
        this.fileBase64 = fileBase64;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileBase64() {
        return fileBase64;
    }
}