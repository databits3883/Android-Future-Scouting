package com.deadman.databitsfuture;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
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

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer;
import com.github.sumimakito.awesomeqr.RenderResult;
import com.github.sumimakito.awesomeqr.option.color.ColorQR;
import com.github.sumimakito.awesomeqr.option.logo.Logo;
import com.github.sumimakito.awesomeqr.option.RenderOption;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Listener.DefaultValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;
import vn.luongvo.widget.iosswitchview.SwitchView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Crowd extends Fragment {

    private int pos = getposition();

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
        // Go Full screen
        View decorView = Objects.requireNonNull(getActivity()).getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        final Button exportButton = view.findViewById(R.id.export);
        exportButton.setOnClickListener(v -> generateQrCode());

        final Button qrButton = view.findViewById(R.id.qr_display);
        qrButton.setOnClickListener(v -> showQrCode());

        NumberPicker match_num =  Objects.requireNonNull(getView()).findViewById(R.id.match_counter);
        match_num.setValueChangedListener(new DefaultValueChangedListener() {
            public void valueChanged(int value, ActionEnum action) {
                teams();
            }});

        teams();

        button_transform();

        reset_info();
    }

    private int getposition(){
        File position = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "misc" + File.separator + "device_position.txt");
        int length = (int) position.length();

        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(position);
            in.read(bytes);
            in.close();
        } catch (
                FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String contents = new String(bytes);

        rescan(position.getAbsolutePath());

        return Integer.parseInt(contents);
    }

    private int getmatch(){
        NumberPicker match =  Objects.requireNonNull(getView()).findViewById(R.id.match_counter);
        return match.getValue();
    }

    private void setmatch(int val){
        NumberPicker match =  Objects.requireNonNull(getView()).findViewById(R.id.match_counter);
        match.setValue(val);
    }

    private void incrementmatch(){
        NumberPicker match =  Objects.requireNonNull(getView()).findViewById(R.id.match_counter);
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
        imagePopup.setFullScreen(true);
        imagePopup.initiatePopup(d);
        imagePopup.viewPopup();
    }

    private void generateQrCode() {
        Bitmap logobit = BitmapFactory.decodeResource(Objects.requireNonNull(getContext()).getResources(), R.drawable.logo);
        Logo logo = new Logo();
        logo.setBitmap(logobit);
        logo.setBorderRadius(10); // radius for logo's corners
        logo.setBorderWidth(10); // width of the border to be added around the logo
        logo.setScale(0.3f); // scale for the logo in the QR code
        logo.setClippingRect(new RectF(0, 0, 200, 200)); // crop the logo image before applying it to the QR code

        ColorQR color = new ColorQR();
        color.setLight(getResources().getColor(R.color.white,null)); // for blank spaces
        color.setDark(getResources().getColor(R.color.green_900,null)); // for non-blank spaces
        color.setBackground(getResources().getColor(R.color.white,null)); // for the background (will be overridden by background images, if set)
        color.setAuto(false); // set to true to automatically pick out colors from the background image (will only work if background image is present)

        RenderOption renderOption = new RenderOption();
        renderOption.setContent(datastring()); // content to encode
        renderOption.setSize(800); // size of the final QR code image
        renderOption.setBorderWidth(20); // width of the empty space around the QR code
        renderOption.setEcl(ErrorCorrectionLevel.M); // (optional) specify an error correction level
        renderOption.setPatternScale(0.85f); // (optional) specify a scale for patterns
        renderOption.setRoundedPatterns(false); // (optional) if true, blocks will be drawn as dots instead
        renderOption.setClearBorder(true); // if set to true, the background will NOT be drawn on the border area
        renderOption.setColorQR(color); // set a colorQR palette for the QR code
        renderOption.setLogo(logo); // set a logo, keep reading to find more about it

        try {
            RenderResult render = AwesomeQrRenderer.render(renderOption);
            if (render.getBitmap() != null) {
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "QR" + File.separator + Integer.toString(getmatch()) + ".png");
                try {
                    FileOutputStream  bytes = new FileOutputStream(f);
                    render.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, bytes);
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
                            Drawable d = new BitmapDrawable(getResources(), render.getBitmap());
                            ImagePopup imagePopup = new ImagePopup(getContext());
                            imagePopup.setImageOnClickClose(true);
                            imagePopup.setHideCloseIcon(true);
                            imagePopup.setFullScreen(true);
                            imagePopup.initiatePopup(d);
                            imagePopup.viewPopup();

                            // Write to the backup CSV file
                            write_data();

                            // Reset all fields to defaults
                            reset_info();

                            // Increment the match number
                            incrementmatch();

                            // Set the team number based on the match number
                            teams();

                        })
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                            // CANCEL
                        })
                        .show();
            }
            else {
                Log.i("QR", "Bad Bitmap in QR");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to create the backup csv file
    private void write_data(){
        String results = datastring();
        String header = getResources().getString(R.string.crowd_header);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + "FRC" + File.separator + "crowd_data.csv");
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

    // Read the team data from teams.csv
    private String read_teams(){
        int match = getmatch();
        String[][] dataArr;
        String test = "";
        if (pos == 0){
//            Toast.makeText(getContext(), "Warning: You are in practice mode", Toast.LENGTH_SHORT).show();
        } else {
            try {
                CSVReader csvReader = new CSVReader(new FileReader(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC" + File.separator + "teams.csv")));
                List<String[]> list = csvReader.readAll();
                dataArr = new String[list.size()][];
                dataArr = list.toArray(dataArr);
                if (match < list.size())
                    test = dataArr[match][pos];
                else {
                    setmatch(list.size() - 1);
                    test = dataArr[list.size() - 1][pos];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return test;
        }
        return "";
    }

    // Sets the team number based on Teams.csv file
    private void teams(){
        EditText team_num = Objects.requireNonNull(getView()).findViewById(R.id.team_field);
        team_num.setText(read_teams());
    }

    private String datastring(){
        EditText team_num = Objects.requireNonNull(getView()).findViewById(R.id.team_field);
        return team_num.getText().toString() + ","
        + getcounters()
        + total_hatch() + ","
        + total_cargo() + ","
        + all_total()
        + getselectors()
        + climb_failed()
        + name()
        + comments();
    }

    private String counter(int id){
        NumberPicker picker = Objects.requireNonNull(getView()).findViewById(id);
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

    // Selectors start at 0 we want it to start at 1 so the data is easier to read
    private String selector(int id){
        SegmentedButtonGroup button = Objects.requireNonNull(getView()).findViewById(id);
        return String.valueOf(button.getPosition() + 1);
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
        NumberPicker top = Objects.requireNonNull(getView()).findViewById(R.id.rocket_top_hatch_counter);
        NumberPicker mid = getView().findViewById(R.id.rocket_middle_hatch_counter);
        NumberPicker bot = getView().findViewById(R.id.rocket_bottom_hatch_counter);
        NumberPicker ship = getView().findViewById(R.id.cargo_ship_hatch_counter);
        int total = top.getValue() + mid.getValue() + bot.getValue() + ship.getValue();
        return Integer.toString(total);
    }

    // All cargo data added together for a match
    private String total_cargo (){
        NumberPicker top = Objects.requireNonNull(getView()).findViewById(R.id.rocket_top_cargo_counter);
        NumberPicker mid = getView().findViewById(R.id.rocket_middle_cargo_counter);
        NumberPicker bot = getView().findViewById(R.id.rocket_bottom_cargo_counter);
        NumberPicker ship = getView().findViewById(R.id.cargo_ship_cargo_counter);
        int total = top.getValue() + mid.getValue() + bot.getValue() + ship.getValue();
        return Integer.toString(total);
    }

    // All cargo + hatch added together
    private String all_total (){
        return Integer.toString(Integer.parseInt(total_cargo()) + Integer.parseInt(total_hatch())) + ",";
    }

    private String climb_failed() {
        SwitchView climb_failed = Objects.requireNonNull(getView()).findViewById(R.id.climb_failed);
        if (climb_failed.isChecked()){
            return "1,";
        } else {
            return "0,";
        }
    }

    private String name() {
        EditText team = Objects.requireNonNull(getView()).findViewById(R.id.name_field);
        return team.getText().toString() + ",";
    }

    private String comments() {
        EditText comments = Objects.requireNonNull(getView()).findViewById(R.id.comment_field);
        String comment_string = comments.getText().toString();
        return comment_string.replaceAll(",", " ");
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

    // Resets all the fields on the page
    private void reset_info(){
        EditText comment_field = Objects.requireNonNull(getView()).findViewById(R.id.comment_field);
        SwitchView climb_failed = getView().findViewById(R.id.climb_failed);
        SegmentedButtonGroup launch = getView().findViewById(R.id.buttonGroup_crowd_launch);
        SegmentedButtonGroup climb = getView().findViewById(R.id.buttonGroup_climb);
        SegmentedButtonGroup sandstorm = getView().findViewById(R.id.buttonGroup_sandstorm);
        SegmentedButtonGroup defense = getView().findViewById(R.id.buttonGroup_crowd_defense);
        NumberPicker top_hatch = getView().findViewById(R.id.rocket_top_hatch_counter);
        NumberPicker mid_hatch = getView().findViewById(R.id.rocket_middle_hatch_counter);
        NumberPicker bot_hatch = getView().findViewById(R.id.rocket_bottom_hatch_counter);
        NumberPicker ship_hatch = getView().findViewById(R.id.cargo_ship_hatch_counter);
        NumberPicker top_cargo = getView().findViewById(R.id.rocket_top_cargo_counter);
        NumberPicker mid_cargo = getView().findViewById(R.id.rocket_middle_cargo_counter);
        NumberPicker bot_cargo = getView().findViewById(R.id.rocket_bottom_cargo_counter);
        NumberPicker ship_cargo = getView().findViewById(R.id.cargo_ship_cargo_counter);

        comment_field.setText("");
        climb_failed.setChecked(false);
        launch.setPosition(0,true);
        climb.setPosition(0,true);
        sandstorm.setPosition(0,true);
        defense.setPosition(0,true);
        top_hatch.setValue(0);
        mid_hatch.setValue(0);
        bot_hatch.setValue(0);
        ship_hatch.setValue(0);
        top_cargo.setValue(0);
        mid_cargo.setValue(0);
        bot_cargo.setValue(0);
        ship_cargo.setValue(0);
    }

    private void button_transform(){
        Button export = Objects.requireNonNull(getView()).findViewById(R.id.export);
        String Position = "";
        if (pos == 0) {
            Position = " Practice Mode";
            export.setBackgroundColor(Color.GREEN);
        } else if (pos == 1) {
            Position = " Red 1";
            export.setBackgroundColor(Color.RED);
            export.setTextColor(Color.WHITE);
        } else if (pos == 2) {
            Position = " Red 2";
            export.setBackgroundColor(Color.RED);
            export.setTextColor(Color.WHITE);
        } else if (pos == 3) {
            Position = " Red 3";
            export.setBackgroundColor(Color.RED);
            export.setTextColor(Color.WHITE);
        } else if (pos == 4) {
            Position = " Blue 1";
            export.setBackgroundColor(Color.BLUE);
            export.setTextColor(Color.WHITE);
        } else if (pos == 5) {
            Position = " Blue 2";
            export.setBackgroundColor(Color.BLUE);
            export.setTextColor(Color.WHITE);
        } else if (pos == 6) {
            Position = " Blue 3";
            export.setBackgroundColor(Color.BLUE);
            export.setTextColor(Color.WHITE);
        }

        export.setText(getString(R.string.export_to_master_device, Position));
    }
}