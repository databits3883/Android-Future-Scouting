package com.deadman.databitsfuture;

import static android.app.Activity.RESULT_OK;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.alexzaitsev.meternumberpicker.MeterView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.mohammedalaa.seekbar.RangeSeekBarView;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import vn.luongvo.widget.iosswitchview.SwitchView;


public class Pit extends Fragment {

  private static final int REQUEST_ACCOUNT_PICKER = 1000;
  private static final int REQUEST_AUTHORIZATION = 1001;
  private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
  private static final String PREF_ACCOUNT_NAME = "accountName";
  private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};
  private GoogleAccountCredential mCredential;
  private ProgressDialog mProgress;


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

    final Button uploadButton = view.findViewById(R.id.pit_upload_button);
    uploadButton.setOnClickListener(v -> getResultsFromApi());

    mProgress = new ProgressDialog(getContext());
    mProgress.setMessage("Calling Google Sheets API ...");

    // Initialize credentials and service object.
    mCredential = GoogleAccountCredential.usingOAuth2(
        getContext(), Arrays.asList(SCOPES))
        .setBackOff(new ExponentialBackOff());
  }

  // Export entered pit data to a CSV file and reset the fields
  private void savebutton() {
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
  private void write_data_pit() {
    String results = datastring();
    String header = getResources().getString(R.string.pit_header);
    File file = new File(
        Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC"
            + File.separator + "pit_data.csv");
    File upload = new File(
        Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC"
            + File.separator + "misc" + File.separator + "pit_upload.csv");
    try {
      FileWriter output = new FileWriter(file, true);
      FileWriter uploadfile = new FileWriter(upload, true);

      CSVWriter writer = new CSVWriter(output, ',',
          CSVWriter.NO_QUOTE_CHARACTER,
          CSVWriter.DEFAULT_ESCAPE_CHARACTER,
          "\r\n");
      CSVWriter uploader = new CSVWriter(uploadfile, ',',
          CSVWriter.NO_QUOTE_CHARACTER,
          CSVWriter.DEFAULT_ESCAPE_CHARACTER,
          "\r\n");
      List<String[]> data = new ArrayList<>();
      List<String[]> upload_data = new ArrayList<>();
      if (file.length() == 0) {
        data.add(new String[]{header});
      }
      data.add(new String[]{results});
      upload_data.add(new String[]{results});
      writer.writeAll(data);
      uploader.writeAll(upload_data);
      writer.flush();
      uploader.flush();
      writer.close();
      uploader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    rescan(file.getAbsolutePath());
    rescan(upload.getAbsolutePath());
  }

  // Function to scan the edited file so it shows up right away in MTP/OTG
  private void rescan(String file) {
    MediaScannerConnection.scanFile(getContext(),
        new String[]{file}, null,
        (path, uri) -> {
          Log.i("ExternalStorage", "Scanned " + path + ":");
          Log.i("ExternalStorage", "-> uri=" + uri);
        });
  }

  private void spinnerinit() {
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

    spinner1.setSelection(0);
  }

  private String spinnerget() {
    Spinner spinner1 = Objects.requireNonNull(getView()).findViewById(R.id.spinner1);
    return spinner1.getSelectedItem().toString() + ",";
  }

  private String datastring() {
    return team()
        + spinnerget()
        + getselectors()
        + getswitches()
        + seekbars()
        + name()
        + comments();
  }

  private String team() {
    MeterView team = Objects.requireNonNull(getView()).findViewById(R.id.meterView);
    return Integer.toString(team.getValue()) + ",";
  }

  public String name() {
    TextView name = Objects.requireNonNull(getView()).findViewById(R.id.extended_edit_text);
    return name.getText().toString() + ",";
  }

  private String comments() {
    TextView name = Objects.requireNonNull(getView()).findViewById(R.id.comment_extended_edit_text);
    return name.getText().toString();
  }

  // Selectors start at 0 we want it to start at 1 so the data is easier to read
  private String selector(int id) {
    SegmentedButtonGroup button = Objects.requireNonNull(getView()).findViewById(id);
    return String.valueOf(button.getPosition() + 1);
  }

  private String getselectors() {
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
  private String switchy(int id) {
    SwitchView switches = Objects.requireNonNull(getView()).findViewById(id);
    if (switches.isChecked()) {
      return "1";
    } else {
      return "0";
    }
  }

  private String getswitches() {
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

  private String seekbars() {
    RangeSeekBarView hatchbar = Objects.requireNonNull(getView()).findViewById(R.id.hatch_seekbar);
    RangeSeekBarView cargobar = getView().findViewById(R.id.cargo_seekbar);
    return hatchbar.getValue() + "," + cargobar.getValue() + ",";
  }

  // Resets all the fields on the page
  private void reset_info_pit() {
    TextView comment_field = Objects.requireNonNull(getView())
        .findViewById(R.id.comment_extended_edit_text);
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
    launch.setPosition(0, true);
    vision.setPosition(0, true);
    sandstorm.setPosition(0, true);
    rocket.setPosition(0, true);
    climb.setPosition(0, true);
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
      new Pit.MakeRequestTask(mCredential).execute();
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

        String range = "PitData!A1:T700";
        ValueRange valueRange = new ValueRange();
        try {
          // Reading CSV into a list
          File file = new File(
              Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC"
                  + File.separator + "misc" + File.separator + "pit_upload.csv");
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