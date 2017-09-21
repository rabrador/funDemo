package com.justdemo.vincent.functiondemo;

/**
 * Created by Vincent on 2017/5/4.
 */

public class Touris {
    private String Data;
    private String Name;
    private String Title;
    private String Introduction;
    private String Longitude;
    private String Latitude;
    private int Classification;

    public Touris(String name, String title, String introduction, String longitude, String latitude, int classification) {
        Name = name;
        Title = title;
        Introduction = introduction;
        Longitude = longitude;
        Latitude = latitude;
        Classification = classification;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }

    public int getClassification() {
        return Classification;
    }

    public void setClassification(int classification) {
        Classification = classification;
    }

    public String getName() {
        return Name;

    }

    public void setName(String name) {
        Name = name;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getIntroduction() {
        return Introduction;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public void setIntroduction(String introduction) {
        Introduction = introduction;
    }
}
