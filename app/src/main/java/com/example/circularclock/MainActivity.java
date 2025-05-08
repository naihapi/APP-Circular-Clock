package com.example.circularclock;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private int sysBar_Top;//系统状态栏边距
    private View MyTop_Module;//最顶部的组件
    private TextView Maintitle;//“圆色时钟”标题
    private View githubButton;//github图标按钮
    private View deviceCard;//设备连接状态卡片
    private View permissionCard;//权限状态卡片
    private TextView permissionCard_Text;//权限状态卡片下的文本信息
    private View wifiCard;//wifi状态卡片
    private TextView wifiCard_Text;//wifi状态卡片下的文本信息
    private View mode1Card;//功能1卡片
    private String GithubURL = "https://github.com/naihapi/APP-Circular-Clock";//github地址
    private ExecutorService threadpool = Executors.newFixedThreadPool(3);//新建3个线程

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //获取id
        MyTop_Module = findViewById(R.id.TopModule);
        githubButton = findViewById(R.id.github);
        deviceCard = findViewById(R.id.deviceState);
        permissionCard = findViewById(R.id.permissionState);
        permissionCard_Text = findViewById(R.id.permissionState_text);
        wifiCard = findViewById(R.id.wifiState);
        wifiCard_Text = findViewById(R.id.wifiState_Text);
        mode1Card = findViewById(R.id.mode1);
        Maintitle = findViewById(R.id.title);

        //设置点击监听器
        githubButton.setOnClickListener(this);
        deviceCard.setOnClickListener(this);
        permissionCard.setOnClickListener(this);
        wifiCard.setOnClickListener(this);
        mode1Card.setOnClickListener(this);
        Maintitle.setOnClickListener(this);

        //处理系统状态栏边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            sysBar_Top = systemBars.top;//获取系统状态栏的上边距
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) MyTop_Module.getLayoutParams();//获取组件的布局参数
            params.topMargin = sysBar_Top;//设置上外边距布局参数
            MyTop_Module.setLayoutParams(params);//应用到组件
            return insets;
        });
    }

    private boolean GetLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void openGithubLink() {
        Uri uri = Uri.parse(GithubURL);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private String getCurrentWifiName() {
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        ssid = ssid.substring(1, ssid.length() - 1);

        return ssid;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (threadpool.isShutdown()) {
            threadpool = Executors.newFixedThreadPool(3);
        }

        threadpool.execute(this::TASK1);
        threadpool.execute(this::TASK2);
    }

    private void TASK1() {
        while (true) {
            runOnUiThread(() -> {

                //获取授权状态并展示
                if (GetLocationPermission() == true) {
                    permissionCard_Text.setText(R.string.Permission_Yes);
                } else if (GetLocationPermission() == false) {
                    permissionCard_Text.setText(R.string.Permission_No);
                }

                //获取wifi状态并展示
                if (getCurrentWifiName() == "nuknown ssid") {
                    wifiCard_Text.setText(R.string.WiFiName_Normal);
                } else {
                    wifiCard_Text.setText(getCurrentWifiName());
                }
            });
            SystemClock.sleep(1000);
        }
    }


    private void TASK2() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        threadpool.shutdown();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.title) {

        } else if (id == R.id.github) {
            openGithubLink();
        } else if (id == R.id.deviceState) {

        } else if (id == R.id.permissionState) {
            Toast.makeText(this, "授权\"定位信息\"", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else if (id == R.id.wifiState) {

            Toast.makeText(this, "WiFi：\"CircularClock_Config\"", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }

}
