package com.example.michael.cv_app3;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;
import org.opencv.core.Core;
import org.opencv.core.Rect;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.*;
import org.opencv.videoio.*;
import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private CameraBridgeViewBase mGrayCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    Mat grayMat;
    Mat CannyMat;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mGrayCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void Tutorial1Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
        {
            mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
            mOpenCvCameraView.disableView();
        }

        if (mGrayCameraView != null)
        {
            mGrayCameraView.setVisibility(SurfaceView.INVISIBLE);
            mGrayCameraView.disableView();
        }


    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
        {
            mOpenCvCameraView.destroyDrawingCache();
            mOpenCvCameraView.surfaceDestroyed(mOpenCvCameraView.getHolder());
            mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
            mOpenCvCameraView.disableView();
        }

        if (mGrayCameraView != null) {
            mGrayCameraView.destroyDrawingCache();
            mGrayCameraView.surfaceDestroyed(mOpenCvCameraView.getHolder());

            mGrayCameraView.setVisibility(SurfaceView.INVISIBLE);
            mGrayCameraView.disableView();
        }
        mOpenCvCameraView = null;
        mGrayCameraView = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 50);
            }
        }


        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.cannyview);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCameraIndex(1);  // font cameram

        mOpenCvCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                CannyMat = new Mat(height,width,CvType.CV_8UC4);
                grayMat = new Mat(height,width,CvType.CV_8UC4);
                Log.i(this.getClass().getName(),"onCameraViewStarted width: " + width +" height: " + height );
            }

            @Override
            public void onCameraViewStopped() {
                Log.i(this.getClass().getName(),"onCameraViewStopped " );
                CannyMat.release();
                grayMat.release();

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat gray_img = inputFrame.gray();
                Mat src_img = inputFrame.rgba();
                Mat edges = new Mat();

                Imgproc.Canny(gray_img,edges,50,100);
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Imgproc.findContours(edges,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE,new Point(0,0));
                Imgproc.drawContours(src_img,contours,-1,new Scalar(0,255,0));

                final Point src_center = new Point(src_img.cols()/2.0, src_img.rows()/2.0);
                Mat rot_mat = Imgproc.getRotationMatrix2D(src_center,90,1.0);
                Imgproc.warpAffine(src_img,CannyMat,rot_mat,CannyMat.size());
                gray_img.release();
                src_img.release();
                edges.release();
                rot_mat.release();
                return CannyMat;
            }
        });

        mGrayCameraView = (JavaCameraView)findViewById(R.id.grayview);

        mGrayCameraView.setVisibility(SurfaceView.VISIBLE);
        mGrayCameraView.setCameraIndex(1);
        mGrayCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
//                grayMat = new Mat(height,width,CvType.CV_8UC4);
//                Log.i(this.getClass().getName()," mGrayCameraView onCameraViewStarted width: " + width +" height: " + height );
            }

            @Override
            public void onCameraViewStopped() {
//                grayMat.release();
//                Log.i(this.getClass().getName(),"mGrayCameraView onCameraViewStopped " );
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

//                Mat src_img = inputFrame.gray();
////                Mat dst = new Mat();
//                final Point src_center = new Point(src_img.cols()/2.0, src_img.rows()/2.0);
//                Mat rot_mat = Imgproc.getRotationMatrix2D(src_center,90,1.0);
//                Imgproc.warpAffine(src_img,grayMat,rot_mat,grayMat.size());
//                src_img.release();

                return grayMat;
            }
        });






        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.tutorial1_activity_java_surface_view);
//        tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
