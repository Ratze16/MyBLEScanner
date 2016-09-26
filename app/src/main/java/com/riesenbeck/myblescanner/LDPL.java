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
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.riesenbeck.myblescanner.Data.BLEDeviceResult;
import com.riesenbeck.myblescanner.Data.BLEResults;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LDPL extends AppCompatActivity {

    private int[][] mLPDLTest;
    private final int MEASUREMENTS = 10;
    private final int DISTANCES = 10;
    private int measurement = 0;

    //View
    private ProgressBar mPBLPDLTest, mPBLPDLMeasurement;
    private int mPBLPDLTestStatus = 0, mPBLPDLMeasurementStatus = 0;
    private Button mBtnNext;
    private TextView mTvLPDLMeasurement;

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
                bleResultsRef.addBLEResult(new BLEDeviceResult(device, rssi,scanRecord,System.nanoTime()));
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
            mLPDLTest[mPBLPDLTestStatus][mPBLPDLMeasurementStatus] = result.getRssi();
            bleResultsRef.addBLEResult(bleDeviceResult);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ldpl);

        mLPDLTest = new int[DISTANCES][MEASUREMENTS];
        initBLE();

        mTvLPDLMeasurement = (TextView)findViewById(R.id.tVLPDLMeasurement);
        mTvLPDLMeasurement.setText(getResources().getStringArray(R.array.LDPLTest)[mPBLPDLTestStatus]);
        mPBLPDLTest = (ProgressBar)findViewById(R.id.pBLPDLScan);
        mPBLPDLMeasurement = (ProgressBar)findViewById(R.id.pBMeasurement);
        mPBLPDLTest.setProgress(mPBLPDLTestStatus);
        mPBLPDLMeasurement.setProgress(mPBLPDLMeasurementStatus);
        mBtnNext = (Button)findViewById(R.id.btnNextMeasurement);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i<10; i++){

                    new CountDownTimer(2000,i*2000){

                        public void onTick(long millisUntilFinished) {

                        }

                        public void onFinish() {
                            scanLEDevices(true);
                            mPBLPDLMeasurementStatus++;
                            mPBLPDLMeasurement.setProgress(mPBLPDLMeasurementStatus);
                            Toast.makeText(getApplicationContext(),"done!",Toast.LENGTH_SHORT).show();
                            scanLEDevices(false);
                        }
                    }.start();

                }
                mPBLPDLTestStatus++;
                mPBLPDLTest.setProgress(mPBLPDLTestStatus);
                mPBLPDLMeasurementStatus = 0;
                mPBLPDLMeasurement.setProgress(mPBLPDLMeasurementStatus);
            }
        });
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
        bleResultsRef = BLEResults.getInstance();
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, R.string.BLE_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    //BLE Data
    private BLEDeviceResult bleDeviceResult;
    private BLEResults bleResultsRef;

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

}
