package com.jupiter.tusa.ui;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.jupiter.tusa.BuildConfig;
import com.jupiter.tusa.MainActivity;
import com.jupiter.tusa.background.PeriodicWorkRequestHelper;
import com.jupiter.tusa.background.TusaWorker;
import com.jupiter.tusa.data.LoginRepository;
import com.jupiter.tusa.data.model.LoggedInUser;
import com.jupiter.tusa.databinding.FragmentLoginBinding;

import com.jupiter.tusa.R;

import java.util.Date;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final TextInputLayout usernameInputLayout = binding.username;
        final TextInputLayout passwordInputLayout = binding.password;
        final EditText usernameEditText = binding.username.getEditText();
        final EditText passwordEditText = binding.password.getEditText();
        final FloatingActionButton loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        usernameEditText.requestFocus();

        if(BuildConfig.BUILD_TYPE == "debug") {
            usernameEditText.setText("invectys");
            passwordEditText.setText("qzwsecrftb");
            loginButton.setEnabled(true);
        }

        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    //usernameEditText.setError(getString(loginFormState.getUsernameError()));
                    usernameInputLayout.setErrorEnabled(true);
                    usernameInputLayout.setError(getString(loginFormState.getUsernameError()));
                } else  usernameInputLayout.setErrorEnabled(false);
                if (loginFormState.getPasswordError() != null) {
                    //passwordEditText.setError(getString(loginFormState.getPasswordError()));
                    passwordInputLayout.setErrorEnabled(true);
                    passwordInputLayout.setError(getString(loginFormState.getPasswordError()));
                } else passwordInputLayout.setErrorEnabled(false);
            }
        });

        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {

                    // save to preferences on success login
                    LoginRepository loginRepository = LoginRepository.getInstance(null);
                    LoggedInUser user = loginRepository.getUser();
                    String accessToken = user.getAccessToken();
                    String refreshToken = user.getRefreshToken();
                    double expireIn = user.getExpireInMilliseconds();
                    long expireTimestamp = user.getExpirationTimestamp();
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences(TusaWorker.SharedPreferencesName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(TusaWorker.SharedPreferencesLastUpdateKey, new Date().toString());
                    editor.putString(TusaWorker.SharedPreferencesAccessTokenKey, accessToken);
                    editor.putString(TusaWorker.SharedPreferencesRefreshTokenKey, refreshToken);
                    editor.putFloat(TusaWorker.SharedPreferencesAccessTokenExpiresInMillisecondsKey, (float)expireIn);
                    editor.putLong(TusaWorker.SharedPreferencesAccessTokenExpiresTimestampMillisecondsKey, expireTimestamp);
                    editor.apply();

                    PeriodicWorkRequestHelper.requestMainWorker(requireContext(), false);

                    updateUiWithUser(loginResult.getSuccess());
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }

        MainActivity mainActivity = (MainActivity)getActivity();
        assert mainActivity != null;
        CheckJwtTokenFragment checkJwtTokenFragment = new CheckJwtTokenFragment();
        mainActivity.replaceFragmentWithAnimation(checkJwtTokenFragment);
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}