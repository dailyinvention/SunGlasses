package com.dailyinvention.sunglasses;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by dailyinvention on 9/24/14.
 */
public class SunGlassesStart extends Activity {

    private String latitude;
    private String longitude;
    private String location;
    Context context = this;
    private List<CardBuilder> repCardsArray;
    private CardScrollView repScrollView;
    private repCardScrollAdapter repAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    public class getRepImage extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... params) {
            String biographID = params[0];
            BufferedInputStream bufferedStream;
            Bitmap imageStream = null;
            try {
                URL repImageURL = new URL("http://theunitedstates.io/images/congress/225x275/" + biographID + ".jpg");
                InputStream stream = null;
                try {
                    stream = repImageURL.openStream();
                    bufferedStream = new BufferedInputStream(stream);
                    Log.i("Bytes:", "Bytes: " + String.valueOf(bufferedStream.available()));

                    imageStream = BitmapFactory.decodeStream(bufferedStream);
                    stream.close();
                    bufferedStream.close();

                    Log.i("Image Loaded:", "Loaded image");


                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return imageStream;

        }
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
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
            JSONArray sunlightJSON = parseJSON(result);
            repCardsArray = new ArrayList<CardBuilder>();

            for (int i = 0; i<sunlightJSON.length();i++){

                String name = null;
                try {
                    String firstName = sunlightJSON.getJSONObject(i).optString("first_name");
                    String lastName = sunlightJSON.getJSONObject(i).optString("last_name");
                    String title = sunlightJSON.getJSONObject(i).optString("title");
                    String party = sunlightJSON.getJSONObject(i).optString("party");
                    String email = sunlightJSON.getJSONObject(i).optString("oc_email");
                    String phone = sunlightJSON.getJSONObject(i).optString("phone");
                    String bioguideID = sunlightJSON.getJSONObject(i).optString("bioguide_id");
                    Bitmap icon = new getRepImage()
                                    .execute(bioguideID)
                                    .get();


                    repCardsArray.add(new CardBuilder(context, CardBuilder.Layout.AUTHOR)
                        .setHeading(firstName + " " + lastName + " (" + party + ")")
                        .setText("e: " + email + "\np: " + phone)
                        .setSubheading(title)
                        .setIcon(icon));



                    Log.i("Name:",firstName + " " + lastName);
                    Log.i("Email:",email);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }


            repScrollView = new CardScrollView(context);
            repAdapter = new repCardScrollAdapter();
            repScrollView.setAdapter(repAdapter);
            repScrollView.activate();
            setContentView(repScrollView);

        }
    }

    private class repCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int getPosition(Object item) {
            return repCardsArray.indexOf(item);
        }

        @Override
        public int getCount() {
            return repCardsArray.size();
        }

        @Override
        public Object getItem(int position) {
            return repCardsArray.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return CardBuilder.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position){
            return repCardsArray.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return repCardsArray.get(position).getView(convertView, parent);
        }
    }


    public JSONArray parseJSON(String result) {
        JSONArray sunlightJSONArray = null;
        try {
            JSONObject sunlightJSON = new JSONObject(result);
            sunlightJSONArray = sunlightJSON.getJSONArray("results");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sunlightJSONArray;


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
