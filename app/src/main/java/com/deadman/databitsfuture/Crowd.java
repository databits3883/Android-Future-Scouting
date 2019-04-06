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

import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.github.sumimakito.awesomeqr.AwesomeQRCode;
import com.travijuu.numberpicker.library.NumberPicker;

import java.io.File;
import java.io.FileOutputStream;

public class Crowd extends Fragment {


    public Crowd() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.crowd, container, false);

        // Go Full screen and hide navbar
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        final Button exportButton = rootview.findViewById(R.id.export);
        exportButton.setOnClickListener(view -> generateQrCode());

        final Button qrButton = rootview.findViewById(R.id.qr_display);
        qrButton.setOnClickListener(view -> showQrCode());

        // Inflate the layout for this fragment
        return rootview;
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
                .contents("4225,8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,2,1,1,0,0,0,0,jacob Nelsen,is a very slow moving robot and cant really do much")
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
                                incrementmatch();
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
                                        imagePopup.initiatePopup(d);
                                        imagePopup.viewPopup();

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

}