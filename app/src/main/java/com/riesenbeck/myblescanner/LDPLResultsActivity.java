package com.riesenbeck.myblescanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.riesenbeck.myblescanner.Data.BleDevice;
import com.riesenbeck.myblescanner.Data.LDPLResults;

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
    }

    @Override
    protected void onResume(){
        super.onResume();
        for (int i = 0 ; i < mLDPLResults.size() ; i++){
            TableRow row= new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            BleDevice bleDevice[] = mLDPLResults.get(i);
            for(int j=0; j< mLDPLResults.get(i).length-1;j++){
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
