package com.justdemo.vincent.functiondemo;

import java.util.ArrayList;

/**
 * Created by Vincent on 2017/6/11.
 */

public class ArChar {
    private int arListSize;
    private ArrayList<ArInfo> dbArInfo = new ArrayList<>();

    public class ArInfo {
        private String name;
        private float latitude;
        private float longitude;
        private float xCoordinate;
        private float yCoordinate;
        private int arQuadrant; // 0:North, 1:east, 2:north, 3:west
        private boolean isShown;
        private int distance;

        public ArInfo(String name, float latitude, float longitude, float xCoordinate, float yCoordinate, int arQuadrant, boolean isShown, int distance) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.xCoordinate = xCoordinate;
            this.yCoordinate = yCoordinate;
            this.arQuadrant = arQuadrant;
            this.isShown = isShown;
            this.distance = distance;
        }

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean getIsShown() {
            return isShown;
        }

        public void setIsShown(boolean isShown) {
            this.isShown = isShown;
        }

        public float getLongitude() {
            return longitude;
        }

        public void setLongitude(float longitude) {
            this.longitude = longitude;
        }

        public float getLatitude() {
            return latitude;
        }

        public void setLatitude(float latitude) {
            this.latitude = latitude;
        }

        public float getxCoordinate() {
            return xCoordinate;
        }

        public void setxCoordinate(float xCoordinate) {
            this.xCoordinate = xCoordinate;
        }

        public float getyCoordinate() {
            return yCoordinate;
        }

        public void setyCoordinate(float yCoordinate) {
            this.yCoordinate = yCoordinate;
        }

        public int getArQuadrant() {
            return arQuadrant;
        }

        public void setArQuadrant(int arQuadrant) {
            this.arQuadrant = arQuadrant;
        }
    }

    public ArChar() {
    }

    public void initArListSize(int size) {
        arListSize = size;
    }

    public void setData(String name, float latitude, float longitude, float xCoordinate, float yCoordinate, int arQuadrant, boolean isShown, int distance) {
        ArInfo arInfo = new ArInfo(name, latitude, longitude, xCoordinate, yCoordinate, arQuadrant, isShown, distance);
        dbArInfo.add(arInfo);
    }

    public int getSize() {
        return dbArInfo.size();
    }

    public String getName(int index) {
        return dbArInfo.get(index).getName();
    }

    public float getLongitude(int index) {
        return dbArInfo.get(index).getLongitude();
    }

    public float getLatitude(int index) {
        return dbArInfo.get(index).getLatitude();
    }

    public float getXCoord(int index) {
        return dbArInfo.get(index).getxCoordinate();
    }

    public float getYCoord(int index) {
        return dbArInfo.get(index).getyCoordinate();
    }

    public void setXYcoord(int index, float x, float y) {
        dbArInfo.get(index).setxCoordinate(x);
        dbArInfo.get(index).setyCoordinate(y);
    }

    public int getQuadrant(int index) {
        return dbArInfo.get(index).getArQuadrant();
    }

    public void setQuadrant(int index, int quad) {
        dbArInfo.get(index).setArQuadrant(quad);
    }

    public boolean getIsShown(int index) {
        return dbArInfo.get(index).getIsShown();
    }

    public void setIsShown(int index, boolean isShown) {
        dbArInfo.get(index).setIsShown(isShown);
    }

    public int getDistance(int index) {
        return dbArInfo.get(index).getDistance();
    }

    public void setDistance(int index, int dist) {
        dbArInfo.get(index).setDistance(dist);
    }
}
