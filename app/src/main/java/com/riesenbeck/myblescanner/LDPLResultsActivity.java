package com.riesenbeck.myblescanner;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.riesenbeck.myblescanner.Data.BleDevice;
import com.riesenbeck.myblescanner.Data.LDPLResults;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LDPLResultsActivity extends AppCompatActivity {
    private  TableLayout mTlLDPLResults;
    private LDPLResults ldplResultsRef;
    private List<BleDevice[]> mLDPLResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ldpl__results);

        ldplResultsRef = LDPLResults.getInstance();
        mLDPLResults = new ArrayList<BleDevice[]>(ldplResultsRef.getBleDeviceResults());

        mTlLDPLResults = (TableLayout)findViewById(R.id.tl_LDPLResults);
        findViewById(R.id.btnExportCSV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportCSV();
                startActivity(new Intent(getApplicationContext(),RoomActivity.class));
            }
        });
    }

    private void exportCSV() {
        String columnString =   "\"distance\"";
        for(int i= 0; i<10;i++) {
            columnString += ",\"value"+i+"\"";
        }
        String dataString   =   "";
        for (int i = 0 ; i < mLDPLResults.size() ; i++){
            BleDevice bleDevice[] = mLDPLResults.get(i);
            dataString += getResources().getStringArray(R.array.LPDLResults)[i];
            for(int j=0; j< mLDPLResults.get(i).length;j++){
                if(bleDevice[j] !=null) {
                    dataString += ",\""+String.valueOf(bleDevice[j].getmRssi())+"\"";
                } else{
                    dataString += ",\"K.A.\"";
                }

            }
            dataString += "\n";
        }
        String combinedString = columnString + "\n" + dataString;

        File file   = null;
        File root   = Environment.getExternalStorageDirectory();
        if (root.canWrite()){
            File dir    =   new File (root.getAbsolutePath() + "/PersonData");
            dir.mkdirs();
            file   =   new File(dir, "Data.csv");
            FileOutputStream out   =   null;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.write(combinedString.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mTlLDPLResults.destroyDrawingCache();
        for (int i = 0 ; i < mLDPLResults.size() ; i++){
            TableRow row= new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            BleDevice bleDevice[] = mLDPLResults.get(i);
            for(int j=0; j< mLDPLResults.get(i).length;j++){
                TextView textView = new TextView(this);
                String s = "K.A.";
                BleDevice bleDevice1 = bleDevice[j];
                if(bleDevice1 !=null) s =String.valueOf(bleDevice1.getmRssi());
                textView.setText(s);
                row.addView(textView,j);
            }
            mTlLDPLResults.addView(row,i);
        }
    }
}
