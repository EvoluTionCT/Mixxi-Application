
package com.example.evolutionct.mixxi.facebook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.evolutionct.mixxi.R;

public class LoginActivity extends AppCompatActivity {

    private com.facebook.login.widget.LoginButton loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_header_main);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.drawerLayout);


            if (fragment == null) {
                fragment = new FacebookFragment();
                fm.beginTransaction()
                        .add(R.id.drawerLayout, fragment)
                        .commit();
            }

    }
}
