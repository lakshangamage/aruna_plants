package com.intelligentz.arunaplants.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.intelligentz.arunaplants.R;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    private EditText search_txt;
    View headerView;
    private RecyclerView accountsRecyclerView;
    private RecyclerView.LayoutManager accountslayoutManager;
    private CircularImageView imageView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private TextView officerNameTxt;
    private TextView officerIdTxt;
    private Menu drawerMenu;
    private SubMenu accountMenu;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        configureUI();
        configureDrawer();
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

}
