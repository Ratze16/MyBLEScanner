package com.riesenbeck.myblescanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.riesenbeck.myblescanner.Data.BLEDeviceResult;
import com.riesenbeck.myblescanner.Data.BLEResults;

public class BLEResultActivity extends AppCompatActivity {

    private TextView tvDevice;
    private TextView tvDeviceInfo;
    private BLEResults bleResultsRef = null;
    private BLEDeviceResult bleResult = null;
    private StringBuilder stringBuilder = new StringBuilder();
    private Button mBtnLPDLTest;

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

            try {
                stringBuilder.append(bleResult.getBluetoothDevice().getAddress()+"\n");
                stringBuilder.append(bleResult.getBluetoothDevice().getBluetoothClass().toString()+"\n");
                stringBuilder.append(bleResult.getBluetoothDevice().getBondState()+"\n");
                stringBuilder.append(bleResult.getBluetoothDevice().getName()+"\n");
                stringBuilder.append(bleResult.getBluetoothDevice().getType()+"\n");
                //stringBuilder.append(bleResult.getBluetoothDevice().getUuids().toString()+"\n");
                stringBuilder.append(bleResult.getRssi()+"\n");
                stringBuilder.append(bleResult.getScanRecord()+"\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            tvDevice.setText(bleResult.getBluetoothDevice().getName());
            tvDeviceInfo.setText(stringBuilder.toString());
        }else{
            //output Errormessage
        }
        mBtnLPDLTest = (Button) findViewById(R.id.btnLPDLTest);
        mBtnLPDLTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LDPL_Test.class);
                intent.putExtra(getString(R.string.position), position);
                startActivity(intent);
            }
        });

    }
}
