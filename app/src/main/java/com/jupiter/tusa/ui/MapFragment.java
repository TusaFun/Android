package com.jupiter.tusa.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jupiter.tusa.R;
import com.jupiter.tusa.map.MapGlSurfaceView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MapGlSurfaceView myGlSurfaceView;

    public MapFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
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

    private float[] getMeLatLng() {
        return new float[] {55.7558f, 37.6173f};
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        MapGlSurfaceView mapGlSurfaceView = view.findViewById(R.id.map_gl_surface);

        FloatingActionButton showMeActionButton = view.findViewById(R.id.show_me_action_button);
        showMeActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float[] myLocationLatLng = getMeLatLng();
                //mapGlSurfaceView.setCameraLatLng(myLocationLatLng[0], myLocationLatLng[1], 11);
                mapGlSurfaceView.setCameraZ(5);
                mapGlSurfaceView.requestRender();
            }
        });

        return view;
    }
}

