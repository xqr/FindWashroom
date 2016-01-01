package com.example.findwashroom.entity;

import java.io.Serializable;

public class AdInfo implements Serializable {
    private static final long serialVersionUID = 9070687073013182163L;
    private String adcode;
    private String province;
    private String city;
    private String district;
    public String getAdcode() {
        return adcode;
    }
    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }
    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getDistrict() {
        return district;
    }
    public void setDistrict(String district) {
        this.district = district;
    }
}

