package com.intelligentz.arunaplants.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

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

public class AddCustomerActivity extends AppCompatActivity {
    private EditText nic_txt;
    private EditText name_txt;
    private EditText birthday_txt;
    private EditText mobile_txt;
    private EditText address_txt;
    private Button add_button;
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
        setContentView(R.layout.activity_add_customer);
        context = this;
        nic_txt = (EditText) findViewById(R.id.input_nic);
        name_txt = (EditText) findViewById(R.id.input_name);
        birthday_txt = (EditText) findViewById(R.id.input_birthday);
        mobile_txt = (EditText) findViewById(R.id.input_mobile);
        address_txt = (EditText) findViewById(R.id.input_address);
        add_button = (Button) findViewById(R.id.btn_add_customer);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateBirthdayTxt();
            }

        };
        birthday_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                try {
                    java.lang.reflect.Field[] datePickerDialogFields = datePickerDialog.getClass().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                        if (datePickerDialogField.getName().equals("mDatePicker")) {
                            datePickerDialogField.setAccessible(true);
                            DatePicker datePicker = (DatePicker) datePickerDialogField.get(datePickerDialog);
                            java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                            for (java.lang.reflect.Field datePickerField : datePickerFields) {
                                if ("mYearPicker".equals(datePickerField.getName())
                                        || "mYearSpinner".equals(datePickerField
                                        .getName())) {
                                    datePickerField.setAccessible(true);
                                    Object dayPicker = datePickerField.get(datePicker);
                                    ((View) dayPicker).setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }
                catch (Exception ex) {
                }
                datePickerDialog.show();
            }
        });

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCustomer();
            }
        });
    }

    private void addCustomer(){
        if (validate()){
            new AddCustomer().execute();
        }
    }
    private void updateBirthdayTxt() {
        String myFormat = "dd/MM"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        birthday_txt.setText(sdf.format(myCalendar.getTime()));
    }


    public boolean validate() {
        boolean valid = true;
        nic = nic_txt.getText().toString();
        name = name_txt.getText().toString();
        birthday = birthday_txt.getText().toString();
        mobile = mobile_txt.getText().toString();
        address = address_txt.getText().toString();

        if (nic.isEmpty() || nic.length() < 10 || nic.length() > 13) {
            nic_txt.setError("Enter valid nic");
            valid = false;
        } else if (name.isEmpty()){
            name_txt.setError("Enter Customer Name");
            valid = false;
        } else if (birthday.isEmpty()){
            birthday_txt.setError("Enter Birthday");
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

    class AddCustomer extends AsyncTask<String, String, String> {
        int success;
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
                params.add(new BasicNameValuePair("name", name));
                params.add(new BasicNameValuePair("birthday", birthday));
                params.add(new BasicNameValuePair("mobile", mobile));
                params.add(new BasicNameValuePair("address", address));
                params.add(new BasicNameValuePair("officer_id", MainActivity.id));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(
                        URL.ADD_CUSTOMER_URL, "POST", params);

                // check your log for json response
                Log.d("add customer attempt", json.toString());

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
                .setContentText("Customer Successfully Added")
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
                .setContentText("Customer could not be added. Please try again")
                .setConfirmText("OK")
                .setConfirmClickListener(unsuccessListner)
                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
    }
}
