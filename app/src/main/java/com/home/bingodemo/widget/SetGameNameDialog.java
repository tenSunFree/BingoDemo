package com.home.bingodemo.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.home.bingodemo.R;

/**
 * 自定义Dialog
 */
public class SetGameNameDialog extends Dialog {

    private CardView yesCardView, noCardView;
    private EditText gameNameEditText;
    private TextView titleTextView;
    private String title;
    private View.OnClickListener onYesListener, onNoListener;                                                                   // 0、隐藏1、显示

    public SetGameNameDialog(Context context) {
        super(context);
    }

    /**
     * @param context 上下文
     * @param theme   给dialog设置的主题
     */
    public SetGameNameDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.widget_dialog_set_game_name);

        initializationView();
        initializeBusinessDetails();
    }

    private void initializationView() {
        titleTextView = findViewById(R.id.titleTextView);

        yesCardView = findViewById(R.id.yesCardView);
        noCardView = findViewById(R.id.noCardView);

        gameNameEditText =  findViewById(R.id.gameNameEditText);
    }

    private void initializeBusinessDetails() {
        if (!TextUtils.isEmpty(title)) {
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
        } else {
            titleTextView.setVisibility(View.GONE);
        }
        if (onYesListener != null) {
            yesCardView.setVisibility(View.VISIBLE);
            yesCardView.setOnClickListener(onYesListener);
        } else {
            yesCardView.setVisibility(View.GONE);
        }
        if (onNoListener != null) {
            noCardView.setVisibility(View.VISIBLE);
            noCardView.setOnClickListener(onNoListener);
        } else {
            noCardView.setVisibility(View.GONE);
        }

        setOnTouchListener(yesCardView);
        setOnTouchListener(noCardView);
    }

    public void setTitleText(String title) {
        this.title = title;
    }

    public void setOnYesListener(View.OnClickListener onYesListener) {
        this.onYesListener = onYesListener;
    }

    public void setOnNoListener(View.OnClickListener onNoListener) {
        this.onNoListener = onNoListener;
    }

    public String getGameNameText() {
        return gameNameEditText.getText().toString();
    }

    /**
     * 針對按下放開的行為, 設定對應的動畫效果
     */
    public void setOnTouchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        touchDownAnimation(view);
                        return false;
                    }
                    case MotionEvent.ACTION_UP: {
                        touchUpAnimation(view);
                        return false;
                    }
                    default: {
                        return false;
                    }
                }
            }
        });
    }

    /**
     * 放大的動畫
     */
    void touchDownAnimation(View view) {
        Log.d("more", "touchDownAnimation");
        view.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    /**
     * 還原的動畫
     */
    void touchUpAnimation(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
}