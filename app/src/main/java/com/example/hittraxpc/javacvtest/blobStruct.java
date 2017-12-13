package com.example.hittraxpc.javacvtest;

import android.util.Log;

//This class will hold each frame's contour data
public class blobStruct {

    private static String TAG = "blobStruct";

    //Number of contours per frame
    private long contourNum;

    //Actual frame number
    private int frameNum;

    //Contour List
    public contourStruct[] contours = new contourStruct[4];

    //Frame start num
    private int frameStart;

    //End boolean
    private boolean isEndFrame;


    /*
    blobStruct() : default constructor
    Pre :
    Post : blob struct initialized to default values
     */
    public blobStruct(){
        for(int i = 0; i < 4; i++){
            contours[i] = new contourStruct();
        }
    }

    /*
    blobStruct(int, int, int, boolean) : parameterized constructor
    Pre : the current frame number, the starting frame number, the
            number of contours in the frame, and a boolean determining
            whether it is the last frame or not
    Post : a blobStruct with an empty contourStruct()
     */
    public blobStruct(int frameNum, int frameStart, long contourNum, boolean isEndFrame){
        this.frameNum = frameNum;
        this.frameStart = frameStart;
        this.contourNum = contourNum;
        this.isEndFrame = isEndFrame;

        //instantiating new contourStructs
        for(int i = 0; i < 4; i++){
            contours[i] = new contourStruct();
        }
    }

    //get functions

    public int getFrameNum(){
        return frameNum;
    }

    public int getFrameStart(){
        return frameStart;
    }

    public long getContourNum(){
        return contourNum;
    }

    public boolean isEndFrame(){
        return isEndFrame;
    }

    //set functions
    public void setFrameNum(int num){
        this.frameNum = num;
        Log.d(TAG, "FrameNum set to" + frameNum);
    }

    public void setFrameStart(int num){
        this.frameStart = num;
        Log.d(TAG, "FrameStart set to" + frameStart);
    }

    public void setContourNum(int num){
        this.contourNum = num;
        Log.d(TAG, "ContourNum set to" + contourNum);
    }

    public void setEndFrame(boolean value){
        this.isEndFrame = value;
        Log.d(TAG, "isEndFrame set to" + isEndFrame);
    }
}
