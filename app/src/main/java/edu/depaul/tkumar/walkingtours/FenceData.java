package edu.depaul.tkumar.walkingtours;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;

import java.io.Serializable;

public class FenceData implements Serializable {

    private final String id;
    private final double lat;
    private final double lon;
    private final String address;
    private final float radius;
    private final String description;
    private final String fenceColor;
    private final String imageURL;

    FenceData(String id, double lat, double lon, String address, float radius, String description, String fenceColor, String imageURL) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.radius = radius;
        this.description = description;
        this.fenceColor = fenceColor;
        this.imageURL = imageURL;
    }

    String getId() {
        return id;
    }

    String getAddress() {
        return address;
    }

    float getRadius() {
        return radius;
    }

    String getDescription() {
        return description;
    }

    String getFenceColor() {
        return fenceColor;
    }

    double getLat() {
        return lat;
    }

    double getLon() {
        return lon;
    }

    String getImageURL(){return imageURL;}

    int getType() {
        int type = Geofence.GEOFENCE_TRANSITION_ENTER;
        return type;
    }


    @NonNull
    @Override
    public String toString() {
        return "FenceData{" +
                "id='" + id + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", address='" + address + '\'' +
                ", radius=" + radius +
                ", description=" + description +
                ", fenceColor='" + fenceColor + '\'' +
                '}';
    }
}
