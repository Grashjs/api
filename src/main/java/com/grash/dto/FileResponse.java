package com.grash.dto;

public class FileResponse {
    private String url;

    public FileResponse(String url) {
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
