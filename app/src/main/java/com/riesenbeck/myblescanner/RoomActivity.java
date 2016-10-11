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
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.riesenbeck.myblescanner.Data.BleDevice;
import com.riesenbeck.myblescanner.Data.Room;
import com.riesenbeck.myblescanner.Data.Trilateration;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RoomActivity extends AppCompatActivity {
    private static int BEACON_X = 0, BEACON_Y = 0;
    private final int MEASUREMENTS = 10;
    private int mPosSearchStatus = 0;
    private int[] mRssiList;
    private double mDistance = 0;
    private double mRssiMean = 0;
    private int mRssiMax = 0, mRssiMin = -200;

    //BLE Scan
    private BleDevice mBleDevice;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<ScanFilter> scanFilters;
    private ScanSettings scanSettings;

    private ProgressBar pbPosSearch;
    private ImageView mIvCircle;

    private Button mBtnCalcPosition;
    private ToggleButton mtBtnRefreshBeacons;
    private TableLayout mTlBeacons;

    private String mBleDeviceSearchAddress;
    private double[][] posEmp = new double[3][2];
    private double[] radius = new double[3];
    private int numEmp = 0;
    private double[] posXY = new double[2];

    private Bitmap mBmp;
    private Canvas mCanvas = new Canvas();;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == mBtnInitBeacons){
                mBmp = Bitmap.createBitmap(findViewById(R.id.iV_Circle).getWidth(),findViewById(R.id.iV_Circle).getHeight(),Bitmap.Config.ARGB_8888);
                mCanvas.setBitmap(mBmp);
                initBeacons();
            }
            else if(v==mBtnCalcPosition){
                double[] result = Trilateration.getInstance().dist2Pos(posEmp,radius,numEmp,posXY);
                BEACON_X = (int)(result[0]/11.3*879);
                BEACON_Y = (int)(result[1]/11.3*879);
                mBmp = Bitmap.createBitmap(mIvCircle.getWidth(),mIvCircle.getHeight(),Bitmap.Config.ARGB_8888);
                mCanvas.setBitmap(mBmp);
                double errRad =result[2];
                drawCircle(0.1);
                drawCircle(result[2]);
            }else{
                mTextviewSelected = (TextView) v;

                TableLayout tl = ((TableLayout)mTextviewSelected.getParent().getParent());
                for (int i = 0 ; i<tl.getChildCount();i++){
                    ((TextView)((TableRow)tl.getChildAt(i)).getChildAt(0)).setClickable(false);
                }
                Toast.makeText(getApplicationContext(),"Beaconposition wählen",Toast.LENGTH_SHORT).show();
                mIvCircle.setOnTouchListener(mOnTouchListener);
            }
        }
    };

    private ImageView.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            scanBle(false);
            BEACON_X = (int)event.getX();
            BEACON_Y = (int)event.getY();
            mBleDeviceSearchAddress = String.valueOf(mTextviewSelected.getText());
            mBeaconsSelected++;
            mTextviewSelected.setBackgroundColor(Color.RED);
            drawCircle(0.1);
            mIvCircle.setOnTouchListener(null);

            mTextviewSelected.setSelected(true);

            //Init Searchdata
            mDistance = 0;
            mPosSearchStatus = 0;
            mRssiList = new int[MEASUREMENTS];
            mRssiMean = 0;
            mRssiMax = 0;
            mRssiMin = -200;

            scanBle(true);
            return false;
        }
    };

    //BLECallback for API Version <21
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            try {
                BleDevice bleDevice = new BleDevice(device, rssi,scanRecord,System.nanoTime());
                if(device.getAddress().equals(bleDevice.getmAddress())){
                    scanBle(false);

                    try {
                        if(mPosSearchStatus < MEASUREMENTS){
                            mRssiList[mPosSearchStatus] = bleDevice.getmRssi();
                            mPosSearchStatus++;
                            pbPosSearch.setProgress(mPosSearchStatus);
                            scanBle(true);
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

            if(mtBtnRefreshBeacons.isChecked()==true){
                //Todo Create Table
                addTableRow(result.getDevice().getAddress());
            }
            else {
                try {
                    BleDevice bleDevice = new BleDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes(), result.getTimestampNanos());
                    if (result.getDevice().getAddress().equals(mBleDeviceSearchAddress)) {
                        scanBle(false);

                        try {
                            if (mPosSearchStatus < MEASUREMENTS) {
                                mRssiList[mPosSearchStatus] = bleDevice.getmRssi();
                                mPosSearchStatus++;
                                pbPosSearch.setProgress(mPosSearchStatus);
                                scanBle(true);
                            } else {
                                for (int rssi : mRssiList) {
                                    if(rssi < mRssiMax) mRssiMax=rssi;
                                    if(rssi > mRssiMin) mRssiMin=rssi;
                                    mRssiMean = mRssiMean + rssi;
                                }

                                mRssiMean = mRssiMean - mRssiMax - mRssiMin; //highest and lowest outliers
                                mRssiMean = mRssiMean / (mRssiList.length-2);
                                mDistance = Room.getInstance().getExponential(mRssiMean, -65);
                                posEmp[numEmp][0]=BEACON_X*11.3/879;
                                posEmp[numEmp][1]=BEACON_Y*11.3/879;
                                double distancePX = mDistance;
                                radius[numEmp] = distancePX;
                                numEmp++;

                                drawCircle(mDistance);

                                TableLayout tl = ((TableLayout)mTextviewSelected.getParent().getParent());
                                for (int i = 0 ; i<tl.getChildCount();i++){
                                    if(((TextView)((TableRow)tl.getChildAt(i)).getChildAt(0)).isSelected()==false)
                                    ((TextView)((TableRow)tl.getChildAt(i)).getChildAt(0)).setClickable(true);
                                }
                                if(mBeaconsSelected>1){
                                    mBtnCalcPosition.setEnabled(true);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private int mBeaconsSelected = 0;
    private Button mBtnSetBeaconPos;
    private TextView mTextviewSelected;
    private double BEACON_X1, BEACON_Y1, BEACON_R1, BEACON_X2, BEACON_Y2, BEACON_R2;
    private Button mBtnInitBeacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        initBLE();

        pbPosSearch = (ProgressBar)findViewById(R.id.pB_posSearch);
        pbPosSearch.setMax(MEASUREMENTS);

        mIvCircle = (ImageView)findViewById(R.id.iV_Circle);
        mTlBeacons = (TableLayout)findViewById(R.id.tl_Beacons);
        mBtnCalcPosition =(Button)findViewById(R.id.btn_calcPosition);
        mtBtnRefreshBeacons =(ToggleButton)findViewById(R.id.tBtn_findBeacons);
        mBtnInitBeacons = (Button) findViewById(R.id.btn_initBeacons);

        mtBtnRefreshBeacons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mBmp = Bitmap.createBitmap(mIvCircle.getWidth(),mIvCircle.getHeight(),Bitmap.Config.ARGB_8888);
                    mCanvas.setBitmap(mBmp);

                    mTlBeacons.removeAllViews();
                    scanBle(true);
                    for(int i=0 ; i<mTlBeacons.getChildCount();i++){
                        ((TextView)((TableRow)mTlBeacons.getChildAt(i)).getChildAt(0)).setOnClickListener(null);
                    }
                }else{
                    scanBle(false);
                    for(int i=0 ; i<mTlBeacons.getChildCount();i++){
                        ((TextView)((TableRow)mTlBeacons.getChildAt(i)).getChildAt(0)).setOnClickListener(mOnClickListener);
                    }
                }
            }
        });
        mBtnCalcPosition.setOnClickListener(mOnClickListener);
        mBtnInitBeacons.setOnClickListener(mOnClickListener);
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

    private void scanBle(boolean enable){
        if(enable){
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
        }else{
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                try {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                } catch (IllegalStateException e) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.errorcode0x0011), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void drawCircle(double r){
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(125);

        mCanvas.drawCircle(BEACON_X,BEACON_Y , (float) (r/11.3*879), paint);
        mIvCircle.setImageBitmap(mBmp);
    }


    private void addTableRow(String BeaconID) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setMinimumHeight(50);
        row.setLayoutParams(lp);
        final TextView textView = new TextView(this);
        textView.setBackgroundColor(Color.LTGRAY);
        textView.setText(BeaconID);
        textView.setClickable(true);
        row.addView(textView);
        mTlBeacons.addView(row);
    }

    private void initBeacons(){
        BEACON_R1 = 0.9;
        BEACON_X = 300;
        BEACON_Y = 160;
        drawCircle(BEACON_R1);
        posEmp[0][0] = BEACON_X*11.3/879;
        posEmp[0][1] = BEACON_Y*11.3/879;
        radius[0] = 0.9;

        BEACON_R1 = 1.1;
        BEACON_X = 500;
        BEACON_Y = 150;
        drawCircle(BEACON_R1);
        posEmp[1][0] = BEACON_X*11.3/879;
        posEmp[1][1] = BEACON_Y*11.3/879;
        radius[1] = 1.1;

        BEACON_R1 = 1.1;
        BEACON_X = 658;
        BEACON_Y = 400;
        drawCircle(BEACON_R1);
        posEmp[2][0] = BEACON_X*11.3/879;
        posEmp[2][1] = BEACON_Y*11.3/879;
        radius[2] = 1.1;

        numEmp = 2;
    }

}
