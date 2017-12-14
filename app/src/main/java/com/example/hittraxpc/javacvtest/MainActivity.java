package com.example.hittraxpc.javacvtest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;

import static com.example.hittraxpc.javacvtest.R.id.end;
import static com.example.hittraxpc.javacvtest.R.id.image;
import static org.bytedeco.javacpp.Loader.sizeof;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_dnn;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    String TAG = "MainActivity";

   // private ProgressBar prog;
    private ImageView iv ;//, iv3;
   // private ImageView iv2;
    private TextView tv;
    private long startTime;
    private long endTime;
    private int firstFrameCounter = 0;
    private int secondFrameCounter = 0;
    //private Button btn, btn2, btn3;
    private FFmpegFrameGrabber grabber, grabberTwo;
    //private LinearLayout LL;
    //private OpenCVFrameGrabber fg;
    private Frame firstFrame = new Frame();
    private Frame secondFrame = new Frame();
    private Frame showFrame = new Frame();
    private Frame showFrameTwo = new Frame();
    private static Bitmap bmpOne;
    private static Bitmap bmpTwo;
    private Bitmap bmpThree;
    private boolean firstBall = false;
    private int firstBallFrame;
    private Mat matOne = new Mat();
    private Boolean notFound = true;
    private MatVector compare;
    private int iAmCounting = 0;
    private opencv_core.Mat bbImg = new opencv_core.Mat();
    private int foundNum = 0;
    private int totalCount = 0;
    private int dontContinue = 0;
    private int blobCounter = 0;
    private int lastFrameWithBall;
    private int badAreaCount = 0;
    private boolean once = true;
    private Mat thresh = new Mat();

    private boolean drawMe = true;

    private blobStruct[] blob;



    private boolean gotFirst = false;
    AndroidFrameConverter convertToBitmap = new AndroidFrameConverter();
    AndroidFrameConverter convertToBitmapTwo = new AndroidFrameConverter();
    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    OpenCVFrameConverter.ToMat converterToMatTwo = new OpenCVFrameConverter.ToMat();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //LL = (LinearLayout)findViewById(R.id.linLay);
        iv = (ImageView)findViewById(R.id.imgV);
        //iv2 = (ImageView)findViewById(R.id.imgV2);
        //iv3 = (ImageView)findViewById(R.id.imgV3);
        tv = (TextView)findViewById(R.id.textView);
        //btn3 = (Button)findViewById(R.id.diffBtn);
        startTime = SystemClock.uptimeMillis();
        initFrameGrab();
        try{
            firstFrame = grabber.grabImage();
            firstFrame = grabber.grabImage();
        }catch (Exception ex){
            ex.printStackTrace();
            //DO SOMETHING HERE TO SHOW SOMETHING IS WRONG
        }
        firstFrameCounter = 2;
        blob = new blobStruct[20];

        //Log.d(TAG, "Initial 2 frames grabbed from first");


        while(notFound){
            //Log.d("FRAMECOUNT", "First: " + firstFrameCounter + " Second: " + secondFrameCounter);
                //Log.d("IAMCOUNTING", "Num: " + iAmCounting);
                try{
                    secondFrame = doWork(secondFrame, grabberTwo);
                    secondFrameCounter++;
                    if(secondFrame == null){
                        Log.d(TAG, "Second Frame is null");
                        break;
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

            if(totalCount < 30) {
                    compare = doSubtraction(converterToMat.convert(firstFrame), converterToMatTwo.convert(secondFrame));
                    Log.d("COMPARENUM: ", "Frame Num: " + firstFrameCounter + " Compare: " + compare);
                    if (((compare.size() - badAreaCount) > 1) && ((compare.size() - badAreaCount) < 5)) {
                        iAmCounting++;
                        if (iAmCounting > 2) {
                            totalCount++;
                            blob[blobCounter] = new blobStruct();
                            if (firstBall == false) {
                                endTime = SystemClock.uptimeMillis();
                                firstBall = true;
                                firstBallFrame = firstFrameCounter;
                                tv.setText("First found in frame number: " + firstFrameCounter + " in " + (endTime - startTime) + " ms");
                                showFrame = converterToMat.convert(thresh);
                                bmpOne = convertToBitmap.convert(showFrame);
                                iv.setImageBitmap(bmpOne);
                            }
                            foundNum++;
                            Log.d(TAG, "FoundCount: " + foundNum);

                            blob[blobCounter].setFrameStart(firstBallFrame);
                            blob[blobCounter].setFrameNum(firstFrameCounter);
                            blob[blobCounter].setContourNum(compare.size() - badAreaCount);
                            blob[blobCounter].setEndFrame(false);
                            lastFrameWithBall = firstFrameCounter;
                            createCStruct(compare, blob[blobCounter]);
                            blobCounter++;

                            //Debugging to save image to internal storage
                            /*if(once == true){
                                showFrame = converterToMat.convert(blob[blobCounter - 1].contours[0].getContourMatrix());
                                bmpOne = convertToBitmap.convert(showFrame);
                                //iv.setImageBitmap(bmpOne);
                                new ImageSaver(getBaseContext()).
                                setFileName(foundNum + "_pic.png").
                                setDirectoryName("images").
                                save(bmpOne);
                                once = false;
                            }*/

                            iAmCounting--;
                        }

                    }else if (iAmCounting > 0) {
                        if (iAmCounting > 2) {
                            iAmCounting = 0;
                        } else {
                            iAmCounting--;
                        }
                    }else if(firstBall == true){
                        dontContinue++;
                    }
                    //totalCount++;
                    if(dontContinue > 3){
                        endTime = SystemClock.uptimeMillis();
                        blob[blobCounter - 1].setEndFrame(true);
                        Log.d("FINAL PROCESSING TIME", "Time (in ms): " + (endTime - startTime));
                        Log.d("LASTFRAME", "Last: " + totalCount);
                        Log.d("LASTWITHBALL", "Last Frame: " + lastFrameWithBall);
                        break;
                    }
                    firstFrame = doWork(firstFrame, grabber);
                    if(firstFrame == null){
                        Log.d(TAG, "First Frame is null");
                        endTime = SystemClock.uptimeMillis();
                        Log.d("FINAL PROCESSING TIME", "Time (in ms): " + (endTime - startTime));
                        break;
                    }
                    firstFrameCounter += 1;
                }
            }

    }


    private void createCStruct(MatVector mv, blobStruct blStr){


        int dirX = 0;

        Rect cropROI = null;
        for(int i = 0; i < mv.size(); i++){
            if(((boundingRect(mv.get(i)).x() > 40) && (boundingRect(mv.get(i)).x() < 1240))&&(((boundingRect(mv.get(i)).y() > 30) && (boundingRect(mv.get(i)).y() < 660)))){
                cropROI = new Rect(boundingRect(mv.get(i)).x() - 15, boundingRect(mv.get(i)).y() - 7, boundingRect(mv.get(i)).width() + 25,  boundingRect(mv.get(i)).height() + 20);
                Mat drawMat = new Mat(bbImg, cropROI);
                if(i != 0){

                    if((abs((boundingRect(mv.get(i)).width()) - boundingRect(mv.get(i - 1)).width()) < 50)) {
                        if(boundingRect(mv.get(i - 1)).x() > boundingRect(mv.get(i)).x()){
                            blStr.contours[i - 1].setDirection(contourStruct.Direction.FORWARD);

                        }else if((abs((boundingRect(mv.get(i)).width()) - boundingRect(mv.get(i - 1)).width()) >= 50)) {
                            blStr.contours[i - 1].setDirection(contourStruct.Direction.BACKWARD);
                        }
                    }
                    Log.d("CONTOUR", "BlobNum: " + blobCounter + "\nContourNum: " + i);
                    blStr.contours[i - 1].logAllData(i);
                }

                blStr.contours[i].setHeight(boundingRect(mv.get(i)).height() + 20);
                blStr.contours[i].setWidth(boundingRect(mv.get(i)).width() + 25);
                blStr.contours[i].setX(boundingRect(mv.get(i)).x() - 15);
                blStr.contours[i].setY(boundingRect(mv.get(i)).y() - 7);
                blStr.contours[i].setContourMatrix(drawMat);

            }

        }

    }
    public Frame doWork(Frame fr, FFmpegFrameGrabber grab){

        Bitmap bmp;
        try{
            fr = grab.grabImage();
            //fr = grab.grabImage();
            return fr;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }



    //This function does all the imaging
    public MatVector doSubtraction(Mat one, Mat two){
        Mat greyOne = new Mat();
        Mat greyTwo = new Mat();
        Mat diffImg = new Mat();
        Mat threImg = new Mat();
        Mat newThreImg = new Mat();
        Mat displayME = new Mat();
        int area;
        badAreaCount = 0;

        MatVector keyMat = new MatVector();
        cvtColor(one, greyOne, COLOR_BGR2GRAY);
        cvtColor(two, greyTwo, COLOR_BGR2GRAY);
        opencv_core.absdiff(greyOne, greyTwo, diffImg);
        opencv_core.absdiff(diffImg, greyTwo, newThreImg);
        threshold(diffImg, threImg, 37, 255, THRESH_BINARY);
        newThreImg.copyTo(thresh);
        findContours(threImg, keyMat, CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);
        greyOne.copyTo(bbImg);


        /*if(firstBall == true){
            showFrame = converterToMat.convert(one);
            bmpOne = convertToBitmap.convert(showFrame);
            new ImageSaver(getBaseContext()).
                    setFileName(foundNum + "_ONEpic.png").
                    setDirectoryName("images").
                    save(bmpOne);

            showFrameTwo = converterToMatTwo.convert(two);
            bmpTwo = convertToBitmap.convert(showFrameTwo);
            new ImageSaver(getBaseContext()).
                    setFileName(foundNum + "_TWOpic.png").
                    setDirectoryName("images").
                    save(bmpTwo);
        }*/
        greyOne.release();
        greyTwo.release();
        diffImg.release();
        threImg.release();
        displayME.release();

        return keyMat;
    }


    //Function to initialize the frame grabber objects?
    //TODO ^^^
    public void initFrameGrab(){

        //PUT THE NAME OF THE VIDEO IN HERE
        File file = new File(Environment.getExternalStorageDirectory() + "/Video/", "test.mp4");
        grabber = new FFmpegFrameGrabber(file);
        grabberTwo = new FFmpegFrameGrabber(file);
        try {
            grabber.setAudioChannels(0);
            grabber.setFormat("MP4");
            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            grabber.start();
            grabberTwo.setAudioChannels(0);
            grabberTwo.setFormat("MP4");
            grabberTwo.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            grabberTwo.start();
        }catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "INIT FAILED");
        }
    }

}

