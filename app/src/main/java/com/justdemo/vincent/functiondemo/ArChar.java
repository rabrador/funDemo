package com.justdemo.vincent.functiondemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Vincent on 2017/6/11.
 */

public class ArChar {
    private final int EAST_NORTH = 1;
    private final int EAST_SOUTH = 3;
    private final int WEST_SOUTH = 5;
    private final int WEST_NORTH = 7;

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

    public void updateDB(Location myLocat) {
        if (dbArInfo.size() <= 0) {
            throw new IllegalArgumentException("DB size must be > 0");
        }

        calDirectionAndDistance(myLocat);
    }

    private void calCoord() {
        float x, y;

        for (int i = 0; i < dbArInfo.size(); i++) {
            switch (dbArInfo.get(i).getArQuadrant()) {
                case EAST_NORTH:

                    break;
            }
//            dbArInfo.get(i).setXYcoord(i, ((float) sampleXCoord[dispCount]), ((float) sampleYCoord[dispCount]));
        }
    }

    /**
     * N
     * 0
     * 7        1
     * W  6                  2   E
     * 5        3
     * 4
     * E
     * Latitude:22.xxxxxx, Longitude:100.xxxxx
     *
     * @param myLocat
     */
    private void calDirectionAndDistance(Location myLocat) {
        float dist[] = new float[1];

        for (int i = 0; i < dbArInfo.size(); i++) {
            if (dbArInfo.get(i).getLatitude() > myLocat.getLatitude()) {
                if (dbArInfo.get(i).getLongitude() > myLocat.getLongitude()) {
//                            Log.d("org", "east north");
                    dbArInfo.get(i).setArQuadrant(EAST_NORTH);
                } else {
//                            Log.d("org", "west north");
                    dbArInfo.get(i).setArQuadrant(WEST_NORTH);
                }
            } else {
                if (dbArInfo.get(i).getLongitude() > myLocat.getLongitude()) {
//                            Log.d("org", "east south");
                    dbArInfo.get(i).setArQuadrant(EAST_SOUTH);
                } else {
//                            Log.d("org", "west south");
                    dbArInfo.get(i).setArQuadrant(WEST_SOUTH);
                }
            }

            Location.distanceBetween(myLocat.getLatitude(), myLocat.getLongitude(), dbArInfo.get(i).getLatitude(), dbArInfo.get(i).getLongitude(), dist);
            dbArInfo.get(i).setDistance(((int) dist[0]));
        }
    }

    private void createNewObj(Canvas canvas, float x, float y, int index, Bitmap mark) {
        Bitmap b = Bitmap.createScaledBitmap(mark, mark.getWidth() / 2, mark.getHeight() / 2, false);
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setTextSize(50);
        canvas.drawBitmap(b, x, y, contentPaint);
        canvas.drawText(dbArInfo.get(index).getName(), x + 110, y + 150, contentPaint);
        canvas.drawText("距離為 : " + dbArInfo.get(index).getDistance() + "公尺", x + 110, y + 200, contentPaint);
    }
}
