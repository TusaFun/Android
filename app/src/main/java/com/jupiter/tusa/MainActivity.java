package com.jupiter.tusa;

import static android.os.Environment.isExternalStorageRemovable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;

import com.google.common.util.concurrent.ListenableFuture;
import com.jupiter.tusa.background.PeriodicWorkRequestHelper;
import com.jupiter.tusa.background.TusaWorker;
import com.jupiter.tusa.cache.CacheStorage;
import com.jupiter.tusa.databinding.ActivityMainBinding;
import com.jupiter.tusa.grpc.TusaGrpc;
import com.jupiter.tusa.ui.CheckAvatarFragment;
import com.jupiter.tusa.ui.LoginFragment;
import com.jupiter.tusa.ui.MapFragment;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'tusa' library on application startup.
    static {
        System.loadLibrary("tusa");
    }

    // cache
    private CacheStorage cacheStorage;

    private ActivityMainBinding binding;
    private FragmentManager fragmentManager;

    public CacheStorage getCacheStorage() {
        return cacheStorage;
    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    public void replaceFragmentWithAnimation(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    public void logout() {
        // Revoke refresh токена
        TusaUserGrpc.TusaUserFutureStub tusaUserFutureStub = TusaGrpc.getInstance()
                .getTusaUserFutureStubWithAccessToken(getApplicationContext());
        Tusa.RevokeTusaTokenRequest revokeTusaTokenRequest = Tusa.RevokeTusaTokenRequest.newBuilder()
                .build();
        ListenableFuture<Tusa.RevokeTusaTokenReply> revokeReply =
                tusaUserFutureStub.revokeTusaToken(revokeTusaTokenRequest);

        // Перекидывваем обратно в логин форму
        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_left,
                R.anim.slide_out_left
        );
        fragmentTransaction.replace(R.id.fragment_container, loginFragment);
        fragmentTransaction.commit();

        // Чистим preferences от информации о токене
        SharedPreferences sharedPreferences = getSharedPreferences(
                TusaWorker.SharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TusaWorker.SharedPreferencesAccessTokenKey);
        editor.remove(TusaWorker.SharedPreferencesRefreshTokenKey);
        editor.remove(TusaWorker.SharedPreferencesLastUpdateKey);
        editor.remove(TusaWorker.SharedPreferencesAccessTokenExpiresTimestampMillisecondsKey);
        editor.remove(TusaWorker.SharedPreferencesAccessTokenExpiresInMillisecondsKey);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cacheStorage = new CacheStorage(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fragmentManager = getSupportFragmentManager();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                TusaWorker.SharedPreferencesName, Context.MODE_PRIVATE);
        long expirationTimestamp = sharedPreferences.getLong(TusaWorker.SharedPreferencesAccessTokenExpiresTimestampMillisecondsKey, 0);
        Date expirationTimestampDate = new Date(expirationTimestamp * 1000);

        if(expirationTimestampDate.before(new Date())) {
            LoginFragment loginFragment = new LoginFragment();
            setFragment(loginFragment);
        } else {
            Fragment mainFragment = new MapFragment();
            setFragment(mainFragment);
            PeriodicWorkRequestHelper.requestMainWorker(getApplicationContext(), true);
        }

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    public File getDiskCacheDir(String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable()
                         ? getExternalCacheDir().getPath() :  getCacheDir().getPath();


        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * A native method that is implemented by the 'tusa' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native byte[] compressJpegImage(byte[] inputImage, int quality);
    //public native void drawImage();
}