package com.deadman.databitsfuture;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;


public class Pit extends Fragment {


    public Pit() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.pit, container, false);

        // Go Full screen and hide navbar
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        Spinner spinner1 = rootview.findViewById(R.id.spinner1);
        List<String> list = new ArrayList<>();
        list.add("Choose one");
        list.add("Kit of parts (KoP)");
        list.add("West Coast Drive (WCD)");
        list.add("Mecanum");
        list.add("Swerve");
        list.add("H-Drive");
        list.add("Kiwi");
        list.add("Octicanum");
        list.add("Posicanum");
        list.add("Butterfly");
        ArrayAdapter dataAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner1.setAdapter(dataAdapter);

        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        spinner1.setSelection(0);

        // Inflate the layout for this fragment
        return rootview;
    }

}