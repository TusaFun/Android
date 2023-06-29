package com.jupiter.tusa.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.util.concurrent.ListenableFuture;
import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.R;
import com.jupiter.tusa.Tusa;
import com.jupiter.tusa.TusaUserGrpc;
import com.jupiter.tusa.background.PeriodicWorkRequestHelper;
import com.jupiter.tusa.background.TusaWorker;
import com.jupiter.tusa.databinding.FragmentCheckJwtTokenBinding;
import com.jupiter.tusa.grpc.TusaGrpc;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CheckJwtTokenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckJwtTokenFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentCheckJwtTokenBinding binding;
    private MainActivity mainActivity;

    private String mParam1;
    private String mParam2;

    public CheckJwtTokenFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckJwtTokenFragment.
     */
    public static CheckJwtTokenFragment newInstance(String param1, String param2) {
        CheckJwtTokenFragment fragment = new CheckJwtTokenFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCheckJwtTokenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        binding.refreshCheckJwt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences(TusaWorker.SharedPreferencesName, Context.MODE_PRIVATE);
                String lastUpdate = sharedPreferences.getString(
                        TusaWorker.SharedPreferencesLastUpdateKey, "No value");
                long expireTimestamp = sharedPreferences.getLong(
                        TusaWorker.SharedPreferencesAccessTokenExpiresTimestampMillisecondsKey, 0);
                Date expireTimestampDate = new Date(expireTimestamp * 1000);
                binding.preferenceDateText.setText("Last update = " + lastUpdate);
                binding.preferenceExpireTimestamp.setText("Expire time = " + expireTimestampDate.toString());
            }
        });

        binding.checkJwtLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.logout();
            }
        });
    }
}