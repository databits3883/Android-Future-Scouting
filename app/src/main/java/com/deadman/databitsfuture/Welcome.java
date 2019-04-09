package com.deadman.databitsfuture;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.kimkevin.cachepot.CachePot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class Welcome extends Fragment {

    public Welcome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Go Full screen and hide navbar
        View decorView = Objects.requireNonNull(getActivity()).getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        spinnerinit();
        mkdirs();

    }

    @Override
    public void onPause() {
        super.onPause();
        Spinner spinner1 =  Objects.requireNonNull(getView()).findViewById(R.id.spinner1);
        SharedPreferences.Editor editor = Objects.requireNonNull(getActivity()).getSharedPreferences("CurrentUser", MODE_PRIVATE).edit();
        editor.putInt("pos", spinner1.getSelectedItemPosition());
        editor.apply();
    }

    public void onResume() {
        super.onResume();
        Spinner spinner1 =  Objects.requireNonNull(getView()).findViewById(R.id.spinner1);
        SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences("CurrentUser", MODE_PRIVATE);
        int pos = prefs.getInt("pos", 0);
        spinner1.setSelection(pos);
    }

    private void spinnerinit(){
        Spinner spinner1 =  Objects.requireNonNull(getView()).findViewById(R.id.spinner1);
        List<String> list = new ArrayList<>();
        list.add("Practice Mode");
        list.add("Red 1");
        list.add("Red 2");
        list.add("Red 3");
        list.add("Blue 1");
        list.add("Blue 2");
        list.add("Blue 3");
        ArrayAdapter dataAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner1.setAdapter(dataAdapter);

        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        Integer intobj = spinner1.getSelectedItemPosition();
        CachePot.getInstance().push(1,intobj);
        CachePot.getInstance().push(2,intobj);
    }

    private boolean practice_mode(){
        Spinner spinner1 =  Objects.requireNonNull(getView()).findViewById(R.id.spinner1);
        return spinner1.getSelectedItemPosition() == 0;
    }

    private void mkdirs(){

        // Create the FRC folders in case they are missing, complain if teams.csv is missing as well
        File frc = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC");
        File robots = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "Robots");
        File qr = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "QR");
        File teams = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "teams.csv");
        if (!frc.exists()) {
            frc.mkdirs();
        }
        if (!robots.exists()) {
            robots.mkdirs();
        }
        if (!qr.exists()) {
            qr.mkdirs();
        }
        if(!teams.exists() & (practice_mode())){
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.confirm_missing_team_dialog_message)
                    .setTitle(R.string.confirm_missing_team_dialog_title)
                    .setPositiveButton(R.string.missing_team_export, (dialog, id) -> {
                    })
                    .show();
        }
    }
}