package com.example.agrawalamod.receiptscanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    ImageView mViewImage;
    Button mSelectPhoto,mProcess;

    private String TAG;
    private static int threshold = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        TAG = "Main Activity";
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mSelectPhoto = (Button) findViewById(R.id.btnSelectPhoto);
        mViewImage = (ImageView) findViewById(R.id.viewImage);
        mProcess = (Button) findViewById(R.id.btnProcess);

        mSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            System.out.print("OpenCV couldn't be loaded!");
        }

       mProcess.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               doCanny();
           }
       });

    }


    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);

                    Mat tmp = new Mat (bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1); //Initial
                    Mat detectedEdges = new Mat (bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1); //With edges
                    Mat dest = new Mat (bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1); //Final

                    //Canny Starts
                    Utils.bitmapToMat(bitmap, tmp);
                    Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2GRAY);
                    Imgproc.blur(tmp, detectedEdges, new Size(3, 3));
                    Imgproc.Canny(detectedEdges, detectedEdges, 75, 200, 3, false);
                    Core.add(dest, Scalar.all(0), dest);
                    detectedEdges.copyTo(dest, detectedEdges);

                    Utils.matToBitmap(dest, bitmap);



                    mViewImage.setImageBitmap(bitmap);
                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null);

                    f.delete();








                } catch (Exception e) {
                e.printStackTrace();
            }
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

               // mViewImage.setImageBitmap(thumbnail);

            }
        }
    }

    //Havn't tried this function
    public void doCanny(){

        Mat grayImage = new Mat();
        File f = new File(Environment.getExternalStorageDirectory().toString());
        for (File temp : f.listFiles()) {
            if (temp.getName().equals("temp.jpg")) {
                f = temp;
                break;
            }
        }
        try {
            Bitmap bitmap;
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

            bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                    bitmapOptions);


            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null);

            Utils.bitmapToMat(bitmap, grayImage);




        } catch (Exception e) {
            e.printStackTrace();
        }


        Mat detectedEdges = new Mat();
        Mat dest = new Mat();

        Imgproc.cvtColor(grayImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
        Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3, 3, false);
        Core.add(dest, Scalar.all(0), dest);
        grayImage.copyTo(dest, detectedEdges);
        Log.i(TAG, dest.dump());
        Imgcodecs.imwrite("test.jpg", dest);
        Log.i(TAG, "Done");


    }



}