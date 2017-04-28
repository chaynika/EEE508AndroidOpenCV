package com.example.csaikia.eee508androidopencv;

import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;



public class MainActivity extends AppCompatActivity implements OnTouchListener,CvCameraViewListener2 {
    private static final String TAG = "EEE508OpenCV::MainActivity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Mat mGray;

    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private Mat mSpectrum;
    private Size SPECTRUM_SIZE;

    double x = -1;
    double y = -1;

    TextView touch_coordinates;
    TextView touch_color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        touch_coordinates = (TextView) findViewById(R.id.touch_coordinates);
//        touch_color = (TextView) findViewById(R.id.touch_color);
//        mOpenCvCameraView = (CustomCamera) findViewById(R.id.opencv_tutorial_activity_surface_view);
//        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
//        mOpenCvCameraView.setCvCameraViewListener(this);
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
        /*Camera.Parameters cParams = mOpenCvCameraView.getParameters();
        cParams.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        mOpenCvCameraView.setParameters(cParams);
        Toast.makeText(this, "Focus mode : "+cParams.getFocusMode(), Toast.LENGTH_SHORT).show();

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        //CONTOUR_COLOR = new Scalar(255,0,0,255);
        //CONTOUR_COLOR_WHITE = new Scalar(255,255,255,255);
        */
    }

    @Override
    public void onCameraViewStopped() {
//        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Feature to detect straight lines
        /*mRgba=inputFrame.rgba();
        Mat lines = new Mat();
        int threshold = 70;
        int minLineSize = 30;
        int lineGap = 10;

        Mat mIntermediateMat = new Mat();
        Mat mRgbaInnerWindow = new Mat();

        mRgba=inputFrame.rgba();
        Imgproc.GaussianBlur(mRgba, mRgba, new org.opencv.core.Size(5, 5), 1);
        Imgproc.Canny(mRgba, mIntermediateMat, 80, 100);

        Imgproc.cvtColor(mIntermediateMat, mRgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);

        Imgproc.HoughLinesP(mIntermediateMat, lines, 1, Math.PI / 180, threshold,
                minLineSize, lineGap);

        Scalar mColors = new Scalar( 0, 255, 0, 128 );
        for (int i = 0; i < lines.cols(); i++)
        {
            double[] vec = lines.get(0, i);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            Imgproc.line(mRgba, start, end, mColors, 3);
        }

        */

        // Feature to do edge detection in image

        mRgba = inputFrame.rgba();

        org.opencv.core.Size sizeRgba = mRgba.size();
        Mat edges = new Mat(mRgba.size(), CvType.CV_8UC1);

        Imgproc.GaussianBlur(mRgba, mRgba, new org.opencv.core.Size(5, 5), 1);
        Mat rgbaInnerWindow = mRgba.submat(0, (int) sizeRgba.height, 0, (int) sizeRgba.width);

        Imgproc.Canny(rgbaInnerWindow, edges, 10, 100);

        //copy the edgesMat back into the sub-image
        Imgproc.cvtColor(edges, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);

        rgbaInnerWindow.release();




 //       mGray=inputFrame.gray();
// Blob detector
        //Imgproc.blur(mRgba, mRgba, new Size(3,3));
     /*   Imgproc.GaussianBlur(mRgba, mRgba, new org.opencv.core.Size(5, 5), 1);
        if (!mIsColorSelected) return mRgba;

        if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();

            Log.d(TAG, "Contours:" + contours.size());
            Scalar color = new Scalar(255, 0, 0, 255);
            Imgproc.drawContours(mRgba, contours, -1, color);
        }

        if (contours.size() <= 0) {
            return mRgba;
        }

       RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0)	.toArray()));

        double boundWidth = rect.size.width;
        double boundHeight = rect.size.height;
        int boundPos = 0;

        for (int i = 1; i < contours.size(); i++) {
            rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            if (rect.size.width * rect.size.height > boundWidth * boundHeight) {
                boundWidth = rect.size.width;
                boundHeight = rect.size.height;
                boundPos = i;
            }
        }

/*        Rect boundRect = Imgproc.boundingRect(new MatOfPoint(contours.get(boundPos).toArray()));

        Imgproc.rectangle( mRgba, boundRect.tl(), boundRect.br(), CONTOUR_COLOR_WHITE, 2, 8, 0 );

/*
        Log.d(TAG,
                " Row start ["+
                        (int) boundRect.tl().y + "] row end ["+
                        (int) boundRect.br().y+"] Col start ["+
                        (int) boundRect.tl().x+"] Col end ["+
                        (int) boundRect.br().x+"]");

        int rectHeightThresh = 0;
        double a = boundRect.br().y - boundRect.tl().y;
        a = a * 0.7;
        a = boundRect.tl().y + a;

        Log.d(TAG,
                " A ["+a+"] br y - tl y = ["+(boundRect.br().y - boundRect.tl().y)+"]");

        //Core.rectangle( mRgba, boundRect.tl(), boundRect.br(), CONTOUR_COLOR, 2, 8, 0 );
        Imgproc.rectangle( mRgba, boundRect.tl(), new Point(boundRect.br().x, a), CONTOUR_COLOR, 2, 8, 0 );

        MatOfPoint2f pointMat = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(boundPos).toArray()), pointMat, 3, true);
        contours.set(boundPos, new MatOfPoint(pointMat.toArray()));
/*
        MatOfInt hull = new MatOfInt();
        MatOfInt4 convexDefect = new MatOfInt4();
        Imgproc.convexHull(new MatOfPoint(contours.get(boundPos).toArray()), hull);

        if(hull.toArray().length < 3) return mRgba;

        Imgproc.convexityDefects(new MatOfPoint(contours.get(boundPos)	.toArray()), hull, convexDefect);

        List<MatOfPoint> hullPoints = new LinkedList<MatOfPoint>();
        List<Point> listPo = new LinkedList<Point>();
        for (int j = 0; j < hull.toList().size(); j++) {
            listPo.add(contours.get(boundPos).toList().get(hull.toList().get(j)));
        }

        MatOfPoint e = new MatOfPoint();
        e.fromList(listPo);
        hullPoints.add(e);

        List<MatOfPoint> defectPoints = new LinkedList<MatOfPoint>();
        List<Point> listPoDefect = new LinkedList<Point>();
        for (int j = 0; j < convexDefect.toList().size(); j = j+4) {
            Point farPoint = contours.get(boundPos).toList().get(convexDefect.toList().get(j+2));
            Integer depth = convexDefect.toList().get(j+3);
            if(depth > iThreshold && farPoint.y < a){
                listPoDefect.add(contours.get(boundPos).toList().get(convexDefect.toList().get(j+2)));
            }
            Log.d(TAG, "defects ["+j+"] " + convexDefect.toList().get(j+3));
        }

        MatOfPoint e2 = new MatOfPoint();
        e2.fromList(listPo);
        defectPoints.add(e2);

        Log.d(TAG, "hull: " + hull.toList());
        Log.d(TAG, "defects: " + convexDefect.toList());

        Imgproc.drawContours(mRgba, hullPoints, -1, CONTOUR_COLOR, 3);

        int defectsTotal = (int) convexDefect.total();
        Log.d(TAG, "Defect total " + defectsTotal);

        this.numberOfFingers = listPoDefect.size();
        if(this.numberOfFingers > 5) this.numberOfFingers = 5;

        mHandler.post(mUpdateFingerCountResults);

        for(Point p : listPoDefect){
            Imgproc.circle(mRgba, p, 6, new Scalar(255,0,255));
        }

/*Mat rgba = inputFrame.rgba();
        org.opencv.core.Size sizeRgba = rgba.size();

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = cols / 8;
        int top = rows / 8;
        int width = cols * 3 / 4;
        int height = rows * 3 / 4;

        //get sub-image
        Mat rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);

        //create edgesMat from sub-image
        Imgproc.Canny(rgbaInnerWindow, rgbaInnerWindow, 100, 100);
        */

        /*Mat edges = new Mat(mRgba.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(mRgba,edges,Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.Canny(edges,edges,80,100);
        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_GRAY2RGB);

       /* Mat colorEdges = new Mat();
        Mat killMe = colorEdges;
        rgbaInnerWindow.copyTo(colorEdges);

        Imgproc.cvtColor(colorEdges, colorEdges, Imgproc.COLOR_GRAY2BGRA);
    */
        //colorEdges = colorEdges.setTo(greenScalar, rgbaInnerWindow);
        //colorEdges.copyTo(rgbaInnerWindow, rgbaInnerWindow);

        /*killMe.release();
        colorEdges.release();
        rgbaInnerWindow.release();
       // detectEdges(mRgba);
       */
        return mRgba;
    }


    /*private void detectEdges(Mat mRgba) {
        Mat edges = new Mat(mRgba.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(mRgba,edges,Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.Canny(edges,edges,80,100);
    }
    */


}
