package com.example.sarpa.autopay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.SearchView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Button btnShowLocation;
    Button btn_card_left;
    Button btn_card_right;
    ImageView iv_card;
    public int card_cnt = 0;

    TextView currentposition;
    TextView tv_card_name;
    TextView tv_card_priority;
    TextView tv_recom_card_name;
    TextView tv_recom_card_value;

    EditText editText;
    GPSTracker gps = null;

    public static Context mContext;
    public static Handler mHandler;
    SearchView searchView;
    public static int RENEW_GPS = 1;
    public static int SEND_PRINT = 2;

    String packName = this.getPackageName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        // 다이얼로그 바디
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
        // 메세지
        alert_confirm.setMessage("GPS 기능을 사용하여 매장을 검색하시겠습니까?");
        // 확인 버튼 리스너
        alert_confirm.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (gps == null) {
                    gps = new GPSTracker(MainActivity.this, mHandler);
                } else {
                    gps.Update();
                }

                // check if GPS enabled
                if (gps.canGetLocation()) {
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "현재 위치 - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });

        currentposition = (TextView)findViewById(R.id.currentposition);
        btn_card_left = (Button)findViewById(R.id.card_left_arrow);
        btn_card_right = (Button)findViewById(R.id.card_right_arrow);
        iv_card = (ImageView)findViewById(R.id.card_img_viewer);
        tv_card_name = (TextView)findViewById(R.id.card_name);
        tv_card_priority = (TextView)findViewById(R.id.card_priority);
        tv_recom_card_name = (TextView)findViewById(R.id.recommend_card_id);
        tv_recom_card_value = (TextView)findViewById(R.id.recommend_card_value);

        final String c_names[] = {"AutoPay", "신한카드 S20 체크", "국민카드 노리 체크", "현대카드 M 체크"};
        final String c_value[] = {"N", "2.50", "2.30", "1.80"};

        //\u002A 할인율 : N % (VAT 비 포함)

        btn_card_left.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (card_cnt > 0) {
                    card_cnt = card_cnt - 1;
                }
                String card_id = "@drawable/card_img"+card_cnt;
                Log.w("left:", card_id);
                int resID = getResources().getIdentifier(card_id, "drawable", getPackageName());
                iv_card.setImageResource(resID);

                tv_card_name.setText(c_names[card_cnt]);
                tv_recom_card_name.setText(c_names[card_cnt]);
                tv_recom_card_value.setText("\u002A 할인율 : " + c_value[card_cnt] + " % (VAT 비 포함)");
                if (card_cnt == 1){
                    tv_card_priority.setText("(주거래) ");
                }
                else{
                    tv_card_priority.setText("");
                }
            }
        });
        btn_card_right.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (card_cnt < 3) {
                    card_cnt = card_cnt + 1;
                }
                String card_id = "@drawable/card_img"+card_cnt;
                Log.w("right:", card_id);
                int resID = getResources().getIdentifier(card_id, "drawable", getPackageName());
                iv_card.setImageResource(resID);

                tv_card_name.setText(c_names[card_cnt]);
                tv_recom_card_name.setText(c_names[card_cnt]);
                tv_recom_card_value.setText("\u002A 할인율 : " + c_value[card_cnt] + " % (VAT 비 포함)");
                if (card_cnt == 1){
                    tv_card_priority.setText("(주거래) ");
                }
                else{
                    tv_card_priority.setText("");
                }
            }
        });

        // 취소버튼
        alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alert = alert_confirm.create();


        // 다이얼로그 타이틀
        alert.setTitle("GPS Search");
        // 다이얼로그 보기
        alert.show();




        searchView=(SearchView)findViewById(R.id.searchView);
        searchView.setQueryHint("목적지 검색");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getBaseContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(getBaseContext(), newText, Toast.LENGTH_LONG).show();
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //////////////////////init////////////////////////
/*
        btnShowLocation = (Button) findViewById(R.id.cGPS);
        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // create class object
                if (gps == null) {
                    gps = new GPSTracker(MainActivity.this, mHandler);
                } else {
                    gps.Update();
                }

                // check if GPS enabled
                if (gps.canGetLocation()) {
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });
*/

        ////////////   Future SAMSUNG PAY   //////////////

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //////////////////////////////////////////////////


        //////////// Open and Close Drawer Bar Action //////////

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //////////////////////////////////////////////////////////


    }

    public void makeNewGpsService() {
        if (gps == null) {
            gps = new GPSTracker(MainActivity.this, mHandler);
        } else {
            gps.Update();
        }

    }

    public void logPrint(String str) {
        editText.append(getTimeStr() + " " + str + "\n");
    }

    public String getTimeStr() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("MM/dd HH:mm:ss");
        return sdfNow.format(date);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    public void change(int num){
        if (num ==1){
            currentposition.setText("울주군 구영리 랑콩뜨레");
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.MyPage) {
            // Handle MyPage item action
            Intent intent = new Intent(this, MyPageActivity.class);
            startActivity(intent);
        } else if (id == R.id.MyCard) {
            // Handle MyCard item action
            Intent intent = new Intent(this, MyCardActivity.class);
            startActivity(intent);
        } else if (id == R.id.EnrollCard) {
            // Handle EnrollCard item action
            Intent intent = new Intent(this, EnrollCardActivity.class);
            startActivity(intent);
        } else if (id == R.id.Map) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        } else if (id == R.id.MonthlyReport) {
            // Handle MonthlyReport item action
            Intent intent = new Intent(this, MonthlyReportActivity.class);
            startActivity(intent);
        } else if (id == R.id.Analysis) {
            // Handle Analysis item action
            Intent intent = new Intent(this, AnalysisActivity.class);
            startActivity(intent);
        } else if (id == R.id.Setting) {
            // Handle Setting item action
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
