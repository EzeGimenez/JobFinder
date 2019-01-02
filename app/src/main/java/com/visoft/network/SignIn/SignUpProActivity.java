package com.visoft.network.SignIn;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.visoft.network.R;
import com.visoft.network.TurnProFragments.TurnProActivity;
import com.visoft.network.funcionalidades.AccountManager;
import com.visoft.network.funcionalidades.AccountManagerFirebaseNormal;
import com.visoft.network.funcionalidades.HolderCurrentAccountManager;
import com.visoft.network.funcionalidades.LoadingScreen;

public class SignUpProActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGNUP = 3, RC_GOPRO = 5;

    private EditText etEmail, etPassword, etConfirmPassword, etUsername;
    private LoadingScreen loadingScreen;
    private AccountManager accountManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        this.accountManager = HolderCurrentAccountManager.getCurrent(new AccountManagerFirebaseNormal.ListenerRequestResult() {
            @Override
            public void onRequestResult(boolean result, int requestCode, Bundle data) {
                loadingScreen.hide();
                if (requestCode == RC_SIGNUP && result) {
                    startProConfig();
                } else if (!result) {
                    if (data != null) {
                        showSnackBar(data.getString("error"));
                    }
                }
            }
        });

        this.loadingScreen = new LoadingScreen(this, (ViewGroup) findViewById(R.id.rootView));

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.edConfirmPassword);

        findViewById(R.id.btnSignUp).setOnClickListener(this);
    }

    private void startProConfig() {
        Intent intent = new Intent(this, TurnProActivity.class);
        intent.putExtra("proUser", accountManager.getCurrentUser(1));
        intent.putExtra("isEditing", false);
        startActivityForResult(intent, RC_GOPRO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, null);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignUp:
                signUp(etUsername.getText().toString(), etEmail.getText().toString(), etPassword.getText().toString(), etConfirmPassword.getText().toString());
                break;
        }
    }

    private void signUp(final String username, String email, String pw, String pw2) {
        if (pw.equals(pw2)) {
            try {
                loadingScreen.show();
                accountManager.signUp(username, email, pw2, RC_SIGNUP);
            } catch (Exception e) {
                loadingScreen.hide();
                showSnackBar(e.getMessage());
            }
        } else {
            showSnackBar(getString(R.string.passwords_dont_match));
        }
    }

    private void showSnackBar(String msg) {
        Snackbar.make(findViewById(R.id.rootView),
                msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}