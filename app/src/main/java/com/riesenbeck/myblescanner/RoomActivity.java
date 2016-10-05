package com.riesenbeck.myblescanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.riesenbeck.myblescanner.Data.BleDevice;
import com.riesenbeck.myblescanner.Data.Room;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RoomActivity extends AppCompatActivity {
    private static int BEACON_X = 0, BEACON_Y = 0;
    private final int MEASUREMENTS = 10;
    private int mPosSearchStatus = 0;
    private int[] mRssiList;
    private double mRssiMean = 0, mDistance = 0;

    //BLE Scan
    private BleDevice mBleDevice;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<ScanFilter> scanFilters;
    private ScanSettings scanSettings;

    private ProgressBar pbPosSearch;
    private ImageView mIvCircle;

    //BLECallback for API Version <21
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            try {
                BleDevice bleDevice = new BleDevice(device, rssi,scanRecord,System.nanoTime());
                if(device.getAddress().equals(bleDevice.getmAddress())){
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    try {
                        if(mPosSearchStatus < MEASUREMENTS){
                            mRssiList[mPosSearchStatus] = bleDevice.getmRssi();
                            mPosSearchStatus++;
                            pbPosSearch.setProgress(mPosSearchStatus);
                            scanBle();
                        }else{
                            //Calc Position
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    //BLECallback for API Version >=21
    private final ScanCallback mScanCallback = new ScanCallback() {
        @ Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            try {
                BleDevice bleDevice = new BleDevice(result.getDevice(), result.getRssi(),result.getScanRecord().getBytes(), result.getTimestampNanos());
                if(result.getDevice().getAddress().equals(bleDevice.getmAddress())){
                    mBluetoothLeScanner.stopScan(mScanCallback);

                    try {
                        if(mPosSearchStatus < MEASUREMENTS){
                            mRssiList[mPosSearchStatus] = bleDevice.getmRssi();
                            mPosSearchStatus++;
                            pbPosSearch.setProgress(mPosSearchStatus);
                            scanBle();
                        }else{
                            for(int rssi: mRssiList){
                                mRssiMean = mRssiMean + rssi;
                            }
                            mRssiMean = mRssiMean/mRssiList.length;
                            mDistance = Room.getInstance().getExponential(mRssiMean,-65);
                            drawCircle(mDistance);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        initBLE();
        pbPosSearch = (ProgressBar)findViewById(R.id.pB_posSearch);
        pbPosSearch.setMax(MEASUREMENTS);
        mRssiList = new int[MEASUREMENTS];

        mIvCircle = (ImageView)findViewById(R.id.iV_Circle);
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
        scanBle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        BEACON_X = (int)event.getX()-50;
        BEACON_Y = (int)event.getY()-160;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: drawCircle(mDistance); break;
            case MotionEvent.ACTION_MOVE: break;
            case MotionEvent.ACTION_UP:  break;
        }
        return false;
    }


    private void initBLE(){
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, R.string.BLE_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
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

    private void drawCircle(double r){
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(125);


        Bitmap bmp = Bitmap.createBitmap(mIvCircle.getWidth(),mIvCircle.getHeight(),Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmp);
        canvas.drawCircle(BEACON_X,BEACON_Y , (float) r*mIvCircle.getWidth()/11, paint);

        mIvCircle.setImageBitmap(bmp);
    }

    /*
    private class PositionSearchFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.searchingPosition)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            scanBle();
                        }
                    })
                    .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            // Create the AlertDialog object and return
            return builder.create();
        }
    }
    */

}
