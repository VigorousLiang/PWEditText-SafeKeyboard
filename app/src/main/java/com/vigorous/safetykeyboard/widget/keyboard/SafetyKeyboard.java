package com.vigorous.safetykeyboard.widget.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.vigorous.safetykeyboard.R;
import com.vigorous.safetykeyboard.data.Constant;
import com.vigorous.safetykeyboard.jni.IJniInterface;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

public class SafetyKeyboard {

    private static final String TAG = SafetyKeyboard.class.getName();

    private Context mContext = null;
    private LinearLayout mkeyboardLayout, mTitleLayout;
    private GrapeGridview mKeyBoardControl = null;
    private PopupWindow mPopup;
    private TextView mTitleView;
    private KeyBoardDataAdapter mAdapter;

    private boolean mIsDefaultPosition = true;
    private Drawable mKeyBoardBgDrawable;
    private Drawable[] mNumForeSelectorArray, mNumBgSelectorArray;
    private Drawable mDoneForeSelector, mDelForeSelector;
    private Drawable mDoneBgSelector, mDelBgSelector;
    private Drawable mSecureDrawable;
    private Drawable mNumBgDrawSelector;
    private int mNumColor = Color.BLACK;
    private int mNumSize = 50;

    private int mStartX, mStartY;
    private int mWidth, mHeight, mTitleHeight;
    private int mMarginCol, mMarginRow;
    private int mPaddingLeft, mPaddingRight, mPaddingTop, mPaddingBottom;
    private int mKeyWidth, mKeyHeight;
    private int mAnimationStyle;
    private int mPwdSize = 0;

    private OnShowListener mOnShowListener;
    private OnHideListener mOnHideListener;
    private OnEditorListener mOnEditorListener;

    private AudioManager mAudioManger;
    private Vibrator mVibrator;
    private boolean mIsTitleVisble = false;
    private boolean mIsAudio = false;
    private boolean mIsVibrate = false;
    private boolean mSoundEffect = false;

    private static List<Integer> NUMBER_DIGIT_LIST = new ArrayList<Integer>(10);

    static {
        for (int i = 0; i < 10; i++) {
            NUMBER_DIGIT_LIST.add(i);
        }
    }

    public SafetyKeyboard(Context context) {
        mContext = context;
        initParams();
        initView(context);
    }

    public SafetyKeyboard(Context context, Drawable bgDrawable) {
        mContext = context;
        mKeyBoardBgDrawable = bgDrawable;
        initParams();
        initView(context);
    }

    private void initParams() {
        mStartX = 0;
        mStartY = 0;
        mKeyWidth = LayoutParams.MATCH_PARENT;
        mKeyHeight = getSaftyKeyHeight() / 15;
        mTitleHeight = getSaftyKeyHeight() / 16;
        mMarginCol = 10;
        mMarginRow = 10;
        mPaddingLeft = 0;
        mPaddingTop = 0;
        mPaddingRight = 0;
        mPaddingBottom = 0;
        mWidth = LayoutParams.MATCH_PARENT;
        mHeight = getSaftyKeyboardHeight();
        try {
            mVibrator = (Vibrator) mContext
                    .getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        mAudioManger = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        mSoundEffect = Settings.System.SOUND_EFFECTS_ENABLED
                .equals("sound_effects_enabled");
        mAdapter = new KeyBoardDataAdapter();
    }

    /**
     * 密码键盘默认样式
     * @param context
     */
    private void initView(Context context) {
        mkeyboardLayout = new LinearLayout(context);
        mkeyboardLayout.setOrientation(LinearLayout.VERTICAL);
        if (null != mKeyBoardBgDrawable) {
            mkeyboardLayout.setBackgroundDrawable(mKeyBoardBgDrawable);
        } else {
            mkeyboardLayout.setBackgroundColor(Color.GRAY);
        }
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 6);
        mTitleLayout = new LinearLayout(context);
        mTitleLayout.setGravity(Gravity.CENTER);
        mTitleLayout.setOrientation(LinearLayout.HORIZONTAL);

        mTitleLayout.setLayoutParams(titleParams);
        int titleBackgroundColor = mContext.getResources()
                .getColor(R.color.bg_security_keyboard_title);
        mTitleLayout.setBackgroundColor(titleBackgroundColor);
        mTitleView = new TextView(context);
        mTitleView.setText("Secure Mode");
        mTitleView.setTextColor(Color.WHITE);
        mTitleView.setHeight(getSaftyKeyHeight() / 16);
        mTitleView.setGravity(Gravity.CENTER);
        mTitleLayout.addView(mTitleView);

        mKeyBoardControl = new GrapeGridview(context);
        mKeyBoardControl.setHorizontalScrollBarEnabled(false);
        mKeyBoardControl.setVerticalScrollBarEnabled(false);
        mKeyBoardControl.setEnabled(false);
        mKeyBoardControl.setNumColumns(Constant.COLUMN_SOFT_KEYBOARD);
        mKeyBoardControl.setVerticalSpacing(mMarginRow);
        mKeyBoardControl.setHorizontalSpacing(mMarginCol);
        mKeyBoardControl.setAdapter(mAdapter);

        mkeyboardLayout.addView(mTitleLayout);
        mkeyboardLayout.addView(mKeyBoardControl);
        mkeyboardLayout.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight,
                mPaddingBottom);

