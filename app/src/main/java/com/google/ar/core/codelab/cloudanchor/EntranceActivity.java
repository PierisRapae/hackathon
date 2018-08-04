package com.google.ar.core.codelab.cloudanchor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class EntranceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        View view = findViewById(R.id.content);

        view.findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleJump();
            }
        });

        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleJump();
            }
        });
    }

    private void handleJump() {
        UploadSymbolDialogFragment dialogFragment = new UploadSymbolDialogFragment();
        dialogFragment.listener = this::onClick;
        dialogFragment.show(getFragmentManager(), "Entrance");
    }

    public void onClick() {
        Intent intent = new Intent(EntranceActivity.this, SearchByNameActivity.class);
        startActivity(intent);

        finish();
    }


    public static class UploadSymbolDialogFragment extends DialogFragment {
        private OnDialogBtnClickListener listener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure you don't want to customize your symbol?")
                    .setPositiveButton("Yes", (dialog, id) -> {
                        if (listener != null) {
                            listener.onClick();
                        }
                    })
                    .setNegativeButton("No", (dialog, id) -> {
                        if (listener != null) {
                            listener.onClick();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }

        public void setDialogBtnClickListener(OnDialogBtnClickListener listener) {
            this.listener = listener;
        }
    }

    interface OnDialogBtnClickListener {
        void onClick();
    }

}
