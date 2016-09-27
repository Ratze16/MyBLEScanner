package com.riesenbeck.myblescanner;

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
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.riesenbeck.myblescanner.Data.BLEDeviceResult;
import com.riesenbeck.myblescanner.Data.BLEResults;
import com.riesenbeck.myblescanner.Data.LDPLResults;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LDPL_Test extends AppCompatActivity {

    private static BLEDeviceResult[] LDPLMeasurementResults;
    private final int MEASUREMENTS = 10;
    private final int DISTANCES = 10;
    private Handler mHandler;
    private final int SCAN_PERIOD = 2000;
    private BLEResults bleResultsRef;
    private LDPLResults LDPLResultsRef;

    //View
    private ProgressBar mPBLDPLTest, mPBLDPLMeasurement;
    private int mPBLDPLTestStatus = 0, mPBLDPLMeasurementStatus = 0;
    private Button mBtnNext, mBtnCancel, mBtnLDPLResults;
    private TextView mTvLDPLMeasurement;
    private TextView mBleDevice;

    //BLE Scan
    private BLEDeviceResult mBleDeviceResult;
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
                BLEDeviceResult bleDeviceResult = new BLEDeviceResult(device, rssi,scanRecord,System.nanoTime());
                if(device.getAddress().equals(bleDeviceResult.getBluetoothDevice().getAddress())){
                    LDPLMeasurementResults[mPBLDPLMeasurementStatus] = bleDeviceResult;
                }
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

            try {
                BLEDeviceResult bleDeviceResult = new BLEDeviceResult(result.getDevice(), result.getRssi(),result.getScanRecord().getBytes(), result.getTimestampNanos());
                if(result.getDevice().getAddress().equals(bleDeviceResult.getBluetoothDevice().getAddress())){
                    LDPLMeasurementResults[mPBLDPLMeasurementStatus] = bleDeviceResult;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ldpl_test);

        LDPLMeasurementResults = new BLEDeviceResult[MEASUREMENTS];
        initBLE();

        mTvLDPLMeasurement = (TextView)findViewById(R.id.tVLDPLMeasurement);
        mTvLDPLMeasurement.setText(getResources().getStringArray(R.array.LDPLTest)[mPBLDPLTestStatus]);
        mPBLDPLTest = (ProgressBar)findViewById(R.id.pBLDPLScan);
        mPBLDPLMeasurement = (ProgressBar)findViewById(R.id.pBMeasurement);
        mPBLDPLTest.setProgress(mPBLDPLTestStatus);
        mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
        mBtnNext = (Button)findViewById(R.id.btnNextMeasurement);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnNext.setEnabled(false);
                scanLEDevices(true);
                mPBLDPLTestStatus++;
                mPBLDPLTest.setProgress(mPBLDPLTestStatus);
                mPBLDPLMeasurementStatus = 0;
                mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
            }
        });
        mBtnCancel = (Button)findViewById(R.id.btnCancel);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
        mBtnLDPLResults = (Button)findViewById(R.id.btnLPDLResults);
        mBtnLDPLResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LDPL_Results.class);
                startActivity(intent);
            }
        });
        mHandler = new Handler();
        bleResultsRef = BLEResults.getInstance();
        LDPLResultsRef = LDPLResults.getInstance();
        mBleDeviceResult = bleResultsRef.getBleDeviceResult(getIntent().getIntExtra(getString(R.string.position),-1));
        mBleDevice = (TextView)findViewById(R.id.tv_BleDevice);
        mBleDevice.setText(mBleDeviceResult.getBluetoothDevice().getAddress());
    }

    @Override
    protected void onResume(){
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
    }

    private void initBLE(){
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
            try {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(Build.VERSION.SDK_INT<21){
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            if(mPBLDPLMeasurementStatus ==MEASUREMENTS-1){
                                LDPLResultsRef.addLDPLMeasurementResult(LDPLMeasurementResults);
                                mBtnNext.setEnabled(true);
                            }
                            if(mPBLDPLMeasurementStatus <MEASUREMENTS-1){
                                mPBLDPLMeasurementStatus++;
                                mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                                scanLEDevices(true);
                            }else{
                                mPBLDPLMeasurementStatus++;
                                mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                                scanLEDevices(false);
                            }
                        }else{
                            try {
                                mBluetoothLeScanner.stopScan(mScanCallback);
                                if(mPBLDPLMeasurementStatus ==MEASUREMENTS-1){
                                    LDPLResultsRef.addLDPLMeasurementResult(LDPLMeasurementResults);
                                    mBtnNext.setEnabled(true);
                                }
                                if(mPBLDPLMeasurementStatus < MEASUREMENTS-1){
                                    mPBLDPLMeasurementStatus++;
                                    mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                                    scanLEDevices(true);
                                }else{
                                    mPBLDPLMeasurementStatus++;
                                    mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                                    mTvLDPLMeasurement.setText(getResources().getStringArray(R.array.LDPLTest)[mPBLDPLTestStatus]);
                                    scanLEDevices(false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },SCAN_PERIOD);
                if(Build.VERSION.SDK_INT<21){
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }else{
                    mBluetoothLeScanner.startScan(scanFilters,scanSettings, mScanCallback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            if(Build.VERSION.SDK_INT<21){
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }else{
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }
    }
}
