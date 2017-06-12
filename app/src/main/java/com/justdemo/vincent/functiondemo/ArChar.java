package com.justdemo.vincent.functiondemo;

import java.util.ArrayList;

/**
 * Created by Vincent on 2017/6/11.
 */

public class ArChar {
    private int arListSize;

    ArrayList<ArInfo> dbArInfo = new ArrayList<>();

    private class ArInfo {
        private int longitude;
        private int latitude;
        private float xCoordinate;
        private float yCoordinate;
        private int arQuadrant; // 0:North, 1:east, 2:north, 3:west

        public ArInfo(int longitude, int latitude, float xCoordinate, float yCoordinate, int arQuadrant) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.xCoordinate = xCoordinate;
            this.yCoordinate = yCoordinate;
            this.arQuadrant = arQuadrant;
        }

        public int getLongitude() {
            return longitude;
        }

        public void setLongitude(int longitude) {
            this.longitude = longitude;
        }

        public int getLatitude() {
            return latitude;
        }

        public void setLatitude(int latitude) {
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

    public void setData(int longitude, int latitude, float xCoordinate, float yCoordinate, int arQuadrant) {
        ArInfo arInfo = new ArInfo(longitude, latitude, xCoordinate, yCoordinate, arQuadrant);
        dbArInfo.add(arInfo);
    }

}
