package com.example.dpiotr.ocreader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private static final int PHOTO_REQUEST = 1;
    private static final int SELECT_PICTURE = 2;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    /****************OPTIONS VARS*****************/
    private static int[] scales = {800, 600, 400};
    private static int[] qualities = {100, 95, 90};
    private static boolean binarizationIsOn = false;
    private static int cursor = 0;
    private static double treshold = 0.45;
    private final Context context = this;
    /****************END**************************/
    private Toolbar toolbar;
    private TextView scanResults;
    private Uri imageUri;
    private Uri resultUri;
    private TextRecognizer detector;

    public static Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = scales[cursor];
        int targetH = scales[cursor];
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }

    public static boolean isBinarizationIsOn() {
        return binarizationIsOn;
    }

    public static void setBinarizationIsOn(boolean binarizationIsOn) {
        MainActivity.binarizationIsOn = binarizationIsOn;
    }

    public static void crop(Activity activity, Uri uri) {
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(activity);
    }

    public static int getCursor() {
        return cursor;
    }

    public static void setCursor(int cursor) {
        MainActivity.cursor = cursor;
    }

    public static double getTreshold() {
        return treshold;
    }

    public static void setTreshold(double treshold) {
        MainActivity.treshold = treshold;
    }

    public static int[] getQualities() {
        return qualities;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);

        SharedPreferences sharedPref = getSharedPreferences("OPTIONS", Context.MODE_PRIVATE);
        binarizationIsOn = sharedPref.getBoolean("binarizationOption", false);
        treshold = Double.parseDouble(sharedPref.getString("treshold", "0.45"));
        cursor = sharedPref.getInt("cursor", 0);

        detector = new TextRecognizer.Builder(getApplicationContext()).build();

        ImageButton editButton = (ImageButton) findViewById(R.id.editButton);
        editButton.setVisibility(View.INVISIBLE);
        ImageButton saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setVisibility(View.INVISIBLE);

        final ImageButton scanButton = (ImageButton) findViewById(R.id.scanButton);
        ImageButton openButton = (ImageButton) findViewById(R.id.openButton);
        ImageButton openInOtherButton = (ImageButton) findViewById(R.id.openInOtherButton);
        scanResults = (TextView) findViewById(R.id.scanResultField);
        if (scanResults.getText().length() == 0) {
            scanResults.setText(getResources().getString(R.string.run_scan));
            scanResults.setGravity(Gravity.CENTER);

        }

        if (savedInstanceState != null) {
            resultUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
            scanResults.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));

        }


        editButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                final AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View layout = inflater.inflate(R.layout.edit_dialog, null);
                final EditText editDialogEditText = (EditText) layout.findViewById(R.id.editDialogEditText);
                alert.setView(layout);
                editDialogEditText.setText(scanResults.getText().toString());
                alert.show();

                Button button = (Button) layout.findViewById(R.id.saveEditButton);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        {
                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                            builder.setMessage(R.string.save_edit)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            scanResults.setText(editDialogEditText.getText().toString());
                                            alert.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            alert.dismiss();
                                        }
                                    });
                            android.support.v7.app.AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                final AlertDialog alertSave = new AlertDialog.Builder(MainActivity.this).create();
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View layout = inflater.inflate(R.layout.save_dialog, null);
                final EditText saveDialogEditText = (EditText) layout.findViewById(R.id.saveDialogEditText);
                alertSave.setView(layout);
                alertSave.show();

                Button buttonSave = (Button) layout.findViewById(R.id.saveFileButton);
                buttonSave.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        String fileName = saveDialogEditText.getText().toString().trim();


                        if (fileName.length() != 0) {
                            fileName = fileName + ".txt";
                            String path =
                                    Environment.getExternalStorageDirectory() + File.separator + "OCRFolder";
                            File folder = new File(path);
                            folder.mkdirs();
                            File file = new File(folder, fileName);
                            try {
                                file.createNewFile();
                                FileOutputStream fOut = new FileOutputStream(file);
                                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                myOutWriter.append(scanResults.getText().toString());

                                myOutWriter.close();

                                fOut.flush();
                                fOut.close();
                                Toast.makeText(MainActivity.this,
                                        getResources().getString(R.string.saved), Toast.LENGTH_LONG).show();
                                alertSave.dismiss();

                            } catch (IOException e) {
                                Log.e("Exception: ", getResources().getString(R.string.save_fail) + e.toString());
                            }
                        } else Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.add_name), Toast.LENGTH_LONG).show();
                    }
                });

                Button buttonClose = (Button) layout.findViewById(R.id.closeButton);
                buttonClose.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        alertSave.dismiss();
                    }
                });


            }
        });


        scanButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);

            }
        });
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), SELECT_PICTURE);
            }
        });
        openInOtherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scanResults.getText().toString() != getResources().getString(R.string.run_scan) &&
                        scanResults.getText().toString() != getResources().getString(R.string.scan_fail)) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, scanResults.getText().toString());
                        intent.setType("text/plain");
                        startActivity(intent);

                    } catch (ActivityNotFoundException e) {
                    }
                } else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.cant_send), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.permission_dendied), Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            if (binarizationIsOn) {
                Binarize binarizeTask = new Binarize(MainActivity.this);
                binarizeTask.execute(imageUri);
            } else {
                crop(this, imageUri);
            }

        }
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            if (binarizationIsOn) {
                imageUri = data.getData();
                Binarize binarizeTask = new Binarize(MainActivity.this);
                binarizeTask.execute(imageUri);
            } else {
                imageUri = data.getData();
                crop(this, imageUri);

            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                launchMediaScanIntent();
                try {
                    Bitmap bitmap = decodeBitmapUri(this, resultUri);
                    if (detector.isOperational() && bitmap != null) {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> textBlocks = detector.detect(frame);
                        String blocks = "";
                        String lines = "";
                        String words = "";
                        for (int index = 0; index < textBlocks.size(); index++) {
                            //extract scanned text blocks here
                            TextBlock tBlock = textBlocks.valueAt(index);
                            blocks = blocks + tBlock.getValue() + "\n" + "\n";
                            for (Text line : tBlock.getComponents()) {
                                //extract scanned text lines here
                                lines = lines + line.getValue() + "\n";
                                for (Text element : line.getComponents()) {
                                    //extract scanned text words here
                                    words = words + element.getValue() + " ";
                                }
                            }
                        }
                        if (textBlocks.size() == 0) {
                            scanResults.setText(getResources().getString(R.string.scan_fail));
                            (findViewById(R.id.saveButton)).setVisibility(View.INVISIBLE);
                            (findViewById(R.id.editButton)).setVisibility(View.INVISIBLE);
                        } else {
                            scanResults.setGravity(Gravity.LEFT);
                            scanResults.setText("");
                            (findViewById(R.id.saveButton)).setVisibility(View.VISIBLE);
                            (findViewById(R.id.editButton)).setVisibility(View.VISIBLE);
                            //       editButton.setVisibility(View.INVISIBLE);
                            /*scanResults.setText(scanResults.getText() + "Blocks: " + "\n");
                            scanResults.setText(scanResults.getText() + blocks + "\n");
                            scanResults.setText(scanResults.getText() + "---------" + "\n");
                            scanResults.setText(scanResults.getText() + "Lines: " + "\n");
                            scanResults.setText(scanResults.getText() + lines + "\n");
                            scanResults.setText(scanResults.getText() + "---------" + "\n");
                            scanResults.setText(scanResults.getText() + "Words: " + "\n");*/
                            scanResults.setText(scanResults.getText() + words + "\n");
                            //scanResults.setText(scanResults.getText() + "---------" + "\n");
                        }
                    } else {
                        scanResults.setText(getResources().getString(R.string.detector_fail));
                        // Check for low storage.  If there is low storage, the native library will not be
                        // downloaded, so detection will not become operational.
                        IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                        boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                        if (hasLowStorage) {
                            Toast.makeText(this, getResources().getString(R.string.low_storage), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, getResources().getString(R.string.load_fail), Toast.LENGTH_SHORT)
                            .show();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (resultUri != null) {
            outState.putString(SAVED_INSTANCE_URI, resultUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, scanResults.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(resultUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            case R.id.options:
                Intent intentOptions = new Intent(this, OptionsActivity.class);
                startActivity(intentOptions);
                return true;
        }
        return false;
    }
}
