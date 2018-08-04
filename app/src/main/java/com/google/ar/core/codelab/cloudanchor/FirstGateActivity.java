package com.google.ar.core.codelab.cloudanchor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class FirstGateActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_gate);

        View button = findViewById(R.id.ar_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstGateActivity.this, MainActivity3.class);
                startActivity(intent);
                finish();
            }
        });

    }


}
