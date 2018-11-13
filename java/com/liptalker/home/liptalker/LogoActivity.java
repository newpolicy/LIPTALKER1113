package com.liptalker.home.liptalker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LogoActivity extends AppCompatActivity {

    //파이어베이스 RemoteConfig 사용
    FirebaseRemoteConfig firebaseRemoteConfig;
    FirebaseRemoteConfigSettings firebaseRemoteConfigSettings;

    //uid값을 LogoActivity에서 사용
    private String uid;


    private LinearLayout linearLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        linearLayout = (LinearLayout)findViewById(R.id.background_linearlayout_LogoActivity);

        //싱글톤 패턴을 활용해 앱에 RemoteConfig 추가
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        //RemoteConfig 초기값 설정
        firebaseRemoteConfig.setDefaults(R.xml.default_config);

        //RemoteConfig 사용자 설정 추가
        firebaseRemoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);

        //RemoteConfig 설정 값이 업데이트 되는 주기 설정 ex) 0 -> 0초,  3600 -> 1시간
        firebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    firebaseRemoteConfig.activateFetched();
                }else{}
                //로고 화면에 띄울 메시지창
                displayWelcomeMessage();
            }
        });
    }

    void displayWelcomeMessage(){

        //파이어 베이스에 RemoteConfig에 저장된 liptalker _backgroundcolor, _message_caps, _message 가져오기
        String liptalker_backgroundcolor = firebaseRemoteConfig.getString("liptalker_backgroundcolor");
        boolean caps = firebaseRemoteConfig.getBoolean("liptalker_message_caps");
        String liptalker_message = firebaseRemoteConfig.getString("liptalker_message");

        //가져온 값을 로고화면에 적용
        linearLayout.setBackgroundColor(Color.parseColor(liptalker_backgroundcolor));

        //caps값이 true면 어플 강제 종료 ex) 서버점검시 활용 가능
        if(caps){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(liptalker_message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //어플 강제 종료
                    finish();
                }
            });
            builder.create().show();
        }//caps값이 false면 계속 실행
        else {

            //내부 저장소 안의 LIPTALKER.txt에 있는 uid값 가져오기
            String filename = "LIPTALKER";
            try {
                Log.i("logodir", getFilesDir().toString());
                BufferedReader br = new BufferedReader(new FileReader(getFilesDir() + filename + ".txt"));
                String readStr = "";
                String str = null;
                while ((str = br.readLine()) != null)
                    readStr = readStr + str + "\n";
                br.close();
                uid = readStr;
            }catch(FileNotFoundException f){
            }catch (IOException e){}
            //uid값이 있으면 Main으로, 없으면 Singup으로 이동
            if(uid != null){
                Log.i("sos", uid);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }else {
                startActivity(new Intent(this, SignupActivity.class));
                //LogoActivity 종료
                finish();
            }
        }
    }
}






















