package com.example.studeezy.library;

public class LibraryFile {
    private String base64;
    private String uploaderId;
    private String uploaderName;  // Add this field
    private String filename;

    public LibraryFile() {}

    public LibraryFile(String base64, String uploaderId, String uploaderName, String filename) {
        this.base64 = base64;
        this.uploaderId = uploaderId;
        this.uploaderName = uploaderName;
        this.filename = filename;
    }

    public String getBase64() {
        return base64;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public String getFilename() {
        return filename;
    }
}