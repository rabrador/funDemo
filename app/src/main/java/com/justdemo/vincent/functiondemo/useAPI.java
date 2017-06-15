package com.justdemo.vincent.functiondemo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Vincent on 2017/6/12.
 */

public class useAPI {
    public useAPI() {
    }

    public static String covRawToString(InputStream inputStream) {
        String data = null;

        try {
            int size = inputStream.available();

            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            data = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static ArrayList<Touris> parserJsonFromTouris(String sJson) {
        ArrayList<Touris> list = new ArrayList<>();

        try {
            JSONObject obj = new JSONObject(sJson);
            JSONArray array = obj.getJSONArray("data");

            for (int i = 0; i < array.length(); i++) {
                JSONObject subObj = array.getJSONObject(i);

                Touris touris = new Touris(subObj.getString("Name"), subObj.getString("Title"), subObj.getString("Introduction"),
                        subObj.getString("Elong"), subObj.getString("Nlat"));
                list.add(touris);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean isTouchInContain(int width, float touchX, float touchY, float targetX, float targetY) {
        return touchX > width && touchX < targetX + width &&
                touchY > width && touchY < targetY + width;
    }
}
