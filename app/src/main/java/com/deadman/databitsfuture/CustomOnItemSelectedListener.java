package com.deadman.databitsfuture;

import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CustomOnItemSelectedListener implements OnItemSelectedListener {

    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        File position = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "misc" + File.separator + "device_position.txt");
        try {
            FileOutputStream stream = new FileOutputStream(position);
            stream.write(Integer.toString(pos).getBytes());
            stream.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // Auto-generated method stub
    }

}