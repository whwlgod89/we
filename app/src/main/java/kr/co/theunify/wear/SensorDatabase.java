package kr.co.theunify.wear;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import kr.co.theunify.wear.data.SensorInfo;
import kr.co.theunify.wear.sensor.Sensor;

public class SensorDatabase extends SQLiteOpenHelper {
    private static final String TAG = "[" + SensorDatabase.class.getSimpleName() + "]";

    private static final String DATABASE_SENSOR_LIST_NAME = "sensor_list";


    private static final String COLUMN_SENSOR_ID = "id";
    private static final String COLUMN_SENSOR_NAME = "name";
    private static final String COLUMN_SENSOR_WEAR = "wear";
    private static final String COLUMN_SENSOR_COVER = "cover";          // cover
    private static final String COLUMN_SENSOR_PHONE_NUM = "phone";
    private static final String COLUMN_SENSOR_MODE = "mode";
    private static final String COLUMN_SENSOR_RSSI = "rssi";
    private static final String COLUMN_SENSOR_LATITUDE = "latitude";
    private static final String COLUMN_SENSOR_LONGITUDE = "longitude";
    private static final String COLUMN_SENSOR_BATTERY = "battery";

    private static final String DATABASE_ALTER_NAME = "ALTER TABLE "
            + DATABASE_SENSOR_LIST_NAME + " ADD COLUMN " + COLUMN_SENSOR_WEAR + " text;";
    private static final String DATABASE_ALTER_COVER = "ALTER TABLE "
            + DATABASE_SENSOR_LIST_NAME + " ADD COLUMN " + COLUMN_SENSOR_COVER + " integer;";


    public SensorDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE sensor_list (" +
                "id text PRIMARY KEY," +
                "name text," +
                "wear text," +
                "cover integer," +
                "phone text," +
                "mode integer," +
                "rssi integer," +      // THeft RSSI 감도
                "latitude real," +
                "longitude real," +
                "battery integer );";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            db.execSQL(DATABASE_ALTER_NAME);
            db.execSQL(DATABASE_ALTER_COVER);
        }
        //db.execSQL("DROP TABLE IF EXISTS " + DATABASE_SENSOR_LIST_NAME);
    }

    // 처음 추가.
    public void insertSensor(SensorInfo sensor) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(db != null) {
            ContentValues values = new ContentValues();
            values.put("id",sensor.getId());
            values.put("name",sensor.getName());
            values.put("wear",sensor.getWearname());
            values.put("cover",sensor.getCover());
            values.put("phone", sensor.getPhone());
            values.put("mode", sensor.getMode());
            values.put("rssi", sensor.getRssi());
            values.put("latitude", sensor.getLatitude());
            values.put("longitude", sensor.getLongitude());
            values.put("battery", sensor.getBattery());

            db.insert(DATABASE_SENSOR_LIST_NAME,null,values);
        }
    }

    // 정보 변경 (이름, 동작 모드, 전화 번호)
    public void updateSensor(SensorInfo sensor) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(db != null) {
            ContentValues values = new ContentValues();
            values.put("name",sensor.getName());
            values.put("wear",sensor.getWearname());
            values.put("cover",sensor.getCover());
            values.put("phone", sensor.getPhone());
            values.put("mode", sensor.getMode());
            values.put("rssi", sensor.getRssi());
            db.update(DATABASE_SENSOR_LIST_NAME, values ,"id" + "= ?",
                    new String[] { sensor.getId() } );
        }
    }

    // 위치 변경가 변경된 경우 or 연결 끊겼을 때 최종 위치 변경 or 배터리 상태 변경
    public void updateSensorLoc(SensorInfo sensor) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(db != null) {
            ContentValues values = new ContentValues();
            //values.put("state",sensor.isConnected());
            values.put("latitude", sensor.getLatitude());
            values.put("longitude", sensor.getLongitude());
            values.put("battery", sensor.getBattery());
            db.update(DATABASE_SENSOR_LIST_NAME, values ,"id" + "= ?",
                    new String[] { sensor.getId() } );
        }
    }

    // 제거
    public void deleteSensor(String sensorId) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(db != null) {
            db.delete(DATABASE_SENSOR_LIST_NAME, "id" + "= ?",
                    new String[] { sensorId });
        }
    }

    // 제거
    public void deleteSensor(SensorInfo sensor) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(db != null) {
            db.delete(DATABASE_SENSOR_LIST_NAME, "id" + "= ?",
                    new String[] { sensor.getId() });
        }
    }

    // 등록된 모든 센서 목록
    public ArrayList<Sensor> selectAllSensors() {
        ArrayList<Sensor> sensors = new ArrayList<Sensor>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + DATABASE_SENSOR_LIST_NAME;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {

            String id, name, wear, phone;
            int cover, rssi, mode, battery;
            double latitude, longitude;

            do {
                id = c.getString(c.getColumnIndex(COLUMN_SENSOR_ID));
                name = c.getString(c.getColumnIndex(COLUMN_SENSOR_NAME));
                wear = c.getString(c.getColumnIndex(COLUMN_SENSOR_WEAR));
                cover = c.getInt(c.getColumnIndex(COLUMN_SENSOR_COVER));
                phone = c.getString(c.getColumnIndex(COLUMN_SENSOR_PHONE_NUM));
                mode = c.getInt(c.getColumnIndex(COLUMN_SENSOR_MODE));
                rssi = c.getInt(c.getColumnIndex(COLUMN_SENSOR_RSSI));
                latitude = c.getDouble(c.getColumnIndex(COLUMN_SENSOR_LATITUDE));
                longitude = c.getDouble(c.getColumnIndex(COLUMN_SENSOR_LONGITUDE));
                battery = c.getInt(c.getColumnIndex(COLUMN_SENSOR_BATTERY));

                SensorInfo info = new SensorInfo(id, name, wear, cover, phone, mode, rssi, latitude, longitude, battery);
                Sensor sensor = new Sensor(info);
                sensors.add(sensor);
            } while (c.moveToNext());
        }
        return sensors;
    }
}
