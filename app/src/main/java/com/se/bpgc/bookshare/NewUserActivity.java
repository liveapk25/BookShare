package com.se.bpgc.bookshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        final EditText phoneEdit = findViewById(R.id.phone_number_start);
        final EditText favBook = findViewById(R.id.fav_book_start);
        final EditText favGenre = findViewById(R.id.fav_genre_start);
        Button skipB = findViewById(R.id.skip_button);
        Button continueB = findViewById(R.id.continue_button);

        skipB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toIntent = new Intent(getApplicationContext(), MainActivity.class);
                FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("userData").child(fUser.getUid());
                mDatabase.child("uid").setValue(fUser.getUid());
                mDatabase.child("count").setValue(0);
                startActivity(toIntent);
            }
        });

        continueB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phoneEdit.getText().toString().length() != 10){
                    phoneEdit.setError("Invalid Input");
                }
                else{
                    FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("userData").child(fUser.getUid());
                    mDatabase.child("uid").setValue(fUser.getUid());
                    mDatabase.child("phoneNumber").setValue(phoneEdit.getText().toString());
                    mDatabase.child("favBook").setValue(favBook.getText().toString().equals("")?"-":favBook.getText().toString());
                    mDatabase.child("favGenre").setValue(favGenre.getText().toString().equals("")?"-":favBook.getText().toString());
                    mDatabase.child("count").setValue(0);
                    Intent toIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(toIntent);
                    finish();
                }
            }
        });




    }
}
