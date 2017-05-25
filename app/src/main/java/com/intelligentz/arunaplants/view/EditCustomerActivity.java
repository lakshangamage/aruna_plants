package com.intelligentz.arunaplants.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.intelligentz.arunaplants.R;
import com.intelligentz.arunaplants.constants.Tags;
import com.intelligentz.arunaplants.constants.URL;
import com.intelligentz.arunaplants.parser.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditCustomerActivity extends AppCompatActivity {
    private EditText nic_txt;
    private EditText name_txt;
    private EditText birthday_txt;
    private EditText mobile_txt;
    private EditText address_txt;
    private Button edit_button;
    final Calendar myCalendar = Calendar.getInstance();
    private Context context;
    private String nic;
    private String name;
    private String birthday;
    private String mobile;
    private String address;
    private SweetAlertDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_customer);context = this;
        nic = getIntent().getStringExtra("nic");
        name = getIntent().getStringExtra("name");
        birthday = getIntent().getStringExtra("birthday");
        mobile = getIntent().getStringExtra("mobile");
        address = getIntent().getStringExtra("address");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0);
        nic_txt = (EditText) findViewById(R.id.input_nic);
        nic_txt.setKeyListener(null);
        name_txt = (EditText) findViewById(R.id.input_name);
        birthday_txt = (EditText) findViewById(R.id.input_birthday);
        birthday_txt.setKeyListener(null);
        mobile_txt = (EditText) findViewById(R.id.input_mobile);
        address_txt = (EditText) findViewById(R.id.input_address);
        edit_button = (Button) findViewById(R.id.btn_add_customer);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editCustomer();
            }
        });

        nic_txt.setText(nic);
        name_txt.setText(name);
        birthday_txt.setText(birthday);
        mobile_txt.setText(mobile);
        address_txt.setText(address);
    }

    private void editCustomer(){
        if (validate()){
            new EditCustomer().execute();
        }
    }
    public boolean validate() {
        boolean valid = true;
        name = name_txt.getText().toString();
        mobile = mobile_txt.getText().toString();
        address = address_txt.getText().toString();

        if (name.isEmpty()){
            name_txt.setError("Enter Customer Name");
            valid = false;
        }else if (mobile.isEmpty() || mobile.length() != 10){
            mobile_txt.setError("Mobile format 07xxxxxxxx");
            valid = false;
        }else if (address.isEmpty()){
            address_txt.setError("Enter customer address");
            valid = false;
        }
        return valid;
    }
    class EditCustomer extends AsyncTask<String, String, String> {
        int success;
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            progressDialog.setTitleText("Updating...");
            progressDialog.setContentText("This may take a few seconds...");
            progressDialog.getProgressHelper().setRimColor(R.color.colorPrimary);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag


            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("nic", nic));
                params.add(new BasicNameValuePair("name", name));
                params.add(new BasicNameValuePair("mobile", mobile));
                params.add(new BasicNameValuePair("address", address));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(
                        URL.EDIT_CUSTOMER_URL, "POST", params);

                // check your log for json response
                Log.d("edit customer attempt", json.toString());

                // json success tag
                success = json.getInt(Tags.TAG_SUCCESS);
                return json.getString(Tags.TAG_MESSAGE);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (success == 1) {
                onAddSuccess();
            }else{
                onAddFailed();
            }
        }
    }
    public void onAddSuccess() {
//        _loginButton.setEnabled(true);
        SweetAlertDialog.OnSweetClickListener successListner = new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                progressDialog.dismissWithAnimation();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        };
        progressDialog.setTitleText("Success!")
                .setContentText("Customer Successfully Updated")
                .setConfirmText("OK")
                .setConfirmClickListener(successListner)
                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

    }
    public void onAddFailed() {
        SweetAlertDialog.OnSweetClickListener unsuccessListner = new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                progressDialog.dismissWithAnimation();
            }
        };
        progressDialog.setTitleText("Failed!")
                .setContentText("Customer could not be updated. Please try again")
                .setConfirmText("OK")
                .setConfirmClickListener(unsuccessListner)
                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
