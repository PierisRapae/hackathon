package com.google.ar.core.codelab.cloudanchor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import static com.google.ar.core.codelab.cloudanchor.MainActivity3.EXTRA_USERNAME;

public class FirstGateActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_gate);

        String name = getIntent().getStringExtra(EXTRA_USERNAME);

        View button = findViewById(R.id.ar_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstGateActivity.this, MainActivity3.class);
                intent.putExtra(EXTRA_USERNAME, name);

                startActivity(intent);
                finish();
            }
        });

    }


}
