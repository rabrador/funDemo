package com.justdemo.vincent.functiondemo;

/**
 * Created by Vincent on 2017/5/4.
 */

public class Touris {
    String Data;
    String Name;
    String Title;
    String Introduction;

    public Touris(String name, String title, String introduction) {
        Name = name;
        Title = title;
        Introduction = introduction;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
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

    public void setIntroduction(String introduction) {
        Introduction = introduction;
    }
}
