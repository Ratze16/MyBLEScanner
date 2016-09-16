package com.riesenbeck.myblescanner;

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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private ListView lvBLE;
    private ToggleButton tBtnScanBLE;
    private ArrayAdapter<String> arrayAdapter;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<ScanFilter> scanFilters;
    private ScanSettings scanSettings;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            bleDeviceResults.add(new BLEDeviceResult(device, rssi,scanRecord));
        }
    };
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            bleDeviceResult = new BLEDeviceResult(result.getDevice(), result.getRssi(),result.getScanRecord().getBytes());
            bleDeviceResults.add(bleDeviceResult);
            bleDeviceStringList.add(bleDeviceResult.toString());

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private BLEDeviceResult bleDeviceResult;
    private List<BLEDeviceResult> bleDeviceResults = new ArrayList<BLEDeviceResult>();
    private List<String> bleDeviceStringList = new ArrayList<String>();

    private final int DELAY = 1000;
    private Handler mHandler;

    private Runnable refresh = new Runnable(){
        @Override
        public void run() {
            mHandler.postDelayed(this,DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initBLE();
        mHandler = new Handler();
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
        lvBLE.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), BLEDeviceResult.class);
                intent.putExtra("Position", position);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //Initialize all GUI Items at Startup
    private void initView() {
        lvBLE = (ListView) findViewById(R.id.lv_BLE);
        tBtnScanBLE = (ToggleButton) findViewById(R.id.tBtn_scanBLE);
        tBtnScanBLE.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    bleDeviceStringList.clear();
                    scanLEDevices(true);
                }else{
                    scanLEDevices(false);
                    arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, bleDeviceStringList);
                    lvBLE.setAdapter(arrayAdapter);
                }
            }
        });
    }

    // Checks if the Device supports BLE
    // Initialize the BluetoothManager and the Bluetooth Adapter
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

    //A BLE Device
    private class BLEDeviceResult{
        private BluetoothDevice bluetoothDevice;
        private int rssi;
        private byte[] scanRecord;
        public BLEDeviceResult(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord){
            this.bluetoothDevice = bluetoothDevice;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }
        @Override
        public String toString(){
            return "Device: "+bluetoothDevice.toString()+ "\nRSSI: "+String.valueOf(rssi)+"\nScanRecord: "+scanRecord.toString();
        }
    }

}
