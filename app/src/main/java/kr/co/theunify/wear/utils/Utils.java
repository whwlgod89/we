package kr.co.theunify.wear.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.theunify.wear.R;
import kr.co.theunify.wear.dialog.CommonDialog;


/**
 *
 * 유틸 관련 클래스
 */

public class Utils {

    private static String TAG = Utils.class.getSimpleName();

    public static CommonDialog showPopupDlg(Context context, String title, String desc,
                                            String ok, View.OnClickListener okClick,
                                            String cancel, View.OnClickListener cancelClick, Integer descColor) {

        CommonDialog commonDialog = new CommonDialog(context);
        if (!UString.isEmpty(title)) {
            commonDialog.setTitle(title);
        } else {
            commonDialog.setTitle(context.getResources().getString(R.string.app_name));
        }
        commonDialog.setMsg(desc);
        commonDialog.setOk(ok);
        commonDialog.setOkListener(okClick);
        commonDialog.setCancel(cancel);
        commonDialog.setCancelListener(cancelClick);
        if (descColor != null) {
            commonDialog.setMsgColor(descColor);
        }

        return commonDialog;
    }

    /**
     * 폴더 path 가져오기
     * @return
     */
    public static String FOLDER_TEMP = "Temp/";
    public static String getFolderPath(String folder) {

        String ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/SmartApp/";
        File root = new File(ROOT_PATH);
        if (!root.exists()) {
            root.mkdir();
        }

        if (!TextUtils.isEmpty(folder)) {
            ROOT_PATH = ROOT_PATH + folder;
        }

        File f = new File(ROOT_PATH);
        if (!f.exists()) {
            f.mkdir();
        }

        return ROOT_PATH;
    }

    /**
     * 키해시값 가져오기
     * @param context
     */
    public static void getKeyHash(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                ULog.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    /**
     * 키보드 보이기
     * @param context
     * @param et EditText
     */
    public static void showSoftKeyboard(final Context context, final EditText et){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(et, 0);
            }
        }, 500);
    }

    /**
     * 키보드 숨기기
     * @param context
     * @param view
     */
    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    /**
     * 디바이스의 넓이 가져오기
     * @param context
     * @return int 넓이
     */
    public static int getDeviceWidthSize(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }


    public static void setPopupWindowDimming(PopupWindow window, float dimming) {
        View container;
        if (window.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) window.getContentView().getParent();
            } else {
                container = window.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) window.getContentView().getParent().getParent();
            } else {
                container = (View) window.getContentView().getParent();
            }
        }
        Context context = window.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) container.getLayoutParams();
        lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.dimAmount = 0.7f;
        wm.updateViewLayout(container, lp);
    }


    /**
     * 디바이스의 정보 로그 프린트
     * @param context
     */
    public static void printDiviceInfo(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int getDeviceHeight_Pixel = displayMetrics.heightPixels;
        int getDeviceWidth_Pixel = displayMetrics.widthPixels;

        int getDeviceDpi = displayMetrics.densityDpi;

        ULog.d(TAG, "width px = " + getDeviceWidth_Pixel);
        ULog.d(TAG, "height px = " + getDeviceHeight_Pixel);
        ULog.d(TAG, "dpi = " + getDeviceDpi);

    }


    private static final float MDPI = 160;

    public static void printDimmen(Context context) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int getDeviceHeight_Pixel = displayMetrics.heightPixels;
        int getDeviceWidth_Pixel = displayMetrics.widthPixels;

        int getDeviceDpi = displayMetrics.densityDpi;

        ULog.d(TAG, "width px = " + getDeviceWidth_Pixel);
        ULog.d(TAG, "height px = " + getDeviceHeight_Pixel);
        ULog.d(TAG, "dpi = " + getDeviceDpi);
	/*
		1280	: width
		160		: dpi

		dp = px * 160 / dpi

		1280	: width
		213		: dpi
	 */
        float stdWidth = 1920;
//    	float stdDpi = Integer.parseInt(args[1]);
        float tgtWidth = getDeviceHeight_Pixel;
        float tgtDpi = getDeviceDpi;

//    	float stdFactor = MDPI / stdDpi;
        float tgtFactor = MDPI / tgtDpi;
//    	float stdDpWidth = stdWidth * stdFactor;
//    	float tgtDpWidth = tgtWidth * tgtFactor;

        // stdDpWidth : tgtDpWidth = stdFactor : tgtFactor * ratio
        float ratio = tgtWidth / stdWidth;
        System.out.println(String.format("<?xml version=\"1.0\" encoding=\"utf-8\"?>"));
        System.out.println(String.format("<resources>"));

        for (int i = 0; i <= stdWidth; i++) {
            System.out.println(String.format("	<dimen name=\"px%d\">%.2fdp</dimen>", i, i * tgtFactor * ratio));
        }
        System.out.println(String.format("</resources>"));

    }

    public static void printCurrentDimmen(Context context) {

        ULog.e("DIMM", "1 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px1)));
        ULog.e("DIMM", "2 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px2)));
        ULog.e("DIMM", "3 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px3)));
        ULog.e("DIMM", "4 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px4)));
        ULog.e("DIMM", "5 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px5)));
        ULog.e("DIMM", "6 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px6)));
        ULog.e("DIMM", "7 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px7)));
        ULog.e("DIMM", "8 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px8)));
        ULog.e("DIMM", "9 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px9)));
        ULog.e("DIMM", "10 == " + pxToDp(context, context.getResources().getDimension(R.dimen.px10)));

    }

    public static float pxToDp(Context context, float pxValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return pxValue / metrics.density;
    }
}
