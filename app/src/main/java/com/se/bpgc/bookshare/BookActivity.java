package com.se.bpgc.bookshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

public class BookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        getSupportActionBar().hide();
        final FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
        Intent bookIntent = getIntent();
        final String title = bookIntent.getStringExtra("title");
        final String author = bookIntent.getStringExtra("author");
        final String description = bookIntent.getStringExtra("description");
        final String thumbnail = bookIntent.getStringExtra("thumbnail");
        final float averageRating = Float.parseFloat(bookIntent.getStringExtra("averageRating"));
        final String category = bookIntent.getStringExtra("category");
        final String isbn = bookIntent.getStringExtra("isbn");

        TextView title_view = findViewById(R.id.title_book);
        title_view.setText(title);

        TextView author_view = findViewById(R.id.author_book);
        author_view.setText(author);

        TextView description_view = findViewById(R.id.description_book);
        description_view.setText(description);

        TextView category_view = findViewById(R.id.category_book);
        category_view.setText(category);

        RatingBar rating_bar = findViewById(R.id.rating_book);
        rating_bar.setRating(averageRating);


        final View container = findViewById(R.id.option_container_book);

        ImageView thumbnail_cover = findViewById(R.id.book_cover);
        try {
            Picasso.get().load(thumbnail).into(thumbnail_cover);
        }
        catch (Exception e){
            thumbnail_cover.setImageResource(R.drawable.ic_book_white_80dp);
        }

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("catalog").child(isbn);


        mRef = mRef.child("userList").child(mAuth.getUid());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    container.setVisibility(View.VISIBLE);
                }
                else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Button buy = findViewById(R.id.buy_button);
        Button borrow = findViewById(R.id.borrow_button);



        buy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final DatabaseReference notRef = FirebaseDatabase.getInstance().getReference("notifications").push();

                final String notifID = notRef.getKey().toString();
                notRef.child("notifID").setValue(notifID);

                notRef.child("title").setValue(title);
                notRef.child("author").setValue(author);
                notRef.child("isbn").setValue(isbn);
                notRef.child("thumbnail").setValue(thumbnail);
                notRef.child("description").setValue(description);
                notRef.child("category").setValue(category);
                notRef.child("averageRating").setValue(averageRating);


                notRef.child("requestUid").setValue(mAuth.getUid());
                notRef.child("requestName").setValue(mAuth.getDisplayName());
                notRef.child("requestPhone").setValue(mAuth.getPhoneNumber() != null?mAuth.getPhoneNumber():"");
                notRef.child("requestEmail").setValue(mAuth.getEmail() != null?mAuth.getEmail():"");

                notRef.child("responseUid").setValue("");
                notRef.child("responseName").setValue("");
                notRef.child("responsePhone").setValue("");
                notRef.child("responseEmail").setValue("");

                notRef.child("isPending").setValue("true");
                notRef.child("type").setValue("buy");
                notRef.child("scheduleDelete").setValue("false");

                final String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())*-1);

                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("catalog").child(isbn).child("userList");
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            String uid = snap.child("uid").getValue().toString();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userData").child(uid).child("notifications").child(notifID);
                            ref.child("notifID").setValue(notifID);
                            ref.child("timestamp").setValue(timestamp);
                            notRef.child("userList").child(uid).child("uid").setValue(uid);
                        }
                        notRef.child("count").setValue(dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                DatabaseReference hRef = FirebaseDatabase.getInstance().getReference("userData").child(mAuth.getUid()).child("notifications").child(notifID);
                hRef.child("notifID").setValue(notifID);
                hRef.child("timestamp").setValue(timestamp);

                hRef = hRef.getParent().getParent().child("phoneNumber");
                hRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String reqPhone = dataSnapshot.getValue().toString();
                            notRef.child("requestPhone").setValue(reqPhone);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Toast.makeText(getApplicationContext(),"Check Notifications For Status",Toast.LENGTH_SHORT).show();
                finish();

            }
        });

        borrow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final DatabaseReference notRef = FirebaseDatabase.getInstance().getReference("notifications").push();

                final String notifID = notRef.getKey().toString();
                notRef.child("notifID").setValue(notifID);

                notRef.child("title").setValue(title);
                notRef.child("author").setValue(author);
                notRef.child("isbn").setValue(isbn);
                notRef.child("thumbnail").setValue(thumbnail);
                notRef.child("description").setValue(description);
                notRef.child("category").setValue(category);
                notRef.child("averageRating").setValue(averageRating);

                FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
                notRef.child("requestUid").setValue(mAuth.getUid());
                notRef.child("requestName").setValue(mAuth.getDisplayName());
                notRef.child("requestPhone").setValue(mAuth.getPhoneNumber() != null?mAuth.getPhoneNumber():"");
                notRef.child("requestEmail").setValue(mAuth.getEmail() != null?mAuth.getEmail():"");


                notRef.child("responseUid").setValue("");
                notRef.child("responsePhone").setValue("");
                notRef.child("responseName").setValue("");
                notRef.child("responseEmail").setValue("");

                notRef.child("isPending").setValue("true");
                notRef.child("type").setValue("borrow");
                notRef.child("scheduleDelete").setValue("false");

                final String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())*-1);


                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("catalog").child(isbn).child("userList");
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            String uid = snap.child("uid").getValue().toString();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userData").child(uid).child("notifications").child(notifID);
                            ref.child("notifID").setValue(notifID);
                            ref.child("timestamp").setValue(timestamp);
                            notRef.child("userList").child(uid).child("uid").setValue(uid);
                        }
                        notRef.child("count").setValue(dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                DatabaseReference hRef = FirebaseDatabase.getInstance().getReference("userData").child(mAuth.getUid()).child("notifications").child(notifID);
                hRef.child("notifID").setValue(notifID);
                hRef.child("timestamp").setValue(timestamp);

                hRef = hRef.getParent().getParent().child("phoneNumber");
                hRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String reqPhone = dataSnapshot.getValue().toString();
                            notRef.child("requestPhone").setValue(reqPhone);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                Toast.makeText(getApplicationContext(),"Check Notifications For Status",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

}
