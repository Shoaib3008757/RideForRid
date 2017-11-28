package ranglerz.rideforrid;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by User-10 on 28-Nov-17.
 */

public class LocationAddressService extends IntentService {

    protected ResultReceiver resultReceiver;

    public LocationAddressService() {
        super("LocationAddressService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {



        List<Address> addresses = null;

        double latitude = intent.getDoubleExtra(Constants.LOCATION_LATITUDE_DATA_EXTRA, 0);
        double longitude = intent.getDoubleExtra(Constants.LOCATION_LONGITUDE_DATA_EXTRA, 0);
        resultReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        String finalAddressUrl = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + getResources().getString(R.string.map_geocoding);


// get lat and lng value
        JSONObject ret = getLocationInfo(latitude, longitude);
        JSONObject location;
        String location_string;
        try {
            //Get JSON Array called "results" and then get the 0th complete object as JSON
            location = ret.getJSONArray("results").getJSONObject(0);
            Log.e("TAG", "the result from address: " + location);
            // Get the value of the attribute whose name is "formatted_string"
            location_string = location.getString("formatted_address");
            JSONArray shortc = location.getJSONArray("address_components");
           // String first = shortc.getString(1);
            String logn_name = shortc.getJSONObject(1).get("long_name").toString();
            Log.e("TAG", "formattted address 11 :" + logn_name);
            Log.e("TAG", "formattted address:" + location_string);

            String fullAddress = location_string + " (" + logn_name + ")";

            deliverResultToReceiver(Constants.SUCCESS_RESULT, "message", fullAddress);

        } catch (JSONException e1) {
            e1.printStackTrace();

        }


    }


    public JSONObject getLocationInfo(double lat, double lng) {

        HttpGet httpGet = new HttpGet("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&key=" + getResources().getString(R.string.map_geocoding));
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void deliverResultToReceiver(int resultCode, String message, String address) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_ADDRESS, address);
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }
}