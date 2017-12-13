package com.example.hittraxpc.javacvtest;

import android.util.Log;

import org.bytedeco.javacpp.opencv_core;

/**
 * Created by HitTraxPC on 12/11/2017.
 */

public class contourStruct {

    private static String TAG = "contourStruct";

    //Enum for ease of direction
    enum Direction
    {
        FORWARD, BACKWARD;
    }

    private Direction dir;

    private int x_BB;
    private int y_BB;
    private int w_BB;
    private int h_BB;

    private opencv_core.Mat c_Mat;


    public contourStruct(){}

    public void setDirection(Direction dir){
        this.dir = dir;
        Log.d(TAG, "Direction set to: " + dir);
    }

    public Direction getDirection(){
        return dir;
    }

    public void setX(int x_val){
        this.x_BB = x_val;
        Log.d(TAG, "X value set to: " + x_BB);
    }

    public int X(){
        return x_BB;
    }

    public void setY(int y_val){
        this.y_BB = y_val;
        Log.d(TAG, "Y value set to: " + y_BB);
    }

    public int Y(){
        return y_BB;
    }

    public void setWidth(int w_val){
        this.w_BB = w_val;
        Log.d(TAG, "Width set to: " + w_BB);
    }

    public int width(){
        return w_BB;
    }

    public void setHeight(int h_val){
        this.h_BB = h_val;
        Log.d(TAG, "Height set to: " + h_BB);
    }

    public int height(){
        return h_BB;
    }

    public void setContourMatrix(opencv_core.Mat contourMat){
        this.c_Mat = contourMat;
    }

    public opencv_core.Mat getContourMatrix(){
        return c_Mat;
    }

    public void logAllData(int index){
        Log.d("ContourStructDataLog", "Index: " + index + "Direction: " + dir);
        Log.d("ContourStructDataLog", "Index: " + index + "X: " + x_BB);
        Log.d("ContourStructDataLog", "Index: " + index + "Y: " + y_BB);
        Log.d("ContourStructDataLog", "Index: " + index + "Width: " + w_BB);
        Log.d("ContourStructDataLog", "Index: " + index + "Height: " + h_BB);
    }
}
