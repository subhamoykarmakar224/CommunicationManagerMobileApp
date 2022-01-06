package com.subhamoykarmakar.dm.v2.bean;

public class LatLong {
    private String latitude;
    private String longitude;
    private String isStart;

    public LatLong(String latitude, String longitude, String isStart) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isStart = isStart;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getIsStart() {
        return isStart;
    }

    public void setIsStart(String isStart) {
        this.isStart = isStart;
    }

    @Override
    public String toString() {
        return "LatLong{" +
                "latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", isStart='" + isStart + '\'' +
                '}';
    }
}
