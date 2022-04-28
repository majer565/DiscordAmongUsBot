package com.filipmajewski;

public class MP3Holder {
    private final String name;
    private final String path;

    public MP3Holder(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

}
