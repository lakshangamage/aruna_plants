package com.intelligentz.arunaplants.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.intelligentz.arunaplants.R;
import com.intelligentz.arunaplants.adaptor.CustomerRecyclerAdaptor;
import com.intelligentz.arunaplants.constants.URL;
import com.intelligentz.arunaplants.model.Customer;
import com.intelligentz.arunaplants.parser.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.intelligentz.arunaplants.constants.Tags.TAG_MESSAGE;
import static com.intelligentz.arunaplants.constants.Tags.TAG_SUCCESS;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

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
    private Menu drawerMenu;
    private SubMenu accountMenu;
    private Context context;
    private SweetAlertDialog progressDialog;
    public static String id;
    ArrayList<Customer> customerList;
    private CustomerRecyclerAdaptor customerRecyclerAdaptor;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        activity = this;
        configureUI();
        configureDrawer();
        configureRecyclerView();
        new SearchCustomers().execute();
    }

    private void configureUI(){
        search_txt = (EditText) findViewById(R.id.search_txt);
        search_txt.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Drawable img = context.getResources().getDrawable(
                                R.drawable.search_icon);
                        img.setBounds(0, 0, 60, 60);

                        search_txt.setCompoundDrawables(img, null, null, null);
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
        String type = mPrefs.getString("type","");

        officerIdTxt.setText(id);
        officerNameTxt.setText(name);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }


    public void jumptoaddcustomer(MenuItem item) {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }
    public void logout(MenuItem item) {
        finish();
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
                    JSONArray users = json.getJSONArray("users");
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
            customerRecyclerAdaptor = new CustomerRecyclerAdaptor(customerList,context, activity);
            customerRecyclerView.setAdapter(customerRecyclerAdaptor);
            customerRecyclerView.setNestedScrollingEnabled(true);
        }
    }
}
