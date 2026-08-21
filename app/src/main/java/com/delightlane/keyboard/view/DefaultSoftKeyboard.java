package com.delightlane.keyboard.view;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import org.greenrobot.eventbus.EventBus;

import com.delightlane.keyboard.hangul.EngineMode;
import com.delightlane.keyboard.hangul.HangulEngine;
import com.delightlane.keyboard.layout.LayoutAlphabet;
import com.delightlane.keyboard.layout.Layout12KeyDubul;
import com.delightlane.keyboard.event.*;
import com.delightlane.keyboard.SebeolHangulIME;
import com.delightlane.keyboard.R;
import com.delightlane.keyboard.event.SoftKeyLongPressEvent;

public class DefaultSoftKeyboard extends com.delightlane.keyboard.DefaultSoftKeyboard {

	public static final int HARD_KEYMODE_LANG = 10000;
	public static final int HARD_KEYMODE_LANG_ENGLISH = HARD_KEYMODE_LANG + LANG_EN;
	public static final int HARD_KEYMODE_LANG_KOREAN = HARD_KEYMODE_LANG + LANG_KO;

	private static final int KEYMODE_LENGTH = 11;

	protected static final int DEFAULT_FLICK_SENSITIVITY = 100;

	protected static final int BACKSPACE_SLIDE_UNIT = 250;

	protected static final int KEYCODE_NOP = -310;

	public static final int KEYCODE_KR12_ADDSTROKE = -310;

	public static final int KEYCODE_RIGHT = -217;
	public static final int KEYCODE_LEFT = -218;
	public static final int KEYCODE_DOWN = -219;
	public static final int KEYCODE_UP = -220;

	public static final int KEYCODE_NON_SHIN_DEL = -510;
	public static final int KEYCODE_TOGGLE_ONE_HAND_SIDE = -520;

	protected static final int INVALID_KEYMODE = -1;

	public static final int KEYMODE_HANGUL = 1;
	public static final int KEYMODE_HANGUL_CHO = 2;
	public static final int KEYMODE_HANGUL_JUNG = 3;
	public static final int KEYMODE_HANGUL_JONG = 4;
	public static final int KEYMODE_ENGLISH = 1;
	public static final int KEYMODE_ALT_SYMBOLS = 0;
	public static final int KEYMODE_NUMERIC_12KEY = 5;

	protected KeyboardView mNumKeyboardView;
	protected Keyboard[][][][][][] mNumKeyboard;

	protected boolean mCapsLock;

	protected int mLastInputType = 0;
	protected int mLastKeyMode = -1;
	protected int mReturnLanguage = -1;

	protected EngineMode[] mCurrentKeyboards;
	protected EngineMode mAltKeyMode;

	protected int[] mLimitedKeyMode = null;

	protected int mPreferenceKeyMode = INVALID_KEYMODE;
	protected int mPreferenceLanguage = INVALID_KEYMODE;

	protected boolean mHardwareLayout;

	protected boolean mUse12Key = false;
	protected boolean mUseAlphabetQwerty = true;
	protected boolean mUseExtensionRow = true;

	protected boolean mUseFlick = true;
	protected int mFlickSensitivity = DEFAULT_FLICK_SENSITIVITY;
	protected int mSpaceSlideSensitivity = DEFAULT_FLICK_SENSITIVITY;

	protected int mTimeoutDelay = 0;

	protected int mVibrateDuration = 30;

	protected int mKeyHeightPortrait = 60;
	protected int mKeyHeightLandscape = 55;

	protected int mMarginLeft = 0;
	protected int mMarginRight = 0;
	protected int mMarginBottom = 0;

	public static final boolean ONE_HAND_LEFT = false;
	public static final boolean ONE_HAND_RIGHT = true;

	protected boolean mOneHandedMode;
	protected int mOneHandedRatio;
	// 오른손 한손 모드를 기본값으로 사용한다.
	protected boolean mOneHandedSide = ONE_HAND_RIGHT;

	protected boolean mShowSubView = true;

	protected boolean mShowNumKeyboardViewPortrait = true;
	protected boolean mShowNumKeyboardViewLandscape = true;

	protected boolean mShowKeyPreview = false;

	protected boolean mForceHangul;

	protected int[] mLanguageCycleTable = {
			LANG_EN, LANG_KO
	};
	int mCurrentLanguageIndex = 1;

	Map<String, SoftKeyboardDisplay> mKeyboardDisplays = new HashMap<String, SoftKeyboardDisplay>() {{
		put("dark", new SoftKeyboardDisplay() {{
			add(KEYCODE_QWERTY_SHIFT, new SoftKeyDisplay(R.drawable.key_qwerty_shift, R.drawable.keybg_dark_mod_def));
			add(KEYCODE_QWERTY_ENTER, new SoftKeyDisplay(R.drawable.key_qwerty_enter, R.drawable.keybg_dark_enter_def));
			add(-10, new SoftKeyDisplay(R.drawable.key_qwerty_space));
			add(KEYCODE_QWERTY_BACKSPACE, new SoftKeyDisplay(R.drawable.key_qwerty_del, R.drawable.keybg_dark_mod_def));
			add(KEYCODE_JP12_ENTER, new SoftKeyDisplay(R.drawable.key_12key_enter, R.drawable.keybg_dark_enter_def));
			add(KEYCODE_JP12_SPACE, new SoftKeyDisplay(R.drawable.key_12key_space, R.drawable.keybg_dark_mod_def));
			add(KEYCODE_JP12_BACKSPACE, new SoftKeyDisplay(R.drawable.key_12key_del, R.drawable.keybg_dark_mod_def));
			add(KEYCODE_QWERTY_ALT, new SoftKeyDisplay(0, R.drawable.keybg_dark_mod));
			add(KEYCODE_CHANGE_LANG, new SoftKeyDisplay(0, R.drawable.keybg_dark_mod_def));
		}});
		put("white", new SoftKeyboardDisplay() {{
			add(KEYCODE_QWERTY_SHIFT, new SoftKeyDisplay(R.drawable.key_qwerty_shift_b, R.drawable.keybg_white_mod_def));
			add(KEYCODE_QWERTY_ENTER, new SoftKeyDisplay(R.drawable.key_qwerty_enter, R.drawable.keybg_white_enter_def));
			add(-10, new SoftKeyDisplay(R.drawable.key_qwerty_space_b));
			add(KEYCODE_QWERTY_BACKSPACE, new SoftKeyDisplay(R.drawable.key_qwerty_del_b, R.drawable.keybg_white_mod_def));
			add(KEYCODE_JP12_ENTER, new SoftKeyDisplay(R.drawable.key_12key_enter, R.drawable.keybg_white_enter_def));
			add(KEYCODE_JP12_SPACE, new SoftKeyDisplay(R.drawable.key_12key_space_b, R.drawable.keybg_white_mod_def));
			add(KEYCODE_JP12_BACKSPACE, new SoftKeyDisplay(R.drawable.key_12key_del_b, R.drawable.keybg_white_mod_def));
			add(KEYCODE_QWERTY_ALT, new SoftKeyDisplay(0, R.drawable.keybg_white_mod_def, Color.BLACK));
			add(KEYCODE_CHANGE_LANG, new SoftKeyDisplay(0, R.drawable.keybg_white_mod_def, Color.BLACK));
		}});
		put("flat_dark", new SoftKeyboardDisplay(R.drawable.keybg_flat_bg, R.drawable.keybg_flat_def, Color.WHITE) {{
			add(KEYCODE_QWERTY_SHIFT, new SoftKeyDisplay(R.drawable.key_qwerty_shift));
			add(KEYCODE_QWERTY_ENTER, new SoftKeyDisplay(R.drawable.key_qwerty_enter, R.drawable.keybg_flat_enter_def, true));
			add(-10, new SoftKeyDisplay(0, R.drawable.keybg_flat_space_def));
			add(KEYCODE_QWERTY_BACKSPACE, new SoftKeyDisplay(R.drawable.key_qwerty_del));
			add(KEYCODE_JP12_ENTER, new SoftKeyDisplay(R.drawable.key_12key_enter, R.drawable.keybg_flat_enter_def, true));
			add(KEYCODE_JP12_SPACE, new SoftKeyDisplay(R.drawable.key_12key_space));
			add(KEYCODE_JP12_BACKSPACE, new SoftKeyDisplay(R.drawable.key_12key_del));
		}});
		put("flat_blue", new SoftKeyboardDisplay(R.drawable.keybg_blue_bg, R.drawable.keybg_blue_def, Color.WHITE) {{
			add(KEYCODE_QWERTY_SHIFT, new SoftKeyDisplay(R.drawable.key_qwerty_shift));
			add(KEYCODE_QWERTY_ENTER, new SoftKeyDisplay(R.drawable.key_qwerty_enter, R.drawable.keybg_flat_enter_def, true));
			add(-10, new SoftKeyDisplay(R.drawable.key_qwerty_space));
			add(KEYCODE_QWERTY_BACKSPACE, new SoftKeyDisplay(R.drawable.key_qwerty_del));
			add(KEYCODE_JP12_ENTER, new SoftKeyDisplay(R.drawable.key_12key_enter, R.drawable.keybg_flat_enter_def, true));
			add(KEYCODE_JP12_SPACE, new SoftKeyDisplay(R.drawable.key_12key_space));
			add(KEYCODE_JP12_BACKSPACE, new SoftKeyDisplay(R.drawable.key_12key_del));
		}});
	}};

