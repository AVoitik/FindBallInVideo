package com.example.hittraxpc.javacvtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.bilateralFilter;
import static org.bytedeco.javacpp.opencv_imgproc.connectedComponents;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.threshold;

/**
 * Created by HitTraxPC on 11/16/2017.
 */

//Change name to ballFinderListener
public class computerVisionUtilities extends AsyncTask {

    //New FFMPEGFrameGrabber object
    private FFmpegFrameGrabber VIDEO_GRABBER;

    //Name of the Video File and Path to be read in through FFMPEGFrameGrabber
    private File file = new File(Environment.getExternalStorageDirectory() + "/Video/", "test.mp4");

    private String TAG = "THREAD";

    //Set up the listener
    private final BallFindFinishedListener finishedListener;
    private int frameCounter;
    private Bitmap bmp;
    private Bitmap bmp2;
    private Frame firstFrame;
    private Frame secondFrame;
    private opencv_core.Mat printme;
    private opencv_core.Mat printme2;
    private opencv_core.Mat convTwo;
    private opencv_core.Mat convOne;
    private opencv_core.IplImage tempVideoFrame;
    private long startTime;
    private long endTime;

    protected int frameCnt = 0;

    private boolean keepGoing;
    //DEBUGGING
    ImageView iv;

    //Various conversion tools
    OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
    AndroidFrameConverter convertToBitmap = new AndroidFrameConverter();
    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    OpenCVFrameConverter.ToMat converterToMat2 = new OpenCVFrameConverter.ToMat();
    //Instantiate the interface
    public interface BallFindFinishedListener{

        //Callback to the main Activity
        void onTaskfinished(Bitmap bmp, Bitmap bmp2, long startTime, long endTime);
    }

    //Constructor
    public computerVisionUtilities(Context context, ImageView iv, BallFindFinishedListener finishedListener, int frameCounter, boolean hasStarted) {
        this.finishedListener = finishedListener;
        VIDEO_GRABBER = new FFmpegFrameGrabber(file);
        this.frameCounter = frameCounter;
        this.iv = iv;
        if(!hasStarted){



        }
    }


    //Don't touch
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //Real work is done here
    @Override
    protected Object doInBackground(Object[] params) {
        findMeABaseball();
        return null;
    }


    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Frame frame = converterToMat.convert(printme);
        bmp = convertToBitmap.convert(frame);

        Frame frame2 = converterToMat.convert(printme2);
        bmp2 = convertToBitmap.convert(frame2);

        finishedListener.onTaskfinished(bmp, bmp2, startTime, endTime);
    }

    protected int findMeABaseball(){
        //Open the video
        try{
            VIDEO_GRABBER.setAudioChannels(0);
            VIDEO_GRABBER.setFormat("MP4");
            VIDEO_GRABBER.start();
                //VIDEO_GRABBER.setFrameNumber(frameCnt);

            startTime = android.os.SystemClock.uptimeMillis();
            while(firstFrame != null) {
                //Frame to be used as a background
                firstFrame = VIDEO_GRABBER.grab();
                Log.d(TAG, "position: " + frameCnt);
                frameCnt++;

            }
            endTime = android.os.SystemClock.uptimeMillis();


        }catch(Exception e){
            e.printStackTrace();
        }

        return 0;
    }


    public void removeBackground(Frame frameOne, Frame frameTwo){
        opencv_core.Mat greyOne = new opencv_core.Mat();
        opencv_core.Mat greyTwo = new opencv_core.Mat();
        opencv_core.Mat diffImg = new opencv_core.Mat();
        opencv_core.Mat threImg = new opencv_core.Mat();

        opencv_core.Mat convOne = converterToMat.convert(frameOne);
        opencv_core.Mat convTwo = converterToMat.convert(frameTwo);

        //Convert to grayscale
        cvtColor(convOne, greyOne, COLOR_BGR2GRAY);
        cvtColor(convTwo, greyTwo, COLOR_BGR2GRAY);
        printme = convOne;
        printme2 = convTwo;
        //Take the difference
        opencv_core.absdiff(greyOne, greyTwo, diffImg);

        //Bilateral Filter on the difference image TAKES LONG
        //bilateralFilter(diffImg, biLaImg, 11, 30, 17);

        //Threshold the image
        threshold(diffImg, threImg, 50, 255, THRESH_BINARY);


        //BEGIN FINDBALL


        final opencv_features2d.SimpleBlobDetector simpBlob = opencv_features2d.SimpleBlobDetector.create(
                new opencv_features2d.SimpleBlobDetector.Params()
                        .minThreshold(10)
                        .maxThreshold(80)
                        .filterByColor(true)
                        .blobColor((byte)255)
                        .filterByArea(true)
                        .minArea(50)
                        .filterByCircularity(true)
                        .minCircularity((float)0.4)
        );

        opencv_core.KeyPointVector keypoints = new opencv_core.KeyPointVector();

        simpBlob.detect(threImg, keypoints);
        Log.d("KEYPOINTS", "VALUE: " + keypoints.size());

        if(keypoints.size() > 0){
           //opencv_features2d.drawKeypoints();
            Log.d("KEYPOINT FOUND", "AT: " + frameCounter);
        }
        //printme = diffImg;
    }

}
