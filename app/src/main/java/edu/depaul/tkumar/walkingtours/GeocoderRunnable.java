package edu.depaul.tkumar.walkingtours;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;

public class GeocoderRunnable implements Runnable{

    private final MapsActivity mapsActivity;
    private final double longitude;
    private final double latitude;

    public GeocoderRunnable(MapsActivity mapsActivity, double latitude, double longitude) {
        this.mapsActivity = mapsActivity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void run() {
        try {
            Geocoder geocoder = new Geocoder(mapsActivity);
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            mapsActivity.runOnUiThread(()->mapsActivity.setCurrentAddressTextView(address.getAddressLine(0)));
        } catch (IOException e) {
            e.printStackTrace();
            mapsActivity.runOnUiThread(()->mapsActivity.setCurrentAddressTextView(""));
        }
    }
}
