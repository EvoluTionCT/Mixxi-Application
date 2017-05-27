package com.example.evolutionct.mixxi.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.evolutionct.mixxi.GlobalActivity;
import com.example.evolutionct.mixxi.R;
import com.example.evolutionct.mixxi.speechmixxi.SpeechActivity;
import com.example.evolutionct.mixxi.speed.SpeedActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.evolutionct.mixxi.fragment.FacebookFragment.VOICE_RECOGNITION_REQUEST_CODE;


public class PlaceActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int PLACE_PICKER_REQUEST = 1000;
    private GoogleApiClient mClient;
    String ApiKey = "AIzaSyCKfxvvNsLhPb1Q8YBgpKNEChYgr0EqPpM"; // Server Key

    private ArrayList<ThreadPlace> threadPlace = new ArrayList<ThreadPlace>();
    private ThreadPlace threadPlace2 = null ;
    int countThread = 0 ;
    List<Marker> marker = new ArrayList<Marker>() ;

    private static final String TAG = "DemoActivity";
    ListView lv;
    private SlidingUpPanelLayout mLayout;

    private TextView listee;
    private  ImageView mixImage;

    private MaterialSearchView searchView;
    public String placeQuery = "food" ;
    Toolbar toolbar;
    GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    boolean locationChangedCheck = false;
    boolean threadFirstCheck = false ;

    private List<String> nameOfL = new ArrayList<String>();
    private List<String> nameOfLo = new ArrayList<String>(); // name + km.

    ImageButton speakButton;
    ArrayList<String> matches = new ArrayList<String>();
    TextToSpeech Mixxi;
    public double closest = 0;
    public String closestPlace ;
    Marker closestLo;
    Marker closestLos; // For remove

    private SpeechActivity threadSpeech = null;

    private boolean  speakerClosest = false ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //mClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).build();
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /*subdragview = (LinearLayout) findViewById(R.id.subdragView);
        subdragview.setBackgroundColor(Color.argb(5,0,0,0));*/
        threadPlace.add(new ThreadPlace(this));




        lv = (ListView) findViewById(R.id.list);

        listee = (TextView) findViewById(R.id.name);
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.custom_cursor);
        searchView.setEllipsize(true);
        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Log.d("checky",query);
                Snackbar.make(findViewById(R.id.container), "Query: " + query, Snackbar.LENGTH_LONG)
                        .show();
                listee.setText(query);
                query = query.replaceAll("\\s","_");
                placeQuery = query;


                if(threadFirstCheck==true)
                {
                    //reset
                    nameOfLo.clear();
                    nameOfL.clear();

                    for(Marker markerx : marker)
                    {
                        markerx.remove();
                    }
                    closestLo.remove();
                    marker.clear();
                   /* threadPlace.get(countThread).interrupt();
                    threadPlace.clear();*/

                    //countThread++;
                    /*threadPlace.add(new ThreadPlace(PlaceActivity.this));
                    threadPlace.get(countThread).start();*/
                    new ThreadPlace(PlaceActivity.this).start();
                }
                else
                {
                    //threadPlace.get(countThread).start();
                    //threadPlace2.start();
                    new ThreadPlace(PlaceActivity.this).start();
                }


                //threadPlace.ThreadPlaceInvoke(query);


                // Toast.makeText(PlaceActivity.this,query,Toast.LENGTH_SHORT).show();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState
                    previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        threadSpeech = new SpeechActivity(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_place, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it
            // could have heard
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            boolean checkMatch = false ;
            for(String toSpeak : matches)
            {
                if(toSpeak.contains("traffic"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("traffic will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this, TrafficMapsActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("weather"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("weather will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this, GlobalActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("places"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("nearby places will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this, PlaceActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("speedometer"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("speedometer will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this, SpeedActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("accident"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("real-time accident will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this, JsOneActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("fuel calculation"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("fuel calculation will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this, SimpleDirectionActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("who are you") || toSpeak.contains("what's your name"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("I am Mixxi the intelligent driving assistant", TextToSpeech.QUEUE_FLUSH, null);
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("hello"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("Sa wad dee Ka", TextToSpeech.QUEUE_FLUSH, null);
                    checkMatch = true;
                    break;
                }
                // Places Zone
                else if((toSpeak.contains("closest") ) && threadFirstCheck == true)
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak(closestPlace + String.valueOf(closest) +"kilometers from here", TextToSpeech.QUEUE_FLUSH, null);
                    checkMatch = true;


                    closestLos = mMap.addMarker(new MarkerOptions()
                            .position(closestLo.getPosition())
                            .title("Nearby Place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                    //Log.d("closest",closestLo.getPosition().toString());






                    break;
                }

                else if(toSpeak.contains("school"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("nearby schools will show", TextToSpeech.QUEUE_FLUSH, null);
                    checkMatch = true;

                    String query = "school";
                    Snackbar.make(findViewById(R.id.container), "Query: " + query, Snackbar.LENGTH_LONG)
                            .show();
                    listee.setText(query);
                    query = query.replaceAll("\\s","_");
                    placeQuery = query;

                    if(threadFirstCheck==true)
                    {
                        //reset
                        nameOfLo.clear();
                        nameOfL.clear();

                        for(Marker markerx : marker)
                        {
                            markerx.remove();
                        }

                        marker.clear();
                        threadPlace.get(countThread).interrupt();
                        threadPlace.clear();

                        //countThread++;
                        threadPlace.add(new ThreadPlace(PlaceActivity.this));
                        threadPlace.get(countThread).start();
                    }
                    else
                    {
                        threadPlace.get(countThread).start();
                    }

                    break;
                }
                else if(toSpeak.contains("police"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    Mixxi.speak("nearby police stations will show", TextToSpeech.QUEUE_FLUSH, null);
                    checkMatch = true;

                    String query = "police";
                    Snackbar.make(findViewById(R.id.container), "Query: " + query, Snackbar.LENGTH_LONG)
                            .show();
                    listee.setText(query);
                    query = query.replaceAll("\\s","_");
                    placeQuery = query;

                    if(threadFirstCheck==true)
                    {
                        //reset
                        nameOfLo.clear();
                        nameOfL.clear();

                        for(Marker markerx : marker)
                        {
                            markerx.remove();
                        }

                        marker.clear();
                        threadPlace.get(countThread).interrupt();
                        threadPlace.clear();

                        //countThread++;
                        threadPlace.add(new ThreadPlace(PlaceActivity.this));
                        threadPlace.get(countThread).start();
                    }
                    else
                    {
                        threadPlace.get(countThread).start();
                    }

                    break;
                }



            }
            if(checkMatch == false)
            {
                Log.d("Speak",matches.get(0).toString());
                Mixxi.speak("I don't know what you said", TextToSpeech.QUEUE_FLUSH, null);
            }

            // matches is the result of voice input. It is a list of what the
            // user possibly said.
            // Using an if statement for the keyword you want to use allows the
            // use of any activity if keywords match
            // it is possible to set up multiple keywords to use the same
            // activity so more than one word will allow the user
            // to use the activity (makes it so the user doesn't have to
            // memorize words from a list)
            // to use an activity from the voice input information simply use
            // the following format;
            // if (matches.contains("keyword here") { startActivity(new
            // Intent("name.of.manifest.ACTIVITY")


        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void speakerSearch(String nameSearch)
    {
     //   Mixxi.speak("nearby schools will show", TextToSpeech.QUEUE_FLUSH, null);

        threadSpeech.checkMixxi = false;
        threadSpeech.checkFirstEntry = false ;
        threadSpeech.dialog.cancel();

        String query = nameSearch;
        Snackbar.make(findViewById(R.id.container), "Query: " + query, Snackbar.LENGTH_LONG)
                .show();
        listee.setText(query);
        query = query.replaceAll("\\s","_");
        placeQuery = query;

        if(threadFirstCheck==true)
        {
            //reset
            nameOfLo.clear();
            nameOfL.clear();

            for(Marker markerx : marker)
            {
                markerx.remove();
            }

            marker.clear();
            new ThreadPlace(PlaceActivity.this).start();
        }
        else
        {
            new ThreadPlace(PlaceActivity.this).start();
        }
    }
    public void speakerClosest()
    {
        threadSpeech.checkMixxi = false;
        threadSpeech.checkFirstEntry = false ;
        threadSpeech.dialog.cancel();
        speakerClosest = true ;
//        Mixxi.speak(closestPlace + String.valueOf(closest) +"kilometers from here", TextToSpeech.QUEUE_FLUSH, null);



        closestLos = mMap.addMarker(new MarkerOptions()
                .position(closestLo.getPosition())
                .title("Nearby Place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

    }

    @Override
    public void onBackPressed() {
        boolean mLayoutCheck = false;
        boolean searchViewCheck = false ;
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayoutCheck = true;
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        }
        if (searchView.isSearchOpen()) {
            searchViewCheck = true;
            searchView.closeSearch();
        }
        else if(mLayoutCheck == false && searchViewCheck == false) {
            Mixxi.stop();
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
            super.onBackPressed();
        }


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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("checky", "onMapReady");
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        //buildGoogleApiClient();
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
        mMap.setMyLocationEnabled(true);
    }
    //Initialize Google Play Services

  /*  protected synchronized void buildGoogleApiClient() {
        Log.d("checky", "buildGoogleApiClient");
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mClient.connect();
    }*/

    @Override
    public void onConnected(Bundle bundle) {


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
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
        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("checky","onConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("checky","onConnectionFailed");

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }



        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("checky","LocationChanged"+mLastLocation.getLatitude());

        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);*/

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }
        if(locationChangedCheck == false)
        {
            locationChangedCheck = true;
            //threadPlace.get(countThread).start();

        }

    }

    public void threadPlaceProcess(ArrayList<ContentValues> arr_cv , final ArrayList<Double> dist, final ArrayList<Bitmap> bitMapList )
    {
        // closest method
        int minIndexing ;
        minIndexing = dist.indexOf(Collections.min(dist));
        closest = dist.get(minIndexing);

        final List<String> addressOfLo = new ArrayList<String>();
        final List<String>  distance = new ArrayList<>();
        if(arr_cv!=null){
            for(int i = 0 ; i < arr_cv.size();i++)
            {
                String name = arr_cv.get(i).getAsString(ThreadPlace.NAME);
                String lat = arr_cv.get(i).getAsString(ThreadPlace.LATITUDE);
                String lon = arr_cv.get(i).getAsString(ThreadPlace.LONGITUDE);
                String address = arr_cv.get(i).getAsString(ThreadPlace.ADDRESS);


                Double lat_d = Double.parseDouble(lat);
                Double lon_d = Double.parseDouble(lon);

                LatLng latLng = new LatLng(lat_d,lon_d);

                //Log.d("latlong","Lat: " + lat + "Lon: " + lon);



                if(dist.get(i)==-1)
                {
                    distance.add("N/A");
                }
                else
                {
                    distance.add(dist.get(i).toString());
                }
                nameOfL.add(name);

                if(dist.get(i)>30)
                {
                    nameOfLo.add(name+"  "+distance.get(i).toString()+" m.");
                }
                else
                {
                    nameOfLo.add(name+"  "+distance.get(i).toString()+" km.");
                }

                addressOfLo.add(address);

                if(placeQuery.contains("food"))
                {
                    if(dist.get(i)>30)
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" m.").icon(BitmapDescriptorFactory.fromResource(R.drawable.food_32))));
                    }
                    else
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" km.").icon(BitmapDescriptorFactory.fromResource(R.drawable.food_32))));
                    }
                }

                else if(placeQuery.contains("gas_station"))
                {
                    if(dist.get(i)>30)
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" m.").icon(BitmapDescriptorFactory.fromResource(R.drawable.gas_station_32))));
                    }
                    else
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" km.").icon(BitmapDescriptorFactory.fromResource(R.drawable.gas_station_32))));
                    }
                }

                else if(placeQuery.contains("police"))
                {
                    if(dist.get(i)>30)
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" m.").icon(BitmapDescriptorFactory.fromResource(R.drawable.police_32))));
                    }
                    else
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" km.").icon(BitmapDescriptorFactory.fromResource(R.drawable.police_32))));
                    }
                }

                else if(placeQuery.contains("school"))
                {
                    if(dist.get(i)>30)
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" m.").icon(BitmapDescriptorFactory.fromResource(R.drawable.school_32))));
                    }
                    else
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" km.").icon(BitmapDescriptorFactory.fromResource(R.drawable.school_32))));
                    }
                }

                else if(placeQuery.contains("shopping_mall"))
                {
                    if(dist.get(i)>30)
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" m.").icon(BitmapDescriptorFactory.fromResource(R.drawable.shopping_32))));
                    }
                    else
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" km.").icon(BitmapDescriptorFactory.fromResource(R.drawable.shopping_32))));
                    }
                }

                else
                {
                    if(dist.get(i)>30)
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" m.")));
                    }
                    else
                    {
                        marker.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(distance.get(i)+" km.")));
                    }
                }

            }


            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    nameOfLo );
            lv.setAdapter(arrayAdapter);

            //closest Method
            closestPlace = nameOfL.get(minIndexing);
            closestLo = marker.get(minIndexing);


            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Dialog dialog = new Dialog(PlaceActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.customlayout);
                    dialog.setCancelable(true);

                    TextView textView1 = (TextView)dialog.findViewById(R.id.textView1);
                    textView1.setText("Name: " +nameOfL.get((int)id));
                    TextView textView2 = (TextView)dialog.findViewById(R.id.textView2);
                    textView2.setText("Addres: "+addressOfLo.get((int) id));
                    TextView textView3 = (TextView)dialog.findViewById(R.id.textView3);
                    if(dist.get((int) id)>30)
                    {
                        textView3.setText("Distance: " + distance.get((int) id).toString() + " m.");
                    }
                    else
                    {
                        textView3.setText("Distance: " + distance.get((int) id).toString() + " km.");
                    }
                    mixImage = (ImageView) dialog.findViewById(R.id.mixImage);

//                    Log.d("bitmap",bitMapList.get((int) id).toString());
                    Bitmap bm = bitMapList.get((int) id);

                    mixImage.setImageBitmap(bm);


                    dialog.show();
                    /*String id_str = Long.toString(id);
                    Toast.makeText(PlaceActivity.this, id_str, Toast.LENGTH_SHORT).show();*/
                }
            });

        }
        else
        {
            Toast.makeText(PlaceActivity.this,"Zero_result",Toast.LENGTH_SHORT).show();
        }


            //threadPlace.interrupt();
        if(threadFirstCheck == true)
        {
            if( speakerClosest == true)
                closestLos.remove();
            threadFirstCheck = true ;
        }
        else{
            threadFirstCheck = true ;
        }


        Log.i("threadFirst", String.valueOf(threadFirstCheck));

    }

}