package edu.depaul.tkumar.walkingtours;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;

import edu.depaul.tkumar.walkingtours.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private final ArrayList<LatLng> latLonRoute = new ArrayList<>();
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Polyline llHistoryPolyline;
    private Polyline llRoutePolyline;
    private ActivityMapsBinding binding;
    private FenceManager fenceManager;
    private Marker personMarker;
    private GoogleMap mMap;
    private TextView currentAddressTextView;
    private CheckBox travelPathCheckbox;
    private CheckBox geofenceCheckbox;
    private CheckBox tourPathCheckbox;
    private CheckBox addressCheckbox;
    private ProgressBar progressBar;
    public static int screenWidth;
    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //binding = ActivityMapsBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());
        setContentView(R.layout.activity_maps);
        initMap();
        initializeFields();
        getScreenDimensions();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void initMap() {
        fenceManager = new FenceManager(this);

        //geocoder = new Geocoder(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    private void initializeFields(){
        currentAddressTextView = findViewById(R.id.mapsCurrentAddressTextView);
        addressCheckbox = findViewById(R.id.mapsAddressCheckbox);
        geofenceCheckbox = findViewById(R.id.mapsGeofenceCheckbox);
        travelPathCheckbox = findViewById(R.id.mapsTravelPathCheckbox);
        tourPathCheckbox = findViewById(R.id.mapsTourPathCheckbox);
        progressBar = findViewById(R.id.mapsProgressBar);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        currentAddressTextView.setTypeface(font);
        addressCheckbox.setTypeface(font);
        geofenceCheckbox.setTypeface(font);
        travelPathCheckbox.setTypeface(font);
        tourPathCheckbox.setTypeface(font);
    }

    private void getScreenDimensions() {
        Display display = this.getDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDisplay().getMetrics(displayMetrics);
        display.getRealMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        progressBar.setVisibility(View.GONE);

        mMap = googleMap;

        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        setupLocationListener();
    }

    private void setupLocationListener() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocationListener(this);

        //minTime	    long: minimum time interval between location updates, in milliseconds
        //minDistance	float: minimum distance between location updates, in meters
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }
    }

    public void geofenceVisibilityOption(View v) {
        //CheckBox cb = (CheckBox) v;
        if (geofenceCheckbox.isChecked()) {
            fenceManager.drawFences();
        } else {
            fenceManager.eraseFences();
        }
    }

    public void addressVisibilityOption(View v){
        if(addressCheckbox.isChecked()){
            currentAddressTextView.setVisibility(View.VISIBLE);
        } else {
            currentAddressTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void travelPathVisibility(View view){
        llHistoryPolyline.setVisible(travelPathCheckbox.isChecked());
    }

    public void tourPathVisibility(View view){
        llRoutePolyline.setVisible(tourPathCheckbox.isChecked());
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng); // Add the LL to our location history

//        try {
//            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//            Address address = addresses.get(0);
//            mapsAddress.setText(address.getAddressLine(0));
//        } catch (IOException e) {
//            e.printStackTrace();
//            mapsAddress.setText("");
//        }
        GeocoderRunnable geocoderRunnable = new GeocoderRunnable(this, latLng.latitude, latLng.longitude);
        new Thread(geocoderRunnable).start();

        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }
            llHistoryPolyline = mMap.addPolyline(polylineOptions);
//            if(travelPathCheckbox.isChecked()){
//                llHistoryPolyline.setVisible(true);
//            }else{
//                llHistoryPolyline.setVisible(false);
//            }
            View view = getCurrentFocus();
            travelPathVisibility(view);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(12);
            llHistoryPolyline.setColor(Color.parseColor("#00723d"));


            float r = getRadius();
            if (r > 0) {
                Bitmap icon;
                if(location.getBearing()<=180)
                {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);
                }else{
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
                }
                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);

                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);
                Log.d(TAG, "updateLocation: " + location.getBearing());
                //options.rotation(location.getBearing());

                if (personMarker != null) {
                    personMarker.remove();
                }

                personMarker = mMap.addMarker(options);
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
    }

    public void setCurrentAddressTextView(String s){
        currentAddressTextView.setText(s);
    }

    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
        float multiplier = ((7.0f / 7200.0f) * screenWidth) - (1.0f / 20.0f);
        return factor * multiplier;
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (locationManager != null && locationListener != null)
//            locationManager.removeUpdates(locationListener);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager != null && locationListener != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }
    }

    public void drawRoute(ArrayList<LatLng> latLngs){
        latLonRoute.clear();
        latLonRoute.addAll(latLngs);
        PolylineOptions polylineOptions = new PolylineOptions();

        for (LatLng ll : latLonRoute) {
            polylineOptions.add(ll);
        }
        llRoutePolyline = mMap.addPolyline(polylineOptions);
        llRoutePolyline.setEndCap(new RoundCap());
        llRoutePolyline.setWidth(8);
        llRoutePolyline.setColor(Color.YELLOW);
    }
}