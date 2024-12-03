package com.example.video;



public class viewmodal  {
String title,videourl;

    public viewmodal() {
    }

    public viewmodal(String title, String videourl) {
        this.title = title;
        this.videourl = videourl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }
}
