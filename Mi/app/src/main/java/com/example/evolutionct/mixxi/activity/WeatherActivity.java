package com.example.evolutionct.mixxi.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.evolutionct.mixxi.GlobalActivity;
import com.example.evolutionct.mixxi.R;
import com.example.evolutionct.mixxi.fragment.WeatherFragment;
import com.example.evolutionct.mixxi.permission.LocationRequestActivity;
import com.example.evolutionct.mixxi.speechmixxi.SpeechActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    String lat, lon;
    FloatingActionButton fab;
    FloatingActionButton fab1;
    Toolbar toolbar;

    ImageButton speakButton;
    ArrayList<String> matches = new ArrayList<String>();
    TextToSpeech Mixxi;
    private SpeechActivity threadSpeech ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        toolbar = (Toolbar) findViewById(R.id.toolbar_weather);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        buildGoogleApiClient();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new WeatherFragment())
                    .commit();
        }
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClick();
            }
        });

        threadSpeech = new SpeechActivity(this);
    }
    @Override
    protected  void onResume()
    {
        super.onResume();
        threadSpeech = new SpeechActivity(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        threadSpeech.speechDisable = true ;

    }
    private void fabClick() {
        fab.hide();
        showInputDialog();
    }

    private void fabPlace() {
        fab1.hide();
        cityLocation();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.swap_weather_logo :
                Intent tent = new Intent(this, Weather2Activity.class);
                startActivity(tent);
                break;
            case R.id.refresh : changeCity(GlobalActivity.cp.getCity());
                break;
            case R.id.location : cityLocation();
                break;
            case android.R.id.home:
                Intent in = new Intent(this, MainActivity.class);
                startActivity(in);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent in = new Intent(this, MainActivity.class);
        startActivity(in);
    }

    private void showInputDialog() {
        new MaterialDialog.Builder(this)
                .title("Change City")
                .content("You can change the city by entering City name or the ZIP Code")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        fab.show();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        fab.show();
                    }
                })
                .negativeText("CANCEL")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog , @NonNull DialogAction which) {
                        dialog.dismiss();
                        fab.show();
                    }
                })
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, @NonNull CharSequence input) {
                        changeCity(input.toString());
                        fab.show();
                    }
                }).show();
    }

    public void changeCity(String city){
        WeatherFragment wf = (WeatherFragment)getSupportFragmentManager()
                .findFragmentById(R.id.container);
        wf.changeCity(city);
        GlobalActivity.cp.setCity(city);
    }

    public void changeCity(String lat, String lon){
        WeatherFragment wf = (WeatherFragment)getSupportFragmentManager()
                .findFragmentById(R.id.container);
        wf.changeCity(lat , lon);
    }

    private void showCity() {
        changeCity(lat , lon);
    }

    private void cityLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Intent intent  = new Intent(WeatherActivity.this , LocationRequestActivity.class);
                startActivity(intent);
            } else {
                showCity();
            }
        }
        else {
            showCity();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(100); // Update location every second
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
            }
            catch (SecurityException ex) {
                Log.e("Error" , "Helloworld");
            }
            if (mLastLocation != null) {
                lat = String.valueOf(mLastLocation.getLatitude());
                lon = String.valueOf(mLastLocation.getLongitude());
            }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
            DecimalFormat df = new DecimalFormat("###.##");
            double la = Double.parseDouble(lat);
            double lo = Double.parseDouble(lon);
            lat = df.format(la);
            lon = df.format(lo);
            Log.i("lat", lat);
            Log.i("lon", lon);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            buildGoogleApiClient();
    }

    synchronized void buildGoogleApiClient() {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
            mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
            mGoogleApiClient.disconnect();
        finish();
    }
}
