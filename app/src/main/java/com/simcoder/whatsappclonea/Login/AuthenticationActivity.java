package com.simcoder.whatsappclonea.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simcoder.whatsappclonea.Activity.MainActivity;
import com.simcoder.whatsappclonea.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AuthenticationActivity extends AppCompatActivity {

    FragmentManager fm = getSupportFragmentManager();
    PhoneFragment phoneFragment = new PhoneFragment();

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    String phoneNumber, mVerificationId;

    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        userIsLoggedIn();

        fm.beginTransaction()
                .replace(R.id.container, phoneFragment, "phoneFragment")
                .addToBackStack(null)
                .commit();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                dialog = ProgressDialog.show(AuthenticationActivity.this, "",
                        "Signing In. Please wait...", true);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                nextClick();
                mVerificationId = verificationId;
            }
        };
    }

    public void nextClick(){
        fm.beginTransaction()
                .replace(R.id.container, new CodeFragment(), "CodeFragment")
                .addToBackStack(null)
                .commit();
    }

    public void verifyPhoneNumberWithCode(String code){
        if(code.length() <= 0)
            return;
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if(user != null){
                        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()){
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("name", user.getPhoneNumber());
                                    mUserDB.updateChildren(userMap);
                                }
                                userIsLoggedIn();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }else{
                    if(dialog!=null)
                        dialog.dismiss();
                }

            }
        });
    }



    public void startPhoneNumberVerification(String phoneNumber) {
        if(phoneNumber.length() <= 0)
            return;

        this.phoneNumber = phoneNumber;

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            return;
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }
}
