package com.example.smarthouse;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


public class LockFragment extends Fragment {

    private MainActivity activity;

    public LockFragment(MainActivity activity) {
        this.activity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lock, container, false);
        ImageView a = view.findViewById(R.id.lock_open);

        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.clickOpenCamera();
            }
        });

        ImageView b = view.findViewById(R.id.lock_close);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.closeLock();
            }
        });

        return view;
    }

}