package com.se.bpgc.bookshare.ui.home;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.se.bpgc.bookshare.BookMetadataModel;
import com.se.bpgc.bookshare.R;

import java.util.concurrent.TimeUnit;

public class AddManually extends DialogFragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.add_manually, container, false);

        final EditText title = root.findViewById(R.id.title_edit_man);
        final EditText author = root.findViewById(R.id.author_edit_man);
        final EditText category  = root.findViewById(R.id.category_edit_man);
        final Button add = root.findViewById(R.id.add_man);
        final Button cancel = root.findViewById(R.id.cancel_man);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(title.getText().toString().isEmpty()){
                   title.setError("Field cannot be left empty");
                }
                else{
                    String isbn_val;
                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("catalog").push();
                    isbn_val = mRef.getKey();

                    String title_t = title.getText().toString();
                    String titleLowerCase_t = title_t.toLowerCase();
                    String author_t = author.getText().toString();
                    String category_t = category.getText().toString();

                    FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

                    String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())*-1);
                    mRef.child("title").setValue(title_t);
                    mRef.child("titleLowerCase").setValue(titleLowerCase_t);
                    mRef.child("author").setValue(author_t);
                    mRef.child("category").setValue(category_t);
                    mRef.child("description").setValue("");
                    mRef.child("thumbnail").setValue("");
                    mRef.child("averageRating").setValue(0);
                    mRef.child("isbn").setValue(isbn_val);
                    mRef.child("timestamp").setValue(timestamp);
                    mRef.child("userList").child(mAuth.getUid()).child("uid").setValue(mAuth.getUid());
                    mRef.child("count").setValue(1);

                    mRef = FirebaseDatabase.getInstance().getReference("userData").child(mAuth.getUid()).child("bookList").child(isbn_val);
                    mRef.child("title").setValue(title_t);
                    mRef.child("titleLowerCase").setValue(titleLowerCase_t);
                    mRef.child("author").setValue(author_t);
                    mRef.child("category").setValue(category_t);
                    mRef.child("description").setValue("");
                    mRef.child("thumbnail").setValue("");
                    mRef.child("averageRating").setValue(0);
                    mRef.child("isbn").setValue(isbn_val);
                    mRef.child("timestamp").setValue(timestamp);
                    mRef = mRef.getParent().getParent().child("count");

                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long val = (long)dataSnapshot.getValue();
                            dataSnapshot.getRef().setValue(val+1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    mRef = mRef.getParent().getParent().child("totalCount");

                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long val = (long) dataSnapshot.getValue();
                            dataSnapshot.getRef().setValue(val+1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    dismiss();
                }
            }
        });




        return root;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // safety check
        if (getDialog() == null)
            return;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;


        getDialog().getWindow().setLayout((9*width)/10, ViewGroup.LayoutParams.WRAP_CONTENT);

        // ... other stuff you want to do in your onStart() method
    }

}
