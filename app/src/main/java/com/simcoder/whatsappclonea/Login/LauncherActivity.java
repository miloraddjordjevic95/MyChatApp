package com.simcoder.whatsappclonea.Login;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.simcoder.whatsappclonea.Activity.MainActivity;


public class LauncherActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }else{
            Intent intent = new Intent(LauncherActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }
}
