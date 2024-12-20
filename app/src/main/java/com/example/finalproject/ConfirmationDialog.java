// ConfirmationDialog.java
package com.example.finalproject;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmationDialog extends DialogFragment {

    public interface ConfirmationListener {
        void onConfirm();

        void onCancel();
    }

    private ConfirmationListener listener;
    private String title;
    private String message;

    public static ConfirmationDialog newInstance(String title, String message) {
        ConfirmationDialog dialog = new ConfirmationDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            if (getParentFragment() instanceof ConfirmationListener) {
                listener = (ConfirmationListener) getParentFragment();
            } else {
                listener = (ConfirmationListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Host must implement ConfirmationListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Retrieve title and message from arguments
        if (getArguments() != null) {
            title = getArguments().getString("title");
            message = getArguments().getString("message");
        }

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    if (listener != null) {
                        listener.onConfirm();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    if (listener != null) {
                        listener.onCancel();
                    }
                })
                .setCancelable(true);

        return builder.create();
    }
}
