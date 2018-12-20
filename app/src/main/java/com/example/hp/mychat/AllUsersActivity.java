package com.example.hp.mychat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView allUsersList;
    private DatabaseReference allDatabaseUsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mToolbar=(Toolbar)findViewById(R.id.all_users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allUsersList= (RecyclerView)findViewById(R.id.all_users_list);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this ));
        allDatabaseUsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        allDatabaseUsersRef.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>
                (
                        AllUsers.class,
                        R.layout.all_users_display_layout,
                        AllUsersViewHolder.class,
                        allDatabaseUsersRef

                        )
        {
            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position)
            {
                viewHolder.setUser_Name(model.getUser_name());
                viewHolder.setUser_Status(model.getUser_status());
                viewHolder.setUser_thumb_image(getApplicationContext(),model.getUser_thumb_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id=getRef(position).getKey();
                        Intent profileIntent= new Intent(AllUsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        allUsersList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class AllUsersViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public AllUsersViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setUser_Name(String user_name)
        {
            TextView name= (TextView) mView.findViewById(R.id.all_users_username);
            name.setText(user_name);
        }

        public void setUser_Status(String user_status){
            TextView status= (TextView) mView.findViewById(R.id.all_users_status);
            status.setText(user_status);

        }
        public void setUser_thumb_image(final Context ctx,final String user_thumb_image){
           final CircleImageView thumb_image= (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
           // Picasso.get().load(user_thumb_image).placeholder(R.drawable.default_profile).into(thumb_image);
            Picasso.get().load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e)
                        {
                            Picasso.get().load(user_thumb_image).placeholder(R.drawable.default_profile).into(thumb_image);
                        }
                    });

        }
    }
}
