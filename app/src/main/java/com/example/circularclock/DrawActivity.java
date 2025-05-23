package com.example.circularclock;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.circularclock.Connect;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener {
    private Connect connect = Connect.getInstance();//UDP连接
    private ExecutorService threadpool = Executors.newFixedThreadPool(1);//新建1个线程
    private boolean TASK_isRUNNING = false;//线程运行标志位
    private View MyTop_Module;//最顶部的组件
    private GridLayout gridLayout;//网格布局
    private TextView[][] gridButton = new TextView[8][24];//每个按钮的位置
    private int[][] gridColor = new int[8][24];//每个按钮的颜色
    private int WHUnit;//一个单元的宽度
    private LinearLayout stateBar;//通知栏
    private ImageButton BackButton;//返回按钮
    private ImageButton ClearButton;//清空按钮
    private ImageButton ClearPointButton;//清空按钮(点阵式)
    private TextView styleButton1;//颜色样式1
    private TextView styleButton2;//颜色样式2
    private TextView styleButton3;//颜色样式3
    private TextView styleButton4;//颜色样式4
    private TextView styleButton5;//颜色样式5
    private int CurrentstyleMode;//当前样式模式
    private int CurrentColor = R.color.ColorDefine_5;//当前颜色样式(默认为样式5)
    private volatile boolean ButtonStyleChange_Flag = false;//任意一个点阵按钮被按下的标志位
    private volatile int[] ButtonStyleChange_Unit = new int[2];//最近一次点阵按钮被按下的坐标(0:row 1:col)
    private volatile boolean Clear_Flag = false;//全屏清空标志位
    private volatile boolean Back_Flag = false;//退出举牌绘制标志位

    //初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.active_draw);
        connect.Connect_InitPro();

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
        WHUnit = (displayMetrics.heightPixels) / 9;
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

    //清空面板
    private void ClearPanel() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 24; col++) {
                //设置按钮颜色
                TextView tv = gridButton[row][col];
                tv.setBackgroundResource(R.color.PureWhite);
                gridColor[row][col] = R.color.PureWhite;
            }
        }
    }

    //清空面板弹窗(链式调用：每次调用，都会返回已经设置好的上一个Builder对象，以便再次调用)
    private void ClearPanelPop() {
        new AlertDialog.Builder(this).
                setTitle("确认清空面板吗")
                .setPositiveButton("取消", (dialog, witch) -> {
                })
                .setNegativeButton("清空", (dialog, witch) -> {
                    Clear_Flag = true;
                    ClearPanel();
                }).setMessage("清空后，灯板恢复初始状态")
                .show();
    }

    //颜色切换按钮、返回按钮控制
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.back && !ButtonStyleChange_Flag && !Clear_Flag) {

            Back_Flag = true;
            ProgressDialog progressDialog = new ProgressDialog((this));
            progressDialog.setMessage("请稍等，正在退出...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Thread(() -> {
                while (Back_Flag) {
                }
                progressDialog.dismiss();
                finish();
            }).start();

        } else if (id == R.id.clear && !ButtonStyleChange_Flag) {
            stateBar.setBackgroundResource(R.color.DelBG);
            ClearPanelPop();
        } else if (id == R.id.clearPoint) {
            CurrentstyleMode = 0;
            CurrentColor = R.color.PureWhite;
            stateBar.setBackgroundResource(R.color.PureWhite);
        } else if (id == R.id.colorStyle1) {
            CurrentstyleMode = 1;
            CurrentColor = R.color.ColorDefine_1;
            stateBar.setBackgroundResource(R.color.ColorDefine_1);
        } else if (id == R.id.colorStyle2) {
            CurrentstyleMode = 2;
            CurrentColor = R.color.ColorDefine_2;
            stateBar.setBackgroundResource(R.color.ColorDefine_2);
        } else if (id == R.id.colorStyle3) {
            CurrentstyleMode = 3;
            CurrentColor = R.color.ColorDefine_3;
            stateBar.setBackgroundResource(R.color.ColorDefine_3);
        } else if (id == R.id.colorStyle4 && !ButtonStyleChange_Flag) {
            CurrentstyleMode = 4;
            CurrentColor = R.color.ColorDefine_4;
            stateBar.setBackgroundResource(R.color.ColorDefine_4);
        } else if (id == R.id.colorStyle5) {
            CurrentstyleMode = 5;
            CurrentColor = R.color.ColorDefine_5;
            stateBar.setBackgroundResource(R.color.ColorDefine_5);
        }
    }

    //单元格控制
    private final View.OnClickListener gridButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                int[] position = (int[]) v.getTag();
                int row = position[0];
                int col = position[1];

                if (!ButtonStyleChange_Flag) {
                    //设置按钮颜色
                    TextView tv = gridButton[row][col];
                    tv.setBackgroundResource(CurrentColor);

                    //保存按钮颜色
                    gridColor[row][col] = CurrentColor;

                    //保存按钮坐标
                    ButtonStyleChange_Unit[0] = row;
                    ButtonStyleChange_Unit[1] = col;

                    ButtonStyleChange_Flag = true;
                }

                Log.d("gridButtonClickListener", "按钮点击");
                Log.d("gridButtonClickListener", position[0] + "|" + position[1]);
            } catch (Exception error) {
                Log.e("gridButtonClickListener", "按钮点击错误");
            }
        }
    };

    //任务启动控制
    @Override
    protected void onStart() {
        super.onStart();
        if (threadpool.isShutdown()) {
            threadpool = Executors.newFixedThreadPool(1);
        }

        TASK_isRUNNING = true;
        threadpool.execute(this::TASK1);
    }

    //任务停止控制
    @Override
    protected void onStop() {
        super.onStop();

        TASK_isRUNNING = false;
        threadpool.shutdown();
    }

    //任务1控制
    private void TASK1() {
        while (TASK_isRUNNING) {

            if (Back_Flag) {
                boolean Result = false;
                String buffer = "#upperlink#back";

//                connect.Rec_Buffer.set(null);
                while (!Result) {
                    Result = connect.Connect_Command(buffer, "ok");
                }

                Back_Flag = false;
            }

            if (Clear_Flag) {
                String buffer = "#upperlink#clear";

//                connect.Rec_Buffer.set(null);
                connect.Connect_Command(buffer, "ok");

                Clear_Flag = false;
            }

            //格式：#upperlink#x#y#color_mode#
            if (ButtonStyleChange_Flag) {
                String buffer = String.format(Locale.US, "#upperlink#color#%d#%d#%d#", ButtonStyleChange_Unit[1], ButtonStyleChange_Unit[0], CurrentstyleMode);

                while (true) {
                    connect.Rec_Buffer.set("");
                    if (connect.Connect_Command(buffer, "ok")) {
                        break;
                    }
                }
                SystemClock.sleep(200);
                ButtonStyleChange_Flag = false;
            }
        }
    }
}
