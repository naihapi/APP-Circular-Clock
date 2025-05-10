package com.example.circularclock;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.atomic.AtomicBoolean;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener {
    private View MyTop_Module;//最顶部的组件
    private GridLayout gridLayout;//网格布局
    private TextView[][] gridButton = new TextView[8][24];//每个按钮的位置
    private int WHUnit;//一个单元的宽度
    private LinearLayout stateBar;//通知栏
    private ImageButton BackButton;//返回按钮
    private ImageButton ClearButton;//清空按钮
    private ImageButton ClearPointButton;//清空按钮(点阵式)
    private TextView styleButton1;
    private TextView styleButton2;
    private TextView styleButton3;
    private TextView styleButton4;
    private TextView styleButton5;
    private int CurrentColor = R.color.ColorDefine_5;

    //初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.test);

        //id配置
        stateBar = findViewById(R.id.StateBar);
        MyTop_Module = findViewById(R.id.TopModule);
        gridLayout = findViewById(R.id.gridLayout);
        BackButton = findViewById(R.id.back);
        ClearButton = findViewById(R.id.clear);
        ClearPointButton = findViewById(R.id.clearPoint);
        styleButton1 = findViewById(R.id.colorStyle1);
        styleButton2 = findViewById(R.id.colorStyle2);
        styleButton3 = findViewById(R.id.colorStyle3);
        styleButton4 = findViewById(R.id.colorStyle4);
        styleButton5 = findViewById(R.id.colorStyle5);

        //按下监听器设置
        BackButton.setOnClickListener(this);
        ClearButton.setOnClickListener(this);
        ClearPointButton.setOnClickListener(this);
        styleButton1.setOnClickListener(this);
        styleButton2.setOnClickListener(this);
        styleButton3.setOnClickListener(this);
        styleButton4.setOnClickListener(this);
        styleButton5.setOnClickListener(this);

        //获取屏幕宽度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        WHUnit = (displayMetrics.heightPixels) / 8;
        Log.d("asdfasd", "dsa" + WHUnit);

        //阵列按钮初始化
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 24; col++) {

                //文本视图配置
                TextView textView = new TextView(this);
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
                GridParams.width = WHUnit; // 每个单元格宽度

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

            Insets cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout());

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) MyTop_Module.getLayoutParams();//获取组件的布局参数
            params.setMarginStart(cutoutInsets.left + 10);  //设置左外边距布局参数
            MyTop_Module.setLayoutParams(params);//应用到组件
            return insets;
        });
    }

    //清空面板弹窗(链式调用：每次调用，都会返回已经设置好的上一个Builder对象，以便再次调用)
    private AtomicBoolean ClearPop() {
        AtomicBoolean flag = new AtomicBoolean(false);

        new AlertDialog.Builder(this).
                setTitle("确认清空面板吗")
                .setPositiveButton("取消", (dialog, witch) -> {
                    flag.set(false);
                })
                .setNegativeButton("清空", (dialog, witch) -> {
                    flag.set(true);
                }).setMessage("清空后，灯板恢复初始状态")
                .show();

        return flag;
    }

    //颜色、返回按钮控制
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.back) {
            finish();
        } else if (id == R.id.clear) {
            stateBar.setBackgroundResource(R.color.DelBG);
            ClearPop();
        } else if (id == R.id.clearPoint) {
            CurrentColor = R.color.PureWhite;
            stateBar.setBackgroundResource(R.color.PureWhite);
        } else if (id == R.id.colorStyle1) {
            CurrentColor = R.color.ColorDefine_1;
            stateBar.setBackgroundResource(R.color.ColorDefine_1);
        } else if (id == R.id.colorStyle2) {
            CurrentColor = R.color.ColorDefine_2;
            stateBar.setBackgroundResource(R.color.ColorDefine_2);
        } else if (id == R.id.colorStyle3) {
            CurrentColor = R.color.ColorDefine_3;
            stateBar.setBackgroundResource(R.color.ColorDefine_3);
        } else if (id == R.id.colorStyle4) {
            CurrentColor = R.color.ColorDefine_4;
            stateBar.setBackgroundResource(R.color.ColorDefine_4);
        } else if (id == R.id.colorStyle5) {
            CurrentColor = R.color.ColorDefine_5;
            stateBar.setBackgroundResource(R.color.ColorDefine_5);
        }
    }

    //绘制单元格控制
    private final View.OnClickListener gridButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                int[] position = (int[]) v.getTag();
                int row = position[0];
                int col = position[1];

                //从全局变量中引用
                TextView tv = gridButton[row][col];
                tv.setBackgroundResource(CurrentColor);

                Log.d("gridButtonClickListener", "按钮点击");
                Log.d("gridButtonClickListener", position[0] + "|" + position[1]);
            } catch (Exception error) {
                Log.e("gridButtonClickListener", "按钮点击错误");
            }
        }
    };
}
