package com.liptalker.home.liptalker;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.liptalker.home.liptalker.model.UserModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final int PICK_FROM_ALBUM = 10;

    private ImageView profileImage;
    private EditText name;
    private Button save;

    private String uid;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = (ImageView)findViewById(R.id.Profileimg_ImageView_ProfileActivity);
        name = (EditText)findViewById(R.id.Name_EditText_ProfileActiviy);
        save = (Button)findViewById(R.id.Save_Button_ProfileActivity);
        try{//휴대폰 앨범 불러오기
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent, PICK_FROM_ALBUM);
                }
            });
        }catch (Exception e){}
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //이름이 null일 경우 리턴
                String nickname = name.getText().toString();
                if (TextUtils.isEmpty(nickname)){
                    name.setError("Name 필수 입력");
                    return;
                }
                try{//프로젝트 내부에 저장된 uid 불러오기
                    String filename = "LIPTALKER";
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(getFilesDir() + filename + ".txt"));
                        String readStr = "";
                        String str = null;
                        while ((str = br.readLine()) != null)
                            readStr = readStr + str;
                        br.close();
                        uid = readStr;
                    }catch(FileNotFoundException f){
                    }catch (IOException e){}
                    //프로필 사진 추가 이미지뷰를 클릭한 뒤 이미지파일을 선택하지 않았거나 아얘 이미지뷰를 클릭하지 않았으면 이름만 저장한다.
                    if(imageUri == null){
                        UserModel userModel = new UserModel();
                        userModel.username = name.getText().toString();
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel);
                    }else {
                        //imageView에 있는 프로필이미지 파일을 파이어베이스 스토리지에 저장하기
                        StorageReference storageRef = storage.getReference();
                        Uri file = imageUri;
                        final StorageReference uploadRef = storageRef.child("userImages").child(uid);
                        UploadTask uploadTask = uploadRef.putFile(file);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            }
                        });
                        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return uploadRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    // 파이어베이스 스토리지에 저장된 이미지 파일의 다운로드 주소,EditText에 있는 이름을
                                    //파이어베이스 데이터베이스에 저장하기
                                    Uri downloadUri = task.getResult();
                                    String down = downloadUri.toString();
                                    UserModel userModel = new UserModel();
                                    userModel.profileImageUrl = down;
                                    userModel.username = name.getText().toString();
                                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel);
                                } else {
                                    return;
                                }
                            }
                        });
                        uriTask.isSuccessful();
                    }
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    finish();
                }catch (Exception e){}
            }
        });
    }
    //앨범에서 선택한 이미지를 ImageView에 붙여넣기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try{
            if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){
                if(data.getData() == null){
                    //앨범에서 아무것도 선택하지 않고 앨범을 닫은 경우
                    return;
                }else {
                    //앨범에서 선택한 이미지를 저장
                    profileImage.setImageURI(data.getData());//ㄱㅏ운데 뷰를 바꿈
                    imageUri = data.getData(); //이미지 경로 원본
                }
            }}catch (Exception e){}
    }
}























