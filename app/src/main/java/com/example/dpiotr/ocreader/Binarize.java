package com.example.dpiotr.ocreader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * Created by dpiotr on 28.04.17.
 */

public class Binarize extends AsyncTask<Uri, Void, Uri> {

    private Activity activity;
    private Bitmap binarizedImage;
    private ProgressDialog dialog;
    private Bitmap mBitmap = null;

    private static final boolean TRASNPARENT_IS_BLACK = true;


    public Binarize(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Uri doInBackground(Uri... uris) {
        try {
            mBitmap = MainActivity.decodeBitmapUri(activity.getApplicationContext(), uris[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //Make mutable bitmap
        binarizedImage = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        // I will look at each pixel and use the function shouldBeBlack to decide
        // whether to make it black or otherwise white
        for (int i = 0; i < binarizedImage.getWidth(); i++) {
            for (int c = 0; c < binarizedImage.getHeight(); c++) {
                int pixel = binarizedImage.getPixel(i, c);
                if (shouldBeBlack(pixel))
                    binarizedImage.setPixel(i, c, Color.BLACK);
                else
                    binarizedImage.setPixel(i, c, Color.WHITE);
            }
        }
        //Return original image
        return uris[0];
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = ProgressDialog.show(activity, activity.getResources().getString(R.string.binarization_in_progres),
                activity.getResources().getString(R.string.please_wait), true);
    }

    @Override
    protected void onPostExecute(Uri uri) {
        super.onPostExecute(uri);
        dialog.dismiss();
        MainActivity.crop(activity, getImageUri(activity.getApplicationContext(), binarizedImage));
    }

    private static boolean shouldBeBlack(int pixel) {
        int alpha = Color.alpha(pixel);
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);
        if (alpha == 0x00) //if this pixel is transparent let me use TRASNPARENT_IS_BLACK
            return TRASNPARENT_IS_BLACK;
        // distance from the white extreme
        double distanceFromWhite = Math.sqrt(Math.pow(0xff - redValue, 2) + Math.pow(0xff - blueValue, 2) + Math.pow(0xff - greenValue, 2));
        // distance from the black extreme //this should not be computed and might be as well a function of distanceFromWhite and the whole distance
        double distanceFromBlack = Math.sqrt(Math.pow(0x00 - redValue, 2) + Math.pow(0x00 - blueValue, 2) + Math.pow(0x00 - greenValue, 2));
        // distance between the extremes //this is a constant that should not be computed :p
        double distance = distanceFromBlack + distanceFromWhite;
        // distance between the extremes
        return ((distanceFromWhite / distance) > MainActivity.getTreshold());
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        int[] qualities = MainActivity.getQualities();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, qualities[MainActivity.getCursor()], bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
