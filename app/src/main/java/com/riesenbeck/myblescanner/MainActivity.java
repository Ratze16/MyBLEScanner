package com.riesenbeck.myblescanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.riesenbeck.myblescanner.Data.BleDevice;
import com.riesenbeck.myblescanner.Data.BLEResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    //View
    private ListView lvBLE;
    private ToggleButton tBtnScanBLE;
    private ArrayAdapter<String> arrayAdapter;
    private TextureView mTvCamera;
    private static ListView lvWifiConnectionInfo;
    private Spinner spWiFi;

    //BLE Scan
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<ScanFilter> scanFilters;
    private ScanSettings scanSettings;
    //BLECallback for API Version <21
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            try {
                bleResultsRef.addBLEResult(new BleDevice(device, rssi,scanRecord,System.nanoTime()));
                paintBLEList();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    //BLECallback for API Version >=21
    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            bleDevice = new BleDevice(result.getDevice(), result.getRssi(),result.getScanRecord().getBytes(), result.getTimestampNanos());
            bleResultsRef.addBLEResult(bleDevice);
            paintBLEList();
        }
    };

    //BLE Data
    private BleDevice bleDevice;
    private BLEResults bleResultsRef;

    //WiFi
    public static final String TAG = "Basic Network Demo";
    private static boolean wifiConnected = false;
    private static boolean mobileConnected = false;
    private static final IntentFilter INTENT_FILTER = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    private static WifiManager mWifiManager;
    private static WifiReceiver receiverWifi;
    private List<String> wifiInfoList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    //Camera
    private CameraDevice mCameraDevice;
    private CameraManager mCameraManager;
    private Size mImageSize;
    private String mCameraID;
    private CameraCaptureSession mCameraCaptureSessions;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            //opens the Camera
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            //TODO Comment
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback(){

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            mCameraOpenCloseLock.release();
            mCameraDevice.close();
            mCameraDevice = null;
            Activity activity = getParent();
            if(activity != null){
                activity.finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initBLE();
        initWiFi();

        mTvCamera = (TextureView)findViewById(R.id.tv_Camera);
    }

    //Checks if the BLE Adapter is initialized and enabled
    //
    @Override
    public void onResume(){
        super.onResume();
        if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()){
            if(Build.VERSION.SDK_INT >=21){
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                scanFilters = new ArrayList<ScanFilter>();
                scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            }
        }else{
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
        //Register the OnItemClickListener for the BLEDevices Listview
        lvBLE.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Starts the BLEResult Activity
                Intent intent = new Intent(getApplicationContext(), BLEResultActivity.class);
                intent.putExtra(getString(R.string.position), position);
                startActivity(intent);
            }
        });
        //paintBLEList();

        //WiFi
        this.registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        //startCameraThread
        startBackgroundThread();

        if (mTvCamera.isAvailable()) {
            openCamera(mTvCamera.getWidth(),mTvCamera.getHeight());
        } else {
            mTvCamera.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        //unregister WiFi Listener
        //this.unregisterReceiver(receiverWifi);

        //Camera
        closeCamera();
        stopBackgroundThread();

        tBtnScanBLE.setChecked(false);

        super.onPause();
    }

    //Initialize all GUI Items at Startup
    private void initView() {
        lvBLE = (ListView) findViewById(R.id.lv_BLE);
        tBtnScanBLE = (ToggleButton) findViewById(R.id.tBtn_scanBLE);
        tBtnScanBLE.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    bleResultsRef.clearBLEResults();
                    scanLEDevices(true);
                }else{
                    scanLEDevices(false);
                }
            }
        });

        lvWifiConnectionInfo = (ListView) findViewById(R.id.lv_WiFiConnectionInfo);
        spWiFi = (Spinner) findViewById(R.id.sp_wifi);
        mTvCamera = (TextureView)findViewById(R.id.tv_Camera);
        findViewById(R.id.btn_RoomMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),RoomActivity.class));
            }
        });
    }

    // Checks if the Device supports BLE
    // Initialize the BluetoothManager and the Bluetooth Adapter
    private void initBLE(){
        bleResultsRef = BLEResults.getInstance();
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, R.string.BLE_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    private void initWiFi(){
        ArrayAdapter<CharSequence> adapterDropdown = ArrayAdapter.createFromResource(this, R.array.WiFiSpinner, android.R.layout.simple_spinner_item);
        adapterDropdown.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWiFi.setAdapter(adapterDropdown);
        spWiFi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0: //Show nothing
                        lvWifiConnectionInfo.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        lvWifiConnectionInfo.setVisibility(View.VISIBLE);
                        refreshWiFiScan(mWifiManager.getScanResults());break;
                    case 2:
                        lvWifiConnectionInfo.setVisibility(View.VISIBLE);
                        refreshWiFiInfo();break;
                    default: break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        checkNetworkConnection();
    }

    // Starts or Stops BLE Scan
    private void scanLEDevices(boolean start){

        if (start) {
            if(Build.VERSION.SDK_INT<21){
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }else{
                mBluetoothLeScanner.startScan(scanFilters,scanSettings, mScanCallback);
            }
        } else {
            if(Build.VERSION.SDK_INT<21){
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }else{
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }
    }

    //Redraw the BLE List
    private void paintBLEList(){
        List<String> bleDeviceStringList = new ArrayList<String>();
        List<BleDevice> bleDeviceList = bleResultsRef.getBleDevices();
        for(BleDevice bleDevice : bleResultsRef.getBleDevices()){
            bleDeviceStringList.add(bleDevice.toString());
        }
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, bleDeviceStringList);
        lvBLE.setAdapter(arrayAdapter);
    }

    //Opens the Camera
    private void openCamera(int width, int height){
        mCameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            mCameraID = mCameraManager.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraID);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            mImageSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            configureTransform(width, height);
            mCameraManager.openCamera(mCameraID, mStateCallback, null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }
    //Closes the Camera
    private void closeCamera(){
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraCaptureSessions) {
                mCameraCaptureSessions.close();
                mCameraCaptureSessions = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    //Creates the Preview of the Camera
    private void createCameraPreview(){
        try {
            SurfaceTexture mCameraSurfaceTexture = mTvCamera.getSurfaceTexture();
            assert mCameraSurfaceTexture != null;

            //Configure Size of Camera Preiview
            mCameraSurfaceTexture.setDefaultBufferSize(mImageSize.getWidth(), mImageSize.getHeight());
            //Set Texture to Surface
            Surface surface = new Surface(mCameraSurfaceTexture);

            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if(mCameraDevice == null){
                        return;
                    }
                    mCameraCaptureSessions = cameraCaptureSession;

                    //Updates Preview
                    mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    try{
                        mCameraCaptureSessions.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                    }catch(CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = this;
        if (null == mTvCamera || null == mImageSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mTvCamera.getHeight(), mTvCamera.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mTvCamera.getHeight(),
                    (float) viewWidth / mTvCamera.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTvCamera.setTransform(matrix);
    }

    //Starts the new Thread for the Camera
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    //Stops the Thread for the Camera
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (wifiConnected) {
                Toast.makeText(getApplicationContext(),"WifiConnected",Toast.LENGTH_SHORT).show();
            } else if (mobileConnected) {
                Toast.makeText(getApplicationContext(),"WifiConnected",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void refreshWiFiInfo(){
        wifiInfoList.clear();
        wifiInfoList.add(String.valueOf(new Date()));
        if(mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_ENABLED){
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            wifiInfoList.add("BSSID: "+wifiInfo.getBSSID());
            wifiInfoList.add("Frequency: "+String.valueOf(wifiInfo.getFrequency()));
            wifiInfoList.add("HiddenSSID: "+String.valueOf(wifiInfo.getHiddenSSID()));
            wifiInfoList.add("IPAddress:"+String.valueOf(wifiInfo.getIpAddress()));
            wifiInfoList.add("LinkSpeed: "+String.valueOf(wifiInfo.getLinkSpeed()));
            wifiInfoList.add("MacAddress: "+wifiInfo.getMacAddress());
            wifiInfoList.add("NetworkId: "+String.valueOf(wifiInfo.getNetworkId()));
            wifiInfoList.add("RSSI: "+String.valueOf(wifiInfo.getRssi()));
            wifiInfoList.add("SSID: "+String.valueOf(wifiInfo.getSSID()));

        }else{
            wifiInfoList.clear();
            wifiInfoList.add("Keine WLAN Verbindung vorhanden");
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, wifiInfoList);
        lvWifiConnectionInfo.setAdapter(adapter);
    }

    public void refreshWiFiScan(List<android.net.wifi.ScanResult> scanResults){
        wifiInfoList.clear();

        if(!mWifiManager.isWifiEnabled()){
            wifiInfoList.add("Keine WLAN Verbindung vorhanden");
        } else{
            wifiInfoList.add("Hier werden alle WLANs angezeigt");
            for (int i = 0; i < scanResults.size(); i++) {
                wifiInfoList.add(scanResults.get(i).toString());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, wifiInfoList);
        lvWifiConnectionInfo.setAdapter(adapter);
    }


    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            Toast.makeText(getApplicationContext(), "Scan finished", Toast.LENGTH_SHORT).show();
            StringBuilder sb = new StringBuilder();
            List<android.net.wifi.ScanResult> scanResults = mWifiManager.getScanResults();
            Toast.makeText(getApplicationContext(),"Number Of Wifi connections :" + scanResults.size(),Toast.LENGTH_SHORT).show();

            for (int i = 0; i < scanResults.size(); i++) {

                sb.append(new Integer(i + 1).toString() + ". ");
                sb.append((scanResults.get(i)).toString());
                sb.append("\n\n");
            }
            Toast.makeText(getApplicationContext(),sb.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
