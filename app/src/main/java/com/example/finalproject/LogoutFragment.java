package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class LogoutFragment extends Fragment {

    private Button btnLogout;

    public LogoutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_logout, container, false);

        btnLogout = rootView.findViewById(R.id.btn_logout);

        // Handle logout click
        btnLogout.setOnClickListener(v -> {
            // Clear session, authentication, etc.
            // Redirect to SignInActivity
            Intent intent = new Intent(getActivity(), MainPageActivity.class);
            startActivity(intent);
            getActivity().finish();  // Close the current activity
        });

        return rootView;
    }
}
