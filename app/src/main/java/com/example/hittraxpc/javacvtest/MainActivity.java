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
    private static Bitmap bmpOne;
    private Bitmap bmpThree;
    private boolean firstBall = false;
    private int firstBallFrame;
    private Mat matOne = new Mat();
    private Boolean notFound = true;
    private Long compare;
    private int iAmCounting = 0;
    private opencv_core.Mat bbImg = new opencv_core.Mat();
    private int foundNum = 0;
    private int totalCount = 0;
    private int dontContinue = 0;

    private boolean drawMe = true;

    private blobStruct blob;



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
        blob = new blobStruct();

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
                    if ((compare > 1) && (compare < 5)) {
                        iAmCounting++;
                        if (iAmCounting > 2) {
                            totalCount++;
                            if (firstBall == false) {
                                endTime = SystemClock.uptimeMillis();
                                firstBall = true;
                                firstBallFrame = firstFrameCounter;
                                tv.setText("First found in frame number: " + firstFrameCounter + " in " + (endTime - startTime) + " ms");
                            }
                            foundNum++;
                            Log.d(TAG, "FoundCount: " + foundNum);


                            //Debugging to save image to internal storage
                            //bmpOne = convertToBitmap.convert(showFrame);
                            //new ImageSaver(getBaseContext()).
                                        //setFileName(foundNum + "_pic.png").
                                        //setDirectoryName("images").
                                        //save(bmpOne);


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
                        Log.d("FINAL PROCESSING TIME", "Time (in ms): " + (endTime - startTime));
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
    public Long doSubtraction(Mat one, Mat two){
        Mat greyOne = new Mat();
        Mat greyTwo = new Mat();
        Mat diffImg = new Mat();
        Mat threImg = new Mat();
        Mat displayME = new Mat();
        int area;
        int badAreaCount = 0;
        MatVector keyMat = new MatVector();
        cvtColor(one, greyOne, COLOR_BGR2GRAY);
        cvtColor(two, greyTwo, COLOR_BGR2GRAY);
        opencv_core.absdiff(greyOne, greyTwo, diffImg);
        threshold(diffImg, threImg, 50, 255, THRESH_BINARY);
        showFrame = converterToMat.convert(threImg);
        findContours(threImg, keyMat, CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);

        /*Find Directon*/
        if(keyMat.size() > 1){
            for(int i = 0; i < keyMat.size(); i++){
                area = boundingRect(keyMat.get(i)).height() * boundingRect(keyMat.get(i)).width();
                Log.d("CONTOURAREA", "Area of Contour #" + (i + 1) + ": " + area);
                if(area < 400){
                    badAreaCount++;
                }
            }
        }

        //if(drawMe == true){
            //if(keyMat.size() > 1){
                //drawContours(bbImg, keyMat, 1, new Scalar(0.0, 0.0, 255.0, 2.0));
                //drawContours(bbImg, keyMat, 1, new Scalar(0.0, 0.0, 255.0, 2.0));
                //drawMe = false;
                //Log.d(TAG, "Bounding Rect: H ->" + Integer.toString(boundingRect(keyMat.get(0)).height()));
                //Log.d(TAG, "Bounding Rect: W ->" + Integer.toString(boundingRect(keyMat.get(0)).width()));
                //Log.d(TAG, "Bounding Rect: X ->" + Integer.toString(boundingRect(keyMat.get(0)).x()));
                //Log.d(TAG, "Bounding Rect: Y ->" + Integer.toString(boundingRect(keyMat.get(0)).y()));
            //}
        //}

        greyOne.release();
        greyTwo.release();
        diffImg.release();
        threImg.release();
        displayME.release();

        return keyMat.size() - badAreaCount;
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
            //grabber.grabImage();
            grabberTwo.setAudioChannels(0);
            grabberTwo.setFormat("MP4");
            grabberTwo.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            grabberTwo.start();
            //prog.setVisibility(View.INVISIBLE);
            //Log.d(TAG, "STARTED SUCCESSFULLY, Frames: " + fg.getLengthInFrames());
        }catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "INIT FAILED");
            //prog.setVisibility(View.INVISIBLE);
        }
    }

}

