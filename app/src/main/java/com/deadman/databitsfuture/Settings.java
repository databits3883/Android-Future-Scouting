package com.deadman.databitsfuture;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Settings extends Fragment {

  private static final int REQUEST_ACCOUNT_PICKER = 1000;
  private static final int REQUEST_AUTHORIZATION = 1001;
  private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
  private static final String PREF_ACCOUNT_NAME = "accountName";
  private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};
  private GoogleAccountCredential mCredential;
  private ProgressDialog mProgress;

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

    Button downloadButton = view.findViewById(R.id.download_team);
    downloadButton.setOnClickListener(v -> download_teams());

    Button uploadButton = Objects.requireNonNull(getView()).findViewById(R.id.get_config);
    uploadButton.setOnClickListener(v -> getResultsFromApi());

    mProgress = new ProgressDialog(getContext());
    mProgress.setMessage("Calling Google Sheets API ...");

    // Initialize credentials and service object.
    mCredential = GoogleAccountCredential.usingOAuth2(
        getContext(), Arrays.asList(SCOPES))
        .setBackOff(new ExponentialBackOff());
  }

  @Override
  public void onPause() {
    super.onPause();
    EditText id = Objects.requireNonNull(getView()).findViewById(R.id.spreadsheet_id);
    SharedPreferences.Editor editor = Objects.requireNonNull(getActivity())
        .getSharedPreferences("CurrentUser", MODE_PRIVATE).edit();
    editor.putString("id", id.getText().toString());
    editor.apply();
  }

  public void onResume() {
    super.onResume();
    EditText id = Objects.requireNonNull(getView()).findViewById(R.id.spreadsheet_id);
    SharedPreferences prefs = Objects.requireNonNull(getActivity())
        .getSharedPreferences("CurrentUser", MODE_PRIVATE);
    String new_id = prefs.getString("id", "");
    id.setText(new_id);
  }


  // Remove the FRC folder
  private void delete() {
    new AlertDialog.Builder(getContext())
        .setMessage(R.string.confirm_dialog_message)
        .setTitle(R.string.confirm_dialog_title)
        .setPositiveButton(R.string.confirm, (dialog, id) -> {
          File file = new File(
              Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FRC");
          // Delete the files
          deleteRecursive(file);
          // Relaunch the app so the basic structure is made
          Intent intent = getActivity().getBaseContext().getPackageManager()
              .getLaunchIntentForPackage(
                  getActivity().getBaseContext().getPackageName());
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(intent);
        })
        .setNegativeButton(R.string.cancel, (dialog, id) -> {
          // CANCEL
        })
        .show();
  }

  private void deleteRecursive(File fileOrDirectory) {
    if (fileOrDirectory.isDirectory()) {
      for (File child : fileOrDirectory.listFiles()) {
        deleteRecursive(child);
      }
    }

    fileOrDirectory.delete();
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

  private void download_teams() {
    File team_sheet = new File(
        Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator + "misc"
            + File.separator + "teams_url.txt");
    if (team_sheet.exists()) {
      int length = (int) team_sheet.length();

      byte[] bytes = new byte[length];

      try {
        FileInputStream in = new FileInputStream(team_sheet);
        in.read(bytes);
        in.close();
      } catch (
          FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      String DownloadUrl = new String(bytes);
      File teams = new File(
          Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator
              + "teams.csv");
      DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(DownloadUrl));
      request1.setDescription("Team match info");
      request1.setTitle("Teams.csv");
      request1.setVisibleInDownloadsUi(false);
      request1.allowScanningByMediaScanner();
      request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
      request1.setDestinationInExternalPublicDir("/FRC", "teams.csv");

      DownloadManager manager1 = (DownloadManager) Objects.requireNonNull(getContext())
          .getSystemService(Context.DOWNLOAD_SERVICE);
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
      new Settings.MakeRequestTask(mCredential).execute();
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
        return getDataFromApi();
      } catch (Exception e) {
        mLastError = e;
        cancel(true);
        return null;
      }
    }

    /**
     * Reads a google sheet to get dynamic variables
     */
    private List<String> getDataFromApi() throws IOException {

      EditText id = Objects.requireNonNull(getView()).findViewById(R.id.spreadsheet_id);
      String spreadsheetId = id.getText().toString();
      String range = "Config!A1:A2";
      List<String> results = new ArrayList<>();
      ValueRange response = this.mService.spreadsheets().values()
          .get(spreadsheetId, range)
          .setMajorDimension("COLUMNS")
          .execute();
      List<List<Object>> values = response.getValues();
      if (values != null) {
        for (List row : values) {
          results.add(row.get(0) + "," + row.get(1));

          String teams_sheet = row.get(0).toString();
          File teams_url = new File(
              Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator
                  + "misc" + File.separator + "teams_url.txt");
          try {
            FileOutputStream stream = new FileOutputStream(teams_url);
            stream.write(teams_sheet.getBytes());
            stream.close();
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }

          String upload_sheet = row.get(1).toString();
          File upload_id = new File(
              Environment.getExternalStorageDirectory() + File.separator + "FRC" + File.separator
                  + "misc" + File.separator + "upload_id.txt");
          try {
            FileOutputStream stream = new FileOutputStream(upload_id);
            stream.write(upload_sheet.getBytes());
            stream.close();
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }

          rescan(teams_url.getAbsolutePath());
          rescan(upload_id.getAbsolutePath());
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
      if (output == null || output.size() == 0) {
        Toast.makeText(getContext(), "No results returned.", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getContext(), TextUtils.join("\n", output), Toast.LENGTH_SHORT).show();
      }

    }

    @Override
    protected void onCancelled() {
      mProgress.hide();
      if (mLastError instanceof UserRecoverableAuthIOException) {
        startActivityForResult(
            ((UserRecoverableAuthIOException) mLastError).getIntent(),
            Settings.REQUEST_AUTHORIZATION);
      } else {
        Toast.makeText(getContext(), "The following error occurred:\n"
            + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
      }
    }
  }
}