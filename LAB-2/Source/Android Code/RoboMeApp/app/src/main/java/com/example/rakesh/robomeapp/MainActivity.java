package com.example.rakesh.robomeapp;

import android.app.ProgressDialog;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    private ProgressDialog pDialog;
    EditText temper,pressure,humidity,mintemp,maxtemp,description,speed;

    String t,h,p,mm,ma,dc="h",sp;
    // URL to get contacts JSON
    private static String url = "http://api.openweathermap.org/data/2.5/weather?lat=39&lon=94&appid=44db6a862fba0b067b1930da0d769e98";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       //EditText temp= (EditText) findViewById(R.id.temp);
       /* EditText pressure= (EditText) findViewById(R.id.pressureedit);
        EditText humidity= (EditText) findViewById(R.id.humedit);
        EditText maxtemp= (EditText) findViewById(R.id.maxtemedit);
        EditText mintemp= (EditText) findViewById(R.id.mtemedt);
        EditText description= (EditText) findViewById(R.id.descedit);
        EditText speed= (EditText) findViewById(R.id.wspeededit);*/

        temper= (EditText) findViewById(R.id.tempedit);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                        //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    /**
     * If connected get lat and long
     *
     */
    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
            new LocationWeather().execute();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
            /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /**
     * If locationChanges change lat and long
     *
     *
     * @param location
     */


    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();

    }

    private class LocationWeather extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    //Toast.makeText(MainActivity.this, jsonObj.toString(), Toast.LENGTH_LONG).show();
                     Log.e("JSON",""+jsonObj.toString());
                    JSONArray data1 = jsonObj.getJSONArray("weather");

                    JSONObject des=data1.getJSONObject(0);
                    JSONObject details=jsonObj.getJSONObject("main");
                    JSONObject wdet=jsonObj.getJSONObject("wind");

                    dc=des.getString("description");
                    Double kel=details.getDouble("temp");
                    Double celsius =kel - 273.15;
                    Double roundOff = (double) Math.round(celsius * 100) / 100;
                    t=roundOff.toString()+" degree celcius";

                    Double pr=details.getDouble("pressure");
                    Double hu=details.getDouble("humidity");
                    p=pr.toString()+" ";
                    h=hu.toString();
                     kel=details.getDouble("temp_min");
                     celsius =kel - 273.15;
                     roundOff = (double) Math.round(celsius * 100) / 100;
                    mm=roundOff.toString()+" degree celcius";

                     kel=details.getDouble("temp_max");
                     celsius =kel - 273.15;
                     roundOff = (double) Math.round(celsius * 100) / 100;
                    ma=roundOff.toString()+" degree celcius";
                    pr=wdet.getDouble("speed");
                    sp=pr.toString();

                }  catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
                //description.setText("hh"+dc);
                //temp.setText("dc");
            Toast.makeText(MainActivity.this,"hello"+p+h+mm+ma+sp, Toast.LENGTH_LONG).show();
            temper.setText("Temperature:  "+t+"\nPressure:  "+p+"\nHumidity:  "+h+"\nMax Temp:  "+mm+"\nMin Temp:  "+ma+"\nSpeed: "+sp+"\nSummary:  "+dc);
          //  pressure.setText(p);
           // humidity.setText(h);
          //  mintemp.setText(mm);
          //  maxtemp.setText(ma);
          //  description.setText(dc);
            //speed.setText(sp);
         // description.setText(dc);
           // maxtemp.setText("hhh");
        }

    }

}