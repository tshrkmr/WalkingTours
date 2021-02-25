package edu.depaul.tkumar.walkingtours;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    private static final int LOC_COMBO_REQUEST = 111;
    private static final int LOC_ONLY_PERM_REQUEST = 222;
    private static final int BGLOC_ONLY_PERM_REQUEST = 333;
    private static final int ACCURACY_REQUEST = 444;
    private static final int SPLASH_TIME_OUT = 1000;
    private Activity activity;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_splash);
        determineLocation();
    }

    private void determineLocation() {
        if (checkPermission()) {
            checkLocationAccuracy();
        }
    }

    private void checkLocationAccuracy(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.d(TAG, "onSuccess: High Accuracy Already Present");
            openMapsActivity();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(SplashActivity.this, ACCURACY_REQUEST);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCURACY_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: ");
            openMapsActivity();
        } else {
            noLocationPermission("High Accuracy Location Service Required");
        }
    }

    private boolean checkPermission() {
//        ArrayList<String> perms = new ArrayList<>();
//
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) !=
//                PackageManager.PERMISSION_GRANTED) {
//            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            if (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
//                    PackageManager.PERMISSION_GRANTED) {
//                perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
//            }
//        }
//
//        if (!perms.isEmpty()) {
//            String[] array = perms.toArray(new String[0]);
//            ActivityCompat.requestPermissions(this,
//                    array, LOCATION_REQUEST);
//            return false;
//        }
//        return true;

        // If R or greater, need to ask for these separately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_ONLY_PERM_REQUEST);
                return false;
            }
            return true;
        } else {

            ArrayList<String> perms = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }
            }

            if (!perms.isEmpty()) {
                String[] array = perms.toArray(new String[0]);
                ActivityCompat.requestPermissions(this,
                        array, LOC_COMBO_REQUEST);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        if (requestCode == LOCATION_REQUEST) {
//            for (int i = 0; i < permissions.length; i++) {
//                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                        determineLocation();
//                    } else {
//                        noLocationPermission("Background Location Permission Required");
//                        //Toast.makeText(this, "Location Permission not Granted", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        }

        if (requestCode == LOC_ONLY_PERM_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestBgPermission();
            }else{
                noLocationPermission("Background Location Permission Required");
            }
        } else if (requestCode == LOC_COMBO_REQUEST) {
            int permCount = permissions.length;
            int permSum = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permSum++;
                } else {
                    sb.append(permissions[i]).append(", ");
                }
            }
            if (permSum == permCount) {
                determineLocation();
            } else {
//                Toast.makeText(this,
//                        "Required permissions not granted: " + sb.toString(),
//                        Toast.LENGTH_LONG).show();
                noLocationPermission("Background Location Permission Required");
            }
        } else if (requestCode == BGLOC_ONLY_PERM_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                determineLocation();
            } else{
               noLocationPermission("Background Location Permission Required");
            }
        }
    }

    public void requestBgPermission() {
        //Please select Allow all the time and come back
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important Detour");
        builder.setMessage("Select Allow all the time");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BGLOC_ONLY_PERM_REQUEST);
                    }
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void openMapsActivity(){
        Handler handler = new Handler(Looper.myLooper());
        handler.postDelayed(()->{
            Intent i = new Intent(SplashActivity.this, MapsActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // new act, old act
            // close this activity
            finish();
        }, SPLASH_TIME_OUT);
    }

    private void noLocationPermission(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("App ShutDown Initiated");
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, id) -> finish());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}