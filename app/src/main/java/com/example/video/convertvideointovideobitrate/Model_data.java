package com.example.video.convertvideointovideobitrate;

public class Model_data
{
     String videoId,hlsUrl,description,timeStamp,videoUrl;

    public Model_data() {
    }

    public Model_data(String videoId, String hlsUrl, String description, String timeStamp, String videoUrl) {
        this.videoId = videoId;
        this.hlsUrl = hlsUrl;
        this.description = description;
        this.timeStamp = timeStamp;
        this.videoUrl = videoUrl;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getHlsUrl() {
        return hlsUrl;
    }

    public void setHlsUrl(String hlsUrl) {
        this.hlsUrl = hlsUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