        mPopup = new PopupWindow(mkeyboardLayout, mWidth, mHeight);
        mPopup.setBackgroundDrawable(new BitmapDrawable());
        mPopup.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mPopup.setOutsideTouchable(true);
        mPopup.setFocusable(true);
        mPopup.setOnDismissListener(dismissListener);
    }

    private int getSaftyKeyboardHeight() {
        int deKeyHeight = getSaftyKeyHeight() / 15;
        int deTitleHeight = getSaftyKeyHeight() / 16;
        int height = deTitleHeight + 4 * deKeyHeight + 3 * mMarginRow + 2 * 10;
        return height;
    }

    private int getSaftyKeyHeight() {
        if (null != mContext) {
            return mContext.getResources().getDisplayMetrics().heightPixels;
        } else {
            return 0;
        }
    }

    public void setKeyboardBackground(Drawable backgroundDrawable) {
        if (null != backgroundDrawable) {
            mKeyBoardBgDrawable = backgroundDrawable;
            mkeyboardLayout.setBackgroundDrawable(mKeyBoardBgDrawable);
        }
    }

    public void setTitleVisibility(boolean visibility) {
        mIsTitleVisble = visibility;
        if (mTitleLayout != null) {
            if (visibility) {
                mTitleLayout.setVisibility(View.VISIBLE);
            } else {
                mTitleLayout.setVisibility(View.GONE);
                setTitleHeight(0);
            }
        }
    }

    public void setTitleDrawable(Drawable secureDrawable) {
        mSecureDrawable = secureDrawable;
        mTitleView.setCompoundDrawablesWithIntrinsicBounds(secureDrawable, null,
                null, null);
        mTitleView.setCompoundDrawablePadding(10);
    }

    public void setTitleDrawableSize(int secureWidth, int secureHeight) {
        if (null != mSecureDrawable) {
            mSecureDrawable.setBounds(0, 0, secureWidth, secureHeight);
            mTitleView.setCompoundDrawables(mSecureDrawable, null, null, null);
        }
    }

    public void setTitleHeight(int titleHeight) {
        if (0 >= titleHeight) {
            mTitleHeight = 0;
            mkeyboardLayout.removeView(mTitleLayout);
            mPopup.setContentView(mkeyboardLayout);
        } else {
            mTitleHeight = titleHeight;
            mTitleView.setHeight(titleHeight);
        }
    }

    public void setTitleText(String title) {
        mTitleView.setText(title);
    }

    public String getEncryptedPin(String vid) {
        return IJniInterface.gEP(vid);
    }

    public void setTitleDrawablePadding(int margin) {
        mTitleView.setCompoundDrawablePadding(margin);
    }

    public void setTitleFont(Typeface font) {
        if (null != font) {
            mTitleView.setTypeface(font);
        }
    }

    public void setTitleColor(int color) {
        mTitleView.setTextColor(color);
    }

    public void setTitleSize(int size) {
        if (0 <= size) {
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    public void setNumberKeyColor(int color) {
        mNumColor = color;
    }

    public void setNumberKeySize(int size) {
        if (0 <= size) {
            mNumSize = size;
        }
    }

    public void setKeyboardStartPosition(int startX, int startY) {
        if (0 <= startX && 0 <= startY) {
            mStartX = startX;
            mStartY = startY;
            mIsDefaultPosition = false;
        }
    }

    public void setKeyBoardSize(int keyboardWidth, int keyboardHeight) {
        if (0 <= keyboardWidth && 0 <= keyboardHeight) {
            mWidth = keyboardWidth;
            mHeight = keyboardHeight;
            mkeyboardLayout.setLayoutParams(
                    new LinearLayout.LayoutParams(mWidth, mHeight));
        }
    }

    public void setKeyboardMargin(int marginRow, int marginCol) {
        if (0 <= marginRow) {
            mMarginRow = marginRow;
            mKeyBoardControl.setVerticalSpacing(marginRow);
        }
        if (0 <= marginCol) {
            mMarginCol = marginCol;
            mKeyBoardControl.setHorizontalSpacing(marginCol);
        }
    }

    public void setKeyboardPadding(int paddingLeft, int paddingTop,
            int paddingRight, int paddingBottom) {
        mPaddingLeft = paddingLeft;
        mPaddingTop = paddingTop;
        mPaddingRight = paddingRight;
        mPaddingBottom = paddingBottom;
        mkeyboardLayout.setPadding(paddingLeft, paddingTop, paddingRight,
                paddingBottom);
    }

    public void setNumberKeyDrawableSelector(Drawable[] numForeSelector,
            Drawable[] numBgSelector) {
        mNumForeSelectorArray = numForeSelector;
        mNumBgSelectorArray = numBgSelector;
    }

    public void setNumKeyBackgroud(Drawable numBgSelector) {
        mNumBgDrawSelector = numBgSelector;
    }

    public void setDoneKeyBackgroud(Drawable doneBgSelector) {
        mDoneBgSelector = doneBgSelector;
    }

    public void setDoneKeyDrawableSelector(Drawable doneForeSelector,
            Drawable doneBgSelector) {
        mDoneForeSelector = doneForeSelector;
        mDoneBgSelector = doneBgSelector;
    }

    public void setDelKeyDrawableSelector(Drawable delForeSelector,
            Drawable delBgSelector) {
        mDelForeSelector = delForeSelector;
        mDelBgSelector = delBgSelector;
    }

    public void setAnimationStyle(int animationStyle) {
        mAnimationStyle = animationStyle;
    }

    public void setKeyboardAudio(boolean isKeyAudio) {
        int mode = -1;
        if (null != mAudioManger) {
            mode = mAudioManger.getRingerMode();
        }
        if (isKeyAudio) {
            if (mSoundEffect) {
                mIsAudio = true;
            } else {
                mIsAudio = false;
            }
            if (AudioManager.RINGER_MODE_SILENT == mode) {
                mIsAudio = false;
            }
        }
    }

    public void setKeyboardVibrate(boolean vibrate) {
        mIsVibrate = vibrate;
    }

    /**
     * @param view
     */
    public void show(View view) {
        IJniInterface.cP();
        mPwdSize = 0;
        if (null != mAdapter) {
            mAdapter.setRandomKeyNum();
            mAdapter.notifyDataSetChanged();
        }
        mKeyBoardControl.requestLayout();
        mkeyboardLayout.invalidate();
        if (null != mPopup) {
            mPopup.setWidth(mWidth);
            mPopup.setHeight(mHeight);
        }
        if (null != mPopup) {
            mPopup.setAnimationStyle(mAnimationStyle);
            if (null != mOnShowListener) {
                mOnShowListener.onShow();
            }
            if (!mIsDefaultPosition) {
                mPopup.showAtLocation(view, Gravity.TOP | Gravity.LEFT, mStartX,
                        mStartY);
            } else {
                mPopup.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            }
            if (Build.VERSION.SDK_INT <= 23) {
                mPopup.update();
            }
        }
    }

    public void hide() {
        if (null != mPopup && mPopup.isShowing()) {
            mPopup.dismiss();
        }
        if (null != mOnHideListener) {
            mOnHideListener.onHide();
        }
    }

    private OnDismissListener dismissListener = new OnDismissListener() {
        @Override
        public void onDismiss() {
            if (null != mOnHideListener) {
                mOnHideListener.onHide();
            }
        }
    };

    public void setOnShowListener(OnShowListener l) {
        mOnShowListener = l;
    }

    public void setOnHideListener(OnHideListener l) {
        mOnHideListener = l;
    }

    public void setOnEditorListener(OnEditorListener l) {
        mOnEditorListener = l;
    }

    public interface OnShowListener {
        void onShow();
    }

    public interface OnHideListener {
        void onHide();
    }

    public interface OnEditorListener {

        void onEditorChanged(int pinLength);
    }

    final OnClickListener onPasswordButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsVibrate && null != mVibrator) {
                mVibrator.vibrate(new long[] { 0, 100 }, -1);
            }
            int id = v.getId();
            if (id == Constant.CONFIRM_BUTTON_ITEM_ID) {
                hide();
                return;
            } else if (id == Constant.DELETE_BUTTON_ITEM_ID) {
                if (mPwdSize > 0) {
                    IJniInterface.dOP();
                    mPwdSize--;
                }
            } else {
                if (mPwdSize == Constant.MAX_PASSWORD) {
                    return;
                }
                IJniInterface.aP(Integer.toString(id));
                mPwdSize++;
            }
            if (null != mOnEditorListener) {
                mOnEditorListener.onEditorChanged(mPwdSize);
            }
        }
    };

    private class KeyBoardDataAdapter extends BaseAdapter {

        public KeyBoardDataAdapter() {
            super();
            setRandomKeyNum();
        }

        public void setRandomKeyNum() {
            Collections.shuffle(NUMBER_DIGIT_LIST);
        }

        @Override
        public int getCount() {
            return Constant.BOARD_BUTTON_COUNT;
        }

        @Override
        public Object getItem(int position) {
            if (position == 9) {
                return Constant.STRING_CONFIRM_BUTTON;
            } else if (position == 11) {
                return Constant.STRING_DELETE_BUTTON;
            } else if (position == 10) {
                return String.valueOf(NUMBER_DIGIT_LIST.get(position - 1));
            }
            return String.valueOf(NUMBER_DIGIT_LIST.get(position));
        }

        @Override
        public long getItemId(int position) {
            if (position == 9) {
                return Constant.CONFIRM_BUTTON_ITEM_ID;
            } else if (position == 11) {
                return Constant.DELETE_BUTTON_ITEM_ID;
            } else if (position == 10) {
                return NUMBER_DIGIT_LIST.get(position - 1);
            }
            return NUMBER_DIGIT_LIST.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (LayoutParams.MATCH_PARENT != mWidth) {
                mKeyWidth = (mWidth - 2 * mMarginCol - mPaddingLeft
                        - mPaddingRight) / 3;
            }
            mKeyHeight = (mHeight - mTitleHeight - mPaddingTop - mPaddingBottom
                    - 3 * mMarginRow) / 4;
            ImageButton buttonView = new ImageButton(mContext);
            buttonView.setScaleType(ScaleType.CENTER_INSIDE);
            buttonView.setLayoutParams(
                    new AbsListView.LayoutParams(mKeyWidth, mKeyHeight));

            TextView textView = new TextView(mContext);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.setTextColor(
                        mContext.getResources().getColor(mNumColor));
            } else {
                textView.setTextColor(mNumColor);
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNumSize);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(
                    new AbsListView.LayoutParams(mKeyWidth, mKeyHeight));
            if (!mIsAudio) {
                buttonView.setSoundEffectsEnabled(false);
                textView.setSoundEffectsEnabled(false);
            }

            long id = getItemId(position);
            String key = (String) getItem(position);
            if (Constant.CONFIRM_BUTTON_ITEM_ID == id) {
                if (null != mDoneForeSelector && null != mDoneBgSelector) {
                    setDoneKey(buttonView);
                } else {
                    setDefaultDoneDelKey(textView);
                    String doneKeyStr = mContext.getResources()
                            .getString(R.string.str_done);
                    textView.setText(doneKeyStr);
                    int doneKeyColor = mContext.getResources()
                            .getColor(R.color.btn_security_keyboard_done);

                    textView.setTextColor(doneKeyColor);
                    int doneBackGroundColor = mContext.getResources()
                            .getColor(R.color.bg_security_keyboard_title);
                    textView.setBackgroundColor(doneBackGroundColor);
                    textView.setTextSize(16);
                    textView.setId((int) getItemId(position));
                    textView.setOnClickListener(onPasswordButtonClickListener);
                    return textView;
                }
            } else if (Constant.DELETE_BUTTON_ITEM_ID == id) {
                if (null != mDelForeSelector || null != mDelBgSelector) {
                    setDelKey(buttonView);
                } else {
                    textView.setText(key);
                    setDefaultDoneDelKey(textView);
                    textView.setId((int) getItemId(position));
                    textView.setOnClickListener(onPasswordButtonClickListener);
                    return textView;
                }
            } else {
                if (null != mNumForeSelectorArray) {
                    setNumberKey(buttonView, (int) id);
                } else {
                    setDefaultNumKey(textView, (int) id);
                    textView.setId((int) getItemId(position));
                    textView.setOnClickListener(onPasswordButtonClickListener);
                    return textView;
                }
            }
            buttonView.setId((int) getItemId(position));
            buttonView.setOnClickListener(onPasswordButtonClickListener);
            return buttonView;
        }

        private void setDefaultNumKey(TextView view, int key) {
            view.setText(String.valueOf(key));
            if (null != mNumBgDrawSelector) {
                Drawable tempNumBg = mNumBgDrawSelector.getConstantState()
                        .newDrawable();
                view.setBackgroundDrawable(tempNumBg);
            } else {
                ColorDrawable normal = new ColorDrawable(Color.WHITE);
                ColorDrawable press = new ColorDrawable(Color.GRAY);
                view.setBackgroundDrawable(getDrawableSeletor(normal, press));
            }
        }

        private void setDefaultDoneDelKey(TextView view) {
            ColorDrawable normal = new ColorDrawable(Color.WHITE);
            ColorDrawable press = new ColorDrawable(Color.GRAY);
            view.setBackgroundDrawable(getDrawableSeletor(normal, press));
        }

        private void setNumberKey(ImageButton buttonView, int key) {
            Drawable tempDrawable = null;
            if (null != mNumForeSelectorArray[key]) {
                tempDrawable = mNumForeSelectorArray[key].getConstantState()
                        .newDrawable();
                buttonView.setImageDrawable(tempDrawable);
            }
            if (null != mNumBgSelectorArray
                    && null != mNumBgSelectorArray[key]) {
                tempDrawable = mNumBgSelectorArray[key].getConstantState()
                        .newDrawable();
                buttonView.setBackgroundDrawable(tempDrawable);
            } else if (null != mNumBgDrawSelector) {
                tempDrawable = mNumBgDrawSelector.getConstantState()
                        .newDrawable();
                buttonView.setBackgroundDrawable(tempDrawable);
            }
        }

        private void setDoneKey(ImageButton buttonView) {
            if (null != mDoneForeSelector) {
                buttonView.setImageDrawable(mDoneForeSelector);

            }
            if (null != mDoneBgSelector) {
                buttonView.setBackgroundDrawable(mDoneBgSelector);
            }
        }

        private void setDelKey(ImageButton buttonView) {
            if (null != mDelForeSelector) {
                buttonView.setImageDrawable(mDelForeSelector);
            }
            if (null != mDelBgSelector) {
                buttonView.setBackgroundDrawable(mDelBgSelector);
            }
        }
    }

    private StateListDrawable getDrawableSeletor(Drawable normal,
            Drawable press) {
        StateListDrawable bg = new StateListDrawable();
        if (null != normal) {
            bg.addState(new int[] { android.R.attr.state_pressed }, press);
        }
        if (null != press) {
            bg.addState(new int[] { android.R.attr.state_enabled }, normal);
        }
        return bg;
    }

    public class GrapeGridview extends GridView {

        public GrapeGridview(Context context) {
            super(context);
        }

        public GrapeGridview(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public GrapeGridview(Context context, AttributeSet attrs,
                int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                return true;
            }
            return super.dispatchTouchEvent(ev);
        }
    }

}
