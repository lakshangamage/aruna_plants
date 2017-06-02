package com.intelligentz.arunaplants.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.intelligentz.arunaplants.BuildConfig;
import com.intelligentz.arunaplants.R;
import com.intelligentz.arunaplants.constants.Tags;
import com.intelligentz.arunaplants.constants.URL;
import com.intelligentz.arunaplants.parser.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    int success;
    private int version;
    private static final int REQUEST_SIGNUP = 0;
    private SweetAlertDialog progressDialog;
    private Context context;
    private String username;
    private String password;
    private EditText usernameText;
    private EditText passwordText;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        usernameText = (EditText) findViewById(R.id.input_username);
        passwordText = (EditText) findViewById(R.id.input_password);
        loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            //onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);
        username = usernameText.getText().toString();
        password = passwordText.getText().toString();

        new AttemptLogin().execute();
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
//        _loginButton.setEnabled(true);
        progressDialog.dismissWithAnimation();
        finish();
    }

    public void onLoginFailed() {
        SweetAlertDialog.OnSweetClickListener successListner = new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                progressDialog.dismissWithAnimation();
                loginButton.setEnabled(true);
            }
        };
        progressDialog.setTitleText("Failed!")
            .setContentText("Invalid username or password.")
            .setConfirmText("OK")
            .setConfirmClickListener(successListner)
            .changeAlertType(SweetAlertDialog.ERROR_TYPE);

    }

    public void onVersionFailed() {
        SweetAlertDialog.OnSweetClickListener successListner = new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                progressDialog.dismissWithAnimation();
                loginButton.setEnabled(true);
                Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse("market://details?id=com.intelligentz.arunaplants"));
                startActivity(intent);
                finish();
            }
        };
        progressDialog.setTitleText("New Version Available!")
            .setContentText("A new version of the app is available. Please update to continue.")
            .setConfirmText("OK")
            .setConfirmClickListener(successListner)
            .changeAlertType(SweetAlertDialog.WARNING_TYPE);

    }

    public boolean validate() {
        boolean valid = true;

        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        if (password.isEmpty() ) {
            passwordText.setError("Enter passsword");
            valid = false;
        } else if (username.isEmpty()){
            usernameText.setError("Enter Username");
        }

        return valid;
    }

    class AttemptLogin extends AsyncTask<String, String, String> {

        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            progressDialog.setTitleText("Logging In");
            progressDialog.setContentText("This may take a few seconds...");
            progressDialog.getProgressHelper().setRimColor(R.color.colorPrimary);
            progressDialog.setCancelable(false);
            progressDialog.show();
            version = BuildConfig.VERSION_CODE;

        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("version", String.valueOf(version)));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(
                        URL.LOGIN_URL, "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(Tags.TAG_SUCCESS);
                JSONObject user = json.getJSONObject("user");
                String id = user.getString("id");
                String name = user.getString("name");
                String type = user.getString("type");

                if (success == 1) {
                    Log.d("Login Successful!", json.toString());
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.putExtra("id",id);
                    i.putExtra("name", name);
                    i.putExtra("type", type);
                    SharedPreferences prefs = getSharedPreferences(
                            "arunaplant.username", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("id",id);
                    editor.putString("password",password);
                    editor.putString("name", name);
                    editor.putString("type", type);
                    editor.commit();
                    startActivity(i);

                }else{

                    Log.d("Login Failure!", json.getString(Tags.TAG_MESSAGE));
                    //onLoginFailed();
                    //Toast.makeText(Login.this, "Invalid login details", Toast.LENGTH_LONG).show();

                }
                return json.getString(Tags.TAG_MESSAGE);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            //progressDialog.dismissWithAnimation();
//            if (file_url == ""){
//                Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_LONG).show();
//            }
            if (success == 3){
                onVersionFailed();
            }else if (success != 1){
                onLoginFailed();
            } else {
                onLoginSuccess();
            }
        }
    }
}
