package com.dailyinvention.sunglasses;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dailyinvention on 9/24/14.
 */
public class SunGlassesStart extends Activity {

    private String latitude;
    private String longitude;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this;
        latitude = String.valueOf(getLocation(context).getLatitude());
        longitude = String.valueOf(getLocation(context).getLongitude());
        location = "Latitude: " + latitude + "\r\n" + "Longitude: " + longitude;

        setContentView(R.layout.activity_sunglasses);
        TextView locationView = (TextView) findViewById(R.id.locationText);
        locationView.setText(location);
    }

    public Location getLocation(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        List<String> providers = manager.getProviders(criteria, true);
        List<Location> locations = new ArrayList<Location>();

        LocationListener locationListen = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        for (String provider : providers) {


            manager.requestLocationUpdates(
                    provider,
                    10,
                    0, locationListen );

            Location location = manager.getLastKnownLocation(provider);
            if (location != null && location.getAccuracy() != 0.0) {
                locations.add(location);
            }


        }

        Collections.sort(locations, new Comparator<Location>() {
            @Override
            public int compare(Location location, Location location2) {
                return (int) (location.getAccuracy() - location2.getAccuracy());
            }
        });
        if (locations.size() > 0) {
            return locations.get(0);
        }
        else {
            return null;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
