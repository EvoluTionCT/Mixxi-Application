package com.example.evolutionct.mixxi.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.example.evolutionct.mixxi.CountryInfo;
import com.example.evolutionct.mixxi.Database;
import com.example.evolutionct.mixxi.GlobalActivity;
import com.example.evolutionct.mixxi.JSONWeatherParser;
import com.example.evolutionct.mixxi.R;
import com.example.evolutionct.mixxi.WeatherHttpClient;
import com.example.evolutionct.mixxi.model.Weather;
import com.example.evolutionct.mixxi.speechmixxi.SpeechActivity;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
public class Weather2Activity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    ArrayList<String> tempLat = new ArrayList<>();
    ArrayList<String> tempLon = new ArrayList<>();
    int totalThailand = 0 ;
    private Firebase mRef;
    ArrayList<DataSnapshot> countryInfo = new ArrayList<>() ;
    ArrayList<DataSnapshot> countryInfo_store = new ArrayList<>() ;
    Database mHelper;
    SQLiteDatabase mDb;

    public CountryInfo nearLo = null ;
    private SpeechActivity threadSpeech ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_weather2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        Firebase.setAndroidContext(this);

        mRef = new Firebase("https://realtimeweather-caae2.firebaseio.com/Thailand/");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalThailand = (int)dataSnapshot.getChildrenCount();

                int counter = 0 ;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    countryInfo.add(child);
                    //Log.d("countryinfo",countryInfo.get(counter).child("lat").getValue().toString());
                    tempLat.add(child.child("lat").getValue().toString());
                    tempLon.add(child.child("lng").getValue().toString());

                    counter++;
                }
                counter = 0 ;
                //new MyTask().execute();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        mHelper = new Database(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        threadSpeech = new SpeechActivity(this);



    }
    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();

    }

    @Override
    protected void onStop() {
        mClient.disconnect();
        super.onStop();
        mHelper.close();
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
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            // buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        new JSONWeatherTask().execute();



        //stop location updates
        if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mClient == null) {
                            // buildGoogleApiClient();
                            mClient = new GoogleApiClient.Builder(this)
                                    .addApi(LocationServices.API)
                                    .addConnectionCallbacks(this)
                                    .addOnConnectionFailedListener(this)
                                    .build();
                        }
                        mMap.setMyLocationEnabled(true);
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
   private class JSONWeatherTask extends AsyncTask<Void, Void, List<CountryInfo>> {


       @Override
       protected List<CountryInfo>  doInBackground(Void... voids) {

           List<CountryInfo> data = mHelper.getList(mLastLocation.getLatitude(),mLastLocation.getLongitude());
           //String data = ( (new WeatherHttpClient()).getWeatherData(params[0]));
           for(int i = 0 ; i<data.size() ;i++)
           {
               try {
                  // Log.d("latlng", String.valueOf(data.get(i).latlng));

                   String tempdata = ( (new WeatherHttpClient()).getWeatherData(data.get(i).latlng));
                   Weather weather = JSONWeatherParser.getWeather(tempdata);
                   data.get(i).temperature = Math.round((weather.temperature.getTemp() - 273.15));
                   data.get(i).humidity = weather.currentCondition.getHumidity();
                   data.get(i).windSpeed = weather.wind.getSpeed();
                   data.get(i).sky = weather.currentCondition.getCondition();


                   // Let's retrieve the icon
                   //weather.iconData = ( (new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

               } catch (JSONException e) {
                   e.printStackTrace();
               }
           }

           nearLo = mHelper.getNearLo(data);

          return data ;

       }




       @Override
       protected void onPostExecute(List<CountryInfo> allData) {
           super.onPostExecute(allData);

            sss(allData);

       }







   }
    private void sss(List<CountryInfo> allData)
    {

        for(int i = 0 ; i<allData.size() ; i++)
        {
            if(allData.get(i).sky.contains("Cloud"))
            {
                mMap.addMarker(new MarkerOptions().position(allData.get(i).latlng).title(allData.get(i).city + " " + "("+ allData.get(i).sky +")")
                        .snippet("Temp: " +allData.get(i).temperature.toString() + " °C  " + allData.get(i).humidity.toString()+" % "+ allData.get(i).windSpeed.toString() + " mps " )
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cloud_s)));
            }
            else if(allData.get(i).sky.contains("Clear"))
            {
                mMap.addMarker(new MarkerOptions().position(allData.get(i).latlng).title(allData.get(i).city+ " " + "("+ allData.get(i).sky +")")
                        .snippet("Temp: " +allData.get(i).temperature.toString() + " °C  " + allData.get(i).humidity.toString()+" % "+ allData.get(i).windSpeed.toString() + " mps " )
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.clearsky)));
            }
            else  if(allData.get(i).sky.contains("Rain"))
            {
                mMap.addMarker(new MarkerOptions().position(allData.get(i).latlng).title(allData.get(i).city+ " " + "("+ allData.get(i).sky +")")
                        .snippet("Temp: " +allData.get(i).temperature.toString() + " °C  " + allData.get(i).humidity.toString()+" % "+ allData.get(i).windSpeed.toString() + " mps " )
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cloudrain_s)));
            }
            else if(allData.get(i).sky.contains("Mist"))
            {
                mMap.addMarker(new MarkerOptions().position(allData.get(i).latlng).title(allData.get(i).city+ " " + "("+ allData.get(i).sky +")")
                        .snippet("Temp: " +allData.get(i).temperature.toString() + " °C  " + allData.get(i).humidity.toString()+" % "+ allData.get(i).windSpeed.toString() + " mps " )
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mist_s)));
            }
            else if(allData.get(i).sky.contains("Thunderstorm"))
            {
                mMap.addMarker(new MarkerOptions().position(allData.get(i).latlng).title(allData.get(i).city+ " " + "("+ allData.get(i).sky +")")
                        .snippet("Temp: " +allData.get(i).temperature.toString() + " °C  " + allData.get(i).humidity.toString()+" % "+ allData.get(i).windSpeed.toString() + " mps " )
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.thunderstorm_s)));
            }
            else
            {
                mMap.addMarker(new MarkerOptions().position(allData.get(i).latlng).title(allData.get(i).city+ " " + "("+ allData.get(i).sky +")")
                        .snippet("Temp: " +allData.get(i).temperature.toString() + " °C  " + allData.get(i).humidity.toString()+" % "+ allData.get(i).windSpeed.toString() + " mps " ));
            }

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.weather2weather, menu);
        return true;
    }
    //and this to handle actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.swapweather :
                Intent swap = new Intent(this,GlobalActivity.class);
                startActivity(swap);
                break;
            case android.R.id.home:
                Intent in = new Intent(this, MainActivity.class);
                startActivity(in);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
