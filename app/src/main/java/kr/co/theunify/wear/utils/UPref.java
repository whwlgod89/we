
package kr.co.theunify.wear.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;


/**
 * Preference 클래스
 *
 */
public class UPref {

	private static final String TAG = UPref.class.getSimpleName();
	
	private static String mPreferenceName = "WEAR_CONFIG_PREF";
	
	public static void setPreferenceName(String name){
		mPreferenceName = name;
	}

	/**
	 * SharedPreferences에 Key, Value로 값을 저장한다. Value는 String이다.
	 * @param context
	 * @param name			preference name
	 * @param key			key name
	 * @param value		String value
	 */
	public static void setString(Context context, String name, String key, String value) {
		SharedPreferences pref = null;
		
		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}
		
		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "setStrPreferences() ==> preference name is not defined...!!");
			return;
		}

		pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		Editor prefEditor = pref.edit();
		prefEditor.putString(key, value);

		prefEditor.commit();
	}

	/**
	 * SharedPreferences에서 String 값을 가져온다.
	 * @param context
	 * @param name		preference name
	 * @param key		key name
	 * @return			String
	 */
	public static String getString(Context context, String name, String key) {
		String returnValue = null;

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}
		
		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "getStrPreferences() ==> preference name is not defined...!!");
			return returnValue;
		}
		
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		returnValue = pref.getString(key, "");

		return returnValue;
	}
	
	/**
	 * SharedPreferences에서 String 값을 가져온다.
	 * @param context
	 * @param name		preference name
	 * @param key		key name
	 * @param def		default value
	 * @return			String
	 */
	public static String getString(Context context, String name, String key, String def) {
		String returnValue = null;

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}
		
		if (TextUtils.isEmpty(name)) {
			ULog.d(TAG, "getStrPreferences() ==> preference name is not defined...!!");
			return returnValue;
		}
		
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		returnValue = pref.getString(key, def);

		return returnValue;
	}


	/**
	 * SharedPreferences에 Key, Value로 값을 저장한다. Value는 boolean이다.
	 * @param context
	 * @param name		preference name
	 * @param key		key name
	 * @param value	boolean value
	 */
	public static void setBool(Context context, String name, String key, boolean value) {
		SharedPreferences pref = null;

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}
		
		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "setBoolPreferences() ==> preference name is not defined...!!");
			return;
		}
		
		pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		Editor prefEditor = pref.edit();
		prefEditor.putBoolean(key, value);

		prefEditor.commit();
	}

	/**
	 * SharedPreferences에서 boolean 값을 가져온다.
	 * @param context
	 * @param name		preference name
	 * @param key		key name
	 * @param def		default value
	 * @return			boolean
	 */
	public static boolean getBool(Context context, String name, String key, boolean def) {
		boolean returnValue = false;

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}
		
		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "getBoolPreferences() ==> preference name is not defined...!!");
			return returnValue;
		}
		
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		returnValue = pref.getBoolean(key, def);

		return returnValue;
	}

	/**
	 * SharedPreferences에 Key, Value로 값을 저장한다. Value는 int이다.
	 * @param context
	 * @param name		preference name
	 * @param key		key name
	 * @param value	int value
	 */
	public static void setInt(Context context, String name, String key, int value) {

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}
		
		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "setIntPreferences() ==> preference name is not defined...!!");
			return;
		}
		
		SharedPreferences pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		Editor prefEditor = pref.edit();
		prefEditor.putInt(key, value);

		prefEditor.commit();
	}

	/**
	 * SharedPreferences에서 int 값을 가져온다.
	 * @param context
	 * @param name		preference name
	 * @param key		key name
	 * @return			int
	 */
	public static int getInt(Context context, String name, String key) {
		int returnValue = 0;

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}
		
		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "getIntPreferences() ==> preference name is not defined...!!");
			return returnValue;
		}
		
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		returnValue = pref.getInt(key, 0);

		return returnValue;
	}
	
	/**
	 * SharedPreferences에서 int 값을 가져온다.
	 * @param context
	 * @param name			preference name
	 * @param key			key name
	 * @param defValue		int value
	 * @return				int
	 */
	public static int getInt(Context context, String name, String key, int defValue) {
		int returnValue = 0;

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}
		
		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "getIntPreferences() ==> preference name is not defined...!!");
			return defValue;
		}
		
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		returnValue = pref.getInt(key, defValue);

		return returnValue;
	}

	/**
	 * key 이름에 저장된 값을 지운다
	 * @param context
	 * @param name		preference name
	 * @param key		key name
	 */
	public static void remove(Context context, String name, String key) {
		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}
		
		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "setIntPreferences() ==> preference name is not defined...!!");
			return;
		}
		
		SharedPreferences pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		Editor prefEditor = pref.edit();
		prefEditor.remove(key);
		
		prefEditor.commit();
	}

	/**
	 * SharedPreferences에 Key, Value로 값을 저장한다. Value는 long이다.
	 * @param context
	 * @param name		preference name
	 * @param key		key name
	 * @param value	long value
	 */
	public static void setLong(Context context, String name, String key, long value) {

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}

		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "setIntPreferences() ==> preference name is not defined...!!");
			return;
		}

		SharedPreferences pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		Editor prefEditor = pref.edit();
		prefEditor.putLong(key, value);

		prefEditor.commit();
	}

	/**
	 * SharedPreferences에서 long 값을 가져온다.
	 * @param context
	 * @param name		preference name
	 * @param key		key name
	 * @return			long
	 */
	public static long getLong(Context context, String name, String key) {
		long returnValue = 0;

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}

		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "getIntPreferences() ==> preference name is not defined...!!");
			return returnValue;
		}

		SharedPreferences pref = null;
		pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		returnValue = pref.getLong(key, 0);

		return returnValue;
	}

	/**
	 * SharedPreferences에서 long 값을 가져온다.
	 * @param context
	 * @param name			preference name
	 * @param key			key name
	 * @param defValue		long value
	 * @return				long
	 */
	public static long getLong(Context context, String name, String key, long defValue) {
		long returnValue = 0;

		if (TextUtils.isEmpty(name)) {
			name = mPreferenceName;
		}

		if (TextUtils.isEmpty(name)) {
			ULog.e(TAG, "getIntPreferences() ==> preference name is not defined...!!");
			return defValue;
		}

		SharedPreferences pref = null;
		pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
		returnValue = pref.getLong(key, defValue);

		return returnValue;
	}


	/**
	 * 모든값을 초기화한다
	 * @param context
	 * @param name		preference name
	 */
	public static void removeAllPreferences(Context context, String name) {
		SharedPreferences pref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.clear();
		editor.commit();
	}

}