package com.riesenbeck.myblescanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.riesenbeck.myblescanner.Data.BLEDeviceResult;
import com.riesenbeck.myblescanner.Data.BLEResults;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    //View
    private ListView lvBLE;
    private ToggleButton tBtnScanBLE;
    private Button btnClrBLEList;
    private ArrayAdapter<String> arrayAdapter;
    private TextureView mTvCamera;

    //BLE Scan
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<ScanFilter> scanFilters;
    private ScanSettings scanSettings;
    //BLE Data
    private BLEDeviceResult bleDeviceResult;
    private BLEResults bleResultsRef;

    private Timer mTimer;
    private TimerTask mTimerTask;

    //Camera
    private CameraDevice mCameraDevice;
    private Size mImageDimension;
    private String mCameraID;
    private CameraCaptureSession mCameraCaptureSessions;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initBLE();

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                scanLEDevices(true);
                mTimer.cancel();
                mTimer.purge();
                //tBtnScanBLE.setChecked(false);
            }
        };
        mTvCamera.setSurfaceTextureListener(mSurfaceTextureListener);
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
        paintBLEList();
        //startCameraThread
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        //closeCamera();
        stopBackgroundThread();
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
                    btnClrBLEList.setEnabled(false);
                    scanLEDevices(true);
                    /*
                    mTimer = new Timer();
                    mTimer.schedule(mTimerTask, 1000, 1000);
                    */
                }else{
                    /*
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer.purge();
                        mTimer = null;
                    }*/
                    scanLEDevices(false);
                    btnClrBLEList.setEnabled(true);
                }
            }
        });
        btnClrBLEList = (Button)findViewById(R.id.btn_clrBLEList);
        btnClrBLEList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleResultsRef.clearBLEResults();
                paintBLEList();
            }
        });
        mTvCamera = (TextureView)findViewById(R.id.tv_Camera);
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
        for(BLEDeviceResult bleDeviceResult: bleResultsRef.getBleDeviceResults()){
            bleDeviceStringList.add(bleDeviceResult.toString());
        }
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, bleDeviceStringList);
        lvBLE.setAdapter(arrayAdapter);
    }

    //BLECallback for API Version <21
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            try {
                bleResultsRef.addBLEResult(new BLEDeviceResult(device, rssi,scanRecord,System.nanoTime()));
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
            bleDeviceResult = new BLEDeviceResult(result.getDevice(), result.getRssi(),result.getScanRecord().getBytes(), result.getTimestampNanos());
            bleResultsRef.addBLEResult(bleDeviceResult);
            paintBLEList();
        }
    };



    TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            //open Camera
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private void openCamera(){
        CameraManager mCameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            mCameraID = mCameraManager.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraID);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            mImageDimension = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            mCameraManager.openCamera(mCameraID, mStateCallback, null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback(){

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraDevice.close();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };

    private void createCameraPreview(){
        try {
            SurfaceTexture mCameraSurfaceTexture = mTvCamera.getSurfaceTexture();
            mCameraSurfaceTexture.setDefaultBufferSize(mImageDimension.getWidth(),mImageDimension.getHeight());
            Surface surface = new Surface(mCameraSurfaceTexture);
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    mCameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview(){
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try{
            mCameraCaptureSessions.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
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

}
