package com.example.csaikia.eee508androidopencv;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.graphics.Color;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.BaseLoaderCallback;

import org.opencv.core.Point;


import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;



public class MainActivity extends AppCompatActivity implements OnTouchListener,CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;

    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;

    double x = -1;
    double y = -1;

    TextView touch_coordinates;
    TextView touch_color;


    Intent incomingLaunch;
    Boolean lineDet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the Flag value from Launch Screen
        lineDet = getIntent().getExtras().getBoolean("LineDetFlag");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        touch_coordinates = (TextView) findViewById(R.id.touch_coordinates);
        touch_color = (TextView) findViewById(R.id.touch_color);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.opencv_tutorial_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    // onTouch() method contains the code such that when screen is touched, it displays the color
    // and coordinates of the point which is clicked
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        double yLow = (double)mOpenCvCameraView.getHeight() * 0.2401961;
        double yHigh = (double)mOpenCvCameraView.getHeight() * 0.769078;
        double xScale = (double)cols / (double)mOpenCvCameraView.getWidth();
        double yScale = (double)rows / (yHigh - yLow);
        x = event.getX();
        y = event.getY();
        x = x * xScale;
        y = y * yScale;


        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) {
            return false;
        }

        touch_coordinates.setText("X: " + Double.valueOf(x) + ", Y: " + Double.valueOf(y));
        Rect touchedRect = new Rect();

        touchedRect.x = (int)x;
        touchedRect.y = (int)y;

        touchedRect.width = 8;
        touchedRect.height = 8;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);
        Mat touchedRegionHsv = new Mat();

        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++ ) {
            mBlobColorHsv.val[i] /= pointCount;
        }

        mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

        touch_color.setText("Color: #" + String.format("%02X", (int)mBlobColorRgba.val[0]) +
                String.format("%02X", (int)mBlobColorRgba.val[1]) + String.format("%02X", (int)mBlobColorRgba.val[2]));

        touch_color.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                (int) mBlobColorRgba.val[1], (int) mBlobColorRgba.val[2]));

        touch_coordinates.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                (int) mBlobColorRgba.val[1], (int) mBlobColorRgba.val[2]));


        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    /* onCameraFrame() method implements the following two features:
        1. Straight line detection on a video frame
        2. Edge detection on a video frame while maintaining color of image
        */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Input frame is taken and stored as an RGBA matrix
        mRgba=inputFrame.rgba();
        // lineDet variable is returned from Launchscreen class depending on what the user picks
        if(lineDet){
            // Feature to detect straight lines
            Mat lines = new Mat();
            int threshold = 50;
            int minLineSize = 20;
            int lineGap = 20;

            Mat mIntermediateMat = new Mat();
            Mat mRgbaInnerWindow = new Mat();

            // It was noticed that the performance of line detection was more robust
            // when Gaussian blurring was used prior to detection
            Imgproc.GaussianBlur(mRgba, mRgba, new org.opencv.core.Size(5, 5), 1);
            // Canny edge detection is used as a preprocessing step
            Imgproc.Canny(mRgba, mIntermediateMat, 70, 100);

            Imgproc.cvtColor(mIntermediateMat, mRgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);

            // Hough Transform is used to detect straight lines
            Imgproc.HoughLinesP(mIntermediateMat, lines, 1, Math.PI / 180, threshold,
                minLineSize, lineGap);

            Scalar mColors = new Scalar( 0, 255, 0 );


            for (int i = 0; i < lines.cols(); i++) {
                double[] vec = lines.get(0, i);
                if(vec==null)
                    continue;
                double x1 = vec[0],
                        y1 = vec[1],
                        x2 = vec[2],
                        y2 = vec[3];
                Point start = new Point(x1, y1);
                Point end = new Point(x2, y2);
                // Lines are plotted using the function given below
                Imgproc.line(mRgba, start, end, mColors, 3);
            }
        } else {
            // Feature to do edge detection in image
            org.opencv.core.Size sizeRgba = mRgba.size();
            Mat edges = new Mat(mRgba.size(), CvType.CV_8UC1);
            // It was noticed that the performance of edge detection was more robust
            // when Gaussian blurring was used prior to detection
            Imgproc.GaussianBlur(mRgba, mRgba, new org.opencv.core.Size(5, 5), 1);
            Mat rgbaInnerWindow = mRgba.submat(0, (int) sizeRgba.height, 0, (int) sizeRgba.width);
            Imgproc.Canny(rgbaInnerWindow, edges, 10, 100);

            // The following changes are required so that the edge detection happens while the
            // video frame retains color of the video frame.
            Mat colorEdges = new Mat();
            Mat otherEdge = colorEdges;
            edges.copyTo(colorEdges);
            Imgproc.cvtColor(colorEdges, colorEdges, Imgproc.COLOR_GRAY2BGRA);

            colorEdges = colorEdges.setTo(new Scalar(0, 255, 0), edges);
            colorEdges.copyTo(rgbaInnerWindow, edges);

            otherEdge.release();
            colorEdges.release();
            rgbaInnerWindow.release();
        }
        return mRgba;
    }
}