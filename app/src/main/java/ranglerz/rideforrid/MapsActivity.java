package ranglerz.rideforrid;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.maps.model.Marker;

import com.google.maps.android.SphericalUtil;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    TextView tv_destination;



    private static final int REQUEST_FINE_LOCATION = 11;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    int indecator = -1;

    private LatLng latlngDestinationFromTv;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    LatLng mCurrentLatLng;


    Location myCurrentLocation;
    Location myStaticCurrentLocation;
    Marker mCurrLocationMarker;
    Marker mySelectedDestinationl;
    Marker myMarkers;
    Marker mSelectedPinMarker;

    private int timer = 3;
    Handler mHandler;


    double latitude; // latitude
    double longitude; // longitude

    MapHelper mapHelper;


    PolylineOptions lineOptions;
    Polyline polyline = null;


    TextView tv_distance;
    boolean isStartTravling = true;
    AddressResultReceiver mResultReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        turnOnGPS();

        inisialization();
        createNetErrorDialog();
        checkPermission();
        tvDestinationClickListner();
        settVisibilityforButton();


        useHandler();


        //cLocation();

    }//end of onCreate


    public void inisialization() {


        tv_destination = (TextView) findViewById(R.id.tv_destination);
        tv_distance = (TextView) findViewById(R.id.tv_distance);

        mResultReceiver = new AddressResultReceiver(null);


        //temporary

        //latlngDestinationFromTv = new LatLng(tempLat, temLng);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mapHelper = new MapHelper();

        //


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setMyLocationEnable();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            mMap.setMyLocationEnabled(true);
            return;
        }

        mMap.setMyLocationEnabled(true);
        //setMapLongclickLisnter();


        buildGoogleApiClient();

        mGoogleApiClient.connect();
        setMapLongclickLisnter();


        ///

    }


    ///

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnected(Bundle bundle) {
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
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            //mGoogleMap.clear();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            myCurrentLocation = mLastLocation;
            myStaticCurrentLocation = mLastLocation;

            mapHelper.setLatitude(latitude);
            mapHelper.setLongitude(longitude);

            Log.e("latlang", "latitudeCustomer " + latitude);
            Log.e("latlang", "longitudeCustomer " + longitude);
            mCurrentLatLng = new LatLng(latitude, longitude);
            latlngDestinationFromTv = mCurrentLatLng;
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mCurrentLatLng);
            markerOptions.title("Current Location");

            if (myMarkers!=null){
                myMarkers.remove();
            }
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.starup_person_icon));

           myMarkers =  mMap.addMarker(markerOptions);


            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mCurrentLatLng).zoom(12.f).build();

            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));


        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); //1 seconds
        mLocationRequest.setFastestInterval(1000); //1 seconds
        mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (mGoogleApiClient.isConnected()) {

            Log.e("TAG", "the travling: " + isStartTravling);

            if (isStartTravling) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    ///

/*

    //nearest location
    public void nearestLocation(){

        nearestLocatinoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNetErrorDialog();
                latitude = mapHelper.getLatitude();
                longitude = mapHelper.getLongitude();
                //Toast.makeText(getApplicationContext(), " Latitude: " + latitude + " Longitude: " + longitude, Toast.LENGTH_SHORT).show();

                if (polyline != null) {
                    polyline.remove();
                }
                calculateShorDistance();


            }
        });
    }
*/


    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d("MapActivity", "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d("MapActivity", "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION
        );
    }

 /*   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("MapActivity", "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkPermission())
                        mMap.setMyLocationEnabled(true);

                } else {
                    // Permission denied

                }
                break;
            }
        }//end of switch
    }// of onRequestPermissionResult*/


    public void setMyLocationEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);


        }
    }

