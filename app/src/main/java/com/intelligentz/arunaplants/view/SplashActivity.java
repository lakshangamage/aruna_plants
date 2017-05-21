package com.intelligentz.arunaplants.view;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.intelligentz.arunaplants.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {
    final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private ImageView logo_image;
    private Context context;
    LocationManager lm;
    boolean network_enabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this;
        logo_image = (ImageView) findViewById(R.id.logo_image);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                checkNetworkAndLocation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        logo_image.startAnimation(fadeInAnimation);
    }

    private void checkNetworkAndLocation(){
            new CheckInternet().execute();
    }

    private void goToNextActivity() {
        SharedPreferences mPrefs = getSharedPreferences("arunaplant.username", Context.MODE_PRIVATE);
        boolean isLogedIn = mPrefs.getBoolean("isLoggedIn", false);
        if (!isLogedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("id", mPrefs.getString("id", null));
            startActivity(intent);
            finish();

        }
    }

    class CheckInternet extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204").openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(5500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0 ) ? "True" : "False";
            } catch (IOException e) {
                return "False";
            }
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url.equals("True")) {
                goToNextActivity();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage(context.getResources().getString(R.string.mobile_network_not_enabled));
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        checkNetworkAndLocation();
                    }
                });
                dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        finish();
                    }
                });
                dialog.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            for (int permisson : grantResults) {
                if (permisson != PackageManager.PERMISSION_GRANTED) {
                    showMessageOKCancel("You need to provide permisson to access Internet to continue.", null);
                    return;
                }
            }
            checkNetworkAndLocation();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SplashActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkNetworkAndLocation();
    }

}
