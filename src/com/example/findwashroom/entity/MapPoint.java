package com.example.findwashroom.entity;

import com.tencent.lbssearch.object.Location;
import com.tencent.mapsdk.raster.model.LatLng;

/**
 * 地图上的点
 *
 */
public class MapPoint {
    private LatLng latlng;
    private String address;
    private String name;
    private int distance;
    
    public MapPoint() {
        
    }

    public MapPoint(double lat, double lng, String address) {
        latlng = new LatLng(lat, lng);
        this.address = address;
    }
    
    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public Location changeToLocation() {
        if (latlng == null) {
            return null;
        }
        return new Location((float)latlng.getLatitude(), (float)latlng.getLongitude());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
