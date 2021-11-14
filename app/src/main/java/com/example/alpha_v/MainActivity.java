package com.example.alpha_v;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactor;
import com.google.firebase.auth.MultiFactorAssertion;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    EditText mailET, phoneET, codeET;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String storedVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    ActionCodeSettings actionCodeSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mailET = (EditText) findViewById(R.id.mailET);
        phoneET = (EditText) findViewById(R.id.phoneET);
        codeET = (EditText) findViewById(R.id.codeET);

        FirebaseApp.initializeApp(this); // ??????????????????????????????????

        SharedPreferences signInState = getSharedPreferences("Sign_In_State", MODE_PRIVATE);

        if (signInState.getBoolean("isMailSent", false)) {
            Intent intent = getIntent();
            String emailLink = intent.getData().toString();

            // put it back to false (would not read the file every time)
            SharedPreferences.Editor editor = signInState.edit();
            editor.putBoolean("isMailSent", false);
            editor.commit();

            // Confirm the link is a sign-in with email link.
            if (FBref.auth.isSignInWithEmailLink(emailLink)) {
                // The client SDK will parse the code from the link for you.
                FBref.auth.signInWithEmailLink(signInState.getString("mail", ""), emailLink)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    final Gson gson = new Gson();
                                    linkMailPhone(gson.fromJson(signInState.getString("credential", ""), PhoneAuthCredential.class));
                                } else {
                                    Toast.makeText(MainActivity.this, "error in isSignInWithEmailLink", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId;
                resendToken = token; // ?????????????????????????

                codeET.setVisibility(View.VISIBLE);
            }
        };
    }

    public void linkMailPhone(PhoneAuthCredential credential) {
        FBref.auth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "LASTTTTTTT", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        /*
        FBref.auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {




                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(MainActivity.this, "workkk", Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/

        actionCodeSettings = ActionCodeSettings.newBuilder()
                // URL you want to redirect back to. The domain (www.example.com) for this
                // URL must be whitelisted in the Firebase Console.
                .setUrl("https://alphav.page.link/finishSignUp")
                // This must be true
                .setHandleCodeInApp(true)
                /*.setIOSBundleId("com.example.ios")*/
                .setAndroidPackageName(
                        "com.example.alpha_v",
                        false, /* installIfNotAvailable */
                        null    /* minimumVersion */)
                .build();

        FBref.auth.sendSignInLinkToEmail(mailET.getText().toString(), actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Email sent", Toast.LENGTH_SHORT).show();

                            SharedPreferences signInState = getSharedPreferences("Sign_In_State", MODE_PRIVATE);
                            SharedPreferences.Editor editor = signInState.edit();
                            editor.putBoolean("isMailSent", true);
                            editor.putString("mail", mailET.getText().toString());

                            // save the current user credential
                            final Gson gson = new Gson();
                            String serializedObject = gson.toJson(credential);
                            editor.putString("credential", serializedObject);

                            editor.commit();
                        } else {
                            Toast.makeText(MainActivity.this, "Email wasnt sent :( !!!!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        /*
        AuthCredential email0credential = EmailAuthProvider.getCredential("itay.v2004@gmail.com", "aa123456789");
        FBref.auth.getCurrentUser().linkWithCredential(email0credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            // Report failure to user
                        }
                    }
                });*/

        // todo: https://droidmentor.com/password-less-email-firebase-auth/
    }

    public void getCode(View view) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FBref.auth)
                        .setPhoneNumber(phoneET.getText().toString())       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void signUp(View view) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(storedVerificationId, codeET.getText().toString());
        //PhoneAuthCredential credential = PhoneAuthProvider.getCredential(storedVerificationId, "123456");
        signInWithPhoneAuthCredential(credential);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.gallery) {
            Intent si = new Intent(this, GalleryActivity.class);
            startActivity(si);
        }
        else if (id == R.id.camera)
        {
            Intent si = new Intent(this, CameraActivity.class);
            startActivity(si);
        }
        else if (id == R.id.uploadFile)
        {
            Intent si = new Intent(this, UploadFileActivity.class);
            startActivity(si);
        }
        else if (id == R.id.calendar)
        {
            Intent si = new Intent(this, CalendarActivity.class);
            startActivity(si);
        }

        return true;
    }
}