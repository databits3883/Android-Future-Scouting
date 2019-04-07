package com.deadman.databitsfuture;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.github.sumimakito.awesomeqr.AwesomeQRCode;
import com.opencsv.CSVReader;
import com.travijuu.numberpicker.library.NumberPicker;
import vn.luongvo.widget.iosswitchview.SwitchView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Crowd extends Fragment {


    public Crowd() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.crowd, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Go Full screen and hide navbar
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        final Button exportButton = view.findViewById(R.id.export);
        exportButton.setOnClickListener(v -> generateQrCode());

        final Button qrButton = view.findViewById(R.id.qr_display);
        qrButton.setOnClickListener(v -> showQrCode());

        teams();
    }

    private int getmatch(){
        NumberPicker match =  getView().findViewById(R.id.match_counter);
        return match.getValue();
    }

    private void incrementmatch(){
        NumberPicker match =  getView().findViewById(R.id.match_counter);
        match.setValue(match.getValue() + 1);
    }

    private void showQrCode(){
        int prev = getmatch() - 1;
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "QR" + File.separator + Integer.toString(prev) + ".png");
        Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        Drawable d = new BitmapDrawable(getResources(), myBitmap);
        ImagePopup imagePopup = new ImagePopup(getContext());
        imagePopup.setImageOnClickClose(true);
        imagePopup.setHideCloseIcon(true);
        imagePopup.initiatePopup(d);
        imagePopup.viewPopup();
    }

    private void generateQrCode(){
        Bitmap logo = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.logo);
        new AwesomeQRCode.Renderer()
                .contents(datastring())
                .size(800).margin(20)
                .logo(logo)
                .logoScale(0.3f)
                .renderAsync(new AwesomeQRCode.Callback() {
                    @Override
                    public void onRendered(AwesomeQRCode.Renderer renderer, final Bitmap bitmap) {
                        getActivity().runOnUiThread(() -> {
                            // Tip: here we use runOnUiThread(...) to avoid the problems caused by operating UI elements from a non-UI thread.
                            File f = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "QR" + File.separator + Integer.toString(getmatch()) + ".png");
                            try {
                                FileOutputStream  bytes = new FileOutputStream(f);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                                bytes.flush();
                                bytes.close();
                                MediaScannerConnection.scanFile(getContext(),
                                        new String[] {f.getAbsolutePath()}, null,
                                        (path, uri) -> {
                                            Log.i("ExternalStorage", "Scanned " + path + ":");
                                            Log.i("ExternalStorage", "-> uri=" + uri);
                                        });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // Generate the confirmation to make the QR Code
                            new AlertDialog.Builder(getContext())
                                    .setMessage(R.string.confirm_export_dialog_message)
                                    .setTitle(R.string.confirm_export_dialog_title)
                                    .setPositiveButton(R.string.confirm_export, (dialog, id) -> {
                                        // Generates the QR Code
                                        Drawable d = new BitmapDrawable(getResources(), bitmap);
                                        ImagePopup imagePopup = new ImagePopup(getContext());
                                        imagePopup.setImageOnClickClose(true);
                                        imagePopup.setHideCloseIcon(true);
                                        imagePopup.setFullScreen(true);
                                        imagePopup.initiatePopup(d);
                                        imagePopup.viewPopup();

                                        // Increment the match number
                                        incrementmatch();

                                        // Set the team number based on the match number
                                        teams();

                                    })
                                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                        // CANCEL
                                    })
                                    .show();
                        });
                    }

                    @Override
                    public void onError(AwesomeQRCode.Renderer renderer, Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    // Read the team data from teams.csv
    public String read_teams(){
        int pos = 1;
        int match = getmatch();
        String[][] dataArr;
        String test = "";
        if (pos == 0){
            Toast.makeText(getContext(), "Warning: You are in practice mode", Toast.LENGTH_SHORT).show();
        } else {
            try {
                CSVReader csvReader = new CSVReader(new FileReader(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC" + File.separator + "teams.csv")));
                List<String[]> list = csvReader.readAll();
                dataArr = new String[list.size()][];
                dataArr = list.toArray(dataArr);
                test = dataArr[match][pos];
            } catch (IOException e) {
                e.printStackTrace();
            }
            return test;
        }
        return "";
    }

    // Sets the team number based on Teams.csv file
    private void teams(){
        EditText team_num = getView().findViewById(R.id.team_field);
        team_num.setText(read_teams());
    }

    public String datastring(){
        EditText team_num = getView().findViewById(R.id.team_field);
        String final_string =
                team_num.getText().toString() + ","
                + getselectors()
                + climb_failed()
                + getcounters()
                + total_hatch() + ","
                + total_cargo() + ","
                + all_total()
                + name()
                + comments();
        return final_string;
    }

    private String counter(int id){
        NumberPicker picker = getView().findViewById(id);
        return String.valueOf(picker.getValue());
    }

    // Create the string for most of the data
    private String getcounters (){
        AtomicReference<String> result = new AtomicReference<>("");
        List<Integer> list = new ArrayList<>();
        list.add(R.id.match_counter);
        list.add(R.id.rocket_top_hatch_counter);
        list.add(R.id.rocket_middle_hatch_counter);
        list.add(R.id.rocket_bottom_hatch_counter);
        list.add(R.id.cargo_ship_hatch_counter);
        list.add(R.id.rocket_top_cargo_counter);
        list.add(R.id.rocket_middle_cargo_counter);
        list.add(R.id.rocket_bottom_cargo_counter);
        list.add(R.id.cargo_ship_cargo_counter);
        list.forEach(
                (name) -> result.set(result + counter(name) + ",")
        );
        return result.get();
    }

    private String selector(int id){
        SegmentedButtonGroup button = getView().findViewById(id);
        return String.valueOf(button.getPosition());
    }

    private String getselectors(){
        AtomicReference<String> result = new AtomicReference<>("");
        List<Integer> list = new ArrayList<>();
        list.add(R.id.buttonGroup_crowd_launch);
        list.add(R.id.buttonGroup_climb);
        list.add(R.id.buttonGroup_sandstorm);
        list.add(R.id.buttonGroup_crowd_defense);
        list.forEach(
                (name) -> result.set(result + selector(name) + ",")
        );
        return result.get();
    }

    // All hatch data added together for a match
    private String total_hatch (){
        NumberPicker top = getView().findViewById(R.id.rocket_top_hatch_counter);
        NumberPicker mid = getView().findViewById(R.id.rocket_middle_hatch_counter);
        NumberPicker bot = getView().findViewById(R.id.rocket_bottom_hatch_counter);
        NumberPicker ship = getView().findViewById(R.id.cargo_ship_hatch_counter);
        int total = top.getValue() + mid.getValue() + bot.getValue() + ship.getValue();
        return Integer.toString(total);
    }

    // All cargo data added together for a match
    private String total_cargo (){
        NumberPicker top = getView().findViewById(R.id.rocket_top_hatch_counter);
        NumberPicker mid = getView().findViewById(R.id.rocket_middle_hatch_counter);
        NumberPicker bot = getView().findViewById(R.id.rocket_bottom_hatch_counter);
        NumberPicker ship = getView().findViewById(R.id.cargo_ship_hatch_counter);
        int total = top.getValue() + mid.getValue() + bot.getValue() + ship.getValue();
        return Integer.toString(total);
    }

    // All cargo + hatch added together
    private String all_total (){
        return Integer.toString(Integer.parseInt(total_cargo()) + Integer.parseInt(total_hatch())) + ",";
    }

    private String climb_failed() {
        SwitchView climb_failed = getView().findViewById(R.id.climb_failed);
        if (climb_failed.isChecked()){
            return "1,";
        } else {
            return "0,";
        }
    }

    private String name() {
        EditText team = getView().findViewById(R.id.name_field);
        return team.getText().toString() + ",";
    }

    private String comments() {
        EditText comments = getView().findViewById(R.id.comment_field);
        String comment_string = comments.getText().toString();
        return comment_string.replaceAll(",", " ");
    }

}