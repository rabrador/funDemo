package com.justdemo.vincent.functiondemo;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Vincent on 2017/6/5.
 */

public class LocalDb {
    private String data;
    private Double[] dbgLocatX;
    private Double[] dbgLocatY;

    public LocalDb() {

    }

    public String covRawToString(InputStream inputStream) {

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

    public void dbgSetRandomXY() {
        for (int i = 0; i < 10; i++) {
            dbgLocatX[i] = Math.random() * i * 12;
            dbgLocatY[i] = Math.random() * i * 35;
        }
    }

    public int dbgGetLocatX(int i) {
        return dbgLocatX[i].intValue();
    }

    public int dbgGetLocatY(int i) {
        return dbgLocatY[i].intValue();
    }
}
