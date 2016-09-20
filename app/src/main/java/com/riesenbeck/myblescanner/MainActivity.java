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
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    //View
    private ListView lvBLE;
    private ToggleButton tBtnScanBLE;
    private Button btnClrBLEList;
    private ArrayAdapter<String> arrayAdapter;

    //BLE Scan
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<ScanFilter> scanFilters;
    private ScanSettings scanSettings;
    //BLE Data
    private BLEDeviceResult bleDeviceResult;
    private BLEResults bleResultsRef;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initBLE();

        mHandler = new Handler(Looper.getMainLooper()){

        };
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
                }else{
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
            mHandler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    if(Build.VERSION.SDK_INT<21){
                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                    }else{
                        mBluetoothLeScanner.startScan(scanFilters,scanSettings, mScanCallback);
                    }
                }
            },1);
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


}
