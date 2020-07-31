/*
 * Copyright 2020 Levigo Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.levigo.levigoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Logs in user
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth mAuth;
    private EditText mEmail, mPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        userIsLoggedIn();

        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userIsLoggedIn();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to login.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void userIsLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                userIsLoggedIn();
            } else {
                Log.d(TAG, "Sign in cancelled");
            }
        }
    }

    public void resetPassword(View view) {
        startActivity(new Intent(getApplicationContext(), ResetActivity.class));
    }

    public void login(View view) {
        final String email = mEmail.getText().toString(),
                password = mPassword.getText().toString();
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signIn(email, password);
        } else {
            mEmail.setError("Please enter a valid email address.");
        }
    }

    public void register(View view) {
        startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
    }
}