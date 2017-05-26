package com.intelligentz.arunaplants.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.intelligentz.arunaplants.R;
import com.intelligentz.arunaplants.adaptor.CustomerRecyclerAdaptor;
import com.intelligentz.arunaplants.constants.Tags;
import com.intelligentz.arunaplants.constants.URL;
import com.intelligentz.arunaplants.model.Customer;
import com.intelligentz.arunaplants.parser.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.intelligentz.arunaplants.constants.Tags.TAG_MESSAGE;
import static com.intelligentz.arunaplants.constants.Tags.TAG_SUCCESS;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {
    private static final int ADD_CUSTOMER_REQUEST_CODE = 123;
    private String username;
    private String password;
    public static String id;
    public static String type;
    int success;
    int previousLength = 0;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    private EditText search_txt;
    View headerView;
    private RecyclerView customerRecyclerView;
    private RecyclerView.LayoutManager customerlayoutManager;
    private CircularImageView imageView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private TextView officerNameTxt;
    private TextView officerIdTxt;
    private Context context;
    private SweetAlertDialog progressDialog;
    private SweetAlertDialog loginprogressDialog;
    ArrayList<Customer> customerList;
    private CustomerRecyclerAdaptor customerRecyclerAdaptor;
    private Activity activity;
    final Calendar myCalendar = Calendar.getInstance();
    private TextView searchText;
    private ArrayList<Customer> searchCustomerList;
    private SwipeRefreshLayout swipeContainer;
    private Button addCustomerButton;

    private Customer collectingCustomer;
    private double paymeentAmount;
    private String paymentDate;
    private String transId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        activity = this;
        configureUI();
        configureDrawer();
        configureRecyclerView();
        configureSearchText();
        configureSwipeLayout();
        new SearchCustomers().execute();
        new AttemptLogin().execute();
    }
    public void collectPayment(int position){
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_make_payment);
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        TextView name_txt = (TextView) dialog.findViewById(R.id.name_txt);
        TextView nic_txt = (TextView) dialog.findViewById(R.id.nic_txt);
        TextView mobile_txt = (TextView) dialog.findViewById(R.id.mobile_txt);
        TextView address_txt = (TextView) dialog.findViewById(R.id.address_txt);
        final EditText date_txt = (EditText) dialog.findViewById(R.id.input_date);
        final EditText trans_id_txt = (EditText) dialog.findViewById(R.id.input_trans_id);
        final EditText amount_txt = (EditText) dialog.findViewById(R.id.input_amount);
        ImageView cancel_button = (ImageView) dialog.findViewById(R.id.cancel_btn);
        Button confirm_button = (Button) dialog.findViewById(R.id.confirm_btn);

        updateDateTxt(date_txt);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateTxt(date_txt);
            }
        };
        date_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        final Customer customer = searchCustomerList.get(position);
        name_txt.setText(customer.getName());
        nic_txt.setText(customer.getNic());
        mobile_txt.setText(customer.getMobile());
        address_txt.setText(customer.getAddress());

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        confirm_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String amount = amount_txt.getText().toString();
                String date = date_txt.getText().toString();
                String trans_id = trans_id_txt.getText().toString();

                if (amount.isEmpty()){
                    amount_txt.setError("Enter payment amount");
                    return;
                } else if(date.isEmpty()){
                    date_txt.setError("Enter date");
                    return;
                } else if(trans_id.isEmpty()){
                    trans_id_txt.setError("Enter transaction Id");
                    return;
                }

                paymeentAmount = Double.parseDouble(amount);

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date payDate;
                try {
                    payDate = sdf.parse(date);
                    paymentDate = df.format(payDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                transId = trans_id;
                collectingCustomer = customer;

                new MakePayment().execute();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    public void updateDateTxt(EditText date_txt){
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        date_txt.setText(sdf.format(myCalendar.getTime()));
    }
    private void configureUI(){
        search_txt = (EditText) findViewById(R.id.search_txt);
        search_txt.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        DisplayMetrics dm = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(dm);
                        float density = dm.density;
                        Drawable img = context.getResources().getDrawable(
                                R.drawable.search_icon);
                        img.setBounds(0, 0, Math.round(18*density), Math.round(18*density));

                        search_txt.setCompoundDrawables(img, null, null, null);
                    }
                });
        addCustomerButton = (Button) findViewById(R.id.btn_add_customer);
        addCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumptoaddcustomer(null);
            }
        });
    }
    private void configureRecyclerView(){
        customerRecyclerView = (RecyclerView) findViewById(R.id.customers_recycler_view);
        customerlayoutManager = new LinearLayoutManager(this);
        customerRecyclerView.setLayoutManager(customerlayoutManager);
    }
    public void configureDrawer(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navogation_view);
        headerView = navigationView.inflateHeaderView(R.layout.header_drawer);
        imageView = (CircularImageView) headerView.findViewById(R.id.headerimage);
        officerNameTxt = (TextView) headerView.findViewById(R.id.nameTxt);
        officerIdTxt = (TextView) headerView.findViewById(R.id.idTxt);
        navigationView.setItemIconTintList(null);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        SharedPreferences mPrefs = getSharedPreferences("arunaplant.username", Context.MODE_PRIVATE);
        boolean isLogedIn = mPrefs.getBoolean("isLoggedIn", false);
        id = mPrefs.getString("id","");
        String name = mPrefs.getString("name","");
        type = mPrefs.getString("type","");

        officerIdTxt.setText(id);
        officerNameTxt.setText(name);
    }
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }
    public void jumptoaddcustomer(MenuItem item) {
        Intent intent = new Intent(this, AddCustomerActivity.class);
        startActivityForResult(intent, ADD_CUSTOMER_REQUEST_CODE);
    }
    public void logout(MenuItem item) {
        SharedPreferences prefs = getSharedPreferences(
                "arunaplant.username", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.commit();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void editCustomer(int position) {
        Customer customer = searchCustomerList.get(position);
        Intent intent = new Intent(MainActivity.this, EditCustomerActivity.class);
        intent.putExtra("nic",customer.getNic());
        intent.putExtra("name",customer.getName());
        intent.putExtra("birthday",customer.getBirthday());
        intent.putExtra("mobile",customer.getMobile());
        intent.putExtra("address",customer.getAddress());
        startActivityForResult(intent, ADD_CUSTOMER_REQUEST_CODE);
    }

    class SearchCustomers extends AsyncTask<String, String, String> {

        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            progressDialog.setTitleText("Searching Customers");
            progressDialog.setContentText("This may take a few seconds...");
            progressDialog.getProgressHelper().setRimColor(R.color.colorPrimary);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("id", id));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(
                        URL.SEARCH_CUSTOMER_URL, "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);

                customerList = new ArrayList<>();
                if (success == 1) {
                    JSONArray users = json.getJSONArray("customers");
                    Customer customer = null;
                    for (int i = 0; i< users.length();i++){
                        String nic = ((JSONObject)(users.get(i))).getString("nic");
                        String name = ((JSONObject)(users.get(i))).getString("name");
                        String address = ((JSONObject)(users.get(i))).getString("address");
                        String birthday = ((JSONObject)(users.get(i))).getString("birthday");
                        String mobile = ((JSONObject)(users.get(i))).getString("mobile");
                        String officer_id = ((JSONObject)(users.get(i))).getString("officer_id");
                        String officer_name = ((JSONObject)(users.get(i))).getString("officer_name");
                        customer = new Customer(officer_id,officer_name,nic,name,address,birthday,mobile);
                        customerList.add(customer);
                    }
                    return String.valueOf(json.getInt(TAG_SUCCESS));
                }else{
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    //Toast.makeText(Login.this, "Invalid login details", Toast.LENGTH_LONG).show();
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            if (file_url.equals("0")){
                Toast.makeText(context, "No Customers Found", Toast.LENGTH_LONG).show();
            }
            searchCustomerList = new ArrayList<>();
            searchCustomerList.addAll(customerList);
            customerRecyclerAdaptor = new CustomerRecyclerAdaptor(searchCustomerList,context, activity);
            customerRecyclerView.setAdapter(customerRecyclerAdaptor);
            customerRecyclerView.setNestedScrollingEnabled(true);
            progressDialog.dismissWithAnimation();
        }
    }
    class SearchCustomersNoProgress extends AsyncTask<String, String, String> {

        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("id", id));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(
                        URL.SEARCH_CUSTOMER_URL, "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);

                customerList = new ArrayList<>();
                if (success == 1) {
                    JSONArray users = json.getJSONArray("customers");
                    Customer customer = null;
                    for (int i = 0; i< users.length();i++){
                        String nic = ((JSONObject)(users.get(i))).getString("nic");
                        String name = ((JSONObject)(users.get(i))).getString("name");
                        String address = ((JSONObject)(users.get(i))).getString("address");
                        String birthday = ((JSONObject)(users.get(i))).getString("birthday");
                        String mobile = ((JSONObject)(users.get(i))).getString("mobile");
                        String officer_id = ((JSONObject)(users.get(i))).getString("officer_id");
                        String officer_name = ((JSONObject)(users.get(i))).getString("officer_name");
                        customer = new Customer(officer_id,officer_name,nic,name,address,birthday,mobile);
                        customerList.add(customer);
                    }
                    return String.valueOf(json.getInt(TAG_SUCCESS));
                }else{
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    //Toast.makeText(Login.this, "Invalid login details", Toast.LENGTH_LONG).show();
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            if (file_url.equals("0")){
                Toast.makeText(context, "No Customers Found", Toast.LENGTH_LONG).show();
            }
            searchCustomerList = new ArrayList<>();
            searchCustomerList.addAll(customerList);
            customerRecyclerAdaptor = new CustomerRecyclerAdaptor(searchCustomerList,context, activity);
            customerRecyclerView.setAdapter(customerRecyclerAdaptor);
            customerRecyclerView.setNestedScrollingEnabled(true);
            if (swipeContainer != null && swipeContainer.isRefreshing()){
                swipeContainer.setRefreshing(false);
            }
            previousLength = 0;
            search_txt.setText("");
        }
    }
    class MakePayment extends AsyncTask<String, String, String> {

        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            progressDialog.setTitleText("Completing...");
            progressDialog.setContentText("This may take a few seconds.");
            progressDialog.getProgressHelper().setRimColor(R.color.colorPrimary);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("nic", collectingCustomer.getNic()));
                params.add(new BasicNameValuePair("date", paymentDate));
                params.add(new BasicNameValuePair("trans_id", transId));
                params.add(new BasicNameValuePair("amount", String.valueOf(paymeentAmount)));
                params.add(new BasicNameValuePair("officer_id", MainActivity.id));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(
                        URL.PAYMENT_URL, "POST", params);

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                return json.getString(TAG_MESSAGE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            if (success == 1) {
                SweetAlertDialog.OnSweetClickListener successListner = new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        progressDialog.dismissWithAnimation();
                    }
                };
                progressDialog.setTitleText("Success!")
                        .setContentText("Payment Completed Successfully")
                        .setConfirmText("OK")
                        .setConfirmClickListener(successListner)
                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

            }else{
                SweetAlertDialog.OnSweetClickListener unsuccessListner = new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        progressDialog.dismissWithAnimation();
                    }
                };
                progressDialog.setTitleText("Failed!")
                        .setContentText("Payment Could not be completed. Please try again")
                        .setConfirmText("OK")
                        .setConfirmClickListener(unsuccessListner)
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
            }
        }
    }
    private void configureSearchText(){
        searchText = (EditText) findViewById(R.id.search_txt);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                charSequence = charSequence.toString().toLowerCase();
                if (charSequence.length() > 0){
                    if (charSequence.length() > previousLength){
                        previousLength++;
                        for (Customer customer: new ArrayList<>(searchCustomerList)){
                            if (!customer.getName().toLowerCase().contains(charSequence) && !customer.getNic().toLowerCase().contains(charSequence)){
                                searchCustomerList.remove(customer);
                            }
                        }
                    }else {
                        if (previousLength!=0)
                            previousLength--;
                        searchCustomerList = new ArrayList<Customer>();
                        for (Customer customer: customerList){
                            if (customer.getName().toLowerCase().contains(charSequence) || customer.getNic().toLowerCase().contains(charSequence) ){
                                searchCustomerList.add(customer);
                            }
                        }
                    }

                }else {
                    searchCustomerList = new ArrayList<Customer>();
                    searchCustomerList.addAll(customerList);
                }
                customerRecyclerAdaptor = new CustomerRecyclerAdaptor(searchCustomerList,context, activity);
                customerRecyclerView.setAdapter(customerRecyclerAdaptor);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    public void configureSwipeLayout(){
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }
    public void fetchTimelineAsync() {
        new SearchCustomersNoProgress().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new SearchCustomersNoProgress().execute();
    }
    class AttemptLogin extends AsyncTask<String, String, String> {

        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences mPrefs = getSharedPreferences("arunaplant.username", Context.MODE_PRIVATE);
            username = mPrefs.getString("id", "none");
            password = mPrefs.getString("password", "none");
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

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONParser jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(
                        URL.LOGIN_URL, "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

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
            //progressDialog.dismissWithAnimation();
//            if (file_url == ""){
//                Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_LONG).show();
//            }
            if (success == 2){
                onLoginFailed();
            }
        }
    }

    private void onLoginFailed(){
        loginprogressDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        SweetAlertDialog.OnSweetClickListener successListner = new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                loginprogressDialog.dismissWithAnimation();
                logout(null);
            }
        };
        loginprogressDialog.setTitleText("Password Changed!")
                .setContentText("Your account password has reset. You will be logged out.")
                .setConfirmText("OK")
                .setConfirmClickListener(successListner)
                .setCancelable(false);
        loginprogressDialog.show();
    }
}
