package com.vigorous.safetykeyboard.ui;


import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.vigorous.safetykeyboard.R;
import com.vigorous.safetykeyboard.widget.keyboard.SafetyKeyboard;
import com.vigorous.safetykeyboard.widget.password.SafePswText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "safeKeyboard";

    @BindView(R.id.psw_online_pin)
    public SafePswText mPswEditText;

    protected Resources res;
    protected LinearLayout layout;
    protected SafetyKeyboard mKeyboard = null;
    protected Drawable mKeyboardBg;
    protected Drawable[] mNumBgSelector;
    protected Drawable mNumAllBgSelector;
    protected Drawable mDoneForeSelector, mDoneBgSelector, mDelForeSelector,
            mDelBgSelector;

    private SafePswText.InputCallBack inputCallBack = new SafePswText.InputCallBack() {
        @Override
        public void onInputFinish(String password) {
            Log.e(TAG, "input finish");
            if (mKeyboard != null) {
                //Input param is vid
                Log.e(TAG, "getEncryptedPin:" + mKeyboard.getEncryptedPin("111111"));
                mKeyboard.hide();
            }
        }
    };
    final SafetyKeyboard.OnEditorListener listener = new SafetyKeyboard.OnEditorListener() {

        @Override
        public void onEditorChanged(int pinLength) {
            if (mPswEditText != null) {
                if (pinLength == 0) {
                    mPswEditText.clearPsw();
                    return;
                }
                if (mPswEditText.getPsw().length() > pinLength) {
                    mPswEditText.deleteSafeKeyCode();
                } else {
                    mPswEditText.appendSafeKeyCode();
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPswEditText.isMarginBetweenPSW(false);
        mPswEditText.setInputCallBack(inputCallBack);
        mPswEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mPswEditText.requestFocus();
                    showSafeKeyboard();
                    mPswEditText.clearPsw();
                    mPswEditText.setFocusable(true);
                    return true;
                }
                return false;
            }
        });
        initSafeKeyBoardUI();
    }

    private void showSafeKeyboard() {
        mKeyboard.setOnEditorListener(listener);
        mKeyboard.show(layout);
    }

    /**
     * 初始化密码键盘的ui
     */
    protected void initSafeKeyBoardUI() {
        // 资源预加载
        res = getResources();
        layout = (LinearLayout) findViewById(R.id.ll_safe_keyboard);
        mKeyboardBg = res.getDrawable(R.drawable.bg_keyboard);
        mNumAllBgSelector = res.getDrawable(R.drawable.bg_num_drawable);
        // 数字键背景
        mNumBgSelector = new Drawable[10];
        for (int i = 0; i < 10; i++) {
            mNumBgSelector[i] = res.getDrawable(R.drawable.bg_keyboard);
        }
        // 确定键
        mDoneForeSelector = res.getDrawable(R.drawable.done_fore_selector);
        mDoneBgSelector = res.getDrawable(R.drawable.done_bg_selector);

        // 删除键
        mDelForeSelector = res.getDrawable(R.drawable.del_fore_selector);
        mDelBgSelector = res.getDrawable(R.drawable.del_bg_selector);

        // 加载键盘页面
        mKeyboard = new SafetyKeyboard(this);
        float des = getResources().getDisplayMetrics().density;
        mKeyboard.setTitleVisibility(false);
        mKeyboard.setKeyboardMargin(2, 2);
        mKeyboard.setKeyboardBackground(mKeyboardBg);
        mKeyboard.setNumKeyBackgroud(mNumAllBgSelector);
        mKeyboard.setNumberKeyColor(R.color.security_keyboard_number);
        mKeyboard.setNumberKeySize((int) (28 * des));
        mKeyboard.setDoneKeyDrawableSelector(mDoneForeSelector,mDoneBgSelector);
        mKeyboard.setDelKeyDrawableSelector(mDelForeSelector, mDelBgSelector);
        mKeyboard.setAnimationStyle(R.style.PopupAnimation);
        mKeyboard.setKeyboardVibrate(false);
        mKeyboard.setKeyboardAudio(false);
    }
}
