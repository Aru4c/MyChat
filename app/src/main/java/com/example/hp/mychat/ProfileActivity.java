package com.example.hp.mychat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    private Button sendFriendRequestButton;
    private Button declineFriendRequestButton;
    private TextView ProfileName;
    private TextView ProfileStatus;
    private ImageView ProfileImage;

    private DatabaseReference usersReference;

    private DatabaseReference NotificationsReference;

    private String CURRENT_STATE;
    private DatabaseReference friendRequestRef;
    private FirebaseAuth mAuth;
    String sender_user_id;
    String receiver_user_id;
    private DatabaseReference friendsReference;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendRequestRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        NotificationsReference= FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationsReference.keepSynced(true);

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsReference.keepSynced(true);
        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();


        sendFriendRequestButton = (Button) findViewById(R.id.profile_visit_send_req_btn);
        declineFriendRequestButton = (Button) findViewById(R.id.profile_decline_req_btn);
        ProfileName = (TextView) findViewById(R.id.profile_visit_username);
        ProfileStatus = (TextView) findViewById(R.id.profile_visit_userstatus);
        ProfileImage = (ImageView) findViewById(R.id.profile_visit_user_image);

        CURRENT_STATE = "not_friends";

        usersReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                ProfileName.setText(name);
                ProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_profile).into(ProfileImage);

                friendRequestRef.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild(receiver_user_id)) {
                                        String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();
                                        if (req_type.equals("sent")) {
                                            CURRENT_STATE = "request_sent";
                                            sendFriendRequestButton.setText("Cancel Friend Request");

                                            declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                            declineFriendRequestButton.setEnabled(false);
                                        }
                                        else if (req_type.equals("received")) {
                                            CURRENT_STATE = "request_received";
                                            sendFriendRequestButton.setText("Accept Friend Request");


                                            declineFriendRequestButton.setVisibility(View.VISIBLE);
                                            declineFriendRequestButton.setEnabled(true);

                                            declineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    DeclineFriendReq();
                                                }
                                            });
                                        }
                                    }
                                }
                                else
                                {
                                    friendsReference.child(sender_user_id)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(receiver_user_id)){
                                                        CURRENT_STATE="friends";
                                                        sendFriendRequestButton.setText("Unfriend");


                                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                        declineFriendRequestButton.setEnabled(false);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        declineFriendRequestButton.setVisibility(View.INVISIBLE);
        declineFriendRequestButton.setEnabled(false);


       if(!sender_user_id.equals(receiver_user_id))
       {
           sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   sendFriendRequestButton.setEnabled(false);

                   if (CURRENT_STATE.equals("not_friends")) {

                       SendFriendReqToPerson();
                   }
                   if (CURRENT_STATE.equals("request_sent")) {
                       CancelFriendRequest();
                   }
                   if (CURRENT_STATE.equals("request_received")) {
                       AcceptFriendRequest();
                   }
                   if(CURRENT_STATE.equals("friends")){
                       UnfriendAFriend();
                   }
               }
           });
       }
        else{
           declineFriendRequestButton.setVisibility(View.INVISIBLE);
           sendFriendRequestButton.setVisibility(View.INVISIBLE);
       }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }



    private void DeclineFriendReq()
    {
        friendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequestButton.setText("Send Friend Request");

                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });

                        }
                    }
                });
    }






    private void UnfriendAFriend() {

        friendsReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        friendsReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            sendFriendRequestButton.setEnabled(true);
                                            CURRENT_STATE="not_Friends";
                                            sendFriendRequestButton.setText("Send Friend Request");

                                            declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                            declineFriendRequestButton.setEnabled(false);
                                        }
                                    }
                                });
                    }
                    }
                });
    }


    private void AcceptFriendRequest() {
        Calendar CallForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate = currentDate.format(CallForDate.getTime());


        friendsReference.child(sender_user_id).child(receiver_user_id).setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendsReference.child(receiver_user_id).child(sender_user_id).setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        friendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                sendFriendRequestButton.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                sendFriendRequestButton.setText("Unfriend");

                                                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                declineFriendRequestButton.setEnabled(false);

                                                                            }
                                                                        }
                                                                    });

                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });


    }


    private void CancelFriendRequest() {
        friendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequestButton.setText("Send Friend Request");
                                            }
                                        }
                                    });

                        }
                    }
                });
    }


    private void SendFriendReqToPerson() {
        friendRequestRef.child(sender_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> notificationsData= new HashMap<String, String>();
                                                notificationsData.put("from", sender_user_id);
                                                notificationsData.put("type", "request");

                                                NotificationsReference.child(receiver_user_id).push().setValue(notificationsData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                             if(task.isSuccessful()){
                                                                 sendFriendRequestButton.setEnabled(true);
                                                                 CURRENT_STATE = "request_sent";
                                                                 sendFriendRequestButton.setText("Cancel Friend Request");

                                                                 declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                 declineFriendRequestButton.setEnabled(false);
                                                             }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Profile Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
