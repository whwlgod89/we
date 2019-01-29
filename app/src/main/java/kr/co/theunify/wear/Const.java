package kr.co.theunify.wear;

public class Const {
    // 개발용
    public static final boolean DEBUG = false;

    //
    public static boolean mRestartApp = false;
    public static boolean mFinishApp = false;

    // 동작 모드: 분실 방지, 도난 방지 (알람용)
    public static final int MAX_SENSORS = 5;
    public static final int ACTION_MODE_LOSS = 0;
    public static final int ACTION_MODE_THEFT = 1;

    // Activity 호출 요청값
    public static final int REQUEST_CODE_OF_ENABLE_BT = 1;
    public static final int REQUEST_CODE_OF_ACCESS_FINE_LOC = 11;
    public static final int REQUEST_CODE_OF_ACCESS_COARSE_LOC = 12;
    public static final int REQUEST_CODE_OF_READ_EXTERNAL_STORAGE = 21;
    public static final int REQUEST_CODE_OF_WRITE_EXTERNAL_STORAGE = 22;
    public static final int REQUEST_CODE_OF_ADD_SENSOR = 51;     // Added(Main->Search->Register->Main), Canceled(Main->Search->Main, Main->Search->Register->Main)
    //public static final int REQUEST_CODE_OF_SHOW_SENSOR = 52;    // Modified(Main->Info->Modify->Info->Main), Removed(Main->Info->Main), Cancel(Main->Info->Main)
    //public static final int REQUEST_CODE_OF_FIND_SENSOR = 53;    // Nothing...
    public static final int REQUEST_CODE_OF_MODIFY_SENSOR = 54;  // Modified(Info->Modify->Info), Canceled(Info->Modify->Info)
    //public static final int REQUEST_CODE_OF_REMOVE_SENSOR = 55;  // Removed(Info->Main)
    public static final int REQUEST_CODE_OF_APP_SETTINGS = 56;   // Setting(Setting->Main)

    // Activity 응답 결과값
    public static final int RESULT_CODE_OF_SENSOR_ADDED = 1;    // SENSOR 하나가 추가됨
    public static final int RESULT_CODE_OF_SENSOR_MODIFIED = 2; // SENSOR 정보가 변경됨
    public static final int RESULT_CODE_OF_SENSOR_REMOVED = 3;  // SENSOR 하나가 삭제됨
    public static final int RESULT_CODE_OF_RESTART_APP = 4;     // SENSOR 하나가 삭제됨
    public static final int RESULT_CODE_OF_FINISH_APP = 5;      // SENSOR 하나가 삭제됨

    // 센서 추가 전달 Intent Parameter Keys
    public static final String SENSOR_ID = "SENSOR_ID";
    public static final String SENSOR_NAME = "SENSOR_NAME";
    public static final String WEAR_NAME = "WEAR_NAME";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";
    public static final String ACTION_MODE = "ACTION_MODE";
    public static final String RSSI = "THEFT_RSSI";

    // 서비스 to App 전달 Intent 정의
    public static final String EXTRA_ACTION_TYPE = "ACTION_TYPE";
    public static final String EXTRA_ACTION_POSITION="POSITION";
    public static final String EXTRA_ACTION_SENSOR_ID="SENSOR_ID";

    public final static String ACTION_GATT_CONNECTING = "ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECT = "ACTION_GATT_DISCONNECT";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_FAILED = "ACTION_GATT_FAILED";
    public final static String ACTION_GATT_STATUS = "ACTION_GATT_STATUS";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART = "DEVICE_DOES_NOT_SUPPORT_UART";
    public final static String ACTION_SENSOR_INITIALIZE= "ACTION_SENSOR_INITIALIZE";
    public final static String ACTION_SENSOR_DETECTING= "ACTION_SENSOR_DETECTING";
    public final static String ACTION_SENSOR_BATTERY = "ACTION_SENSOR_BATTERY";
    public final static String ACTION_SENSOR_FIND_PHONE_START = "ACTION_SENSOR_FIND_PHONE_START";
    public final static String ACTION_SENSOR_FIND_PHONE_STOP = "ACTION_SENSOR_FIND_PHONE_STOP";
    public final static String ACTION_SENSOR_WARN_THEFT = "ACTION_SENSOR_WARN_THEFT";

    // sensor theft level
    public static final int THEFT_LEVEL_LOW = -75;
    public static final int THEFT_LEVEL_MID = -85;
    public static final int THEFT_LEVEL_HIGH = -100;
}
