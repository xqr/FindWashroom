package com.example.findwashroom.entity;

import java.io.Serializable;

public class Pano implements Serializable {
    private static final long serialVersionUID = 9070687073013182163L;
    private String id;
    private int heading;
    private int pitch;
    private int zoom;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getHeading() {
        return heading;
    }
    public void setHeading(int heading) {
        this.heading = heading;
    }
    public int getPitch() {
        return pitch;
    }
    public void setPitch(int pitch) {
        this.pitch = pitch;
    }
    public int getZoom() {
        return zoom;
    }
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
}