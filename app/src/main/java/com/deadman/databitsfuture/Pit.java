package com.deadman.databitsfuture;

import android.app.AlertDialog;
import android.media.MediaScannerConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import vn.luongvo.widget.iosswitchview.SwitchView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.alexzaitsev.meternumberpicker.MeterView;
import com.mohammedalaa.seekbar.RangeSeekBarView;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


public class Pit extends Fragment {


    public Pit() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.pit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Go Full screen and hide navbar
        View decorView = Objects.requireNonNull(getActivity()).getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        spinnerinit();

        final Button exportButton = view.findViewById(R.id.save_button);
        exportButton.setOnClickListener(v -> savebutton());
    }

    // Export entered pit data to a CSV file and reset the fields
    private void savebutton(){
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.confirm_export_dialog_message)
                .setTitle(R.string.confirm_export_dialog_title)
                .setPositiveButton(R.string.confirm_export, (dialog, id) -> {
                    write_data_pit();
                    reset_info_pit();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    // CANCEL
                })
                .show();
    }

    // Function to create the backup csv file
    private void write_data_pit(){
        String results = datastring();
        String header = getResources().getString(R.string.pit_header);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + "FRC" + File.separator + "pit_data.csv");
        try {
            FileWriter output = new FileWriter(file, true);

            CSVWriter writer = new CSVWriter(output, ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    "\r\n");
            List<String[]> data = new ArrayList<>();
            if (file.length() == 0) {
                data.add(new String[] {header});
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
        rescan(file.getAbsolutePath());
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

    private void spinnerinit(){
        Spinner spinner1 = Objects.requireNonNull(getView()).findViewById(R.id.spinner1);
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
        list.add("Other (Put in comments)");
        ArrayAdapter dataAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner1.setAdapter(dataAdapter);

        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        spinner1.setSelection(0);
    }

    private String spinnerget(){
        Spinner spinner1 = Objects.requireNonNull(getView()).findViewById(R.id.spinner1);
        return spinner1.getSelectedItem().toString() + ",";
    }

    private String datastring(){
        return team()
        + spinnerget()
        + getselectors()
        + getswitches()
        + seekbars()
        + name()
        + comments();
    }

    private String team(){
        MeterView team = Objects.requireNonNull(getView()).findViewById(R.id.meterView);
        return Integer.toString(team.getValue())+ ",";
    }

    public String name(){
        TextView name = Objects.requireNonNull(getView()).findViewById(R.id.extended_edit_text);
        return name.getText().toString() + ",";
    }
    private String comments(){
        TextView name = Objects.requireNonNull(getView()).findViewById(R.id.comment_extended_edit_text);
        return name.getText().toString();
    }

    // Selectors start at 0 we want it to start at 1 so the data is easier to read
    private String selector(int id){
        SegmentedButtonGroup button = Objects.requireNonNull(getView()).findViewById(id);
        return String.valueOf(button.getPosition() + 1);
    }

    private String getselectors(){
        AtomicReference<String> result = new AtomicReference<>("");
        List<Integer> list = new ArrayList<>();
        list.add(R.id.buttonGroup_launch);
        list.add(R.id.buttonGroup_vision);
        list.add(R.id.buttonGroup_pit_sandstorm);
        list.add(R.id.buttonGroup_rocket);
        list.add(R.id.buttonGroup_climb);
        list.forEach(
                (name) -> result.set(result + selector(name) + ",")
        );
        return result.get();
    }

    // Selectors start at 0 we want it to start at 1 so the data is easier to read
    private String switchy(int id){
        SwitchView switches = Objects.requireNonNull(getView()).findViewById(id);
        if (switches.isChecked()){
            return "1";
        } else {
            return "0";
        }
    }

    private String getswitches(){
        AtomicReference<String> result = new AtomicReference<>("");
        List<Integer> list = new ArrayList<>();
        list.add(R.id.hatch_ground_switch);
        list.add(R.id.hatch_station_switch);
        list.add(R.id.cargo_ground_switch);
        list.add(R.id.cargo_station_switch);
        list.forEach(
                (name) -> result.set(result + switchy(name) + ",")
        );
        return result.get();
    }

    private String seekbars(){
        RangeSeekBarView hatchbar = Objects.requireNonNull(getView()).findViewById(R.id.hatch_seekbar);
        RangeSeekBarView cargobar = getView().findViewById(R.id.cargo_seekbar);
        return hatchbar.getValue() + "," + cargobar.getValue() + ",";
    }

    // Resets all the fields on the page
    private void reset_info_pit(){
        TextView comment_field = Objects.requireNonNull(getView()).findViewById(R.id.comment_extended_edit_text);
        SwitchView hatch_ground = getView().findViewById(R.id.hatch_ground_switch);
        SwitchView hatch_station = getView().findViewById(R.id.hatch_station_switch);
        SwitchView cargo_ground = getView().findViewById(R.id.cargo_ground_switch);
        SwitchView cargo_station = getView().findViewById(R.id.cargo_station_switch);
        SegmentedButtonGroup launch = getView().findViewById(R.id.buttonGroup_launch);
        SegmentedButtonGroup vision = getView().findViewById(R.id.buttonGroup_vision);
        SegmentedButtonGroup sandstorm = getView().findViewById(R.id.buttonGroup_pit_sandstorm);
        SegmentedButtonGroup rocket = getView().findViewById(R.id.buttonGroup_rocket);
        SegmentedButtonGroup climb = getView().findViewById(R.id.buttonGroup_climb);
        RangeSeekBarView hatchbar = getView().findViewById(R.id.hatch_seekbar);
        RangeSeekBarView cargobar = getView().findViewById(R.id.cargo_seekbar);
        Spinner spinner1 = getView().findViewById(R.id.spinner1);

        spinner1.setSelection(0);
        hatchbar.setValue(0);
        cargobar.setValue(0);
        comment_field.setText("");
        hatch_ground.setChecked(false);
        hatch_station.setChecked(false);
        cargo_ground.setChecked(false);
        cargo_station.setChecked(false);
        launch.setPosition(0,true);
        vision.setPosition(0,true);
        sandstorm.setPosition(0,true);
        rocket.setPosition(0,true);
        climb.setPosition(0,true);
    }

}