package com.example.layoutoverlaytest2.Models.Video;

import android.graphics.Bitmap;
import android.net.Uri;

public class VideoModel {
    Uri contentUri;
    String titleVideo;
    Bitmap thumbnailVideo;
    String durationVideo;
    String sizeVideo;
    String pathVideo;
    String widthVideo;
    String heightVideo;

    public VideoModel(Uri contentUri, String titleVideo, Bitmap thumbnailVideo, String durationVideo, String sizeVideo, String pathVideo, String widthVideo, String heightVideo) {
        this.contentUri = contentUri;
        this.titleVideo = titleVideo;
        this.thumbnailVideo = thumbnailVideo;
        this.durationVideo = durationVideo;
        this.sizeVideo = sizeVideo;
        this.pathVideo = pathVideo;
        this.widthVideo = widthVideo;
        this.heightVideo = heightVideo;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }

    public String getTitleVideo() {
        return titleVideo;
    }

    public void setTitleVideo(String titleVideo) {
        this.titleVideo = titleVideo;
    }

    public Bitmap getThumbnailVideo() {
        return thumbnailVideo;
    }

    public void setThumbnailVideo(Bitmap thumbnailVideo) {
        this.thumbnailVideo = thumbnailVideo;
    }

    public String getDurationVideo() {
        return durationVideo;
    }

    public void setDurationVideo(String durationVideo) {
        this.durationVideo = durationVideo;
    }

    public String getSizeVideo() {
        return sizeVideo;
    }

    public void setSizeVideo(String sizeVideo) {
        this.sizeVideo = sizeVideo;
    }

    public String getPathVideo() {
        return pathVideo;
    }

    public void setPathVideo(String pathVideo) {
        this.pathVideo = pathVideo;
    }

    public String getWidthVideo() {
        return widthVideo;
    }

    public void setWidthVideo(String widthVideo) {
        this.widthVideo = widthVideo;
    }

    public String getHeightVideo() {
        return heightVideo;
    }

    public void setHeightVideo(String heightVideo) {
        this.heightVideo = heightVideo;
    }
}
