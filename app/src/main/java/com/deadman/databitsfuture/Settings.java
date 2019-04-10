package com.deadman.databitsfuture;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.util.Objects;

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
        // Go Full screen
        View decorView = Objects.requireNonNull(getActivity()).getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        final Button deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> delete());

        Button api_button = view.findViewById(R.id.download_team);
        api_button.setOnClickListener(v -> download_teams());
    }

    private void download_teams() {
        String DownloadUrl = "https://docs.google.com/spreadsheets/d/e/2PACX-1vR-Aayod2wLHCgGKck__rxa0bas3tYn8RL6Lf3oNiD-TWo7InaNXrtRcjCMmuuRtkotoBzftYgSyT0T/pub?gid=0&single=true&output=csv";
        File teams = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "teams.csv");
        DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(DownloadUrl));
        request1.setDescription("Team match info");
        request1.setTitle("Teams.csv");
        request1.setVisibleInDownloadsUi(false);
        request1.allowScanningByMediaScanner();
        request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request1.setDestinationInExternalPublicDir("/FRC", "teams.csv");

        DownloadManager manager1 = (DownloadManager) Objects.requireNonNull(getContext()).getSystemService(Context.DOWNLOAD_SERVICE);
        Objects.requireNonNull(manager1).enqueue(request1);
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Downloaded the latest teams.csv successfully")
                    .setTitle("Download completed")
                    .setPositiveButton("Great!", (dialog, id) -> {
                    })
                    .show();
            rescan(teams.getAbsolutePath());
        }
    }


    // Remove the stats.csv file to prepare for the next competition
    private void delete() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.confirm_dialog_message)
                .setTitle(R.string.confirm_dialog_title)
                .setPositiveButton(R.string.confirm, (dialog, id) -> {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download" + File.separator + "stats.csv");
                    File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download" + File.separator + "crowd_data.csv");
                    file.delete();
                    file2.delete();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    // CANCEL
                })
                .show();
    }

    // Function to scan the edited file so it shows up right away in MTP/OTG
    private void rescan(String file){
        MediaScannerConnection.scanFile(getContext(),
                new String[] {file}, null,
                (path, uri) -> {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                });
    }
}