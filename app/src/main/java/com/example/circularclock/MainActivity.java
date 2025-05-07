package com.example.circularclock;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private int sysBar_Top;//系统状态栏边距
    private View MyTop_Module;//最顶部的组件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MyTop_Module = findViewById(R.id.TopModule);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            sysBar_Top = systemBars.top;//获取系统状态栏的上边距
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) MyTop_Module.getLayoutParams();//获取组件的布局参数
            params.topMargin = sysBar_Top;//设置上外边距布局参数
            MyTop_Module.setLayoutParams(params);//应用到组件
//            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });
    }
}
