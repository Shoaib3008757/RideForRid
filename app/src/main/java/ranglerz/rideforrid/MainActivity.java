package ranglerz.rideforrid;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.sql.Time;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {


    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    RelativeLayout rl_tv_pickuplocation, rl_tv_dropofflocation;
    RelativeLayout rl_tv_pickupdate, rl_tv_pickuptime;
    TextView tv_pickuplocation, tv_dropoff;
    TextView tv_pickupdate, tv_pickuptime;

    RelativeLayout rl_et_name, rl_et_phone;
    EditText et_name, et_phone;

    RelativeLayout rl_tv_submit;

    int indecator = -1;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private int mYear, mMonth, mDay, mHour, mMinute;

    private int myHour, myMinut, myday, myMonth, myYear;

    private LatLng latlngPickup;
    private LatLng latlngDropoff;

    String orderTosend = null;

    ProgressBar progress_bar;
    String userName = null;
    String phone = null;
    String userPickupLocation = null;
    String userDropoffLocation = null;
    String userPickuplatlng = null;
    String userDropoffLatlng = null;
    String userPickDate = null;
    String userPickTime = null;
    String userDistance = null;
    String userFare = null;
    String userCarType = null;

    AlarmManager alarmManager;
    private PendingIntent pendingIntentForNotification;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);



        init();
        checkPermission();
        checkLocationPermission();
        getPickupLocation();
        getDropofflocation();

        selectDate();
        selectTime();
        setingCursorVisible();

        submitclickListener();

        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(MainActivity.this ,R.color.colorGreen)));
        /*getSupportActionBar().setTitle(R.string.about_us);
        getSupportActionBar().setIcon(R.drawable.guard_about);*/


    }

    public void init(){
        rl_tv_pickuplocation = (RelativeLayout) findViewById(R.id.rl_tv_pickuplocation);
        rl_tv_dropofflocation = (RelativeLayout) findViewById(R.id.rl_tv_dropofflocation);
        tv_pickuplocation = (TextView) findViewById(R.id.tv_pickuplocation);
        tv_dropoff = (TextView) findViewById(R.id.tv_dropoff);



        rl_tv_pickupdate = (RelativeLayout) findViewById(R.id.rl_tv_pickupdate);
        rl_tv_pickuptime = (RelativeLayout) findViewById(R.id.rl_tv_pickuptime);
        tv_pickupdate = (TextView) findViewById(R.id.tv_pickupdate);
        tv_pickuptime = (TextView) findViewById(R.id.tv_pickuptime);

        rl_et_name = (RelativeLayout) findViewById(R.id.rl_et_name);
        rl_et_phone = (RelativeLayout) findViewById(R.id.rl_et_phone);

        et_name = (EditText) findViewById(R.id.et_name);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_name.setFocusableInTouchMode(true);
        et_phone.setFocusableInTouchMode(true);
        et_name.setSingleLine(true);

        rl_tv_submit = (RelativeLayout) findViewById(R.id.rl_tv_submit);

        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);








    }//end of init

    public void getPickupLocation(){

        rl_tv_pickuplocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                indecator = 0;
                callingLocationDialog();

            }
        });
    }//end of getting pickup location

    public void getDropofflocation(){

        rl_tv_dropofflocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                indecator = 1;
                callingLocationDialog();

            }
        });
    }//end of getting dropoff location


    public void callingLocationDialog(){


        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setBoundsBias(new LatLngBounds(new LatLng(23.695,  68.149), new LatLng(35.88250, 76.51333)))//south and north latlong bourdy for pakistan
                            .build(MainActivity.this);




            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }


    public void selectDate(){

        rl_tv_pickupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {


                                myday = dayOfMonth;
                                myMonth = monthOfYear + 1;
                                myYear = year;
                                tv_pickupdate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                                tv_pickupdate.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis()- 1000); // for setting set start form current date

                datePickerDialog.show();


            }
        });
    }//end of select date

    public void selectTime(){

        rl_tv_pickuptime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {


                                myMinut = minute;
                                myHour = hourOfDay;

                                //tv_pickuptime.setText(hourOfDay + ":" + minute);


                                String hourString = "";
                                if(hourOfDay < 12) {
                                    hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
                                } else {
                                    hourString = (hourOfDay - 12) < 10 ? "0"+(hourOfDay - 12) : ""+(hourOfDay - 12);
                                }
                                String minuteString = minute < 10 ? "0"+minute : ""+minute;
                                //String secondString = second < 10 ? "0"+second : ""+second;
                                String am_pm = (hourOfDay < 12) ? "AM" : "PM";
                                String time = hourString+":"+minuteString + " " + am_pm;

                                tv_pickuptime.setText(time);
                                tv_pickuptime.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));




                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e("TAg", "the code is result: " + resultCode);
        Log.e("TAg", "the code is resquest: " + requestCode);
        Log.e("TAg", "the code is Intent: " + data);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                String plceName = place.getName().toString();
                String plceAddress = place.getAddress().toString();
                LatLng latlng = place.getLatLng();
                Log.i("TAG", "Place: 123" + place.getName());

                Log.i("TAG", "Place: " + place.getAddress());
                Log.i("TAG", "Place Coordinates: " + place.getLatLng());

                if (indecator==0){

                    tv_pickuplocation.setText(plceAddress);
                    tv_pickuplocation.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                    latlngPickup =latlng;
                }
                else if(indecator==1){
                    tv_dropoff.setText(plceAddress);
                    tv_dropoff.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));

                    latlngDropoff = latlng;
                }


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("TAG", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }//end of onActivity Result


    public void setingCursorVisible(){

        rl_et_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                et_name.setCursorVisible(true);
            }
        });

        rl_et_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                et_phone.setCursorVisible(true);
            }
        });
    }


    public void submitclickListener(){

        rl_tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);

                /*final String pickLocation = tv_pickuplocation.getText().toString();
                final String drobOffLocation  = tv_dropoff.getText().toString();
                final String pickupDate = tv_pickupdate.getText().toString();
                final String pickupTime = tv_pickuptime.getText().toString();
                final String name = et_name.getText().toString();
                final String phone = et_phone.getText().toString();

                if (pickLocation.length()==0){

                    Toast.makeText(MainActivity.this, "Please Select your Pickup Location", Toast.LENGTH_SHORT).show();
                }
                else if (drobOffLocation.length()==0){

                    Toast.makeText(MainActivity.this, "Please Select your Dropoff Location", Toast.LENGTH_SHORT).show();
                }
                else if (pickupDate.length()==0){

                    Toast.makeText(MainActivity.this, "Please Select your Pickup Date", Toast.LENGTH_SHORT).show();
                }
                else if (pickupTime.length()==0){

                    Toast.makeText(MainActivity.this, "Please Select your Pickup Time", Toast.LENGTH_SHORT).show();
                }
                else if (name.length()==0){

                    Toast.makeText(MainActivity.this, "Please Enter your Name", Toast.LENGTH_SHORT).show();
                }
                else if (phone.length()==0){

                    Toast.makeText(MainActivity.this, "Please Enter Your Phone No.", Toast.LENGTH_SHORT).show();
                }
                else if (phone.length()<10){

                    Toast.makeText(MainActivity.this, "Please Enter Valid Phone No", Toast.LENGTH_SHORT).show();
                }

                else if (isNetworkAvailable()==false){
                    Toast.makeText(MainActivity.this, "No Internet Connection found", Toast.LENGTH_SHORT).show();
                }
                else {

                    Log.i("TAG", "The Pickup LatLng: " + latlngPickup);
                    Log.i("TAG", "The Droboff LatLng: " + latlngDropoff);

                    Double selctedMarketTime =  SphericalUtil.computeDistanceBetween(latlngPickup, latlngDropoff);
                    Log.i("TAG", "The distance is: " + selctedMarketTime);
                    double estimateDriveTime = selctedMarketTime/1000;
                    double aa =  round(estimateDriveTime, 1);
                    int distace = ((int)aa) + 4;
                    Log.i("TAG", "The distance is: " + distace);



                    //for calculating time

                    double esTime = distace/11;
                    Log.i("TAG", "The time is: " + esTime);
                    double timeInMinuts = esTime/60;

                    Log.i("TAG", "The timeinminut is: " + esTime);
                    double timeroudn = round(timeInMinuts, 0);
                    Double d = new Double(timeroudn);
                    int totalTime = d.intValue();


                    String mdistance = distace + " km";
                    String mFare =  "Rs." + distace*20;

                    userName = name;
                    userPickupLocation = pickLocation;
                    userDropoffLocation = drobOffLocation;
                    userPickuplatlng = latlngPickup.toString();
                    userDropoffLatlng = latlngDropoff.toString();
                    userPickDate = pickupDate;
                    userPickTime = pickupTime;
                    userDistance = mdistance;
                    userFare = mFare;


                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("The Estimated Fare");
                    dialog.setMessage("The Total Distance is: " + distace + " Km" +
                            // "\n" + "The Total Estimate Traveling Time: " + totalTime + "minuts" +
                            "\n" + "The Total Estimated Fare is: " + "Rs." + (distace*20));

                    dialog.setPositiveButton("Book Now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String textToSend = "Name: " + name
                                    + " \n" + "Phone: " + phone

                                    + " \n" + "Pick Address: " + pickLocation
                                    + " \n" + "Pickup Date: " + pickupDate
                                    + " \n" + "Pickup time: " + pickupTime
                                    + " \n" + "Dropoff Location: " + drobOffLocation
                                    + " \n" + "Pickup : " + latlngPickup
                                    + " \n" + "Dropoff : " + latlngDropoff;

                            //send mail here
                           *//* Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto","shoaib.ranglerz@gmail.com", null));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Cab Booking Order");
                            emailIntent.putExtra(Intent.EXTRA_TEXT, textToSend);
                            startActivityForResult(Intent.createChooser(emailIntent, "Send email..."), 0);*//*

                            orderTosend = textToSend;


                        }
                    });

                    dialog.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                        }
                    });

                    dialog.show();
                }

*/
            }
        });
    }
    //rouding double
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    //sending email

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startAlarmForMorning(int timeHour, int timeMinute, int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month-1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        Log.e("TAG", "Year: " + year);
        Log.e("TAG", "month: " + month);
        Log.e("TAG", "DAY: " + day);
        calendar.set(Calendar.HOUR_OF_DAY, timeHour);
        Log.e("TAG", "TimeHousr: " + timeHour);
        calendar.set(Calendar.MINUTE, timeMinute);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntentForNotification);

    }

    private boolean checkPermission() {
        Log.d("MapActivity", "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );





    }


    public boolean checkLocationPermission(){
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

}
