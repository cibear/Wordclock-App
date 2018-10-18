package de.betzen.wordclock;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class IPActivity extends AppCompatActivity {


    public static final String IP_ADDRESS = "de.betzen.wordclock.IP_ADDRESS";

    NumberPicker npA;
    NumberPicker npB;
    NumberPicker npC;
    NumberPicker npD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        //initialize numberpickers and propose typical values
        npA = findViewById(R.id.numberPickerA);
        npB = findViewById(R.id.numberPickerB);
        npC = findViewById(R.id.numberPickerC);
        npD = findViewById(R.id.numberPickerD);

        NumberPicker npList[] = new NumberPicker[] {npA,npB,npC,npD};

        for (NumberPicker np: npList) {
            np.setMinValue(0);
            np.setMaxValue(255);
        }
        npA.setValue(192);
        npB.setValue(168);

        //prepare connect button
        Button button = findViewById(R.id.buttonConnect);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMainActivity();
            }
        });
    }

    private void gotoMainActivity() {
        String ip_address = new String();
        //generate IP adress from numberpicker (uses TextUtils.join instead of String.join to reduce API level requirements
        ip_address = TextUtils.join(".",new String[] {Integer.toString(npA.getValue()),Integer.toString(npB.getValue()),Integer.toString(npC.getValue()),Integer.toString(npD.getValue())});
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(IP_ADDRESS, ip_address);
        startActivity(intent);
    }
}
