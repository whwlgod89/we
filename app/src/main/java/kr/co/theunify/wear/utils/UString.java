package kr.co.theunify.wear.utils;

/**
 * Created by nashine40 on 2017-12-14.
 * String 관련 클래스
 */

public class UString {
    private static final String TAG = UString.class.getSimpleName();

    /**
     * String을 int로 변환하는 함수
     *
     * @param str 변환할 String
     * @return int        변환된 Integer
     */
    public static int convertToInt(String str) {
        if (isEmpty(str) ) {
            return 0;
        }
        if(!isNumber(str)) {
            if (str.contains(".")) {
                String[] tokens = str.split("\\.");
                if (tokens.length > 1) {
                    return  convertToInt(tokens[0]);
                }
            }

            return 0;
        }
            return Integer.valueOf(str);
    }

    /**
     * major.minor.bugfix 형태의 버전 이름 비교
     * @param me        versionName
     * @param other     versionName
     * @return
     * 음수: me가 낮다. 0: 같다. 양수: me가 높다.
     */
    public static int compareNumberString(String me, String other) {

        if (isEmpty(me) || isEmpty(other)) {
            //ULog.e(TAG, "compareNumberString : " + me + " , " + other + " => " + 0);
            return 0;
        }
        me = me.trim();
        other = other.trim();

        String[] meTokens = me.split("\\.");
        String[] otherTokens = other.split("\\.");

        for (int i = 0; i < 3; i++) {
            int meVersion = (meTokens.length - 1) < i ? 0: Integer.valueOf(meTokens[i]);
            int otherVersion = (otherTokens.length - 1) < i ? 0: Integer.valueOf(otherTokens[i]);
            if (meVersion - otherVersion != 0) {
               // ULog.e(TAG, "compareNumberString : " + me + " , " + other + " => " + (meVersion - otherVersion));
                return meVersion - otherVersion;
            }
        }
        //ULog.e(TAG, "compareNumberString : " + me + " , " + other + " => " + 0);
        return 0;
    }

    /**
     * String이 숫자인지 아닌지 판별
     * @param s
     * @return boolean
     */
    public static boolean isNumber(String s) {
        int size = s.length();
        if (size==0) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            int a = s.charAt(i) - 48;
            if (a < 0 || a > 9) {
                return false;
            }
        }
        return true;
    }

    /**
     * String이 null 이거나 비어있는 것인지 확인
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 입력 String이 null인지 확인
     * @param str 입력 String
     * @return
     */
    public static boolean isNull(String str) {
        if (isEmpty(str) || "null".equals(str)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 이름이나 아이디에 * 추가하기
     * @param text
     * @return
     */
    public static String getStartedName(String text) {
        String resp = text;
        //ULog.d(null, TAG, "getStartedName() - input: " + text);

        if (isEmpty(text)) {
            return resp;
        }

        if (text.length()>2) {
            resp = text.substring(0, 2);
            for (int i=2; i<text.length(); i++) {
                resp += "*";
            }
        }

        //ULog.d(null, TAG, "getTruncatedString() - output: " + resp);
        return resp;
    }

}
