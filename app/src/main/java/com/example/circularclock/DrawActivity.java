package com.example.circularclock;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener {
    private int sysBar_Top;//系统状态栏边距
    private View MyTop_Module;//最顶部的组件
    private GridLayout gridLayout;//网格布局
    private TextView[][] gridButton = new TextView[24][8];//每个按钮的位置
    private int WHUnit;//一个单元的宽度
    private ImageButton BackButton;
    private TextView styleButton1;
    private TextView styleButton2;
    private TextView styleButton3;
    private TextView styleButton4;
    private TextView styleButton5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.active_draw);

        //id配置
        MyTop_Module = findViewById(R.id.TopModule);
        gridLayout = findViewById(R.id.gridLayout);
        BackButton = findViewById(R.id.back);
        styleButton1 = findViewById(R.id.colorStyle1);
        styleButton2 = findViewById(R.id.colorStyle2);
        styleButton3 = findViewById(R.id.colorStyle3);
        styleButton4 = findViewById(R.id.colorStyle4);
        styleButton5 = findViewById(R.id.colorStyle5);

        //按下监听器设置
        BackButton.setOnClickListener(this);

        //获取屏幕宽度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        WHUnit = (displayMetrics.widthPixels) / 8;

        //阵列按钮初始化
        for (int row = 0; row < 24; row++) {
            for (int col = 0; col < 8; col++) {

                //文本视图配置
                TextView textView = new TextView(this);
                textView.setId(row + col);
                textView.setBackgroundResource(R.drawable.led_color);
                textView.setClickable(true);
                textView.setGravity(Gravity.CENTER);
                textView.setHeight(WHUnit);
                textView.setWidth(WHUnit);

                //网格的单元格配置
                GridLayout.LayoutParams GridParams = new GridLayout.LayoutParams();
                GridParams.rowSpec = GridLayout.spec(row);
                GridParams.columnSpec = GridLayout.spec(col);
                GridParams.setMargins(10, 10, 10, 10);
                GridParams.width = 0; // 宽度使用权重布局
                GridParams.setGravity(Gravity.FILL); // 内容填充整个单元格
                GridParams.columnSpec = GridLayout.spec(col, 1f); // 权重设为1

                gridButton[row][col] = textView;//设置到全局变量

                //tag标记
                Object key = new int[]{row, col};
                textView.setTag(key);

                textView.setOnClickListener(gridButtonClickListener);
                gridLayout.addView(textView, GridParams);
            }
        }

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

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.back) {
            finish();
        }
    }

    private final View.OnClickListener gridButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                int[] position = (int[]) v.getTag();
                int row = position[0];
                int col = position[1];

                //从全局变量中引用
                TextView tv = gridButton[row][col];
                tv.setBackgroundResource(R.color.ColorDefine_2);

                Log.d("gridButtonClickListener", "按钮点击");
                Log.d("gridButtonClickListener", position[0] + "|" + position[1]);
            } catch (Exception error) {
                Log.e("gridButtonClickListener", "按钮点击错误");
            }
        }
    };
}
