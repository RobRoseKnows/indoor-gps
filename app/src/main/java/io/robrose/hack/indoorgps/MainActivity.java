package io.robrose.hack.indoorgps;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.robrose.hack.indoorgps.data.TrainingContract;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getName();

    private WifiManager wifiManager;
    private ScanReceiver scanReceiver;
    private ContentValues[] results;
    private String[] locationsArray = {
            "231", "233", "237", "239", "Base of Stairs",
            "Basecamp", "Book Holders", "Hallway 229-231", "Hallway 231-233", "Hallway 233",
            "Hallway 235", "Hardware Lab", "Overlook", "PayPal", "Sign-In",
            "Top of Stairs", "Venture Storm"
    };

    public static final int COL_LOCATION = 0;
    public static final int COL_MAC = 1;
    public static final int COL_AVG_STRENGTH = 2;
    public static final int COL_STD_DEV = 3;
    public static final int COL_SAMPLE = 4;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        populateDatabase();
    }

    @Bind(R.id.locationTextView) TextView locationTextView;

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
                if (stdev == 0) {
                    stdev = 2.001408502;
                }

                int sample = Integer.parseInt(rowData[4]);
                if(sample > 3) {
                    ContentValues values = new ContentValues();
                    values.put(TrainingContract.TrainingEntry.COLUMN_LOCATION, location);
                    values.put(TrainingContract.TrainingEntry.COLUMN_MAC, mac);
                    values.put(TrainingContract.TrainingEntry.COLUMN_AVG_STRENGTH, average);
                    values.put(TrainingContract.TrainingEntry.COLUMN_STD_DEV, stdev);
                    values.put(TrainingContract.TrainingEntry.COLUMN_SAMPLE, sample);
                    contentsList.add(values);
                }
            }

            ContentValues[] valueArray = (ContentValues[]) contentsList.toArray();
            getContentResolver().bulkInsert(TrainingContract.BASE_CONTENT_URI, valueArray);
            in.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "CSV didn't load correctly");
            e.printStackTrace();
        }
    }

    public String findLocation(ContentValues[] res) {
        String locationString = "";
        HashMap<String, ArrayList<Double>> map = new HashMap<>();

        for(ContentValues cv : res) {
            String address = (String) cv.get(TrainingContract.SignalEntry.COLUMN_MAC);
            int strength = (int) cv.get(TrainingContract.SignalEntry.COLUMN_STRENGTH);
            Uri macSeartchUri = TrainingContract.TrainingEntry.buildTrainingMac(address);

            Cursor cursor = getContentResolver().query(
                    macSeartchUri,
                    null,
                    null,
                    null,
                    null);

            while(cursor.moveToNext()){
                double variance = calculateVariance(
                        cursor.getDouble(COL_AVG_STRENGTH),
                        cursor.getDouble(COL_STD_DEV),
                        strength);
                String locKey = cursor.getString(COL_LOCATION);
                if(map.get(locKey) == null) {
                    map.put(locKey, new ArrayList<Double>());
                }

                ArrayList<Double> tempList = map.get(locKey);
                tempList.add(variance);
                map.put(locKey, tempList);
            }
        }

        double[] totalVariances = new double[17];
        for(int i = 0; i < totalVariances.length; i++) {
            ArrayList<Double> values = map.get(locationsArray[i]);
            if(values != null){
                double sum = 0;
                for(double n : values) {
                    sum += n;
                }
                totalVariances[i] = (sum / values.size()) * (sum / values.size());
            }
        }

        int mindex = 0;
        for(int i = 0; i < totalVariances.length; i++){
            if(totalVariances[i] < totalVariances[mindex]) {
                mindex = i;
            }
        }

        return locationsArray[mindex];
    }

    public double calculateVariance(double avg, double stddev, int val) {
        return ((val - avg) / stddev) * ((val - avg) / stddev);
    }

    public void updateLocation(ContentValues[] res) {
        locationTextView.setText(findLocation(res));
    }

    private class ScanReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> wifiList = wifiManager.getScanResults();
            results = new ContentValues[wifiList.size()];

            for(int i = 0; i < wifiList.size(); i++) {
                ScanResult resultOn = wifiList.get(i);
                String macAddress = resultOn.BSSID;
                int signalStrength = resultOn.level;

                ContentValues cvs = new ContentValues();
                cvs.put(TrainingContract.SignalEntry.COLUMN_MAC, macAddress);
                cvs.put(TrainingContract.SignalEntry.COLUMN_STRENGTH, signalStrength);

                results[i] = cvs;
            }

            updateLocation(results);
        }
    }
}
