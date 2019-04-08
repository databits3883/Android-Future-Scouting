package com.deadman.databitsfuture;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.kimkevin.cachepot.CachePot;
import com.opencsv.CSVReader;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Listener.DefaultValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class Master extends Fragment {

    private int mCounter = 0;

    public Master() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Object obj = "";
        CachePot.getInstance().push(3,obj);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.master, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Go Full screen and hide navbar
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        NumberPicker match_num =  getView().findViewById(R.id.match_counter);
        match_num.setValueChangedListener(new DefaultValueChangedListener() {
            public void valueChanged(int value, ActionEnum action) {
                read_teams();
            }});

        read_teams();
    }

    private void scanner(){
        Intent intent = new Intent(getActivity(), Scanner.class);
        startActivity(intent);
    }

    private void incrementmatch(){
        NumberPicker match =  getView().findViewById(R.id.match_counter);
        match.setValue(match.getValue() + 1);
        read_teams();
    }

    @Override
    public void onResume() {
        read_teams();
        super.onResume();
    }

    // Read the team data from teams.csv
    private String read_teams(){
        NumberPicker match_num =  getView().findViewById(R.id.match_counter);
        TextView redone = getView().findViewById(R.id.red1);
        TextView redtwo = getView().findViewById(R.id.red2);
        TextView redthree = getView().findViewById(R.id.red3);
        TextView blueone = getView().findViewById(R.id.blue1);
        TextView bluetwo = getView().findViewById(R.id.blue2);
        TextView bluethree = getView().findViewById(R.id.blue3);
        int match = match_num.getValue();
        String[][] dataArr;
        String test = "";
        try {
            File teams = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC" + File.separator + "teams.csv");
            CSVReader csvReader = new CSVReader(new FileReader(teams));
            List<String[]> list = csvReader.readAll();
            dataArr = new String[list.size()][];
            dataArr = list.toArray(dataArr);
            redone.setText(dataArr[match][1]);
            redtwo.setText(dataArr[match][2]);
            redthree.setText(dataArr[match][3]);
            blueone.setText(dataArr[match][4]);
            bluetwo.setText(dataArr[match][5]);
            bluethree.setText(dataArr[match][6]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File temp = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC" + File.separator + "temp.csv");
            CSVReader teamReader = new CSVReader(new FileReader(temp));
            List<String[]> team_num = teamReader.readAll();
            dataArr = new String[team_num.size()][];
            dataArr = team_num.toArray(dataArr);
            String team = (dataArr[0][0]);

            if (team.contentEquals(redone.getText())){
                redone.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            } else if (team.contentEquals(redtwo.getText())){
                redtwo.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            } else if (team.contentEquals(redthree.getText())){
                redthree.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            } else if (team.contentEquals(blueone.getText())){
                blueone.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            } else if (team.contentEquals(bluetwo.getText())){
                bluetwo.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            } else if (team.contentEquals(bluethree.getText())){
                bluethree.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            }
            boolean deleted = temp.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button btn = getView().findViewById(R.id.scan_qr_button);
        btn.setOnClickListener(view -> {
             scanner();
             mCounter ++;
            if (mCounter == 6){
                mCounter = 0;
                incrementmatch();
                redone.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                redtwo.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                redthree.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                blueone.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
                bluetwo.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
                bluethree.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
            }
        });

        return test;
    }

}