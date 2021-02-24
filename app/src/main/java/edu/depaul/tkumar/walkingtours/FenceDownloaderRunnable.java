package edu.depaul.tkumar.walkingtours;

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

    private final FenceManager fenceManager;
    private final MapsActivity mapsActivity;
    private static final String FENCE_URL = "http://www.christopherhield.com/data/WalkingTourContent.json";
    private static final String TAG = "FenceDataDownloader";

    FenceDownloaderRunnable(MapsActivity mapsActivity, FenceManager fenceManager) {
        this.mapsActivity = mapsActivity;
        this.fenceManager = fenceManager;
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
                //Log.d(TAG, "run: Response code: " + connection.getResponseCode());
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
        ArrayList<LatLng> route = new ArrayList<>();
        try {
            String lonLatPairs;
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
                //Log.d(TAG, "processData: " + latitude + "," + longitude);
                FenceData fd = new FenceData(id, latitude, longitude, address, rad, description, color, imageURL);
                fences.add(fd);
            }

            JSONArray jsonArray = jObj.getJSONArray("path");
            for(int j = 0; j<jsonArray.length(); j++){

                lonLatPairs = jsonArray.getString(j);
                String[] parts = lonLatPairs.split(", ");
                float latitude = (float) Double.parseDouble(parts[1]);
                float longitude = (float) Double.parseDouble(parts[0]);
                LatLng latLng = new LatLng(latitude, longitude);
                route.add(latLng);
                //Log.d(TAG, "processData: lon, lat " + parts[0] + "," + parts[1]);
            }
            mapsActivity.runOnUiThread(()->mapsActivity.drawRoute(route));
            fenceManager.addFences(fences);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
