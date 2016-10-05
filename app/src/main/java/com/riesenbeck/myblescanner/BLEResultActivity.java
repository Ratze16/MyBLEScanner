package com.riesenbeck.myblescanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.riesenbeck.myblescanner.Data.BleDevice;
import com.riesenbeck.myblescanner.Data.BLEResults;

public class BLEResultActivity extends AppCompatActivity {

    private TextView tvDevice;
    private TextView tvDeviceInfo;
    private BLEResults bleResultsRef = null;
    private BleDevice bleResult = null;
    private StringBuilder stringBuilder = new StringBuilder();
    private Button mBtnLPDLTest, mBtnRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleresult);

        bleResultsRef = BLEResults.getInstance();

        initView();
    }
    private void initView(){
        tvDevice = (TextView)findViewById(R.id.tv_Device);
        tvDeviceInfo = (TextView)findViewById(R.id.tv_DeviceInfo);


        final int position = getIntent().getIntExtra(getString(R.string.position),-1);
        if(position!=-1){
            bleResult = bleResultsRef.getBleDeviceResult(position);
            byte[] bytes = bleResult.getmScanRecord();

            try {
                stringBuilder.append("Adress:\t"+bleResult.getmAddress()+"\n");
                stringBuilder.append("BluetoothClass:\t"+bleResult.getmBluetoothDevice().getBluetoothClass().toString()+"\n");
                stringBuilder.append("Bondstate:\t"+bleResult.getmBondState()+"\n");
                stringBuilder.append("Name:\t"+bleResult.getmName()+"\n");
                stringBuilder.append("Type:\t"+bleResult.getmType()+"\n");
                //stringBuilder.append("UUIDs:\t"+bleResult.getmUuids().toString()+"\n");
                stringBuilder.append("RSSI:\t"+bleResult.getmRssi()+"\n\n");
                stringBuilder.append("ScanRecord:\n\t[Byte][Hex][Zweierkomplement]\n");
                for(int i = 0; i<bytes.length;i++){
                    stringBuilder.append("\t["+i+"]["+Integer.toHexString(bytes[i])+"]["+ bytes[i]+"]\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            tvDevice.setText(bleResult.getmBluetoothDevice().getName());
            tvDeviceInfo.setText(stringBuilder.toString());
        }else{
            //output Errormessage
        }
        mBtnLPDLTest = (Button) findViewById(R.id.btnLPDLTest);
        mBtnLPDLTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LDPLTestActivity.class);
                intent.putExtra(getString(R.string.position), position);
                startActivity(intent);
            }
        });
        mBtnRoom = (Button) findViewById(R.id.btnRoom);
        mBtnRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
                startActivity(intent);
            }
        });
    }
}