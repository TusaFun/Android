package com.jupiter.tusa.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.background.TusaWorker;
import com.jupiter.tusa.databinding.FragmentCheckAvatarBinding;
import com.jupiter.tusa.uploadfiles.UploadAvatarImageTask;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CheckAvatarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckAvatarFragment extends Fragment {

    private FragmentCheckAvatarBinding binding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ActivityResultLauncher<String> pickImageLauncher;
    private MainActivity mainActivity;


    public CheckAvatarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckAvatarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckAvatarFragment newInstance(String param1, String param2) {
        CheckAvatarFragment fragment = new CheckAvatarFragment();
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
        binding = FragmentCheckAvatarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public static String getPath(Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ContentResolver contentResolver = requireActivity().getContentResolver();
                        InputStream inputStream = null;
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] loadedBytes = null;
                        try {
                            inputStream = contentResolver.openInputStream(uri);
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                byteArrayOutputStream.write(buffer, 0, bytesRead);
                            }

                            loadedBytes = byteArrayOutputStream.toByteArray();
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                if(inputStream != null)
                                    inputStream.close();
                                byteArrayOutputStream.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }

                        assert loadedBytes != null;
                        float currentMegabytes = (float)loadedBytes.length / 1048576;
                        int useQuality = 75;
                        if(currentMegabytes > 1) {
                            useQuality = 50;
                        }

                        // compressing jpeg file
                        byte[] compressed = mainActivity.compressJpegImage(loadedBytes, useQuality);

                        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(TusaWorker.SharedPreferencesName, Context.MODE_PRIVATE);
                        String accessToken = sharedPreferences.getString(TusaWorker.SharedPreferencesAccessTokenKey, "");

                        UploadAvatarImageTask uploadAvatarImageTask = new UploadAvatarImageTask(compressed,accessToken);
                        uploadAvatarImageTask.execute();
                    }
                });

        binding.checkAvatarPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageLauncher.launch("image/jpeg");
            }
        });
    }

    private void saveToFolder(byte[] compressed) {
        // save to download folder
        FileOutputStream fileOutputStream = null;
        try {
            String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/1.jpg";
            fileOutputStream = new FileOutputStream(savePath);
            fileOutputStream.write(compressed, 0, compressed.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}