package com.deadman.databitsfuture;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;


public class Settings extends Fragment {

    public Settings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
                // Inflate the layout for this fragment
        return inflater.inflate(R.layout.settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Go Full screen and hide navbar
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        final Button deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> delete());
    }


    // Remove the stats.csv file to prepare for the next competition
    private void delete (){
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.confirm_dialog_message)
                .setTitle(R.string.confirm_dialog_title)
                .setPositiveButton(R.string.confirm, (dialog, id) -> {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator +"Download"+File.separator+"stats.csv");
                    File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator +"Download"+File.separator+"crowd_data.csv");
                    boolean deleted = file.delete();
                    boolean deleted2 = file2.delete();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    // CANCEL
                })
                .show();
    }

}