package kr.co.theunify.wear.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.WearApp;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.view.TitlebarView;

/**
 * 지갑 위치 확인 화면
 */
public class MapActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private String TAG = MapActivity.class.getSimpleName();

    private final int MAP_DEFAULT_ZOOM = 16;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1022;

    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.v_titlebar)              TitlebarView v_titlebar;

    @BindView(R.id.btn_my_pos)          ImageView   btn_my_pos;

    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    private boolean mLocationPermissionGranted = false;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;


    private GoogleMap mMap;

    LocationManager locationManager ;

    private WearApp mApp = null;
    private Sensor mSensor;

    //********************************************************************************
    //  LifeCycle Functions
    //********************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_map);
        mContext = this;
        ButterKnife.bind(this);

        mApp = (WearApp) getApplication();

        mSensor = mApp.getCurSensor();

        buildGoogleApiClient();

        initView();

        if (GPSStatus() == false) {
            showAlertPopup("", mContext.getResources().getString(R.string.location_required)
                    , mContext.getResources().getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            turnGPSOn();
                        }
                    }, mContext.getResources().getString(R.string.cancel));
        }

    }


    @Override
    public void onResume(){
        super.onResume();

        mGoogleApiClient.connect();
    }


    @Override
    public void onPause(){
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //********************************************************************************
    //  Override Functions
    //********************************************************************************

    //********************************************************************************
    //  Override Event Functions
    //********************************************************************************

    /**
     * 뒤로 이동 버튼 클릭 시
     */
    @OnClick(R.id.img_back)
    public void onClickImgBack() {
        onBackPressed();
    }


    @OnClick(R.id.btn_my_pos)
    public void onClickMyPos() {
        getDeviceLocation();
    }

    //********************************************************************************
    //  User Define Functions
    //********************************************************************************

    /**
     * 사용자 뷰 초기화
     */
    private void initView() {
        initTitle();
    }

    private void initTitle() {
        v_titlebar.setTitleVisible(View.VISIBLE);
        v_titlebar.setTitle(mSensor.getSensorName());
        v_titlebar.setBackVisible(View.VISIBLE);
    }


    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }

        Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent1);
    }

    public boolean GPSStatus(){
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //********************************************************************************
    //  User Functions
    //********************************************************************************

    /**
     * 구글 api client 빌드
     */
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        }
        createLocationRequest();
    }

    /**
     * 위치요청 객체 만들기
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * 디바이스의 위치 가져오기
     */
    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            drawMap();

//            if (mMap != null) {
//
//                mMap.clear();
//
//                if (mCurrentLocation != null) {
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
//                    mMap.addMarker(markerOptions);
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), MAP_DEFAULT_ZOOM));
//                }
//            }

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * update Location UI
     */
    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (mMap == null) return;
        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private void drawMap() {
        if (mMap == null) {
            return;
        }

        mMap.clear();
        ArrayList<Marker> markers = new ArrayList<>();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (mCurrentLocation != null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title("My Position");
            markerOptions.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            Marker marker =  mMap.addMarker(markerOptions);
            markers.add(marker);
        }

        if (mSensor.getLatitude() != 0 && mSensor.getLongitude() != 0 ) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title("Lost Position");
            markerOptions.position(new LatLng(mSensor.getLatitude(), mSensor.getLongitude()));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            Marker marker =  mMap.addMarker(markerOptions);
            markers.add(marker);
        }

        if (markers.size() == 2) {
            for (int j = 0; j < markers.size(); j++) {
                builder.include(markers.get(j).getPosition());
            }

            int padding = 100;
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);
        } else if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), MAP_DEFAULT_ZOOM));
        } else if (mSensor.getLatitude() != 0 && mSensor.getLongitude() != 0 ){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mSensor.getLatitude(), mSensor.getLongitude()), MAP_DEFAULT_ZOOM));
        }
    }


    //********************************************************************************
    // Map Functions
    //********************************************************************************

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawMap();

        //정보창 클릭 리스너
//        mMap.setOnInfoWindowClickListener(infoWindowClickListener);

        //마커 클릭 리스너
//        mMap.setOnMarkerClickListener(markerClickListener);

//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                if (mCurrentLocation != null) {
//                    mCurrentLocation.setLatitude(latLng.latitude);
//                    mCurrentLocation.setLongitude(latLng.longitude);
//
//                    ULog.e(TAG, "lati: " + latLng.latitude + ", long: " + latLng.longitude);
//
//                    mMap.clear();
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.title("");
//                    markerOptions.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
//                    mMap.addMarker(markerOptions);
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 10));
//                }
//            }
//        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getDeviceLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
//                showRequestAgainDialog();
                Toast.makeText(mContext, "위치정보 권한이 거부되어 있습니다. 승인 후 다시 시도 해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        updateLocationUI();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

}
