package com.google.ar.core.codelab.cloudanchor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static com.google.ar.core.codelab.cloudanchor.MainActivity3.EXTRA_USERNAME;

public class SearchByNameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_name);

        EditText tvUsername = findViewById(R.id.tv_name);

        findViewById(R.id.btn_find).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = tvUsername.getText().toString();

                Intent intent = new Intent(SearchByNameActivity.this, MainActivity3.class);
                intent.putExtra(EXTRA_USERNAME, username);
                startActivity(intent);
            }
        });
    }
}
