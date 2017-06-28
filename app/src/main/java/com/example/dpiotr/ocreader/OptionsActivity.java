package com.example.dpiotr.ocreader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class OptionsActivity extends AppCompatActivity {

    private final Context context = this;
    private Toolbar toolbar;
    private CheckBox checkBox;
    private SeekBar seekBar;
    private TextView treshold;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.options));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button toolbarBtn = (Button) findViewById(R.id.toolbar_ok_button);
        toolbarBtn.setVisibility(View.INVISIBLE);

        checkBox = (CheckBox) findViewById(R.id.binarizationOption);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        treshold = (TextView) findViewById(R.id.treshold);
        spinner = (Spinner) findViewById(R.id.spinner);

        spinner.setSelection(MainActivity.getCursor());

    /*    toolbarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.save)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences sharedPref = getSharedPreferences("OPTIONS", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("binarizationOption", MainActivity.isBinarizationIsOn());
                                editor.putString("treshold", String.valueOf(MainActivity.getTreshold()));
                                editor.putInt("cursor", MainActivity.getCursor());
                                editor.commit();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });*/

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                MainActivity.setCursor(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getSettings();

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(true);
                    MainActivity.setBinarizationIsOn(true);
                } else {
                    checkBox.setChecked(false);
                    MainActivity.setBinarizationIsOn(false);
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Double d = (((seekBar.getProgress() + 1) * 0.055) + 0.33);
                treshold.setText(String.valueOf(((seekBar.getProgress() + 1) * 0.055) + 0.33));
                MainActivity.setTreshold(d);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.save)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences sharedPref = getSharedPreferences("OPTIONS", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("binarizationOption", MainActivity.isBinarizationIsOn());
                            editor.putString("treshold", String.valueOf(MainActivity.getTreshold()));
                            editor.putInt("cursor", MainActivity.getCursor());
                            editor.commit();
                            onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            onBackPressed();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
                //Call the back button's method
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void getSettings() {
        if (MainActivity.isBinarizationIsOn()) checkBox.setChecked(true);
        else checkBox.setChecked(false);
        Double d = MainActivity.getTreshold();
        d = (d - 0.385) / 0.055;
        Log.d("VALUE", String.valueOf(d));
        seekBar.setProgress((int) Math.round(d));
        treshold.setText(String.valueOf(MainActivity.getTreshold()));
    }

}
