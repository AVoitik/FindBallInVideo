package com.example.hittraxpc.javacvtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import static com.example.hittraxpc.javacvtest.R.id.image;
import static org.bytedeco.javacpp.Loader.sizeof;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_dnn;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_videoio;
import org.bytedeco.javacpp.opencv_xfeatures2d;
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

public class MainActivity extends Activity implements computerVisionUtilities.BallFindFinishedListener {

    String TAG = "MainActivity";
    private ImageView iv , iv3;
    private ImageView iv2;
    private long startTime;
    private long endTime;
    private int counter = 0;
    private Button btn, btn2, btn3;
    private FFmpegFrameGrabber grabber, grabberTwo;
    private OpenCVFrameGrabber fg;
    private Frame firstFrame = new Frame();
    private Frame secondFrame = new Frame();
    private Frame showFrame = new Frame();
    private static Bitmap bmpOne;
    private static Bitmap bmpTwo;
    private static Bitmap bmpThree;
    private Mat matOne, matTwo;
    private boolean gotFirst = false;
    AndroidFrameConverter convertToBitmap = new AndroidFrameConverter();
    AndroidFrameConverter convertToBitmapTwo = new AndroidFrameConverter();
    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    OpenCVFrameConverter.ToMat converterToMatTwo = new OpenCVFrameConverter.ToMat();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        iv = (ImageView)findViewById(R.id.imgV);
        iv2 = (ImageView)findViewById(R.id.imgV2);
        iv3 = (ImageView)findViewById(R.id.imgV3);
        btn = (Button)findViewById(R.id.next);
        btn2 = (Button)findViewById(R.id.next2);
        btn3 = (Button)findViewById(R.id.diffBtn);
        initFrameGrab();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    btn2.setText("FRAME #2 HIDDEN");
                    btn.setText("FRAME #1 SHOWN");
                    startTime = android.os.SystemClock.uptimeMillis();
                    firstFrame = grabber.grabImage();
                    bmpOne = convertToBitmap.convert(firstFrame);
                    iv.setImageBitmap(bmpOne);
                    counter++;
                    endTime = android.os.SystemClock.uptimeMillis();
                    Log.d("ONCLICK", "Timing Frame 1: " + (endTime - startTime));
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    btn.setText("FRAME #1 HIDDEN");
                    btn2.setText("FRAME #2 SHOWN");
                    startTime = android.os.SystemClock.uptimeMillis();
                    secondFrame = grabberTwo.grabImage();
                    bmpTwo = convertToBitmapTwo.convert(secondFrame);
                    //iv.setEnabled(false);
                    iv.setImageBitmap(bmpTwo);
                    counter++;
                    endTime = android.os.SystemClock.uptimeMillis();
                    Log.d("ONCLICK", "Timing Frame 2: " + (endTime - startTime));
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv2.setImageBitmap(doSubtraction(converterToMat.convert(firstFrame), converterToMatTwo.convert(secondFrame)));
            }
        });

    }

//https://stackoverflow.com/questions/9129037/javacv-blob-detection
    public Bitmap doSubtraction(Mat one, Mat two){
        opencv_core.Mat greyOne = new opencv_core.Mat();
        opencv_core.Mat greyTwo = new opencv_core.Mat();
        opencv_core.Mat diffImg = new opencv_core.Mat();
        opencv_core.Mat threImg = new opencv_core.Mat();
        opencv_core.MatVector keyMat = new opencv_core.MatVector();
        Log.d("DOSubtraction", "Started");

        //Convert to grayscale
        cvtColor(one, greyOne, COLOR_BGR2GRAY);
        //
        //return bmpOne;
        cvtColor(two, greyTwo, COLOR_BGR2GRAY);
        //Take the difference
        opencv_core.absdiff(greyOne, greyTwo, diffImg);

        //return bmpOne;
        //Bilateral Filter on the difference image TAKES LONG
        //bilateralFilter(diffImg, biLaImg, 11, 30, 17);

        //Threshold the image

        CvMemStorage mem;

        CvSeq contours = new CvSeq();

        threshold(diffImg, threImg, 50, 255, THRESH_BINARY);
        mem = cvCreateMemStorage();
        //cvFindContours(threImg, mem, contours, sizeof(CvContour.class), CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE, cvPoint(0,0));
        findContours(threImg, keyMat, CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);
        Log.d("KEYPOINT SIZE", "SIZE: " + keyMat.size());
        //showFrame = converterToMat.convert(keypointMat);
        //bmpThree = convertToBitmap.convert(showFrame);
        return bmpThree;
    }

    //Callback from background worker
    @Override
    public void onTaskfinished(Bitmap bmp, Bitmap bmp2, long timingStart, long timingEnd){

        //setImageBitmap(bmp, bmp2);

        Log.d(TAG, "Finished: " + (timingEnd - timingStart));
    }


    public void initFrameGrab(){
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
            //Log.d(TAG, "STARTED SUCCESSFULLY, Frames: " + fg.getLengthInFrames());
        }catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "INIT FAILED");
        }
    }

}
