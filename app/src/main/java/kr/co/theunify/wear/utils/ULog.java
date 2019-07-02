/**************************************************************************************
 * 
 * File Name: QLog.java
 * 
 *************************************************************************************/

package kr.co.theunify.wear.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * 공용 Log class
 *
 */
public class ULog {

	private static final String TAG = ULog.class.getPackage().getName();
	
	/**
	 * 로그를 보여줄 것인지의 설정 Value
	 * 1회의 설정으로 모든 경우에 적용됨
	 */
	private static boolean isShowLog = true;

	/**
	 * 경과시간을 보여줄 것인지 설정 Value
	 * 1회 설정으로 모든 경우에 적용됨
	 */
	private static boolean isShowTime = true;

	private static long currentTime = 0;

	/**
	 * Priority constant for the println method; use Log.v.
	 */
	private static final int VERBOSE = 2;

	/**
	 * Priority constant for the println method; use Log.d.
	 */
	private static final int DEBUG = 3;

	/**
	 * Priority constant for the println method; use Log.i.
	 */
	private static final int INFO = 4;

	/**
	 * Priority constant for the println method; use Log.w.
	 */
	private static final int WARN = 5;

	/**
	 * Priority constant for the println method; use Log.e.
	 */
	private static final int ERROR = 6;

	// ********************************************************************************
	// public functions
	// ********************************************************************************

	/**
	 * 로그를 보여줄 것인지 설정하는 함수 - 최초 1회로 설정됨
	 * @param isShow 보여줄 지 여부 
	 */
	public static void setVisible(boolean isShow) {
		isShowLog = isShow;
	}

	/**
	 * 로그에서 경과된 시간 정보를 보여줄 것인지 설정하는 함수
	 * @param isShow 보여줄 지 여부
	 */
	public static void setTimeVisible(boolean isShow) {
		isShowTime = isShow;
	}
	
	/**
	 * verbose Log
	 * @param tag	TAG
	 * @param msg	Message
	 */
	public static void v(String tag, String msg) {
		println(VERBOSE, tag, msg);
	}

	/**
	 * debug Log
	 * @param tag	TAG
	 * @param msg	Message
	 */
	public static void d(String tag, String msg) {
		println(DEBUG, tag, msg);
	}

	/**
	 * info Log
	 * @param tag	TAG
	 * @param msg	Message
	 */
	public static void i(String tag, String msg) {
		println(INFO, tag, msg);
	}


	/**
	 * warning Log
	 * @param tag	TAG
	 * @param msg	Message
	 */
	public static void w(String tag, String msg) {
		println(WARN, tag, msg);
	}


	/**
	 * error Log
	 * @param tag	TAG
	 * @param msg	Message
	 */
	public static void e(String tag, String msg) {
		println(ERROR, tag, msg);
	}

	/**
	 * error Log for exception
	 * @param tag	TAG
	 * @param msg	Message
	 * @param ex	Exception
	 */
	public static void e(String tag, String msg, Exception ex) {
		println(ERROR, tag, msg + "\n" + ex);
	}
	
	/**
	 * Verbose Log with Filter
	 * @param tag		TAG
	 * @param filter	Filter
	 * @param msg		Message
	 */
	public static void v(String tag, String filter, String msg) {
		println(VERBOSE, tag, filter, msg);
	}

	/**
	 * Debug Log with Filter
	 * @param tag		TAG
	 * @param filter	Filter
	 * @param msg		Message
	 */
	public static void d(String tag, String filter, String msg) {
		println(DEBUG, tag, filter, msg);
	}

	/**
	 * Info Log with Filter
	 * @param tag		TAG
	 * @param filter	Filter
	 * @param msg		Message
	 */
	public static void i(String tag, String filter, String msg) {
		println(INFO, tag, filter, msg);
	}

	/**
	 * Warning Log with Filter
	 * @param tag		TAG
	 * @param filter	Filter
	 * @param msg		Message
	 */
	public static void w(String tag, String filter, String msg) {
		println(WARN, tag, filter, msg);
	}

	/**
	 * Error Log with Filter
	 * @param tag		TAG
	 * @param filter	Filter
	 * @param msg		Message
	 */
	public static void e(String tag, String filter, String msg) {
		println(ERROR, tag, filter, msg);
	}
	
	/**
	 * Verbose Log with Filter & Exception
	 * @param tag		TAG
	 * @param filter	Filter
	 * @param msg		Message
	 * @param ex		Exception
	 */
	public static void e(String tag, String filter, String msg, Exception ex) {
		println(ERROR, tag, filter, msg + "\n" + ex);
	}

	/**
	 * line on log message
	 * @param tag
	 */
	public static void line(String tag){
		if(tag == null){
			tag = TAG;
		}
		println(ERROR, tag, "===================================================");
	}
	
	public static String getCallerName() {
		String callers = null;
		StackTraceElement[] stacks = new Throwable().getStackTrace();
		for (StackTraceElement stack : stacks) {
			if (callers == null) {
				callers = "";
			} else {
				callers += (" <- " + stack.getClassName() + "::" + stack.getMethodName());
			}
		}
		
		return callers;
	}

	public static void printScreen(Context context) {

		DisplayMetrics displayMetrics = new DisplayMetrics();

		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		int deviceWidth = displayMetrics.widthPixels;

		int deviceHeight = displayMetrics.heightPixels;

		// 꼭 넣어 주어야 한다. 이렇게 해야 displayMetrics가 세팅이 된다.

		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		int dipWidth  = (int) (120  * displayMetrics.density);

		int dipHeight = (int) (90 * displayMetrics.density);

		System.out.println("displayMetrics.density : " + displayMetrics.density);

		System.out.println("deviceWidth : " + deviceWidth +", deviceHeight : "+deviceHeight);

	}
	// ********************************************************************************
	// private functions
	// ********************************************************************************
	
	/**
	 * Low-level logging call.
	 * 
	 * @param priority 	
	 *            The priority/type of this log message
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @return The number of bytes written.
	 */
	private static int println(int priority, String tag, String msg) {
		if (isShowLog) {
			if (tag == null) {
				tag = TAG;
			}
			
			if (isShowTime) {
				msg = checkTime(msg);
			}
			return Log.println(priority, tag, msg);
		}
		
		return 0;
	}

	private static int println(int priority, String tag, String filter, String msg) {
		String filterMsg = makeFilterMsg(filter, msg);
		return println(priority, tag, filterMsg);
	}

	private static String makeFilterMsg(String filter, String msg) {
		return "[" + filter + "] : " + msg;
	}

	/**
	 * 타임의 시간 계산이 필요한 경우에 사용
	 * @param msg
	 * @return
	 */
	private static String checkTime(String msg) {

		long nowTime = System.currentTimeMillis();

		if (currentTime == 0) { // 최초 시간은 현재 시간으로
			currentTime = nowTime;
		}

		// 경과된 시간을 D/P 하기 위한 String 구성
		msg += String.format(" - [Elapsed: %d]", nowTime - currentTime);
		
		currentTime = nowTime;

		return msg;
	}

}
