package com.example.evolutionct.mixxi.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.evolutionct.mixxi.R;

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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.example.evolutionct.mixxi.R.id.map_jsone;
import static com.example.evolutionct.mixxi.fragment.FacebookFragment.VOICE_RECOGNITION_REQUEST_CODE;

public class JsOneActivity extends ActionBarActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient mClient;
    final int PLACE_PICKER_REQUEST = 1000;
    Toolbar toolbar;

    List<LatLng> sanamluang = new ArrayList<LatLng>();
    twitter4j.Twitter twitter;
    List<android.location.Address> addressList = null;
    List<String> roadList = new ArrayList<String>();
    List<String> accList = new ArrayList<String>();
    List<String> accLo = new ArrayList<String>();
    ArrayList<Double> dist = new ArrayList<Double>();
    private GoogleMap mMap;
    private Firebase mRef;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private ThreadActivity threadActivity;

    // Special 2
    List<String> tweetList2;
    List<String> accLoDist  = new ArrayList<String>();
    List<String> tweetInfo  = new ArrayList<String>();
    TextToSpeech Mixxi;
    //private static ArrayList<Activity> activities=new ArrayList<Activity>();

    ListView lv;
    boolean threadFirstCheck = false;
    private SlidingUpPanelLayout mLayout;

    private static final String TAG = "DemoActivity";

    private SpeechActivity threadSpeech ;


    ImageButton speakButton;
    ArrayList<String> matches = new ArrayList<String>();

    public List<Double> indexOfCloseAcc = new ArrayList<Double>();
    public int minIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        toolbar = (Toolbar) findViewById(R.id.toolbar_js100);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map_jsone);
        mapFragment.getMapAsync(this);


        Mixxi =new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    Mixxi.setLanguage(Locale.UK);
                }
            }
        });


        lv = (ListView) findViewById(R.id.list);

        Firebase.setAndroidContext(this);
        threadActivity = new ThreadActivity(this);
        mRef = new Firebase("https://realtimeacc-36a0a.firebaseio.com/0/road/");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String value = child.getValue(String.class);
                    roadList.add(value);
                }

                if(threadFirstCheck==false)
                threadActivity.start();
                else
                {
                    //reset
                    sanamluang.clear();
                    tweetList2.clear();
                    accList.clear();
                    accLo.clear();
                    accLoDist.clear();
                    tweetInfo.clear();
                    threadActivity.start();
                }
                //new MyTask().execute();

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Recognizing Speech...");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
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
    protected  void onPause()
    {
        super.onPause();
        threadSpeech.speechDisable = true ;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
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

    //function ไว้ให้ Thread ทำการ Update txtNumber
    public void threadProcess(List<String> tweetList) {
        tweetList2 = tweetList;

        if(tweetList2.size()>0)
        {
            new MyTask().execute();
           // Log.d("Tweet2",tweetList2.get(0));
        }
        else
        {
            Mixxi.speak("There is no any accident occurred ", TextToSpeech.QUEUE_FLUSH, null);
        }



        //threadActivity.requestStop();
        //txtNumber.setText(tweetList.get(0));


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("click","clickkk");
        Toast.makeText(getBaseContext(),"click",Toast.LENGTH_SHORT);
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
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

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);*/

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }
    }
    /*private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            Marker mMarker = mMap.addMarker(new MarkerOptions().position(loc));
            if(mMap != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
        }
    };*/

    public class MyTask extends AsyncTask<Void, Void, Void> {
        Geocoder geocoder = new Geocoder(getBaseContext());
        @Override

        protected Void doInBackground(Void... params) {
            //Log.d("tweetList", tweetList2.get(0));
            int roadto = 0 ;
            for (String st : tweetList2) {
                // Log.d("checkDD", String.valueOf(roadList.size()));
                // Log.d("Tweet",st.getUser().getName()+"---------"+st.getText());

                for (String roadtoNSC : roadList) {
                    //Log.d("checkRoadlist",roadtoNSC);
                    if (st.contains(roadtoNSC)) {
                        try {
                            addressList = geocoder.getFromLocationName(roadtoNSC, 1);
                           // Log.d("checkLo", String.valueOf(addressList.size()));


                            //sanamluang.add(new LatLng(address.getLatitude(), address.getLongitude()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // Log.d("checkLo", addressList.get(0).toString());
                        if (addressList.size() != 0) {
                            android.location.Address address = addressList.get(0);
                            sanamluang.add(new LatLng(address.getLatitude(), address.getLongitude()));
                            String tempSt = "อุบัติเหตุ";
                            accList.add(tempSt);
                            accLo.add(roadtoNSC);


                            double latitude = mLastLocation.getLatitude();
                           // Log.d("lat", String.valueOf(latitude));
                            double longitude = mLastLocation.getLongitude();

                            double tempLat = address.getLatitude();
                            double tempLon = address.getLongitude();
                            StringBuilder stringBuilder = new StringBuilder();
                            // Find distance
                            try {
                                String urls = "https://maps.googleapis.com/maps/api/directions/json?origin=" + latitude + "," + longitude+ "&destination=" + tempLat  + "," + tempLon+ "&key=AIzaSyAOB0vXFj0IFinCqP-VERrZI-yDRgAf8ck";
                                HttpPost httppost = new HttpPost(urls);

                                HttpClient client = new DefaultHttpClient();
                                HttpResponse response;
                                stringBuilder = new StringBuilder();
                                Log.d("Post",httppost.toString());

                                response = client.execute(httppost);
                                HttpEntity entity = response.getEntity();
                                InputStream stream = entity.getContent();
                                int b;
                                while ((b = stream.read()) != -1) {
                                    stringBuilder.append((char) b);
                                }
                            } catch (ClientProtocolException e) {
                                Log.i("Distance", "Error1");
                            } catch (IOException e) {
                                Log.i("Distance", "Error2");
                            }
                            JSONObject jsonObject = new JSONObject();
                            try {

                                jsonObject = new JSONObject(stringBuilder.toString());

                                JSONArray array = jsonObject.getJSONArray("routes");

                                JSONObject routes = array.getJSONObject(0);

                                JSONArray legs = routes.getJSONArray("legs");

                                JSONObject steps = legs.getJSONObject(0);

                                JSONObject distance = steps.getJSONObject("distance");

                                //Log.i("Distance", distance.toString() + arr_cv.get(i).getAsString(NAME) + nl1.getLength() +" " + i);
                                dist.add(Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]","") ));
                                //Log.i("Distance", dist.get(roadto).toString()+accLo.get(roadto)   );

                                    accLoDist.add(accLo.get(roadto)+"  " + dist.get(roadto) + " km.");

                                tweetInfo.add(st);

                                roadto++;





                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                dist.add(-1.00);
                                Log.i("Distance", "Error3" );
                                e.printStackTrace();

                            }
                            //break;

                        }

                    }


                }

            }
            Log.d("checkDDS", String.valueOf(sanamluang.size()));



            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            super.onPostExecute(avoid);

            sss();



            // Add a marker in Sydney and move the camera
           /* LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,13));*/



        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Add a marker in Sydney and move the camera
           /* LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,13));*/



        }
    }

    public void nearbyAcc()
    {
        if(indexOfCloseAcc.size()!=0)
        {
            Mixxi.speak("Accident occurs in"+indexOfCloseAcc.get(minIndex).toString()+"kilometers from here", TextToSpeech.QUEUE_FLUSH, null);
        }
        else
        {
            Mixxi.speak("There is no any accident occurred around you", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void sss() {
        int countAcc = 0;

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,android.R.layout.simple_list_item_1,
                accLoDist);
        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog dialog = new Dialog(JsOneActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.customlayout_maps);
                dialog.setCancelable(true);

                TextView tweet = (TextView)dialog.findViewById(R.id.Tweet);
                tweet.setText("รายละเอียด: " +tweetInfo.get((int) id));
                Mixxi.speak(tweetInfo.get((int) id), TextToSpeech.QUEUE_FLUSH, null);





                dialog.show();
                    /*String id_str = Long.toString(id);
                    Toast.makeText(PlaceActivity.this, id_str, Toast.LENGTH_SHORT).show();*/
            }
        });
        double min = 0;
        double max = 0;

        for (LatLng accPlace : sanamluang) {
            mMap.addMarker(new MarkerOptions().position(accPlace).title(accLo.get(countAcc)).snippet(dist.get(countAcc)+" km."+" "+accList.get(countAcc)).icon(BitmapDescriptorFactory.fromResource(R.drawable.accidenticon_s)));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(accPlace, 13));
            if(dist.get(countAcc) < 10 )
            {
                indexOfCloseAcc.add(dist.get(countAcc));
            }
            Log.d("indexOfCloseAcc",indexOfCloseAcc.toString());
            countAcc++;

        }

        if(indexOfCloseAcc.size()!=0)
        {
            minIndex = indexOfCloseAcc.indexOf(Collections.min(indexOfCloseAcc));

            Mixxi.speak("Accident occurs in"+indexOfCloseAcc.get(minIndex).toString()+"kilometers from here", TextToSpeech.QUEUE_FLUSH, null);
        }


        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(15.089011, 100.200188),5));
        //reset
        threadFirstCheck=true;
    }

    @Override
    public void onBackPressed() {

        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {

            Mixxi.stop();
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        }

        else {
            Mixxi.stop();
            finish();

            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
            super.onBackPressed();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadActivity.requestStop();
        Log.d("dddddd","KUYYYYYY");
        finish();

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
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
}