	Handler mTimeoutHandler;
	class TimeOutHandler implements Runnable {
		@Override
		public void run() {
			EventBus.getDefault().post(new InputTimeoutEvent());
		}
	}

	int mLongPressTimeout = 500;

	class LongClickHandler implements Runnable {
		int keyCode;
		boolean performed = false;
		public LongClickHandler(int keyCode) {
			this.keyCode = keyCode;
		}
		public void run() {
			setPreviewEnabled(keyCode);
			switch(keyCode) {
			case KEYCODE_QWERTY_SHIFT:
				if(mShiftOn > 0) return;
				toggleShiftLock();
				EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT)));
				mCapsLock = true;
				performed = true;
				updateKeyLabels();
				return;

			case KEYCODE_JP12_BACKSPACE:
			case KEYCODE_QWERTY_BACKSPACE:
				mBackspaceLongClickHandler.postDelayed(new BackspaceLongClickHandler(), 50);
				return;
			}
			EventBus.getDefault().post(new SoftKeyLongPressEvent(keyCode));
			try { mVibrator.vibrate(mVibrateDuration*2); } catch (Exception ex) { }
			performed = true;
		}
	}

	Handler mBackspaceLongClickHandler = new Handler();
	class BackspaceLongClickHandler implements Runnable {
		@Override
		public void run() {
			EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_NON_SHIN_DEL)));
			mBackspaceLongClickHandler.postDelayed(new BackspaceLongClickHandler(), 50);
		}
	}

	private final Map<Keyboard.Key, String> mKeyHoldLabels = new HashMap<>();
	public Map<Keyboard.Key, String> getKeyHoldLabels() { return mKeyHoldLabels; }

	private SparseArray<TouchPoint> mTouchPoints = new SparseArray<>();
	class TouchPoint {
		Keyboard.Key key;
		int keyCode;

		float downX, downY;
		float dx, dy;
		float beforeX, beforeY;
		int space = -1;
		int backspace = -1;
		int backspaceDistance;

		LongClickHandler longClickHandler;
		Handler handler;

		public TouchPoint(Keyboard.Key key, float downX, float downY) {
			this.key = key;
			this.keyCode = key.codes[0];
			this.downX = downX;
			this.downY = downY;

			key.onPressed();
			mKeyboardView.invalidateAllKeys();

			setPreviewEnabled(keyCode);

			handler = new Handler();
			handler.postDelayed(longClickHandler = new LongClickHandler(keyCode), mLongPressTimeout);

			/* key click sound & vibration */
			if (mVibrator != null) {
				try { mVibrator.vibrate(mVibrateDuration); } catch (Exception ex) { }
			}
			if (mSound != null) {
				try { mSound.seekTo(0); mSound.start(); } catch (Exception ex) { }
			}
		}

		public boolean onMove(float x, float y) {
			dx = x - downX;
			dy = y - downY;
			switch(keyCode) {
			case KEYCODE_JP12_SPACE:
			case -10:
				if(space == -1 && Math.abs(dx) >= mSpaceSlideSensitivity) {
					space = keyCode;
					if(mCurrentKeyMode != KEYMODE_HANGUL) {
						// 숫자/기호 자판(123)에 있을 때는 언어를 바꾸는 대신 마지막에 쓰던 언어의 문자 자판으로 되돌아간다.
						changeKeyMode(KEYMODE_HANGUL);
					} else {
						nextLanguage();
					}
					updateIndicator(HARD_KEYMODE_LANG + mCurrentLanguage);
				}
				break;

			case KEYCODE_JP12_BACKSPACE:
			case KEYCODE_QWERTY_BACKSPACE:
				if(Math.abs(dx) >= BACKSPACE_SLIDE_UNIT) {
					backspace = keyCode;
					mBackspaceLongClickHandler.removeCallbacksAndMessages(null);
				}
				break;

			default:
				space = -1;
				backspace = -1;
				break;
			}
			if(dy > mFlickSensitivity || dy < -mFlickSensitivity
					|| dx < -mFlickSensitivity || dx > mFlickSensitivity || space != -1) {
				handler.removeCallbacksAndMessages(null);
			}
			if(backspace != -1) {
				backspaceDistance += x - beforeX;
				if(backspaceDistance < -BACKSPACE_SLIDE_UNIT) {
					backspaceDistance = 0;
					EventBus.getDefault().post(new SoftKeyGestureEvent(KeyEvent.KEYCODE_DEL, SoftKeyGestureEvent.Type.SLIDE_LEFT));
				}
				if(backspaceDistance > +BACKSPACE_SLIDE_UNIT) {
					backspaceDistance = 0;
					EventBus.getDefault().post(new SoftKeyGestureEvent(KeyEvent.KEYCODE_DEL, SoftKeyGestureEvent.Type.SLIDE_RIGHT));
				}
			}
			beforeX = x;
			beforeY = y;
			return true;
		}

		public boolean onUp() {
			key.onReleased(true);
			mKeyboardView.setPreviewEnabled(false);
			mBackspaceLongClickHandler.removeCallbacksAndMessages(null);
			mKeyboardView.invalidateAllKeys();
			handler.removeCallbacksAndMessages(null);
			if(space != -1) {
				space = -1;
				return false;
			}
			if(backspace != -1) {
				EventBus.getDefault().post(new SoftKeyGestureEvent(KeyEvent.KEYCODE_DEL, SoftKeyGestureEvent.Type.RELEASE));
				backspace = -1;
				return false;
			}
			// Swipe Detection
			if(dx < -mFlickSensitivity*5) {
				if(Math.abs(dx) > Math.abs(dy)) {
					swipeLeft();
				}
				return false;
			}
			if(dx > mFlickSensitivity*5) {
				if(Math.abs(dx) > Math.abs(dy)) {
					swipeRight();
				}
				return false;
			}

			//Flick detection
			if(dy > mFlickSensitivity) {
				if(Math.abs(dy) > Math.abs(dx)) {
					EventBus.getDefault().post(new SoftKeyFlickEvent(keyCode, SoftKeyFlickEvent.Direction.DOWN));
				}
				return false;
			}
			if(dy < -mFlickSensitivity) {
				if(Math.abs(dy) > Math.abs(dx)) {
					EventBus.getDefault().post(new SoftKeyFlickEvent(keyCode, SoftKeyFlickEvent.Direction.UP));
				}
				return false;
			}
			if(dx < -mFlickSensitivity) {
				if(Math.abs(dx) > Math.abs(dy)) {
					EventBus.getDefault().post(new SoftKeyFlickEvent(keyCode, SoftKeyFlickEvent.Direction.LEFT));
				}
				return false;
			}
			if(dx > mFlickSensitivity) {
				if(Math.abs(dx) > Math.abs(dy)) {
					EventBus.getDefault().post(new SoftKeyFlickEvent(keyCode, SoftKeyFlickEvent.Direction.RIGHT));
				}
				return false;
			}
			if(!longClickHandler.performed) onKey(keyCode);
			return false;
		}

		public void onCancel() {
			key.onReleased(true);
			mKeyboardView.setPreviewEnabled(false);
			mBackspaceLongClickHandler.removeCallbacksAndMessages(null);
			mKeyboardView.invalidateAllKeys();
			handler.removeCallbacksAndMessages(null);
		}

	}

	class OnKeyboardViewTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int pointerIndex = event.getActionIndex();
			int pointerId = event.getPointerId(pointerIndex);
			int action = event.getActionMasked();
			float x = event.getX(pointerIndex), y = event.getY(pointerIndex);
			TouchPoint point = mTouchPoints.get(pointerId);
			switch(action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				Keyboard.Key key = findKey(mCurrentKeyboard, (int) x, (int) y);
				if(key == null) return true;
				point = new TouchPoint(key, x, y);
				mTouchPoints.put(pointerId, point);
				return true;

			case MotionEvent.ACTION_MOVE:
				if(point == null) return false;
				return point.onMove(x, y);

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				if(point == null) return false;
				point.onUp();
				mTouchPoints.remove(pointerId);
				return true;

			case MotionEvent.ACTION_CANCEL:
				for(int i = 0; i < mTouchPoints.size(); i++) {
					mTouchPoints.valueAt(i).onCancel();
				}
				mTouchPoints.clear();
				return true;

			}
			return false;
		}

		private Keyboard.Key findKey(Keyboard keyboard, int x, int y) {
			for(Keyboard.Key key : keyboard.getKeys()) {
				if(key.isInside(x, y)) return key;
			}
			return null;
		}

	}

	public DefaultSoftKeyboard(SebeolHangulIME parent) {
		mIME = parent;
		mCurrentLanguage = mLanguageCycleTable[mCurrentLanguageIndex];
		mCurrentKeyboardType = KEYBOARD_QWERTY;
		mShiftOn = KEYBOARD_SHIFT_OFF;

	}

	@Override
	protected void createKeyboards(SebeolHangulIME parent) {
		/* Keyboard[# of Languages][portrait/landscape][# of keyboard type][shift off/on][max # of key-modes][subkeyboard] */
		mKeyboard = new Keyboard[4][2][4][2][KEYMODE_LENGTH][4];
		mNumKeyboard = new Keyboard[4][2][4][2][1][4];

		mCurrentKeyboards = new EngineMode[4];

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mIME);

		Keyboard[][][] keyList;

		mUse12Key = pref.getBoolean("keyboard_hangul_use_12key", false);
		mUseAlphabetQwerty = pref.getBoolean("keyboard_alphabet_use_qwerty", true);
		mUseExtensionRow = pref.getBoolean("keyboard_use_extension_row", true);
		// LANGKEY_TOGGLE_ONE_HAND_MODE 같은 빠른 토글 액션은 preference만 바꾸고 InputViewChangeEvent를
		// 바로 posting하는데, 그 이벤트로 다시 불리는 initView()/createKeyboards()에서 preference를
		// 새로 읽지 않으면 mOneHandedMode가 오래된 값에 머물러 화면이 갱신되지 않는다. 다른 mUse12Key 등과
		// 마찬가지로 매번 새로 읽어와야 한다.
		mOneHandedMode = pref.getBoolean("keyboard_one_hand", false);
		mOneHandedRatio = readOneHandedRatio(pref);

		boolean use12Key = mUse12Key, useAlphabetQwerty = mUseAlphabetQwerty;

		if(!mHardKeyboardHidden) {
			mHardwareLayout = true;
			use12Key = false;
		} else {
			mHardwareLayout = false;
		}

		if(mDisplayMode == LANDSCAPE) {
			use12Key = false;
		}

		if(use12Key) {
			keyList = mKeyboard[LANG_KO][mDisplayMode][KEYBOARD_12KEY];

			mCurrentKeyboardType = KEYBOARD_12KEY;
			String defaultLayout = pref.getString("keyboard_hangul_12key_layout", "keyboard_12key_sebul_munhwa");

			switch(defaultLayout) {
			case "keyboard_12key_dubul_cheonjiin":
			case "keyboard_12key_dubul_cheonjiin_predictive":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_HANGUL][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_dubul_cheonjiin);
				break;

			case "keyboard_12key_dubul_naratgeul":
			case "keyboard_12key_dubul_naratgeul_predictive":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_HANGUL][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_dubul_naratgeul);
				break;

			case "keyboard_12key_dubul_naratgeul_center":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_HANGUL][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_dubul_naratgeul_center);
				break;

			case "keyboard_12key_dubul_sky2":
			case "keyboard_12key_dubul_sky2_predictive":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_HANGUL][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_dubul_sky2);
				break;

			case "keyboard_dubul_danmoeum_google":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_HANGUL][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_dubul_danmoeum_google);
				break;

			case "keyboard_12key_sebul_munhwa":
			case "keyboard_12key_sebul_munhwa_predictive":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_HANGUL][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_sebul_munhwa);
				break;

			case "keyboard_12key_sebul_hanson":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_HANGUL][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_sebul_hanson);
				break;

			case "keyboard_12key_sebul_sena":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_HANGUL][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_sebul_sena);
				break;

			}
			keyList[KEYBOARD_SHIFT_OFF][KEYMODE_NUMERIC_12KEY][0] = loadKeyboardLayout(mIME,
					"keyboard_12key_dubul_naratgeul_center".equals(defaultLayout)
							? R.xml.keyboard_ko_12key_numeric_naratgeul_center
							: R.xml.keyboard_ko_12key_numeric);
			mCurrentKeyboards[LANG_KO] = EngineMode.get(defaultLayout);

		} else {

			keyList = mKeyboard[LANG_KO][mDisplayMode][KEYBOARD_QWERTY];

			mCurrentKeyboardType = KEYBOARD_QWERTY;
			useAlphabetQwerty = true;
			String defaultLayout = "keyboard_sebul_391";
			if(!mHardKeyboardHidden) {
				if(pref.getBoolean("keyboard_dev_use_hangul_hard", false)) {
					defaultLayout = pref.getString("keyboard_dev_hard_layout", defaultLayout);
				} else {
					defaultLayout = pref.getString("hardware_hangul_layout", defaultLayout);
				}
			} else {
				if(pref.getBoolean("keyboard_dev_use_hangul_soft", false)) {
					defaultLayout = pref.getString("keyboard_dev_soft_layout", defaultLayout);
				} else {
					defaultLayout = pref.getString("keyboard_hangul_layout", "keyboard_dubul_standard");
				}
			}
			mCurrentKeyboards[LANG_KO] = EngineMode.get(defaultLayout);

			String softLayout = "l1.2";
			if(mDisplayMode == PORTRAIT) softLayout = pref.getString("keyboard_hangul_soft_layout_portrait", softLayout);
			else softLayout = pref.getString("keyboard_hangul_soft_layout_landscape", softLayout);
			loadSoftLayout(keyList, softLayout);

		}

		String altSoftLayout = "l1.0";
		if(mDisplayMode == PORTRAIT) altSoftLayout = pref.getString("keyboard_symbols_soft_layout_portrait", altSoftLayout);
		else altSoftLayout = pref.getString("keyboard_symbols_soft_layout_landscape", altSoftLayout);
		String altLayout = pref.getString("keyboard_symbols_layout", "keyboard_symbols_a");
		mAltKeyMode = EngineMode.get(altLayout);
		loadSoftLayout(keyList, KEYMODE_ALT_SYMBOLS, altSoftLayout);

		if(useAlphabetQwerty) {

			keyList = mKeyboard[LANG_EN][mDisplayMode][mCurrentKeyboardType];

			String defaultLayout = "keyboard_alphabet_qwerty";
			if(!mHardKeyboardHidden) {
				defaultLayout = pref.getString("hardware_alphabet_layout", "keyboard_alphabet_qwerty");
			} else {
				defaultLayout = pref.getString("keyboard_alphabet_layout", "keyboard_alphabet_qwerty");
			}
			mCurrentKeyboards[LANG_EN] = EngineMode.get(defaultLayout);

			String softLayout = "l1.0";
			if(mDisplayMode == PORTRAIT) softLayout = pref.getString("keyboard_alphabet_soft_layout_portrait", softLayout);
			else softLayout = pref.getString("keyboard_alphabet_soft_layout_portrait", softLayout);
			loadSoftLayout(keyList, softLayout);

		} else {

			keyList = mKeyboard[LANG_EN][mDisplayMode][mCurrentKeyboardType];

			String defaultLayout = pref.getString("keyboard_alphabet_12key_layout", "keyboard_12key_alphabet_narrow_a");

			switch(defaultLayout) {
			case "keyboard_12key_alphabet_wide_a":
			case "keyboard_12key_alphabet_wide_a_predictive":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_wide_a);
				keyList[KEYBOARD_SHIFT_ON][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_wide_a_shift);
				break;

			case "keyboard_12key_alphabet_wide_b":
			case "keyboard_12key_alphabet_wide_b_predictive":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_wide_b);
				keyList[KEYBOARD_SHIFT_ON][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_wide_b_shift);
				break;

			case "keyboard_12key_alphabet_narrow_a":
			case "keyboard_12key_alphabet_narrow_a_predictive":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_narrow_a);
				keyList[KEYBOARD_SHIFT_ON][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_narrow_a_shift);
				break;

			case "keyboard_12key_alphabet_narrow_b":
			case "keyboard_12key_alphabet_narrow_b_predictive":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_narrow_b);
				keyList[KEYBOARD_SHIFT_ON][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_narrow_b_shift);
				break;

			case "keyboard_12key_alphabet_smallqwerty":
			case "keyboard_12key_alphabet_smallqwerty_predictive":
				keyList[KEYBOARD_SHIFT_OFF][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_smallqwerty);
				keyList[KEYBOARD_SHIFT_ON][KEYMODE_ENGLISH][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_english_smallqwerty_shift);
				break;

			}
			keyList[KEYBOARD_SHIFT_OFF][KEYMODE_NUMERIC_12KEY][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_12key_numeric);
			mCurrentKeyboards[LANG_EN] = EngineMode.get(defaultLayout);

		}

		loadSoftLayout(keyList, KEYMODE_ALT_SYMBOLS, altSoftLayout);

		keyList = mNumKeyboard[LANG_KO][mDisplayMode][mCurrentKeyboardType];
		keyList[KEYBOARD_SHIFT_OFF][0][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_special_number);
		keyList[KEYBOARD_SHIFT_ON][0][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_special_number_shift);

		keyList = mNumKeyboard[LANG_EN][mDisplayMode][mCurrentKeyboardType];
		keyList[KEYBOARD_SHIFT_OFF][0][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_special_number);
		keyList[KEYBOARD_SHIFT_ON][0][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_special_number_shift);

	}

	public void changeKeyMode(int keyMode) {
		int targetMode = filterKeyMode(keyMode);
		if(targetMode == INVALID_KEYMODE) {
			return;
		}

		EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT)));
		if(mCapsLock) {
			mCapsLock = false;
		}
		mShiftOn = KEYBOARD_SHIFT_OFF;
		Keyboard kbd = getModeChangeKeyboard(targetMode);
		mCurrentKeyMode = targetMode;

		EngineMode mode = EngineMode.DIRECT;

		if(targetMode == KEYMODE_HANGUL || targetMode == KEYMODE_ENGLISH) {
			mode = mCurrentKeyboards[mCurrentLanguage];
		} else if(targetMode == KEYMODE_ALT_SYMBOLS) {
			mode = mAltKeyMode;
		}

		EventBus.getDefault().post(new EngineModeChangeEvent(mode));

		changeKeyboard(kbd);
		if(mNumKeyboard != null) {
			changeNumKeyboard(mNumKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][0][0]);
		}

		mLastKeyMode = mCurrentKeyMode;
	}

	public void setDefaultKeyboard() {
		if(mForceHangul) {
			mCurrentLanguage = LANG_KO;
			mCurrentLanguageIndex = 1;
			changeKeyMode(KEYMODE_HANGUL);
			return;
		}
		Locale locale = Locale.getDefault();
		int language = mCurrentLanguage;

		if(mReturnLanguage != -1) {
			mCurrentLanguageIndex = mReturnLanguage;
			language = mLanguageCycleTable[mReturnLanguage];
			mReturnLanguage = -1;
		}
		if(mPreferenceLanguage != -1) {
			mReturnLanguage = mCurrentLanguageIndex;
			mCurrentLanguageIndex = mPreferenceLanguage;
			language = mLanguageCycleTable[mPreferenceLanguage];
		}
		mCurrentLanguage = language;
		changeKeyMode(KEYMODE_HANGUL);
	}

	@Override
	public View initView(SebeolHangulIME parent, int width, int height) {
		mIME = parent;
		mDisplayMode =
				(parent.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
						? LANDSCAPE : PORTRAIT;

		createKeyboards(parent);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(parent);
		String skin = pref.getString("keyboard_skin", mIME.getResources().getString(R.string.keyboard_skin_id_default));
		int id = parent.getResources().getIdentifier("keyboard_" + skin, "layout", parent.getPackageName());
		if(id == 0) id = R.layout.keyboard_white;
		mKeyboardView = (KeyboardView) mIME.getLayoutInflater().inflate(id, null);
		mKeyboardView.setOnKeyboardActionListener(this);
		mCurrentKeyboard = null;
		if(mKeyboardView instanceof DefaultSoftKeyboardView) {
			((DefaultSoftKeyboardView) mKeyboardView).setKeyboardDisplay(mKeyboardDisplays.get(skin));
			((DefaultSoftKeyboardView) mKeyboardView).setHoldLabels(mKeyHoldLabels);
		}

		mNumKeyboardView = (KeyboardView) mIME.getLayoutInflater().inflate(id, null);
		mNumKeyboardView.setOnKeyboardActionListener(this);

		mMainView = (ViewGroup) parent.getLayoutInflater().inflate(R.layout.keyboard_default_main, null);
		mSubView = (ViewGroup) parent.getLayoutInflater().inflate(R.layout.keyboard_default_sub, null);

		boolean initialLaunch = pref.getBoolean("initial_launch", true);
		if(initialLaunch) {
			final View help = parent.getLayoutInflater().inflate(R.layout.initial_launch_helper, null);
			mMainView.addView(help);
			Button close = (Button) mMainView.findViewById(R.id.close);
			close.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mMainView.removeView(help);
					SharedPreferences.Editor editor = pref.edit();
					editor.putBoolean("initial_launch", false);
					editor.apply();
				}
			});
		}

		if (!mHardKeyboardHidden) {
			if(mShowSubView) mMainView.addView(mSubView);
			if(mShowNumKeyboardViewPortrait && mDisplayMode == PORTRAIT) mMainView.addView(mNumKeyboardView);
			if(mShowNumKeyboardViewLandscape && mDisplayMode == LANDSCAPE) mMainView.addView(mNumKeyboardView);
		} else if (mKeyboardView != null) {
			if(mUseExtensionRow) mMainView.addView(buildExtensionRow(skin));
			mMainView.addView(buildKeyboardArea(skin));
		}
		mKeyboardView.setOnTouchListener(new OnKeyboardViewTouchListener());
		mNumKeyboardView.setOnTouchListener(new OnKeyboardViewTouchListener());
		TextView langView = mSubView.findViewById(R.id.lang);
		langView.setOnTouchListener((v, event) -> {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				if(mVibrator != null) {
					mVibrator.vibrate(30);
				}
				nextLanguage();
				updateIndicator(HARD_KEYMODE_LANG + mCurrentLanguage);
			}
			return false;
		});

		// mKeyboardView는 여기서 막 새로 inflate된 상태라 아직 실제 Keyboard가 배정되어 있지 않다.
		// 원래는 이후 onStartInputView()의 setPreferences() -> setDefaultKeyboard()에서 배정되지만,
		// LANGKEY_TOGGLE_ONE_HAND_MODE 같은 빠른 토글 액션은 setPreferences() 없이 InputViewChangeEvent만
		// 곧바로 posting해서 initView()를 다시 부른다. 그 경로에서는 Keyboard가 끝내 배정되지 않아
		// KeyboardView가 계속 크기 0으로 남는다(안드로이드 KeyboardView는 Keyboard가 없으면 padding만큼만
		// 그린다). changeKeyMode()를 그대로 다시 부르면 시프트 키 이벤트/엔진 모드 변경 이벤트까지
		// 다시 브로드캐스트되어 부작용이 생기므로, 지금 모드에 맞는 Keyboard만 조용히 다시 배정한다.
		if(mCurrentKeyMode != INVALID_KEYMODE) {
			changeKeyboard(getModeChangeKeyboard(mCurrentKeyMode));
		}

		return mMainView;
	}

	private LinearLayout buildExtensionRow(String skin) {
		int rowBgColor, keyBgRes, textColor;
		switch(skin) {
		case "white":
			rowBgColor = 0xFFDBDEE3;	// keybg_white_bg
			keyBgRes = R.drawable.keybg_white_mod_def;
			textColor = Color.BLACK;
			break;

		case "flat_dark":
			rowBgColor = 0xFF263238;	// keybg_flat_bg
			keyBgRes = R.drawable.keybg_flat_def;
			textColor = 0xFFD4D6D7;
			break;

		case "flat_blue":
			rowBgColor = 0xFF1D2047;	// keybg_blue_bg (top of gradient)
			keyBgRes = R.drawable.keybg_blue_def;
			textColor = Color.WHITE;
			break;

		case "dark":
		default:
			rowBgColor = 0xFF0A0A0A;	// keybg_dark_bg
			keyBgRes = R.drawable.keybg_dark_mod_def;
			textColor = Color.WHITE;
			break;
		}
		LinearLayout row = new LinearLayout(mIME);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setBackgroundColor(rowBgColor);
		row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(38)));

		row.addView(buildExtensionKey("▲", keyBgRes, textColor, v -> onKey(KEYCODE_UP)));
		row.addView(buildExtensionKey("▼", keyBgRes, textColor, v -> onKey(KEYCODE_DOWN)));
		row.addView(buildExtensionKey("●", keyBgRes, textColor, this::showClipboardMenu));
		row.addView(buildExtensionKey("◀", keyBgRes, textColor, v -> onKey(KEYCODE_LEFT)));
		row.addView(buildExtensionKey("▶", keyBgRes, textColor, v -> onKey(KEYCODE_RIGHT)));

		return row;
	}

	// 한손 모드일 때는 키보드 뷰 위에 빈 공간 전환용 삼각형 키를 겹쳐 그린다.
	private View buildKeyboardArea(String skin) {
		if(!mOneHandedMode) return mKeyboardView;

		FrameLayout frame = new FrameLayout(mIME);
		frame.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		// 이 시점에는 mKeyboardView에 실제 Keyboard가 아직 배정되지 않은 상태다(배정은 이후
		// setPreferences() 쪽에서 이뤄진다). 그래서 높이를 wrap_content로만 두면 Keyboard가 배정된
		// 뒤에야 한 번 더 레이아웃이 갱신되고, 그 사이 프레임이 빈 상태(0에 가까운 높이)로 잠깐
		// 그려졌다가 뒤늦게 채워지는 경우가 생긴다. createKeyboards()가 이미 만들어 둔 키보드 중
		// 하나에서 높이를 미리 읽어와 처음부터 정확한 크기로 고정해두면 그 빈 프레임이 보이지 않는다.
		int estimatedHeight = estimateKeyboardHeight();
		FrameLayout.LayoutParams kbdParams = (estimatedHeight > 0)
				? new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, estimatedHeight)
				: new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		mKeyboardView.setLayoutParams(kbdParams);

		frame.addView(mKeyboardView);
		frame.addView(buildOneHandSwitchKey(skin));
		return frame;
	}

	private static final int ONE_HANDED_RATIO_DEFAULT = 67;
	private static final String LEGACY_ONE_HANDED_RATIO_DEFAULT_MIGRATED_KEY = "keyboard_one_hand_ratio_default_migrated";

	// 예전 버전은 한손 모드 크기 기본값이 90(거의 안 좁혀짐)이었다. 설정 화면을 한 번이라도 열었던
	// 사용자는 그 예전 기본값이 preference에 실제로 저장되어 있어서, XML/코드의 기본값만 2/3로
	// 바꿔도 적용되지 않는다. 사용자가 직접 다른 값으로 바꾼 적이 없어 보이는(예전 기본값 그대로인)
	// 경우에 한해 딱 한 번만 새 기본값(2/3)로 옮겨준다.
	private int readOneHandedRatio(SharedPreferences pref) {
		if(!pref.contains(LEGACY_ONE_HANDED_RATIO_DEFAULT_MIGRATED_KEY)) {
			SharedPreferences.Editor editor = pref.edit();
			if(pref.getInt("keyboard_one_hand_ratio", ONE_HANDED_RATIO_DEFAULT) == 90) {
				editor.putInt("keyboard_one_hand_ratio", ONE_HANDED_RATIO_DEFAULT);
			}
			editor.putBoolean(LEGACY_ONE_HANDED_RATIO_DEFAULT_MIGRATED_KEY, true);
			editor.apply();
		}
		return pref.getInt("keyboard_one_hand_ratio", ONE_HANDED_RATIO_DEFAULT);
	}

	private int estimateKeyboardHeight() {
		try {
			Keyboard kbd = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][KEYBOARD_SHIFT_OFF][KEYMODE_HANGUL][0];
			return kbd != null ? kbd.getHeight() : -1;
		} catch (Exception ex) {
			return -1;
		}
	}

	private View buildOneHandSwitchKey(String skin) {
		int bgColor, textColor;
		switch(skin) {
		case "white":
			bgColor = 0xFFDBDEE3;	// keybg_white_bg
			textColor = Color.BLACK;
			break;

		case "flat_dark":
			bgColor = 0xFF263238;	// keybg_flat_bg
			textColor = 0xFFD4D6D7;
			break;

		case "flat_blue":
			bgColor = 0xFF1D2047;	// keybg_blue_bg (top of gradient)
			textColor = Color.WHITE;
			break;

		case "dark":
		default:
			bgColor = 0xFF0A0A0A;	// keybg_dark_bg
			textColor = Color.WHITE;
			break;
		}

		// 키가 오른쪽으로 치우쳐 있으면 빈 공간은 왼쪽에 있으므로, 눌렀을 때 그쪽으로
		// 옮겨간다는 뜻으로 왼쪽 삼각형을 보여준다. 반대쪽도 마찬가지 논리다.
		boolean keysOnRight = (mOneHandedSide == ONE_HAND_RIGHT);
		String glyph = keysOnRight ? "◀" : "▶";

		int screenWidth = mIME.getResources().getDisplayMetrics().widthPixels;
		int emptyWidth = (int) (screenWidth * (1d - mOneHandedRatio / 100d));
		int minWidth = dip(28);
		if(emptyWidth < minWidth) emptyWidth = minWidth;

		int keyHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				(mDisplayMode == PORTRAIT) ? mKeyHeightPortrait : mKeyHeightLandscape,
				mIME.getResources().getDisplayMetrics());

		TextView key = new TextView(mIME);
		key.setText(glyph);
		key.setTextColor(textColor);
		key.setTextSize(16);
		key.setGravity(Gravity.CENTER);
		key.setBackgroundColor(bgColor);

		// 빈 공간 전체 높이로 늘어나면 세로로 길쭉해 보이니, 키 한 줄 높이만큼만 잡고
		// 세로 중앙에 오도록 한다.
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(emptyWidth, keyHeight);
		lp.gravity = (keysOnRight ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
		key.setLayoutParams(lp);

		key.setFocusable(false);
		key.setFocusableInTouchMode(false);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			key.setDefaultFocusHighlightEnabled(false);
		}
		key.setOnClickListener(v -> toggleOneHandedSide());
		return key;
	}

	private TextView buildExtensionKey(String glyph, int bgRes, int textColor, View.OnClickListener listener) {
		TextView key = new TextView(mIME);
		key.setText(glyph);
		key.setTextColor(textColor);
		key.setTextSize(16);
		key.setGravity(Gravity.CENTER);
		key.setBackgroundResource(bgRes);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
		lp.setMargins(dip(1), 0, dip(1), 0);
		key.setLayoutParams(lp);
		key.setFocusable(false);
		key.setFocusableInTouchMode(false);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			key.setDefaultFocusHighlightEnabled(false);
		}
		key.setOnClickListener(listener);
		return key;
	}

	private PopupWindow mClipboardMenu;
	private long mClipboardMenuDismissedAt;

	private void showClipboardMenu(View anchor) {
		if(mClipboardMenu != null && mClipboardMenu.isShowing()) {
			mClipboardMenu.dismiss();
			return;
		}
		// 동그라미 키를 다시 눌러 팝업을 닫는 것과, 그 같은 터치가 바깥 터치로도 잡혀 곧바로
		// dismiss()가 호출되는 경우가 겹치면 여기서 새로 열어버려 꺼졌다 켜지는 것처럼 보인다.
		// 방금 닫힌 직후의 터치라면 재오픈하지 않고 무시한다.
		if(SystemClock.uptimeMillis() - mClipboardMenuDismissedAt < 250) {
			return;
		}

		LinearLayout menu = new LinearLayout(mIME);
		menu.setOrientation(LinearLayout.HORIZONTAL);
		menu.setBackgroundColor(0xFF303030);

		String[] labels = {"a", "c", "x", "v"};
		final int[] actionIds = {android.R.id.selectAll, android.R.id.copy, android.R.id.cut, android.R.id.paste};

		final PopupWindow popup = new PopupWindow(menu, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
		popup.setOutsideTouchable(true);
		popup.setTouchable(true);
		popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
		popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popup.setOnDismissListener(() -> {
			mClipboardMenu = null;
			mClipboardMenuDismissedAt = SystemClock.uptimeMillis();
		});
		// IME 창 위에서는 바깥 터치가 기본 outside-touch 처리로 항상 전달되지 않는 경우가 있어 직접 가로채서 닫는다.
		// (동그라미 키를 다시 눌러도 이 경로로 닫힘)
		popup.setTouchInterceptor((v, event) -> {
			if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				popup.dismiss();
				return true;
			}
			return false;
		});
		mClipboardMenu = popup;

		for(int i = 0; i < labels.length; i++) {
			TextView item = new TextView(mIME);
			item.setText(labels[i]);
			item.setTextColor(Color.WHITE);
			item.setTextSize(18);
			item.setGravity(Gravity.CENTER);
			item.setPadding(dip(20), dip(12), dip(20), dip(12));
			final int actionId = actionIds[i];
			item.setOnClickListener(v -> {
				InputConnection ic = mIME.getCurrentInputConnection();
				if(ic != null) ic.performContextMenuAction(actionId);
				popup.dismiss();
			});
			menu.addView(item);
			if(i < labels.length - 1) {
				View divider = new View(mIME);
				divider.setLayoutParams(new LinearLayout.LayoutParams(dip(1), ViewGroup.LayoutParams.MATCH_PARENT));
				divider.setBackgroundColor(0x33FFFFFF);
				menu.addView(divider);
			}
		}

		popup.showAsDropDown(anchor, 0, 0);
	}

	private int dip(int value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, mIME.getResources().getDisplayMetrics());
	}

	@Override
	protected boolean changeKeyboard(Keyboard keyboard) {
		return super.changeKeyboard(keyboard);
	}

	@Override
	public void changeKeyboardType(int type) {
		super.changeKeyboardType(type);
	}

	@Override
	public void swipeRight() {
		swipeOneHandedMode(ONE_HAND_RIGHT);
	}

	@Override
	public void swipeLeft() {
		swipeOneHandedMode(ONE_HAND_LEFT);
	}

	// 키보드 전체를 오른쪽/왼쪽으로 스와이프하면 그 방향에 해당하는 한손 모드로 들어간다.
	// 이미 그 방향의 한손 모드라면 다시 같은 방향으로 스와이프했을 때 일반 모드로 돌아간다.
	private void swipeOneHandedMode(boolean side) {
		if(mOneHandedMode && mOneHandedSide == side) {
			mOneHandedMode = false;
		} else {
			mOneHandedMode = true;
			mOneHandedSide = side;
		}
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mIME);
		pref.edit().putBoolean("keyboard_one_hand", mOneHandedMode).apply();
		EventBus.getDefault().post(new InputViewChangeEvent());
	}

	// 한손 모드 프레임의 빈 공간을 눌러 왼쪽/오른쪽으로 치우친 배치를 바꾼다.
	private void toggleOneHandedSide() {
		mOneHandedSide = !mOneHandedSide;
		EventBus.getDefault().post(new InputViewChangeEvent());
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		return;
	}

	public void onKey(int primaryCode) {
		if(mDisableKeyInput) {
			return;
		}

		if(mTimeoutHandler != null) {
			mTimeoutHandler.removeCallbacksAndMessages(null);
			mTimeoutHandler = null;
		}

		switch(primaryCode) {
		case KEYCODE_CHANGE_LANG:
			EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_CHANGE_LANG)));
			break;

		case KEYCODE_UP:
			EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP)));
			break;

		case KEYCODE_DOWN:
			EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)));
			break;

		case KEYCODE_LEFT:
			EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT)));
			break;

		case KEYCODE_RIGHT:
			EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)));
			break;

		case KEYCODE_JP12_BACKSPACE:
		case KEYCODE_QWERTY_BACKSPACE:
			EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)));
			break;

		case KEYCODE_QWERTY_SHIFT:
			mCapsLock = false;
			toggleShiftLock();
			updateKeyLabels();
			if(mShiftOn == 0) {
				EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT)));
			} else {
				EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_RIGHT)));;
			}
			break;

		case KEYCODE_QWERTY_ALT:
			processAltKey();
			break;

		case KEYCODE_JP12_ENTER:
		case KEYCODE_QWERTY_ENTER:
			EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)));
			break;

		case KEYCODE_JP12_SPACE:
		case -10:
			EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)));
			break;

		default:
			if((primaryCode <= -200 && primaryCode > -300) || (primaryCode <= -2000 && primaryCode > -3000)) {
				EventBus.getDefault().post(new InputSoftKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode)));
			} else if(primaryCode >= 0) {
				if(mKeyboardView.isShifted()) {
					primaryCode = Character.toUpperCase(primaryCode);
				}
				EventBus.getDefault().post(new InputCharEvent((char) primaryCode));

				if(mKeyboardView.isShifted()) {
					if(!mCapsLock) {
						onKey(KEYCODE_QWERTY_SHIFT);
						if(!mHardKeyboardHidden) mIME.resetHardShift(false);
						mIME.updateMetaKeyStateDisplay();
					}
				}
			}
			break;
		}
		if (!mCapsLock && (primaryCode != com.delightlane.keyboard.DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT)) {

		}
		if(mTimeoutHandler == null && mTimeoutDelay > 0) {
			mTimeoutHandler = new Handler();
			mTimeoutHandler.postDelayed(new TimeOutHandler(), mTimeoutDelay);
		}
	}

	public void setPreviewEnabled(int x) {
		switch(x) {
		case KEYCODE_QWERTY_SHIFT:
		case KEYCODE_QWERTY_ENTER:
		case KEYCODE_JP12_ENTER:
		case KEYCODE_QWERTY_BACKSPACE:
		case KEYCODE_JP12_BACKSPACE:
		case -10:
		case KEYCODE_JP12_SPACE:
			break;
		default:
			mKeyboardView.setPreviewEnabled(mShowKeyPreview);
		}
	}

	public int getLanguageId() {
		return mCurrentLanguageIndex;
	}

	public boolean isUseAlphabetQwerty() {
		return mUseAlphabetQwerty;
	}

	public void setLanguage(int languageId) {
		mCurrentLanguageIndex = languageId;
		mCurrentLanguage = mLanguageCycleTable[languageId];
		changeKeyMode(KEYMODE_HANGUL);
	}

	public void nextLanguage() {
		if(++mCurrentLanguageIndex >= mLanguageCycleTable.length) mCurrentLanguageIndex = 0;
		setLanguage(mCurrentLanguageIndex);
	}

	public void previousLanguage() {
		if(--mCurrentLanguageIndex < 0) mCurrentLanguageIndex = mLanguageCycleTable.length-1;
		setLanguage(mCurrentLanguageIndex);
	}

	public boolean isLastLanguage() {
		return mCurrentLanguageIndex == mLanguageCycleTable.length-1;
	}

	public boolean isFirstLanguage() {
		return mCurrentLanguageIndex == 0;
	}

	public void setShiftState(int shiftState) {
		mShiftOn = (shiftState == 0) ? 1 : 0;
		toggleShiftLock();
	}

	public void setCapsLock(boolean capsLock) {
		mCapsLock = capsLock;
	}

	public boolean isCapsLock() {
		return mCapsLock;
	}

	@Override
	public void toggleShiftLock() {
		super.toggleShiftLock();
		if(mShiftOn != 0) {
			Keyboard newKeyboard = getShiftChangeNumKeyboard(KEYBOARD_SHIFT_ON);
			if(newKeyboard != null) {
				changeNumKeyboard(newKeyboard);
			}
		} else {
			Keyboard newKeyboard = getShiftChangeNumKeyboard(KEYBOARD_SHIFT_OFF);
			if(newKeyboard != null) {
				changeNumKeyboard(newKeyboard);
			}
		}
	}

	@Override
	protected void processAltKey() {
		// mCurrentKeyboardType은 한/영 공용 버킷이라, 한글은 12키인데 영문은 쿼티(기본값)인 조합에서는
		// 실제로 화면에 그려지는 자판이 쿼티이므로 12키 숫자패드가 아니라 쿼티 특수문자패드로 보내야 한다.
		boolean is12KeyShape = mCurrentKeyboardType == KEYBOARD_12KEY
				&& !(mCurrentLanguage == LANG_EN && mUseAlphabetQwerty);
		int altKeyMode = is12KeyShape ? KEYMODE_NUMERIC_12KEY : KEYMODE_ALT_SYMBOLS;
		if(mCurrentKeyMode == altKeyMode) {
			changeKeyMode(KEYMODE_HANGUL);
		} else {
			changeKeyMode(altKeyMode);
		}
	}

	protected boolean changeNumKeyboard(Keyboard keyboard) {

		if (keyboard == null) {
			return false;
		}
		if (mCurrentKeyboard != keyboard) {
			mNumKeyboardView.setKeyboard(keyboard);
			mNumKeyboardView.setShifted((mShiftOn == 0) ? false : true);
			return true;
		} else {
			mNumKeyboardView.setShifted((mShiftOn == 0) ? false : true);
			return false;
		}
	}

	public Keyboard getShiftChangeNumKeyboard(int shift) {
		try {
			Keyboard[] kbd = mNumKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][shift][0];

			if (!mNoInput && kbd[1] != null) {
				return kbd[1];
			}
			return kbd[0];
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public void setPreferences(SharedPreferences pref, EditorInfo editor) {
		super.setPreferences(pref, editor);

		int keyHeightPortrait = pref.getInt("key_height_portrait", mKeyHeightPortrait);
		int keyHeightLandscape = pref.getInt("key_height_landscape", mKeyHeightLandscape);
		if(keyHeightPortrait != mKeyHeightPortrait || keyHeightLandscape != mKeyHeightLandscape) {
			mKeyHeightPortrait = keyHeightPortrait;
			mKeyHeightLandscape = keyHeightLandscape;
			EventBus.getDefault().post(new InputViewChangeEvent());
		}
		int marginLeft = pref.getInt("keyboard_margin_left", mMarginLeft);
		int marginRight = pref.getInt("keyboard_margin_right", mMarginRight);
		int marginBottom = pref.getInt("keyboard_margin_bottom", mMarginBottom);
		if(marginLeft != mMarginLeft || marginRight != mMarginRight || marginBottom != mMarginBottom) {
			mMarginLeft = marginLeft;
			mMarginRight = marginRight;
			mMarginBottom = marginBottom;
			EventBus.getDefault().post(new InputViewChangeEvent());
		}
		boolean oneHandedMode = pref.getBoolean("keyboard_one_hand", false);
		int oneHandedRatio = readOneHandedRatio(pref);
		if(oneHandedMode != mOneHandedMode || oneHandedRatio != mOneHandedRatio) {
			mOneHandedMode = oneHandedMode;
			mOneHandedRatio = oneHandedRatio;
			EventBus.getDefault().post(new InputViewChangeEvent());
		}

		boolean use12Key = pref.getBoolean("keyboard_hangul_use_12key", false);
		boolean useAlphabetQwerty = pref.getBoolean("keyboard_alphabet_use_qwerty", true);
		if(mUse12Key != use12Key || useAlphabetQwerty != mUseAlphabetQwerty) {
			EventBus.getDefault().post(new InputViewChangeEvent());
		}

		boolean useExtensionRow = pref.getBoolean("keyboard_use_extension_row", true);
		if(useExtensionRow != mUseExtensionRow) {
			mUseExtensionRow = useExtensionRow;
			EventBus.getDefault().post(new InputViewChangeEvent());
		}
		mLongPressTimeout = pref.getInt("keyboard_long_press_timeout", 500);
		mUseFlick = pref.getBoolean("keyboard_use_flick", true);
		mFlickSensitivity = pref.getInt("keyboard_flick_sensitivity", DEFAULT_FLICK_SENSITIVITY);
		mTimeoutDelay = pref.getInt("keyboard_timeout_delay", mTimeoutDelay);
		mSpaceSlideSensitivity = mFlickSensitivity;
		mVibrateDuration = pref.getInt("key_vibration_duration", mVibrateDuration);
		boolean showSubView = pref.getBoolean("hardware_use_subview", true);
		if(showSubView != mShowSubView) {
			mShowSubView = showSubView;
			EventBus.getDefault().post(new InputViewChangeEvent());
		}
		mKeyboardView.setPreviewEnabled(false);
		mNumKeyboardView.setPreviewEnabled(false);
		boolean showNum = pref.getBoolean("hardware_use_numkeyboard", true);
		if(showNum != mShowNumKeyboardViewPortrait || showNum != mShowNumKeyboardViewLandscape) {
			mShowNumKeyboardViewLandscape = mShowNumKeyboardViewPortrait = showNum;
			EventBus.getDefault().post(new InputViewChangeEvent());
		}
		mShowKeyPreview = pref.getBoolean("popup_preview", true);

		mForceHangul = pref.getBoolean("system_force_hangul", false);

		int inputType = editor.inputType;
		if(mHardKeyboardHidden) {

		}

		mLimitedKeyMode = null;
		mPreferenceKeyMode = INVALID_KEYMODE;
		mPreferenceLanguage = -1;
		mNoInput = true;
		mDisableKeyInput = false;
		mCapsLock = false;

		switch(inputType & EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_NUMBER:
		case EditorInfo.TYPE_CLASS_DATETIME:

			break;
		case EditorInfo.TYPE_CLASS_TEXT:
			switch(inputType & EditorInfo.TYPE_MASK_VARIATION) {
			case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
			case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
				mPreferenceLanguage = mLanguageCycleTable[0];
				break;

			case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
			case EditorInfo.TYPE_TEXT_VARIATION_URI:
				mPreferenceLanguage = mLanguageCycleTable[0];
				break;

			default:
				break;
			}
			break;

		default:
			break;
		}

		if(inputType != mLastInputType) {
			mLastInputType = inputType;
		}
		setDefaultKeyboard();
	}

	protected void loadSoftLayout(Keyboard[][][] keyList, String softLayout) {
		this.loadSoftLayout(keyList, KEYMODE_HANGUL, softLayout);
	}

	protected void loadSoftLayout(Keyboard[][][] keyList, int keyMode, String softLayout) {
		switch(softLayout) {
		case "l1.0":
			keyList[KEYBOARD_SHIFT_OFF][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_0_mobile);
			keyList[KEYBOARD_SHIFT_ON][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_0_mobile);
			break;

		case "l1.1":
			keyList[KEYBOARD_SHIFT_OFF][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_1_mobile_num);
			keyList[KEYBOARD_SHIFT_ON][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_1_mobile_num);
			break;

		case "l1.2":
			keyList[KEYBOARD_SHIFT_OFF][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_2_mod_quote);
			keyList[KEYBOARD_SHIFT_ON][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_2_mod_quote);
			break;

		case "l1.3":
			keyList[KEYBOARD_SHIFT_OFF][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_3_punc_grave);
			keyList[KEYBOARD_SHIFT_ON][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_3_punc_grave);
			break;

		case "l1.4":
			keyList[KEYBOARD_SHIFT_OFF][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_4_punc_slash);
			keyList[KEYBOARD_SHIFT_ON][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_4_punc_slash);
			break;

		case "l1.9":
			keyList[KEYBOARD_SHIFT_OFF][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_9_colemak);
			keyList[KEYBOARD_SHIFT_ON][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_9_colemak);
			break;

		case "l1.10":
			keyList[KEYBOARD_SHIFT_OFF][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_10_dvorak);
			keyList[KEYBOARD_SHIFT_ON][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l1_10_dvorak);
			break;

		case "l2.0":
			keyList[KEYBOARD_SHIFT_OFF][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l2_0_11cols);
			keyList[KEYBOARD_SHIFT_ON][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_l2_0_11cols);
			break;

		case "pc1":
			keyList[KEYBOARD_SHIFT_OFF][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_pc1_alphanumeric);
			keyList[KEYBOARD_SHIFT_ON][keyMode][0] = loadKeyboardLayout(mIME, R.xml.keyboard_ko_pc1_alphanumeric);
			break;

		}
	}

	@SuppressWarnings("deprecation")
	public Keyboard loadKeyboardLayout(Context context, int xmlLayoutResId) {
		CustomKeyboard keyboard = new CustomKeyboard(context, xmlLayoutResId);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mIME);
		String skin = pref.getString("keyboard_skin",
				mIME.getResources().getString(R.string.keyboard_skin_id_default));
		int icon = 0;
		switch(skin) {
		case "white":
			icon = 1;
			break;

		default:
			icon = 0;
		}
		DisplayMetrics metrics = mIME.getResources().getDisplayMetrics();
		int height = (mDisplayMode == PORTRAIT) ? mKeyHeightPortrait : mKeyHeightLandscape;
		height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, metrics);
		keyboard.resizeHeight(height);
		keyboard.setMargins(mMarginLeft, mMarginRight, mMarginBottom);
		if(mOneHandedMode) {
			keyboard.oneHandedMode(mIME, mOneHandedSide, mOneHandedRatio / 100d);
		}
		SoftKeyboardDisplay keyboardDisplay = mKeyboardDisplays.get(skin);
		if(keyboardDisplay != null) {
			for (Keyboard.Key key : keyboard.getKeys()) {
				SoftKeyDisplay keyDisplay = keyboardDisplay.get(key.codes[0]);
				if (keyDisplay != null) {
					if (keyDisplay.getKeyIcon() == 0) continue;
					Drawable drawable = mIME.getResources().getDrawable(keyDisplay.getKeyIcon());
					key.icon = drawable;
					key.iconPreview = drawable;
				}
			}
		}

		return keyboard;
	}

	private int filterKeyMode(int keyMode) {
		int targetMode = keyMode;
		int[] limits = mLimitedKeyMode;

		if(limits != null) {
			boolean hasAccepted = false;
			boolean hasRequiredChange = false;
			int size = limits.length;
			int nowMode = mCurrentKeyMode;

			for(int i = 0 ; i < size ; i++) {
				if(targetMode == limits[i]) {
					hasAccepted = true;
					break;
				}
				if(nowMode == limits[i]) {
					hasRequiredChange = false;
				}
			}

			if(!hasAccepted) {
				if(hasRequiredChange) {
					targetMode = mLimitedKeyMode[0];
				} else {
					targetMode = INVALID_KEYMODE;
				}
			}
		}
		return targetMode;
	}

	public void fixHardwareLayoutState() {
		if(mHardwareLayout == mHardKeyboardHidden) {
			EventBus.getDefault().post(new InputViewChangeEvent());
		}
	}

	@Override
	public void updateIndicator(int mode) {
		if(mSubView == null) return;
		TextView text = (TextView) mSubView.findViewById(R.id.lang);
		switch(mode) {
		case HARD_KEYMODE_LANG_ENGLISH:
			text.setText(R.string.indicator_lang_en);
			break;
			
		case HARD_KEYMODE_LANG_KOREAN:
			text.setText(R.string.indicator_lang_ko);
			break;
			
		default:
			super.updateIndicator(mode);
			break;
			
		}
	}

	public void updateKeyLabels() {
		if(mCurrentKeyboardType == KEYBOARD_12KEY
				&& mCurrentKeyMode != KEYMODE_ALT_SYMBOLS
				&& !(mCurrentLanguage == LANG_EN && mUseAlphabetQwerty)) {
			updateTwelveKeyHoldLabels();
			return;
		}
		int[][] layout;
		if(mCurrentKeyMode != KEYMODE_ALT_SYMBOLS) {
			HangulEngine hangulEngine = mIME.getHangulEngine();
			layout = hangulEngine.getJamoTable();
		} else {
			layout = mIME.getAltSymbols();
		}
		updateLabels(mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0], layout);
		mKeyboardView.invalidateAllKeys();
		mKeyboardView.requestLayout();
	}

	protected void updateLabels(Keyboard kbd, int[][] layout) {
		if(!(kbd instanceof CustomKeyboard)) return;
		mKeyHoldLabels.clear();
		boolean isAltSymbols = (mCurrentKeyMode == KEYMODE_ALT_SYMBOLS);
		boolean isEnglishQwerty = (mCurrentLanguage == LANG_EN && mUseAlphabetQwerty && !isAltSymbols);
		if(layout == null) {
			for(Keyboard.Key key : kbd.getKeys()) {
				String label = getKeyLabel(key.codes[0], mShiftOn > 0);
				if(label != null) key.label = label;
				if(isEnglishQwerty) putEnglishQwertyHoldLabel(key);
			}
			return;
		}
		for(Keyboard.Key key : kbd.getKeys()) {
			boolean found = false;
			for(int[] item : layout) {
				if(key.codes[0] == 128) break;
				if(key.codes[0] == item[0]) {
					int code = item[mShiftOn + 1] & 0xffff;
					String label = getKeyLabel(code, false);
					if(label != null) key.label = label;
					if(isAltSymbols && item.length > 2) {
						String holdLabel = getKeyLabel(item[2] & 0xffff, false);
						if(holdLabel != null && !holdLabel.equals(label)) mKeyHoldLabels.put(key, holdLabel);
					}
					found = true;
					break;
				}
			}
			if(!found) {
				String label = getKeyLabel(key.codes[0], mShiftOn > 0);
				if(label != null) key.label = label;
			}
			if(isEnglishQwerty) putEnglishQwertyHoldLabel(key);
		}
	}

	// 12키 자판은 XML의 정적 라벨을 그대로 쓰므로 key.label은 건드리지 않고, hold 시 입력되는 숫자만 우상단에 표시한다.
	private void updateTwelveKeyHoldLabels() {
		Keyboard kbd = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];
		if(!(kbd instanceof CustomKeyboard)) return;
		mKeyHoldLabels.clear();
		for(Keyboard.Key key : kbd.getKeys()) {
			for(int[] item : Layout12KeyDubul.CYCLE_PREDICTIVE) {
				if(item[0] == key.codes[0]) {
					mKeyHoldLabels.put(key, String.valueOf((char) item[1]));
					break;
				}
			}
		}
		mKeyboardView.invalidateAllKeys();
	}

	private void putEnglishQwertyHoldLabel(Keyboard.Key key) {
		for(int[] item : LayoutAlphabet.HOLD_ENGLISH_QWERTY) {
			if(item[0] == key.codes[0]) {
				mKeyHoldLabels.put(key, String.valueOf((char) item[1]));
				break;
			}
		}
	}

	private String getKeyLabel(int code, boolean shift) {
		switch(code) {
		case KEYCODE_CHANGE_LANG:
			return mCurrentLanguage == LANG_KO ? "A" : "가";

		case 128:
			return (shift) ? "," : ". ,";

		default:
			if(code >= 0) {
				if(shift) {
					boolean converted = false;
					for (int[] item : SebeolHangulIME.SHIFT_CONVERT) {
						if(item[0] == code) {
							code = item[1];
							converted = true;
							break;
						}
					}
					if(!converted) code = Character.toUpperCase(code);
				}
				if(code >= 0x1100 && code <= 0x1112) code = HangulEngine.CHO_TABLE[code - 0x1100];
				else if(code >= 0x1161 && code <= 0x1175) code = HangulEngine.JUNG_TABLE[code - 0x1161];
				else if(code >= 0x11a8 && code <= 0x11c2) code = HangulEngine.JONG_TABLE[code - 0x11a8 + 1];
				return String.valueOf(Character.toChars(code));
			}
			else return null;
		}
	}

}
