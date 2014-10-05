package com.dailyinvention.sunglasses;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
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

        new callSunlightAPI().execute(latitude, longitude);

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

    public class callSunlightAPI extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlString="https://congress.api.sunlightfoundation.com/legislators/locate?apikey=" + Globals.sunlightAPICode + "&latitude=" + params[0] + "&longitude=" + params[1]; // URL to call
            String result;
            String resultToDisplay = "";

            InputStream in = null;

            try {

                //URL url = new URL(urlString);

                HttpGet getResponse = new HttpGet(urlString);

                ResponseHandler<String> responseHandler = new BasicResponseHandler();

                HttpClient httpClient = new DefaultHttpClient();

                result = httpClient.execute(getResponse,responseHandler);

            } catch (Exception e ) {

                System.out.println(e.getMessage());

                result = e.getMessage();

            }
            return result;

        }

        protected void onPostExecute(String result) {
            setContentView(R.layout.activity_sunglasses);
            TextView locationView = (TextView) findViewById(R.id.locationText);
            locationView.setText(location);
            Log.i("JSON Result:",result);

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
