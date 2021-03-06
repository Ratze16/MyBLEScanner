package com.riesenbeck.myblescanner.Activity;

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
import android.content.SharedPreferences;
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

import com.riesenbeck.myblescanner.Data.BleDevice;
import com.riesenbeck.myblescanner.Data.BLEResults;
import com.riesenbeck.myblescanner.Data.LDPLResults;
import com.riesenbeck.myblescanner.R;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LDPLTestActivity extends AppCompatActivity {

    private static BleDevice[] LDPLMeasurementResults;
    private final int MEASUREMENTS = 10;
    private final int DISTANCES = 10;
    private Handler mHandler;
    private final int SCAN_PERIOD = 1000;
    private BLEResults bleResultsRef;
    private LDPLResults LDPLResultsRef;

    //View
    private ProgressBar mPBLDPLTest, mPBLDPLMeasurement;
    private int mPBLDPLTestStatus = 0, mPBLDPLMeasurementStatus = 0;
    private Button mBtnNext, mBtnCancel, mBtnLDPLResults;
    private TextView mTvLDPLMeasurement;
    private TextView mTvBleDevice, mTvRSSI;
    private SharedPreferences mSharedPreferences;

    //BLE Scan
    private BleDevice mBleDevice;
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
                BleDevice bleDevice = new BleDevice(device, rssi,scanRecord,System.nanoTime());
                if(device.getAddress().equals(bleDevice.getmAddress())){
                    //mThread.interrupt();

                    if(Build.VERSION.SDK_INT<21){
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }else{
                        mBluetoothLeScanner.stopScan(mScanCallback);
                    }

                    if(Build.VERSION.SDK_INT<21){
                        // interruption condition for the Measurement
                        if(mPBLDPLMeasurementStatus ==MEASUREMENTS-1){
                            LDPLResultsRef.addLDPLMeasurementResult(LDPLMeasurementResults);
                            mBtnLDPLResults.setEnabled(true);
                            mBtnNext.setEnabled(true);
                        }
                        // Set Progressbar
                        if(mPBLDPLMeasurementStatus <MEASUREMENTS-1){
                            mTvRSSI.setText(String.valueOf(rssi));
                            LDPLMeasurementResults[mPBLDPLMeasurementStatus] = bleDevice;
                            mPBLDPLMeasurementStatus++;
                            mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                            mBleScanEnable = true;
                            scanBle();
                        }
                        // interruption condition for the Measurement
                        else{
                            mPBLDPLMeasurementStatus++;
                            mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                        }
                    }else{
                        try {
                            if(mPBLDPLMeasurementStatus ==MEASUREMENTS-1){
                                LDPLResultsRef.addLDPLMeasurementResult(LDPLMeasurementResults);
                                mBtnNext.setEnabled(true);
                                mBtnLDPLResults.setEnabled(true);
                            }
                            if(mPBLDPLMeasurementStatus < MEASUREMENTS-1){
                                mPBLDPLMeasurementStatus++;
                                mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                                scanBle();
                            }else{
                                mPBLDPLMeasurementStatus++;
                                mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                                mTvLDPLMeasurement.setText(getResources().getStringArray(R.array.LDPLTest)[mPBLDPLTestStatus]);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

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
                BleDevice bleDevice = new BleDevice(result.getDevice(), result.getRssi(),result.getScanRecord().getBytes(), result.getTimestampNanos());
                if(result.getDevice().getAddress().equals(bleDevice.getmAddress())){

                    if(Build.VERSION.SDK_INT<21){
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }else{
                        mBluetoothLeScanner.stopScan(mScanCallback);
                    }

                    if(Build.VERSION.SDK_INT<21){
                        // interruption condition for the Measurement
                        if(mPBLDPLMeasurementStatus == MEASUREMENTS){
                            LDPLResultsRef.addLDPLMeasurementResult(LDPLMeasurementResults);
                            mBtnLDPLResults.setEnabled(true);
                            mBtnNext.setEnabled(true);
                        }
                        // Set Progressbar
                        if(mPBLDPLMeasurementStatus <MEASUREMENTS){
                            mTvRSSI.setText(String.valueOf(result.getRssi()));
                            LDPLMeasurementResults[mPBLDPLMeasurementStatus] = bleDevice;
                            mPBLDPLMeasurementStatus++;
                            mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                            mBleScanEnable = true;
                            scanBle();
                        }
                        // interruption condition for the Measurement
                        else{
                            mPBLDPLMeasurementStatus++;
                            mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                        }
                    }else{
                        try {
                            if(mPBLDPLMeasurementStatus ==MEASUREMENTS){
                                LDPLResultsRef.addLDPLMeasurementResult(LDPLMeasurementResults);
                                mBtnNext.setEnabled(true);
                                mBtnLDPLResults.setEnabled(true);
                            }
                            if(mPBLDPLMeasurementStatus < MEASUREMENTS){
                                mTvRSSI.setText(String.valueOf(result.getRssi()));
                                LDPLMeasurementResults[mPBLDPLMeasurementStatus] = bleDevice;
                                mPBLDPLMeasurementStatus++;
                                mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                                scanBle();
                            }else{
                                mPBLDPLMeasurementStatus++;
                                mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                                mTvLDPLMeasurement.setText(getResources().getStringArray(R.array.LDPLTest)[mPBLDPLTestStatus]);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private boolean mBleScanEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ldpl_test);

        initBLE();

        mTvLDPLMeasurement = (TextView)findViewById(R.id.tVLDPLMeasurement);
        mTvLDPLMeasurement.setText(getResources().getStringArray(R.array.LDPLTest)[mPBLDPLTestStatus]);
        mTvRSSI = (TextView)findViewById(R.id.tv_RSSI_LDPLTest);
        mPBLDPLTest = (ProgressBar)findViewById(R.id.pBLDPLScan);
        mPBLDPLMeasurement = (ProgressBar)findViewById(R.id.pBMeasurement);
        mBtnNext = (Button)findViewById(R.id.btnNextMeasurement);
        mBtnCancel = (Button)findViewById(R.id.btnCancel);
        mBtnLDPLResults = (Button)findViewById(R.id.btnLPDLResults);

        mPBLDPLTest.setProgress(mPBLDPLTestStatus);
        mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LDPLMeasurementResults = new BleDevice[MEASUREMENTS];
                mBtnNext.setEnabled(false);
                mBtnLDPLResults.setEnabled(false);
                mPBLDPLMeasurementStatus = 0;
                mPBLDPLMeasurement.setProgress(mPBLDPLMeasurementStatus);
                scanBle();
                mPBLDPLTestStatus++;
                mPBLDPLTest.setProgress(mPBLDPLTestStatus);
            }
        });
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        mBtnLDPLResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LDPLResultsActivity.class);
                startActivity(intent);
            }
        });
        mHandler = new Handler();
        bleResultsRef = BLEResults.getInstance();
        LDPLResultsRef = LDPLResults.getInstance();
        mBleDevice = bleResultsRef.getBleDeviceResult(getIntent().getIntExtra(getString(R.string.position),-1));
        mTvBleDevice = (TextView)findViewById(R.id.tv_BleDevice);
        mTvBleDevice.setText(mBleDevice.getmAddress());
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

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    private void scanBle(){
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            try {
                mBluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);
            } catch (IllegalStateException e) {
                Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.errorcode0x0011), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
