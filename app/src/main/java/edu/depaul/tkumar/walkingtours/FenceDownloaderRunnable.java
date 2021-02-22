package edu.depaul.tkumar.walkingtours;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FenceDownloaderRunnable implements Runnable{

    private static final String TAG = "FenceDataDownloader";
    private final Geocoder geocoder;
    private final FenceManager fenceManager;
    private static final String FENCE_URL = "http://www.christopherhield.com/data/WalkingTourContent.json";

    FenceDownloaderRunnable(MapsActivity mapsActivity, FenceManager fenceManager) {
        this.fenceManager = fenceManager;
        geocoder = new Geocoder(mapsActivity);
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(FENCE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: Response code: " + connection.getResponseCode());
                return;
            }

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            processData(buffer.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processData(String result) {

        if (result == null)
            return;

        ArrayList<FenceData> fences = new ArrayList<>();
        try {
            String lonLatPairs = "";
            JSONObject jObj = new JSONObject(result);
            JSONArray jArr = jObj.getJSONArray("fences");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject fObj = jArr.getJSONObject(i);
                String id = fObj.getString("id");
                String address = fObj.getString("address");
                float latitude = (float) fObj.getDouble("latitude");
                float longitude = (float) fObj.getDouble("longitude");
                float rad = (float) fObj.getDouble("radius");
                String description = fObj.getString("description");
                String color = fObj.getString("fenceColor");
                String imageURL = fObj.getString("image");
                Log.d(TAG, "processData: " + id);
                FenceData fd = new FenceData(id, latitude, longitude, address, rad, description, color, imageURL);
                fences.add(fd);
            }

            JSONArray jsonArray = jObj.getJSONArray("path");
            for(int j = 0; j<jsonArray.length(); j++){

                lonLatPairs += jsonArray.getString(j);
            }
            Log.d(TAG, "processData: latlon" + lonLatPairs);
            fenceManager.addFences(fences);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
