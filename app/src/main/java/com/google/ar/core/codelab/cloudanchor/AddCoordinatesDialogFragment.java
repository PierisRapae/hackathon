package com.google.ar.core.codelab.cloudanchor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/** A DialogFragment for the Resolve Dialog Box. */
public class AddCoordinatesDialogFragment extends DialogFragment {

    interface OkListener {
        void onOkPressed(double latitude, double longitude);
    }

    private OkListener okListener;
    private EditText edtLatitude;
    private EditText edtLongitude;

    /** Sets a listener that is invoked when the OK button on this dialog is pressed. */
    void setOkListener(OkListener okListener) {
        this.okListener = okListener;
    }

    /**
     * Creates a simple layout for the dialog. This contains a single user-editable text field whose
     * input type is retricted to numbers only, for simplicity.
     */
    private LinearLayout getDialogLayout() {
        Context context = getContext();

        edtLatitude = new EditText(context);
        edtLatitude.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtLatitude.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        edtLatitude.setText("1.30005");

        edtLongitude = new EditText(context);
        edtLongitude.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtLongitude.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        edtLongitude.setText("103.78837");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(edtLatitude);
        layout.addView(edtLongitude);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setView(getDialogLayout())
                .setTitle(R.string.resolve_dialog_title)
                .setPositiveButton(
                        R.string.ok,
                        (dialog, which) -> {
                            Editable latitudeText = edtLatitude.getText();
                            Editable longitudeText = edtLongitude.getText();
                            if (okListener != null && latitudeText != null && latitudeText.length() > 0
                                    && longitudeText != null && longitudeText.length() > 0) {
                                // Invoke the callback with the current checked item.
                                double latitude = Double.parseDouble(latitudeText.toString());
                                double longitude = Double.parseDouble(longitudeText.toString());
                                okListener.onOkPressed(latitude, longitude);
                            }
                        })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {});
        return builder.create();
    }
}