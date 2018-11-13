package com.liptalker.home.liptalker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    FirebaseAuth firebaseAuth;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private RelativeLayout phonenumberLayout;
    private EditText nationalcode;
    private EditText phonenumber;
    private Button startverificationButton;

    private RelativeLayout verification;
    private EditText verficationcode;
    private Button verifyphoneButton;
    private Button resendButton;
    private Button editPhonenumberButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        //휴대폰번호 입력하는 프레임
        phonenumberLayout = (RelativeLayout)findViewById(R.id.phonenumber_RelativeLayout_SignupActivity);
        nationalcode = (EditText)findViewById(R.id.nationalcode_EditText_SignupActivity);
        phonenumber = (EditText)findViewById(R.id.phonenumber_EditText_SignupActivity);
        startverificationButton = (Button)findViewById(R.id.startverification_Button_SignupActivity);
        //인증번호 입력하는 프레임
        verification = (RelativeLayout)findViewById(R.id.verification_SignupActivity_RelativeLayout);
        verficationcode = (EditText)findViewById(R.id.fieldVerificationCode_EditText_SignupActivity);
        verifyphoneButton = (Button)findViewById(R.id.verification_Button_SignupActivity);
        resendButton = (Button)findViewById(R.id.resend_Button_SignupActivity);
        editPhonenumberButton = (Button)findViewById(R.id.editPhonenumber_Button_SignupActivity);

        firebaseAuth = FirebaseAuth.getInstance();
        //클릭 메소드 연결
        startverificationButton.setOnClickListener(this);
        verifyphoneButton.setOnClickListener(this);
        resendButton.setOnClickListener(this);
        editPhonenumberButton.setOnClickListener(this);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                mVerificationInProgress = false;
                //여기까지 어디선가에서 sms인증코드 확인절차가 진행이 된다.
                signInWithPhoneAuthCredential(credential);
                //인증 확인이 끝났다.?
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                mVerificationInProgress = false;
                //전화번호를 잘못 입력했을 경우 다시 전화번호 입력하는 레이아웃으로 돌아간다.
                if (e instanceof FirebaseAuthInvalidCredentialsException ||
                        phonenumber.length() < 11
                        || phonenumber == null) {
                    phonenumberLayout.setVisibility(View.VISIBLE);
                    verification.setVisibility(View.INVISIBLE);
                    phonenumber.setError("전화번호를 확인하세요.");
                    return;
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    //인증번호를 계속 요청하거나 인증횟수가 일정 이상 초과하면 1시간 후 인증번호를 받을 수 있다.
                    Snackbar.make(findViewById(android.R.id.content), "요청횟수 초과 잠시후 시도하세요.",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        if (mVerificationInProgress && validatePhoneNumber()) {
            if (phonenumber.getText().toString().length() > 1) {
                startPhoneNumberVerification(nationalcode.getText().toString()
                        + phonenumber.getText().toString()
                        .substring(1, phonenumber.length()));
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }
    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        mVerificationInProgress = true;
    }
    //이곳에서 인증코드 일치여부 확인
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }
    // [START resend_verification] 인증코드 재전송
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    //코드 일치시 계정 생성
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            phonenumberLayout.setVisibility(View.INVISIBLE);
                            verification.setVisibility(View.INVISIBLE);
                            FirebaseUser user = task.getResult().getUser();
                            String uid = user.getUid();
                            createUIDFile(uid);
                            startActivity(new Intent(SignupActivity.this, ProfileActivity.class));
                            finish();
                            try {
                            }catch (Exception e){}
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                verficationcode.setError("코드 불일치.");
                            }
                        }
                    }
                });
    }
    //계정이 생성되면 프로젝트 내에 LIPTALKER.txt파일을 생성하고 uid를 입력한 뒤 다른 액티비티에서 불러와 사용한다.
    //이는 오프라인에서도 앱의 정상 작동을 위해 만들었다.
    private void createUIDFile(String uid){
        try{
            String filename = "LIPTALKER";
            BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir()+filename+".txt", true));
            bw.write(uid);
            bw.close();
        }catch (IOException e){}
    }
    private boolean validatePhoneNumber() {
        String phoneNumber = "";
        if (phonenumber.getText().toString().length() > 1){
            phoneNumber = nationalcode.getText().toString() + phonenumber.getText().toString()
                    .substring(1,phonenumber.length());}
        if (TextUtils.isEmpty(phonenumber.getText().toString()) ||
                phoneNumber.length() < 13
                ) {
            phonenumber.setError("전화번호를 확인하세요.");
            phonenumberLayout.setVisibility(View.VISIBLE);
            verification.setVisibility(View.INVISIBLE);
            return false;
        }
        return true;
    }
    //각각의 버튼 클릭 메소드 재정의
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startverification_Button_SignupActivity:
                if (!validatePhoneNumber()) {
                    return;
                }
                if (phonenumber.getText().toString().length() > 1){
                    startPhoneNumberVerification(nationalcode.getText().toString() + phonenumber.getText().toString()
                            .substring(1,phonenumber.length()));}
                phonenumberLayout.setVisibility(View.INVISIBLE);
                verification.setVisibility(View.VISIBLE);
                break;
            case R.id.verification_Button_SignupActivity:
                String code = verficationcode.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    try{
                        verficationcode.setError("Cannot be empty.");
                    }catch (Exception e){}
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.resend_Button_SignupActivity:
                resendVerificationCode(nationalcode.getText().toString() + phonenumber.getText().toString()
                        .substring(1,phonenumber.length()), mResendToken);
                break;
            case R.id.editPhonenumber_Button_SignupActivity:
                phonenumberLayout.setVisibility(View.VISIBLE);
                verification.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
