package com.example.findwashroom.entity;

import java.io.Serializable;

import com.tencent.lbssearch.object.Location;

public class CustomSearchResultData implements Serializable {
    private static final long serialVersionUID = 9070687073013182163L;
    private String id;
    private String title;
    private String address;
    private String tel;
    private String category;
    private String type;
    private Location location;
    private double _distance;
    private Pano pano;
    private AdInfo ad_info;
    
    
    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double get_distance() {
        return _distance;
    }

    public void set_distance(double _distance) {
        this._distance = _distance;
    }

    public Pano getPano() {
        return pano;
    }

    public void setPano(Pano pano) {
        this.pano = pano;
    }

    public AdInfo getAd_info() {
        return ad_info;
    }

    public void setAd_info(AdInfo ad_info) {
        this.ad_info = ad_info;
    }
}