/*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==REQUEST_FINE_LOCATION){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                setMyLocationEnable();
            }else {
                Toast.makeText(getApplicationContext(), "Untill You Grand Permission Map Cant Display", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/

    //calculation distance
    public void calculateShorDistance(LatLng pickup, LatLng dropOff) {

        String url = getUrl(pickup, dropOff);
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();
        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //   setingTextAndTimeInTextView(dhaDistance);


    }//end of calculate distance


    //rouding double
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    //market for Pickup Location
    public void addingMarketForPickLocation(LatLng pickup, String pickupTitle) {
        mMap.addMarker(new MarkerOptions()
                .position(pickup)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_icon))
                .title(pickupTitle));

    }

    //market for DropOff Location
    public void addingMarketForDropOffLocation(LatLng drobOff, String dropOffTitle) {
        mMap.addMarker(new MarkerOptions()
                .position(drobOff)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.starup_person_icon))
                .title(dropOffTitle));

    }

    public double shortDistance(double fromLong, double fromLat, double toLong, double toLat) {
        double d2r = Math.PI / 180;
        double dLong = (toLong - fromLong) * d2r;
        double dLat = (toLat - fromLat) * d2r;
        double a = Math.pow(Math.sin(dLat / 2.0), 2) + Math.cos(fromLat * d2r)
                * Math.cos(toLat * d2r) * Math.pow(Math.sin(dLong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6367000 * c;
        return Math.round(d);
    }


    //distance between two points

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }


    //Thread for starting mainActivity
    private Runnable mRunnableStartMainActivity = new Runnable() {
        @Override
        public void run() {
            Log.d("Handler", " Calls");
            timer--;
            mHandler = new Handler();
            mHandler.postDelayed(this, 1000);

            if (timer == 0) {
                LatLng currentLatLng = new LatLng(latitude, longitude);
                mapHelper.setScr(currentLatLng);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14.0f));

                // addingMarketForPickLocation(latlngDestinationFromTv, "Destination");
                //addingMarketForDropOffLocation(mLatlngDropoff, mDropOffLocation);


                mHandler.removeCallbacks(mRunnableStartMainActivity);

            }
        }
    };


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                       /* if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }*/
                        mMap.setMyLocationEnabled(true);
                        setMyLocationEnable();
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    //***********************

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    //converting time into hrs and day
    public String timeConvert(int time) {
        return time / 24 / 60 + ":" + time / 60 % 24 + ':' + time % 60;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

       /* Toast toast =  Toast.makeText(this, "Location Changed " + location.getLatitude()
                + location.getLongitude(), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
*/

        //

        Log.e("TAG", "tbe boolen is: " + isStartTravling);
        if (isStartTravling) {




            myCurrentLocation = location;
            if (mCurrLocationMarker != null) {
                mCurrLocationMarker.remove();
            }
            if (myMarkers!=null){
                myMarkers.remove();
            }

            double lat = location.getLatitude();
            double lng = location.getLongitude();
            Log.e("TAg", "  the change location is: " + lat);
            Log.e("TAg", "  the change location is: " + lng);


            //Place current location marker
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon((BitmapDescriptorFactory.fromResource(R.drawable.starup_person_icon)));
            myMarkers = mMap.addMarker(markerOptions);

            //move map camera
          //  mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
          //  mMap.animateCamera(CameraUpdateFactory.zoomTo(18));


            LatLng currentLatLng = new LatLng(latitude, longitude);

            Log.e("TAG", "abc test CurrentLATLNG: " + latLng);
            Log.e("TAG", " abc test Static LatLng: " + currentLatLng);


            calculateShorDistance(latlngDestinationFromTv, latLng);

            mCurrLocationMarker = mMap.addMarker(markerOptions);
            calculateShorDistance(latlngDestinationFromTv, latLng);
            //
            Double selctedMarketTime =  SphericalUtil.computeDistanceBetween(latlngDestinationFromTv, latLng);
            Log.i("TAG", "The distance is: " + selctedMarketTime);
            double estimateDriveTime = selctedMarketTime/1000;
            double aa =  round(estimateDriveTime, 1);
            int distace = ((int)aa) + 4;
            Log.i("TAG", "The distance is: " + distace);
            tv_distance.setText(aa + " Km");


            //

           // latlngDestinationFromTv = latLng;
        }

    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;

            lineOptions = null;


            // Traversing through all the routes
            if (result.size()!=0){
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));

                    Log.d("onPostExecute","onPostExecute lineoptions decoded");

                }
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                if (polyline!=null){
                     polyline.remove();
                    polyline =  mMap.addPolyline(lineOptions);
                }else {
                    polyline = mMap.addPolyline(lineOptions);
                }

            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }


    //handler for the starign activity
    Handler newHandler;
    public void useHandler(){

        newHandler = new Handler();
        newHandler.postDelayed(mRunnableStartMainActivity, 1000);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnableStartMainActivity);
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    protected void createNetErrorDialog() {

        if (isNetworkAvailable()==false){


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.")
                    .setTitle("Unable to connect")
                    .setCancelable(false)
                    .setPositiveButton("Settings",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    startActivity(i);
                                }
                            }
                    )
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    MapsActivity.this.finish();
                                }
                            }
                    );
            AlertDialog alert = builder.create();
            alert.show();
        }else {
            //remainging
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public void turnOnGPS(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        MapsActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }


    }

    @Override
    protected void onResume() {
        super.onResume();


    }



    public LatLng convertStringToLatlng(String latlngString){


        String abcdedf =   latlngString;
        String[] latlong =  abcdedf.split(",");
        String latitude = (latlong[0]);
        String longitude = (latlong[1]);

        Log.e("TAG", "latitude: " + latitude);
        Log.e("TAG", "logitue: " + longitude);

        String[] mLat = latitude.split("\\(");
        String latLeft = (mLat[0]);
        String LatRight = (mLat[1]);
        Log.e("TAG", "latleft: " + latLeft);
        Log.e("TAG", "latright: " + LatRight);

        String[] mLng = longitude.split("\\)");
        String lngleft = (mLng[0]);
        //String lngright = (mLng[1]);
        Log.e("TAG", "lngleft: " + lngleft);

        double myLatitude = Double.parseDouble(LatRight);
        double myLongitude = Double.parseDouble(lngleft);
        // Log.e("TAG", "lngright: " + lngright);

        LatLng latLng = new LatLng(myLatitude, myLongitude);

        Log.e("TAG", "final Latlng: " + latLng);

        return latLng;

    }

    public void reFreshMap(){


    }

    //getting picktup location tv_click listener

    public void tvDestinationClickListner() {

        tv_destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                indecator = 0;
                callingLocationDialog();

            }
        });
    }//end of getting destination from tv



    public void callingLocationDialog(){


        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MapsActivity.this);




            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,  final Intent data) {

        Log.e("TAg", "the code is result: " + resultCode);
        Log.e("TAg", "the code is resquest: " + requestCode);
        Log.e("TAg", "the code is Intent: " + data);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {


                AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                alert.setTitle("Setting Destination");
                alert.setMessage("Set your selected pin as your destination");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        Place place = PlaceAutocomplete.getPlace(MapsActivity.this, data);
                        String plceName = place.getName().toString();
                        String plceAddress = place.getAddress().toString();
                        LatLng latlng = place.getLatLng();
                        Log.i("TAG", "Place: 123" + place.getName());

                        Log.i("TAG", "Place: " + place.getAddress());
                        Log.i("TAG", "Place Coordinates: " + place.getLatLng());

                        if (indecator==0){

                            tv_destination.setText(plceAddress);
                            tv_destination.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                            latlngDestinationFromTv = latlng;
                            MarkerOptions markerOptions  = new MarkerOptions();
                            markerOptions.position(latlngDestinationFromTv);
                            markerOptions.title("Your Destination");
                            markerOptions.icon((BitmapDescriptorFactory.fromResource(R.drawable.destination_icon)));
                            if (mySelectedDestinationl!=null){

                                mySelectedDestinationl.remove();

                            }
                            mySelectedDestinationl = mMap.addMarker(markerOptions);
                            calculateShorDistance(mCurrentLatLng, place.getLatLng());
                            //
                            Double selctedMarketTime =  SphericalUtil.computeDistanceBetween(mCurrentLatLng, place.getLatLng());
                            Log.i("TAG", "The distance is: " + selctedMarketTime);
                            double estimateDriveTime = selctedMarketTime/1000;
                            double aa =  round(estimateDriveTime, 1);
                            int distace = ((int)aa) + 4;
                            Log.i("TAG", "The distance is: " + distace);
                            tv_distance.setText(aa + " Km");
                            //

                            latlngDestinationFromTv = place.getLatLng();

                        }

                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                });

                alert.show();

                //




            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("TAG", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }//end of onActivity Result

    //setting mapLongClickListner to get pin lat and long
    public void setMapLongclickLisnter(){

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {


                AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                alert.setTitle("Setting Destination");
                alert.setMessage("Set your selected pin as your destination");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        MarkerOptions markerOptions = new MarkerOptions();

                        markerOptions.position(latLng);
                        markerOptions.title("Your Destination");
                        markerOptions.icon((BitmapDescriptorFactory.fromResource(R.drawable.destination_icon)));
                        if (mySelectedDestinationl!=null){

                            mySelectedDestinationl.remove();

                        }
                        mySelectedDestinationl = mMap.addMarker(markerOptions);
                        calculateShorDistance(mCurrentLatLng, latLng);
                        //
                        Double selctedMarketTime =  SphericalUtil.computeDistanceBetween(mCurrentLatLng, latLng);
                        Log.i("TAG", "The distance is: " + selctedMarketTime);
                        double estimateDriveTime = selctedMarketTime/1000;
                        double aa =  round(estimateDriveTime, 1);
                        int distace = ((int)aa) + 4;
                        Log.i("TAG", "The distance is: " + aa);
                        tv_distance.setText(aa + " Km");

                        //

                        latlngDestinationFromTv = latLng;
                        double llat = latLng.latitude;
                        double llng = latLng.longitude;

                        startingService(llat, llng);

                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                });

                alert.show();

            }
        });
    }

    public void settVisibilityforButton(){

        if (tv_destination.getText().length()>1){


        }
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            Log.e("AG", " result code: " + resultCode);
            if (resultCode == Constants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(Constants.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // progressBar.setVisibility(View.GONE);
                        //infoText.setVisibility(View.VISIBLE);
                Log.e("TAg", "my location Address: " + address);
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.e("TAG", "Second Result is: " +  (resultData.getString(Constants.RESULT_DATA_KEY)));
                    }
                });
            }
        }
    }

    public void startingService(double lat, double lng){
        Intent intent = new Intent(this, GeocodeAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);


        intent.putExtra(Constants.LOCATION_LATITUDE_DATA_EXTRA,
                lat);
        intent.putExtra(Constants.LOCATION_LONGITUDE_DATA_EXTRA,
                lng);
        startService(intent);

    }



}//end of class


