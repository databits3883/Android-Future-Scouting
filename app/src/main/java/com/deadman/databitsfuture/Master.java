package com.deadman.databitsfuture;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kimkevin.cachepot.CachePot;
import com.opencsv.CSVReader;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Listener.DefaultValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class Master extends Fragment {

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

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

        Button api_button = getView().findViewById(R.id.upload_button);
        api_button.setOnClickListener(v -> getResultsFromApi());

        Button btn = getView().findViewById(R.id.scan_qr_button);
        btn.setOnClickListener(v -> scanner());

        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage("Calling Google Sheets API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
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
        color_teams();
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
            redone.setBackgroundTintList(getResources().getColorStateList(R.color.red));
            redtwo.setBackgroundTintList(getResources().getColorStateList(R.color.red));
            redthree.setBackgroundTintList(getResources().getColorStateList(R.color.red));
            blueone.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
            bluetwo.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
            bluethree.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return test;
    }

    private void color_teams() {
        try {
            TextView redone = getView().findViewById(R.id.red1);
            TextView redtwo = getView().findViewById(R.id.red2);
            TextView redthree = getView().findViewById(R.id.red3);
            TextView blueone = getView().findViewById(R.id.blue1);
            TextView bluetwo = getView().findViewById(R.id.blue2);
            TextView bluethree = getView().findViewById(R.id.blue3);
            String[][] dataArr;
            File temp = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC" + File.separator + "temp.csv");
            if (temp.exists()) {
                CSVReader teamReader = new CSVReader(new FileReader(temp));
                List<String[]> team_num = teamReader.readAll();
                dataArr = new String[team_num.size()][];
                dataArr = team_num.toArray(dataArr);
                String team = (dataArr[0][0]);

                if (team.contentEquals(redone.getText())) {
                    redone.setBackgroundTintList(getResources().getColorStateList(R.color.green));

                } else if (team.contentEquals(redtwo.getText())) {
                    redtwo.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                } else if (team.contentEquals(redthree.getText())) {
                    redthree.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                } else if (team.contentEquals(blueone.getText())) {
                    blueone.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                } else if (team.contentEquals(bluetwo.getText())) {
                    bluetwo.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                } else if (team.contentEquals(bluethree.getText())) {
                    bluethree.setBackgroundTintList(getResources().getColorStateList(R.color.green));
                }
                if (blueone.getBackgroundTintList() == getResources().getColorStateList(R.color.green)
                        & bluetwo.getBackgroundTintList() == getResources().getColorStateList(R.color.green)
                        & bluethree.getBackgroundTintList() == getResources().getColorStateList(R.color.green)
                        & redone.getBackgroundTintList() == getResources().getColorStateList(R.color.green)
                        & redtwo.getBackgroundTintList() == getResources().getColorStateList(R.color.green)
                        & redthree.getBackgroundTintList() == getResources().getColorStateList(R.color.green)) {
                    incrementmatch();
                    temp.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(getContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    private void chooseAccount() { {
        String accountName = getActivity().getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
            getResultsFromApi();
        } else {
            // Start a dialog from which the user can choose an account
            startActivityForResult(
                    mCredential.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER);
        }
    }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                getResultsFromApi();
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                               getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }


    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    @SuppressLint("StaticFieldLeak")
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return writeDatatoApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Write to the sheet
         */
        private List<String> writeDatatoApi() throws IOException {
            String spreadsheetId = "1prtvkrh64TG_9wgz51o2N6GwJhwGK3-03Jcuw0HvMJo";
            String range = "Sheet1!A1:T700";

            List<String> results = new ArrayList<>();
            ValueRange valueRange = new ValueRange();
            try {
                // Reading CSV into a list
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator +"FRC"+File.separator+"upload.csv");
                if (file.exists()) {
                    CSVReader csvReader = new CSVReader(new FileReader(file));
                    List<String[]> list = csvReader.readAll();

                    // Make sure the list has values
                    if (list.size() > 0) {
                        List upload = new ArrayList<String>();

                        // Reformatting from String Array to Array of Strings
                        for (String[] aDataArr : list) {
                            upload.add(Arrays.asList(aDataArr));
                        }

                        // Set the value range to our data
                        valueRange.setValues(upload);

                        // Command to upload the data to google sheets
                        this.mService.spreadsheets().values().append(spreadsheetId, range, valueRange)
                                .setValueInputOption("RAW")
                                .execute();
                        file.delete();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError instanceof UserRecoverableAuthIOException) {
                startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        REQUEST_AUTHORIZATION);
            }
        }
    }
}