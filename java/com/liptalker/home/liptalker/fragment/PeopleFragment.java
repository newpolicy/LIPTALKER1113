package com.liptalker.home.liptalker.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.ViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.liptalker.home.liptalker.LogoActivity;
import com.liptalker.home.liptalker.MainActivity;
import com.liptalker.home.liptalker.R;
import com.liptalker.home.liptalker.friendClickMessage.FriendImageClickMessage;
import com.liptalker.home.liptalker.model.UserModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {

    private String uid;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());

            /*String filename = "LIPTALKER";
            try {
                BufferedReader br = new BufferedReader(new FileReader( + filename + ".txt"));
                String readStr = "";
                String str = null;
                while ((str = br.readLine()) != null)
                    readStr = readStr + str;
                br.close();
                uid = readStr;
                Log.i("uidd",uid);
            }catch(FileNotFoundException f){
            }catch (IOException e){}*/

        return view;
    }
    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<UserModel> userModels;
        public PeopleFragmentRecyclerViewAdapter(){
            userModels = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModels.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        userModels.add(snapshot.getValue(UserModel.class));
                    }
                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,
                    parent,false);

            return new CustomViewHolder(view);
        }
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder,final int position) {
            //이미지 넣어주는 곳
            ((CustomViewHolder) holder).textView.setText(userModels.get(position).username);
            String url = userModels.get(position).profileImageUrl;
            try {
                Log.i("wow", url);
                    Glide.with(holder.itemView.getContext())
                            .load(url).apply(new RequestOptions().circleCrop())
                            .into(((CustomViewHolder) holder).imageView);

            }catch (Exception e){
                Log.i("error", e.getMessage());
            }

            ((CustomViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), FriendImageClickMessage.class);
                    startActivity(intent);
                }
            });


            /*try {
                String uid = user.getUid();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference islandRef = storageRef.child("userImages/" + uid);

                File localFile = File.createTempFile("images", "jpg");
                islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Glide.with
                                (holder.itemView.getContext())
                                .load(userModels.get(position).profileImageUrl)
                                .apply(new RequestOptions().circleCrop())
                                .into(((CustomViewHolder) holder).imageView);

                        ((CustomViewHolder)holder).textView.setText(userModels.get(position).username);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }catch (Exception e){
                Log.i("에러", e.getMessage());
            }*/
        }
        @Override
        public int getItemCount() {
            return userModels.size();
        }
        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;

            public CustomViewHolder(View view) {
                super(view);
                imageView = (ImageView)view.findViewById(R.id.frienditem_imageview);
                textView = (TextView)view.findViewById(R.id.frienditem_textview);
            }
        }
    }
}
