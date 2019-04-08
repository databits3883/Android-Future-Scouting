package com.deadman.databitsfuture;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.google.zxing.Result;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scanner extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view

        setContentView(R.layout.scanner);

        ViewGroup contentFrame = findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    // Function to create the stats.csv from the data gotten from the QR Code
    @Override
    public void handleResult(Result rawResult) {

        String results = rawResult.getText();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator +"FRC"+File.separator+"stats.csv");
        try {
            FileWriter outputfile = new FileWriter(file, true);

            CSVWriter writer = new CSVWriter(outputfile, ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    "\r\n");
            List<String[]> data = new ArrayList<>();
            if (file.length() == 0) {
                data.add(new String[] {getResources().getString(R.string.crowd_header)});
            }
            data.add(new String[] {results});
            writer.writeAll(data);
            writer.flush();
            writer.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        String splitted[] = results.split(",",2);
        String team = splitted[0];
        File team_num = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator +"FRC"+File.separator+"temp.csv");
        try {
            FileWriter outputfile = new FileWriter(team_num, true);

            CSVWriter writer = new CSVWriter(outputfile, ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    "\r\n");
            List<String[]> data = new ArrayList<>();
            data.add(new String[] {team});
            writer.writeAll(data);
            writer.flush();
            writer.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        rescan(file.getAbsolutePath());
        rescan(team_num.getAbsolutePath());

        this.onBackPressed();
    }


    // Function to scan the edited file so it shows up right away in MTP/OTG
    public void rescan(String file){
        MediaScannerConnection.scanFile(this,
                new String[] {file}, null,
                (path, uri) -> {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                });
    }
}