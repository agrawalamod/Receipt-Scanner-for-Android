package com.example.agrawalamod.receiptscanner;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * Created by Shuchita Gupta on 13-11-2015.
 */
public class Preprocessor {

    int threshold = 5;
    private String TAG;

    public void Preprocessor(){

    }

    public void doCanny(Mat bitmap){
        File file = new File("/storage/emulated/0/Pictures/Receipt Scanner/image.jpg");
        bitmap = imread(file.getAbsolutePath(), Imgproc.COLOR_BGR2GRAY);
        bitmap.dump();
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();
        Mat dest = new Mat();

        Imgproc.cvtColor(bitmap, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
        Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3, 3, false);
        Core.add(dest, Scalar.all(0), dest);
        bitmap.copyTo(dest, detectedEdges);
        Log.i(TAG, dest.dump());
        Imgcodecs.imwrite("test.jpg", dest);
        Log.i(TAG,"Done");
    }
}
