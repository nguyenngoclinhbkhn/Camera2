package com.example.librarycameraapi2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class ShowImageActivity extends AppCompatActivity {
    private ImageView img;
    private MediaScannerConnection msConn;
    private File imageFileFolder;
    File imageFileName;
    String camera;
    String path;
    private Bitmap bitmapSum;
    private Bitmap rotatedBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        img = findViewById(R.id.imgView);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        camera = intent.getStringExtra("camera");
        bitmapSum = BitmapFactory.decodeFile(path);
        rotatedBitmap = bitmapSum;
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmapSum, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmapSum, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmapSum, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    rotatedBitmap = bitmapSum;
                    break;
                default:
                    rotatedBitmap = bitmapSum;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        img.setImageBitmap(rotatedBitmap);
//        new LoadImage().execute(path);
        Log.e("TAG", "path " + path);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private class LoadImage extends AsyncTask<String , Void, String>{
        private ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ShowImageActivity.this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String path = strings[0];
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
//            imageFileFolder = new File(Environment.getExternalStorageDirectory(), "Camera2Test");
//            Matrix matrix = new Matrix();
//            if (camera.equals("Back")) {
//                matrix.postRotate(90);
//            } else {
//                matrix.postRotate(270);
//            }
//            Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0,
//                    bitmap.getWidth(), bitmap.getHeight(), matrix, false);
//            savePhoto(bitmap1);
            return path;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.cancel();
//            img.setImageBitmap(BitmapFactory.decodeFile(path));
            img.setImageBitmap(BitmapFactory.decodeFile(s));

        }
    }

    public void savePhoto(Bitmap bmp) {
        FileOutputStream out = null;
        imageFileName = new File(imageFileFolder, String.valueOf(System.currentTimeMillis()) + ".jpg");
        try {
            out = new FileOutputStream(imageFileName);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            scanPhoto(imageFileName.toString());
//            new File("/storage/emulated/0/Camera2Test/CameraPicture.jpg").delete();
            out = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
            }
        });
        msConn.connect();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
