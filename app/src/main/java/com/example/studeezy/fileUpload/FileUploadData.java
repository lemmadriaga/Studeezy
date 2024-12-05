package com.example.studeezy.fileUpload;

public class FileUploadData {
    private String userId;  // Add userId field
    private String campus;
    private String course;
    private String year;
    private String semester;
    private String subject;
    private String fileBase64;
    private String fileName;

    public FileUploadData() {}

    public FileUploadData(String userId, String campus, String course, String year, String semester, String subject, String fileBase64, String fileName) {
        this.userId = userId;
        this.campus = campus;
        this.course = course;
        this.year = year;
        this.semester = semester;
        this.subject = subject;
        this.fileBase64 = fileBase64;
        this.fileName = fileName;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFileBase64() {
        return fileBase64;
    }

    public void setFileBase64(String fileBase64) {
        this.fileBase64 = fileBase64;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}