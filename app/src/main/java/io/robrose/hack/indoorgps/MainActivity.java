package io.robrose.hack.indoorgps;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.robrose.hack.indoorgps.data.TrainingContract;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getName();

    private WifiManager wifiManager;
    private ScanReceiver scanReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        populateDatabase();
    }

    private void populateDatabase() throws SQLiteConstraintException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getAssets().open("averages.csv")));

            String line = "";
            ArrayList<ContentValues> contentsList = new ArrayList<>();
            while ((line = in.readLine()) != null){
                String[] rowData = line.split(",");
                String location = rowData[0];
                String mac = rowData[1];
                double average = Double.parseDouble(rowData[2]);
                double stdev = Double.parseDouble(rowData[3]);
                int sample = Integer.parseInt(rowData[4]);

                ContentValues values = new ContentValues();
                values.put(TrainingContract.TrainingEntry.COLUMN_LOCATION, location);
                values.put(TrainingContract.TrainingEntry.COLUMN_MAC, mac);
                values.put(TrainingContract.TrainingEntry.COLUMN_AVG_STRENGTH, average);
                values.put(TrainingContract.TrainingEntry.COLUMN_STD_DEV, stdev);
                values.put(TrainingContract.TrainingEntry.COLUMN_SAMPLE, sample);
                contentsList.add(values);
            }

            ContentValues[] valueArray = (ContentValues[]) contentsList.toArray();
            getContentResolver().bulkInsert(TrainingContract.BASE_CONTENT_URI, valueArray);
            in.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "CSV didn't load correctly");
            e.printStackTrace();
        }
    }

    private class ScanReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> wifiList = wifiManager.getScanResults();
            String[] wifiText = new String[wifiList.size()];
            ContentValues[] results = new ContentValues[wifiList.size()];

            // This is not my favorite way to do this because the location field could change
            // during the time it takes to scan. The solution is not to do that.
            String location = String.valueOf(locationField.getText());
            resultsTextView.setText("Found " + wifiList.size() + " APs at " + location);

            for(int i = 0; i < wifiList.size(); i++) {
                ScanResult resultOn = wifiList.get(i);
                String macAddress = resultOn.BSSID;
                String ssid = resultOn.SSID;
                int signalStrength = resultOn.level;

                String toStringValue = "SSID: " + ssid + " MAC: " + macAddress + " Signal: " +
                        signalStrength + "dBm";
                wifiText[i] = toStringValue;

                ContentValues cvs = new ContentValues();
                cvs.put(SignalContract.SignalEntry.COLUMN_LOCATION, location);
                cvs.put(SignalContract.SignalEntry.COLUMN_SSID, ssid);
                cvs.put(SignalContract.SignalEntry.COLUMN_MAC, macAddress);
                cvs.put(SignalContract.SignalEntry.COLUMN_STRENGTH, signalStrength);

                results[i] = cvs;
            }

            resultsListView.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, wifiText));
            getContentResolver().bulkInsert(SignalContract.SignalEntry.CONTENT_URI, results);
            currentlyChecking = false;
        }
    }
}
