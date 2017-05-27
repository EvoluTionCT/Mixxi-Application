package com.example.evolutionct.mixxi.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.evolutionct.mixxi.R;
import com.example.evolutionct.mixxi.speechmixxi.SpeechActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SimpleDirectionActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DirectionCallback {
    private Button btnRequestDirection;
    private GoogleMap googleMap;
    private TextView mTextView;
    private TextView gas95;
    private TextView gas91;
    private TextView diesel;
    private TextView e20;
    private TextView e85;
    private EditText edit1;
    private EditText edit2;
    private LinearLayout infoLayout;
    private Toolbar toolbar;
    Geocoder geocoder = new Geocoder(this);
    List<Address> addressOrigin = null;
    List<Address> addressDest = null;

    Double dist = 0.0;
    Double totalFuel95 = 0.0;
    Double totalFuel91 = 0.0;
    Double totalFuelDiesel = 0.0;
    Double totalFuelE20 = 0.0;
    Double totalFuelE85 = 0.0;
    Double fuelPrice95 = 27.65;
    Double fuelPrice91 = 27.38;
    Double fuelDiesel = 26.39;
    Double fuelE20 = 25.14;
    Double fuelE85 = 19.85;
    private String serverKey = "AIzaSyA88HLLDsezHGvDouCvIgNNzQQ8X_PPo2o";
    private LatLng camera = new LatLng(37.782437, -122.4281893);
    /*private LatLng origin = new LatLng(37.7849569, -122.4068855);
    private LatLng destination = new LatLng(37.7814432, -122.4460177);*/
    private LatLng origin = null;
    private LatLng destination = null;
    private TrackGPS gps;
    double longitude;
    double latitude;
    boolean checkOnCreate = true;
    ImageButton imageButton;
    String Long;
    String Lat;

    ImageButton speakButton;
    ArrayList<String> matches = new ArrayList<String>();
    TextToSpeech Mixxi;

    private SpeechActivity threadSpeech = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        infoLayout = (LinearLayout) this.findViewById(R.id.infoLayout);
        // Hide linear layout

        infoLayout.setVisibility(LinearLayout.GONE);
        mTextView = (TextView) findViewById(R.id.textView);
        gas95 = (TextView) findViewById(R.id.Gas95);
        gas91 = (TextView) findViewById(R.id.Gas91);
        diesel = (TextView) findViewById(R.id.Diesel);
        e20 = (TextView) findViewById(R.id.E20);
        e85 = (TextView) findViewById(R.id.E85);
        edit1 = (EditText) findViewById(R.id.edit1);
        edit2 = (EditText) findViewById(R.id.edit2);
        toolbar = (Toolbar) findViewById(R.id.toolbar_fuel);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String str = "Computer Engineering";
        List<String> items = Arrays.asList(str.split("\\s*,\\s*"));
        Log.d("item", items.toString());


        btnRequestDirection = (Button) findViewById(R.id.btn_request_direction);
        btnRequestDirection.setOnClickListener(this);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfuel)).getMapAsync(this);

        imageButton = (ImageButton) findViewById(R.id.get);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                gps = new TrackGPS(SimpleDirectionActivity.this);


                if (gps.canGetLocation()) {


                    longitude = gps.getLongitude();
                    latitude = gps.getLatitude();

                    //Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                } else {

                    gps.showSettingsAlert();
                }
                Long = String.valueOf(longitude);
                //Log.d("Text++++++++++++",Long);
                Lat = String.valueOf(latitude);
                //Log.d("Text------------",Lat);
                checkOnCreate = false;
                edit1.setText(Lat + "," + Long);
            }
        });
        Mixxi =new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    Mixxi.setLanguage(Locale.UK);
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 13));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (checkOnCreate == false) {
            checkOnCreate = true;
            Intent in = new Intent(this, SimpleDirectionActivity.class);
            startActivity(in);
        } else {
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        threadSpeech = new SpeechActivity(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        threadSpeech.speechDisable = true;

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_request_direction) {
            checkOnCreate = false;
            edit1.setVisibility(View.GONE);
            edit2.setVisibility(View.GONE);
            if (edit1.getText().toString().isEmpty() || edit2.getText().toString().isEmpty()) {
                Intent in = new Intent(this, SimpleDirectionActivity.class);
                startActivity(in);
                Toast.makeText(this, "Type again", Toast.LENGTH_LONG).show();

            } else {
                Log.d("Text", edit1.getText().toString() + edit2.getText().toString());

                try {
                    addressOrigin = geocoder.getFromLocationName(edit1.getText().toString(), 1);
                    addressDest = geocoder.getFromLocationName(edit2.getText().toString(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Address addressO = addressOrigin.get(0);
                Address addressD = addressDest.get(0);
                origin = new LatLng(addressO.getLatitude(), addressO.getLongitude());
                destination = new LatLng(addressD.getLatitude(), addressD.getLongitude());

                requestDirection();
            }


        }
    }

    // Input through speaking
    public void speakerDestination(String sentence) {
        threadSpeech.checkMixxi = false;
        threadSpeech.checkFirstEntry = false ;
        threadSpeech.dialog.cancel();
        String tempBefore = null;
        String realBefore = " ";
        String realAfter = " ";



        boolean checkMatchTo = false;
        if (sentence.contains("to")) {
            String[] arr = sentence.split(" ");
            //String[] arr = sentence.split(" ");
            for (String ss : arr) {

                if (ss.equals("to")) {

                    realBefore = tempBefore;
                    checkMatchTo = true;

                } else if (checkMatchTo == true) {
                    realAfter = ss;
                    checkMatchTo = false;
                    break;
                }
                tempBefore = ss;

            }
        } else if (sentence.contains("ถึง")) {
            String[] arr = sentence.split("ถึง");
            realBefore = arr[0];
            realAfter = arr[1];

        }


        checkOnCreate = false;
        edit1.setVisibility(View.GONE);
        edit2.setVisibility(View.GONE);

        Log.d("Text", realBefore + realAfter);

        try {
            addressOrigin = geocoder.getFromLocationName(realBefore, 1);
            addressDest = geocoder.getFromLocationName(realAfter.toString(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addressOrigin.size()!=0)
        {
            Address addressO = addressOrigin.get(0);
            Address addressD = addressDest.get(0);
            origin = new LatLng(addressO.getLatitude(), addressO.getLongitude());
            destination = new LatLng(addressD.getLatitude(), addressD.getLongitude());

            requestDirection();
        }



    }

    public void requestDirection() {
        Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if (direction.isOK()) {
            googleMap.addMarker(new MarkerOptions().position(origin));
            googleMap.addMarker(new MarkerOptions().position(destination));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));

            btnRequestDirection.setVisibility(View.GONE);

            //getDistanceInfo(origin,destination);
            new Mytask().execute();
        } else {
            Toast.makeText(this, "Type again", Toast.LENGTH_LONG).show();
            Intent in = new Intent(this, SimpleDirectionActivity.class);
            startActivity(in);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Snackbar.make(btnRequestDirection, t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    private class Mytask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            StringBuilder stringBuilder = new StringBuilder();

            try {

                //destinationAddress = destinationAddress.replaceAll(" ","%20");
                String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + "," + origin.longitude + "&destination=" + destination.latitude + "," + destination.longitude + "&key=AIzaSyAOB0vXFj0IFinCqP-VERrZI-yDRgAf8ck";

                HttpPost httppost = new HttpPost(url);

                HttpClient client = new DefaultHttpClient();
                HttpResponse response;
                stringBuilder = new StringBuilder();
                Log.d("Post", httppost.toString());

                response = client.execute(httppost);
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

                JSONArray array = jsonObject.getJSONArray("routes");

                JSONObject routes = array.getJSONObject(0);

                JSONArray legs = routes.getJSONArray("legs");

                JSONObject steps = legs.getJSONObject(0);

                JSONObject distance = steps.getJSONObject("distance");

                Log.i("Distance", distance.toString());
                dist = Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]", ""));
                //dist = 1.60934 * dist ;
                totalFuel95 = (dist / 15) * fuelPrice95;
                totalFuel91 = (dist / 15) * fuelPrice91;
                totalFuelDiesel = (dist / 15) * fuelDiesel;
                totalFuelE20 = (dist / 15) * fuelE20;
                totalFuelE85 = (dist / 15) * fuelE85;


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void avoid) {
            super.onPostExecute(avoid);
            DecimalFormat df = new DecimalFormat("#.00");
            if (dist > 100) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 8));
            } else if (dist <= 100) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13));
            }

            infoLayout.setVisibility(LinearLayout.VISIBLE);
            mTextView.setText("Distance : " + df.format(dist).toString() + " km.");
            gas95.setText("Gasohol 95 : " + df.format(totalFuel95).toString() + " Baht");
            gas91.setText("Gasohol 91 : " + df.format(totalFuel91).toString() + " Baht");
            diesel.setText("Diesel     : " + df.format(totalFuelDiesel).toString() + " Baht");
            e20.setText("E20        : " + df.format(totalFuelE20).toString() + " Baht");
            e85.setText("E85        : " + df.format(totalFuelE85).toString() + " Baht");

        }
    }

    public void speakerFuel(String sentence)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        if(sentence.contains("แก๊สโซฮอล์ 95") || sentence.contains("gasohol 95") || sentence.contains("แก๊สโซฮอล์ 9 5"))
        {
            Mixxi.speak(df.format(totalFuel95).toString() + " Baht", TextToSpeech.QUEUE_FLUSH, null);
        }
        else if(sentence.contains("แก๊สโซฮอล์ 91") || sentence.contains("gasohol 91") || sentence.contains("แก๊สโซฮอล์ 9 1"))
        {
            Mixxi.speak(df.format(totalFuel91).toString() + " Baht", TextToSpeech.QUEUE_FLUSH, null);
        }
        else if(sentence.contains("ดีเซล") || sentence.contains("diesel"))
        {
            Mixxi.speak(df.format(totalFuelDiesel).toString() + " Baht", TextToSpeech.QUEUE_FLUSH, null);
        }
        else if(sentence.contains("อี 20") || sentence.contains("e20"))
        {
            Mixxi.speak(df.format(totalFuelE20).toString() + " Baht", TextToSpeech.QUEUE_FLUSH, null);
        }
        else if(sentence.contains("อี 85") || sentence.contains("e85"))
        {
            Mixxi.speak(df.format(totalFuelE85).toString() + " Baht", TextToSpeech.QUEUE_FLUSH, null);
        }
        else if(sentence.contains("ระยะทาง") || sentence.contains("distance") )
        {
            Mixxi.speak(df.format(dist).toString() + "kilometers", TextToSpeech.QUEUE_FLUSH, null);
        }
        else
        {
            Mixxi.speak("I don't know what you said", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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

}
