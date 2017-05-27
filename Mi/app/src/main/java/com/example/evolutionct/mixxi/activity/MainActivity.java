package com.example.evolutionct.mixxi.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;

import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.evolutionct.mixxi.fragment.AboutFragment;
import com.example.evolutionct.mixxi.fragment.Wifi3GFragment;
import com.example.evolutionct.mixxi.receivers.MySimpleReceiver;
import com.example.evolutionct.mixxi.service.MySimpleService;
import com.example.evolutionct.mixxi.speechmixxi.SpeechActivity;
import com.example.evolutionct.mixxi.speed.SpeedActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

import com.example.evolutionct.mixxi.R;
import com.example.evolutionct.mixxi.adapter.ViewPagerAdapter;
import com.example.evolutionct.mixxi.fragment.FacebookFragment;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    Dialog dialog;
    BroadcastReceiver broadcastReceiver;
    ViewPagerAdapter viewPagerAdapter;
    private SharedPreferences sharedPreferences;


    public LinearLayout ll,ll2 ;
    public TextView message ;
    private SpeechActivity threadSpeech = null ;

    public MySimpleReceiver receiverForSimple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        checkWifi();
        printKeyHash();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //tabLayout = (TabLayout) findViewById(R.id.tabs);
        //tabLayout.setupWithViewPager(viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                MainActivity.this,
                drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);
        CheckUserPermsions();

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        // switch the fragment when the tab item was clicked
/*        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*/

        //get current tab
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String position = sharedPreferences.getString("tab_opened", null);
       /* if (position == null) {
            viewPager.setCurrentItem(0, true);

            getSupportActionBar().setTitle("Speed");
        } else if (position == "0") {
            viewPager.setCurrentItem(0, true);
            getSupportActionBar().setTitle("Speed");
        } else if (position == "1") {
            viewPager.setCurrentItem(1, true);
            getSupportActionBar().setTitle("Speech");
        } else if (position == "2") {
            viewPager.setCurrentItem(2, true);
            getSupportActionBar().setTitle("MAPS");
        } else if (position == "3") {
            viewPager.setCurrentItem(3, true);
            getSupportActionBar().setTitle("Place");
        } else if (position == "4") {
            viewPager.setCurrentItem(4, true);
        } else if (position == "5") {
            viewPager.setCurrentItem(5, true);
        } else if (position == "6") {
            viewPager.setCurrentItem(6, true);
        } else if (position == "7") {
            viewPager.setCurrentItem(7, true);
        }*/
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, MySimpleService.class);
        // Add extras to the bundle
        i.putExtra("foo", "bar");
        i.putExtra("receiver", receiverForSimple);
        // Start the service
        startService(i);
        threadSpeech = new SpeechActivity(this);

        threadSpeech.start();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            super.onBackPressed();
        }

    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public void checkWifi(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo m3g = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        NetworkInfo m4g = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (mWifi.isConnected() || m3g.isConnected() || m4g.isConnected()  ) {
            // Do whatever

            Log.i("Wiffi:","connedddd");
        }
        else {
            Log.i("Wiffi:","MIXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            FragmentManager fragmentManager = getFragmentManager();
            Wifi3GFragment wifi3GFragment = new Wifi3GFragment();
            wifi3GFragment.setCancelable(false);
            wifi3GFragment.show(fragmentManager, "Dialog!");
            //onBackPressed();
            /*finish();
            System.exit(0);*/
        }
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash:",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("jk", "Exception(NameNotFoundException) : " + e);
        } catch (NoSuchAlgorithmException e) {
            Log.e("mkm", "Exception(NoSuchAlgorithmException) : " + e);
        }
    }

   /* public void initInstances(){
        fabBtn = (FloatingActionButton) findViewById(R.id.fabBtn);
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 4"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 5"));

    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_main) {
            setupViewPager();
        }
        if (id == R.id.nav_camera) {
            startActivity(new Intent(MainActivity.this, TrafficMapsActivity.class));
            id = R.id.nav_main;
        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(MainActivity.this, Weather2Activity.class));
        } else if (id == R.id.nav_view) {
            startActivity(new Intent(MainActivity.this, PlaceActivity.class));
        } else if (id == R.id.nav_test) {
            startActivity(new Intent(MainActivity.this, SpeedActivity.class));
        } else if (id == R.id.nav_jsone) {
            startActivity(new Intent(MainActivity.this, JsOneActivity.class));
        } else if (id == R.id.nav_fuel) {
            startActivity(new Intent(MainActivity.this, SimpleDirectionActivity.class));
        } else if (id == R.id.nav_share) {
            FragmentManager fragmentManager = getFragmentManager();
            AboutFragment aboutFragment = new AboutFragment();
            aboutFragment.setCancelable(false);
            aboutFragment.show(fragmentManager, "Dialog!");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FacebookFragment(), "Calendar");
        viewPager.setAdapter(adapter);

    }


    //access to permsions
    void CheckUserPermsions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        getLastLocation();// init the contact list

    }

    //get acces to location permsion
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();// init the contact list
                } else {
                    // Permission Denied
                    Toast.makeText(this, "your message", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //Get last location
    public void getLastLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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
        Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null)
        {
    myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    textView.append("\n" +intent.getExtras().get("coordinates"));

                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
       // Log.d("Status", "resume");
        threadSpeech = new SpeechActivity(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("tab_opened", "1");
        editor.commit();

    }
    @Override
    protected  void onPause(){
        super.onPause();
        //Log.d("Status","Status : " + "on Pause");

        threadSpeech.speechDisable = true ;
    }






}

