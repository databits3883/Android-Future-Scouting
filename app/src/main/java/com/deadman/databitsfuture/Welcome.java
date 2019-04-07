package com.deadman.databitsfuture;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.kimkevin.cachepot.CachePot;

import java.util.ArrayList;
import java.util.List;

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
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        spinnerinit();

    }

    @Override
    public void onPause() {
        super.onPause();
        Spinner spinner1 =  getView().findViewById(R.id.spinner1);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("CurrentUser", MODE_PRIVATE).edit();
        editor.putInt("pos", spinner1.getSelectedItemPosition());
        editor.apply();
    }

    public void onResume() {
        super.onResume();
        Spinner spinner1 =  getView().findViewById(R.id.spinner1);
        SharedPreferences prefs = getActivity().getSharedPreferences("CurrentUser", MODE_PRIVATE);
        int pos = prefs.getInt("pos", 0);
        spinner1.setSelection(pos);
    }

    public void spinnerinit(){
        Spinner spinner1 =  getView().findViewById(R.id.spinner1);
        List<String> list = new ArrayList<>();
        list.add("Practice Mode");
        list.add("Red 1");
        list.add("Red 2");
        list.add("Red 3");
        list.add("Blue 1");
        list.add("Blue 2");
        list.add("Blue 3");
        ArrayAdapter dataAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner1.setAdapter(dataAdapter);

        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        Integer intobj = spinner1.getSelectedItemPosition();
        CachePot.getInstance().push(1,intobj);
        CachePot.getInstance().push(2,intobj);
    }

}