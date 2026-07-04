package com.example.nutrimind;
//for diary entry model class
public class DiaryModel {

    private String documentId;
    private String feeling;
    private long timestamp;

    // Firestore requires a no-argument constructor
    public DiaryModel() {}

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFeeling() {
        return feeling;
    }

    public void setFeeling(String feeling) {
        this.feeling = feeling;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
