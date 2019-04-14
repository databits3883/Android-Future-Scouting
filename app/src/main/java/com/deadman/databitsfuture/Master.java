package com.deadman.databitsfuture;

import static android.app.Activity.RESULT_OK;

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
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.opencsv.CSVReader;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Listener.DefaultValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class Master extends Fragment {

  private static final int REQUEST_ACCOUNT_PICKER = 1000;
  private static final int REQUEST_AUTHORIZATION = 1001;
  private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
  private static final String PREF_ACCOUNT_NAME = "accountName";
  private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};
  private GoogleAccountCredential mCredential;
  private ProgressDialog mProgress;

  public Master() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.master, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    // Go Full screen
    View decorView = Objects.requireNonNull(getActivity()).getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    decorView.setSystemUiVisibility(uiOptions);

    NumberPicker match_num = Objects.requireNonNull(getView()).findViewById(R.id.match_counter);
    match_num.setValueChangedListener(new DefaultValueChangedListener() {
      public void valueChanged(int value, ActionEnum action) {
        read_teams();
      }
    });

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

  private void scanner() {
    Intent intent = new Intent(getActivity(), Scanner.class);
    startActivity(intent);
  }

  private void incrementmatch() {
    NumberPicker match = Objects.requireNonNull(getView()).findViewById(R.id.match_counter);
    match.setValue(match.getValue() + 1);
    read_teams();
  }

  @Override
  public void onResume() {
    color_teams();
    super.onResume();
  }

  // Read the team data from teams.csv
  private void read_teams() {
    NumberPicker match_num = Objects.requireNonNull(getView()).findViewById(R.id.match_counter);
    TextView redone = getView().findViewById(R.id.red1);
    TextView redtwo = getView().findViewById(R.id.red2);
    TextView redthree = getView().findViewById(R.id.red3);
    TextView blueone = getView().findViewById(R.id.blue1);
    TextView bluetwo = getView().findViewById(R.id.blue2);
    TextView bluethree = getView().findViewById(R.id.blue3);
    int match = match_num.getValue();
    String[][] dataArr;
    try {
      File teams = new File(
          Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC"
              + File.separator + "teams.csv");
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
      redone.setBackgroundTintList(getResources().getColorStateList(R.color.red, null));
      redtwo.setBackgroundTintList(getResources().getColorStateList(R.color.red, null));
      redthree.setBackgroundTintList(getResources().getColorStateList(R.color.red, null));
      blueone.setBackgroundTintList(getResources().getColorStateList(R.color.blue, null));
      bluetwo.setBackgroundTintList(getResources().getColorStateList(R.color.blue, null));
      bluethree.setBackgroundTintList(getResources().getColorStateList(R.color.blue, null));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void color_teams() {
    TextView redone = Objects.requireNonNull(getView()).findViewById(R.id.red1);
    TextView redtwo = getView().findViewById(R.id.red2);
    TextView redthree = getView().findViewById(R.id.red3);
    TextView blueone = getView().findViewById(R.id.blue1);
    TextView bluetwo = getView().findViewById(R.id.blue2);
    TextView bluethree = getView().findViewById(R.id.blue3);

    File master_team = new File(
        Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "misc"
            + File.separator + "master_team.txt");

    if (master_team.exists()) {
      int length = (int) master_team.length();

      byte[] bytes = new byte[length];

      try {
        FileInputStream in = new FileInputStream(master_team);
        in.read(bytes);
        in.close();
      } catch (
          FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      String team = new String(bytes);

      if (team.contentEquals(redone.getText())) {
        redone.setBackgroundTintList(getResources().getColorStateList(R.color.green, null));

      } else if (team.contentEquals(redtwo.getText())) {
        redtwo.setBackgroundTintList(getResources().getColorStateList(R.color.green, null));
      } else if (team.contentEquals(redthree.getText())) {
        redthree.setBackgroundTintList(getResources().getColorStateList(R.color.green, null));
      } else if (team.contentEquals(blueone.getText())) {
        blueone.setBackgroundTintList(getResources().getColorStateList(R.color.green, null));
      } else if (team.contentEquals(bluetwo.getText())) {
        bluetwo.setBackgroundTintList(getResources().getColorStateList(R.color.green, null));
      } else if (team.contentEquals(bluethree.getText())) {
        bluethree.setBackgroundTintList(getResources().getColorStateList(R.color.green, null));
      }
      if (blueone.getBackgroundTintList() == getResources().getColorStateList(R.color.green, null)
          & bluetwo.getBackgroundTintList() == getResources().getColorStateList(R.color.green, null)
          & bluethree.getBackgroundTintList() == getResources()
          .getColorStateList(R.color.green, null)
          & redone.getBackgroundTintList() == getResources().getColorStateList(R.color.green, null)
          & redtwo.getBackgroundTintList() == getResources().getColorStateList(R.color.green, null)
          & redthree.getBackgroundTintList() == getResources()
          .getColorStateList(R.color.green, null)) {
        incrementmatch();
      }
      master_team.delete();
    }
  }


  /**
   * Attempt to call the API, after verifying that all the preconditions are satisfied. The
   * preconditions are: Google Play Services installed, an account was selected and the device
   * currently has online access. If any of the preconditions are not satisfied, the app will prompt
   * the user as appropriate.
   */
  private void getResultsFromApi() {
    if (mCredential.getSelectedAccountName() == null) {
      chooseAccount();
    } else if (!isDeviceOnline()) {
      Toast.makeText(getContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
    } else {
      new MakeRequestTask(mCredential).execute();
    }
  }

  /**
   * Attempts to set the account used with the API credentials. If an account name was previously
   * saved it will use that one; otherwise an account picker dialog will be shown to the user. Note
   * that the setting the account to use with the credentials object requires the app to have the
   * GET_ACCOUNTS permission, which is requested here if it is not already present. The
   * AfterPermissionGranted annotation indicates that this function will be rerun automatically
   * whenever the GET_ACCOUNTS permission is granted.
   */
  private void chooseAccount() {
    {
      String accountName = Objects.requireNonNull(getActivity())
          .getPreferences(Context.MODE_PRIVATE)
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
   * Called when an activity launched here (specifically, AccountPicker and authorization) exits,
   * giving you the requestCode you started it with, the resultCode it returned, and any additional
   * data from it.
   *
   * @param requestCode code indicating which activity result is incoming.
   * @param resultCode code indicating the result of the incoming activity result.
   * @param data Intent (containing result data) returned by incoming activity result.
   */
  @Override
  public void onActivityResult(
      int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
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
                Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
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
   *
   * @return true if the device has a network connection, false otherwise.
   */
  private boolean isDeviceOnline() {
    ConnectivityManager connMgr =
        (ConnectivityManager) Objects.requireNonNull(getActivity())
            .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
  }


  /**
   * An asynchronous task that handles the Google Sheets API call. Placing the API calls in their
   * own task ensures the UI stays responsive.
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
     *
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
    private List<String> writeDatatoApi() {

      File upload_id = new File(
          Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator
              + "misc" + File.separator + "upload_id.txt");
      List<String> results = new ArrayList<>();
      if (upload_id.exists()) {
        int length = (int) upload_id.length();

        byte[] bytes = new byte[length];

        try {
          FileInputStream in = new FileInputStream(upload_id);
          in.read(bytes);
          in.close();
        } catch (
            FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        String spreadsheetId = new String(bytes);

        String range = "StatsRaw!A1:T700";
        ValueRange valueRange = new ValueRange();
        try {
          // Reading CSV into a list
          File file = new File(
              Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC"
                  + File.separator + "misc" + File.separator + "upload.csv");
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
