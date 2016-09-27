package com.riesenbeck.myblescanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.riesenbeck.myblescanner.Data.BLEDeviceResult;
import com.riesenbeck.myblescanner.Data.LDPLResults;

import java.util.ArrayList;
import java.util.List;


public class LDPL_Results extends AppCompatActivity {
    private  TableLayout mTlLDPLResults;
    private LDPLResults ldplResultsRef;
    private List<BLEDeviceResult[]> mLDPLResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ldpl__results);

        ldplResultsRef = LDPLResults.getInstance();
        mLDPLResults = new ArrayList<BLEDeviceResult[]>(ldplResultsRef.getBleDeviceResults());

        mTlLDPLResults = (TableLayout)findViewById(R.id.tl_LDPLResults);

        for (int i = 0 ; i < mLDPLResults.size() ; i++){
            TableRow row= new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            for(int j=0; j< mLDPLResults.get(i).length;j++){
                TextView textView = new TextView(this);
                textView.setText((mLDPLResults.get(i)[j]).getRssi());
                row.addView(textView);
            }
            mTlLDPLResults.addView(row);
        }
    }
}
