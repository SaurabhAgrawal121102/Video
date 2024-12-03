package com.example.video;

public class fileModal
{
String title;
String Uri;

    public fileModal() {
    }

    public fileModal(String title, String uri) {
        this.title = title;
        Uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return Uri;
    }

    public void setUri(String uri) {
        Uri = uri;
    }
}
