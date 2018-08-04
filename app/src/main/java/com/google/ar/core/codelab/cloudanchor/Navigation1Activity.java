package com.google.ar.core.codelab.cloudanchor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import pl.droidsonroids.gif.GifImageView;

import static com.google.ar.core.codelab.cloudanchor.MainActivity3.EXTRA_USERNAME;

public class Navigation1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_show);

        GifImageView gif = findViewById(R.id.nav1_root);
        gif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = getIntent().getStringExtra(EXTRA_USERNAME);
                Intent intent = new Intent(Navigation1Activity.this, FirstGateActivity.class);
                intent.putExtra(EXTRA_USERNAME, username);
                startActivity(intent);
                finish();
            }
        });
    }
}
