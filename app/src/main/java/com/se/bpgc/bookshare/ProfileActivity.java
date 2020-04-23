package com.se.bpgc.bookshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avatarfirst.avatargenlib.AvatarConstants;
import com.avatarfirst.avatargenlib.AvatarGenerator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.se.bpgc.bookshare.ui.home.HomeFragment;
import com.se.bpgc.bookshare.ui.my_library.MyLibraryFragment;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    FirebaseRecyclerAdapter<BookMetadataModel,ProfileViewHolder> myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        RecyclerView myLibrary = findViewById(R.id.profile_recycler);
        final FloatingActionButton phoneB = findViewById(R.id.call_profile);
        final FloatingActionButton smsB = findViewById(R.id.sms_profile);
        final FloatingActionButton emailB = findViewById(R.id.email_profile);
        final ImageButton whatsappB = findViewById(R.id.whatsapp_profile);
        final TextView displayName = findViewById(R.id.display_name_profile);

        Intent intent = getIntent();

        String uid = intent.getStringExtra("uid");
        String phone = intent.getStringExtra("phone");
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");


        displayName.setText(name);


        if(!phone.isEmpty()){
            phoneB.setOnClickListener(new PhoneOnClickListener(phone));
            smsB.setOnClickListener(new SmsOnClickListener(phone));
            whatsappB.setOnClickListener(new WhatsappOnClickListener(phone));

            phoneB.setVisibility(View.VISIBLE);
            smsB.setVisibility(View.VISIBLE);
            whatsappB.setVisibility(View.VISIBLE);

        }

        if(!email.isEmpty()){
            emailB.setOnClickListener(new EmailOnClickListener(email));
            emailB.setVisibility(View.VISIBLE);
        }

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("userData").child(uid);


        LinearLayoutManager tm = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        tm.setStackFromEnd(true);
        tm.setReverseLayout(true);
        myLibrary.setLayoutManager(tm);

        Query query = mRef.child("bookList").orderByChild("title");

        FirebaseRecyclerOptions<BookMetadataModel> options =
                new FirebaseRecyclerOptions.Builder<BookMetadataModel>()
                        .setQuery(query,BookMetadataModel.class)
                        .build();

        myAdapter = new FirebaseRecyclerAdapter<BookMetadataModel, ProfileViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProfileViewHolder holder, int position, @NonNull final BookMetadataModel model) {
                try {
                    Picasso.get().load(model.getThumbnail()).into(holder.cover);
                }
                catch (Exception e){
                    holder.cover.setImageResource(R.drawable.ic_book_white_80dp);
                }
                holder.title.setText(model.getTitle());
                holder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent bookIntent = new Intent(getApplicationContext(), BookActivity.class);
                        bookIntent.putExtra("title", model.getTitle());
                        bookIntent.putExtra("author", model.getAuthor());
                        bookIntent.putExtra("thumbnail", model.getThumbnail());
                        bookIntent.putExtra("category", model.getCategory());
                        bookIntent.putExtra("isbn", model.getIsbn());
                        bookIntent.putExtra("description", model.getDescription());
                        bookIntent.putExtra("averageRating", Double.toString(model.getAverageRating()));
                        startActivity(bookIntent);
                    }
                });
            }

            @NonNull
            @Override
            public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_grid, parent, false);
                return new ProfileViewHolder(view);
            }
        };

        myLibrary.addItemDecoration(new ProfileItemDecoration(18));
        myLibrary.setAdapter(myAdapter);
        myLibrary.setItemViewCacheSize(20);
        myLibrary.setDrawingCacheEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        myAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myAdapter.stopListening();
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder{
        View mainView;
        ImageView cover;
        TextView title;
        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            cover = itemView.findViewById(R.id.cover_grid);
            title = itemView.findViewById(R.id.title_grid);
        }
    }

    public class ProfileItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public ProfileItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.left = space;
            } else {
                outRect.left = 0;
            }
        }
    }

    public class PhoneOnClickListener implements View.OnClickListener {
        String phone;
        public PhoneOnClickListener(String phone) {
            this.phone = phone;
        }

        @Override
        public void onClick(View view) {
            String uri = "tel:" + phone ;
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        }
    }

    public class SmsOnClickListener implements View.OnClickListener{
        String phone;
        public SmsOnClickListener(String phone) {
            this.phone = phone;
        }

        @Override
        public void onClick(View view) {
            Uri uri = Uri.parse("smsto:"+phone);
            Intent it = new Intent(Intent.ACTION_SENDTO, uri);
            startActivity(it);
        }
    }

    public class WhatsappOnClickListener implements View.OnClickListener{
        String phone;
        public WhatsappOnClickListener(String phone) {
            this.phone = phone;
        }

        @Override
        public void onClick(View view) {

            try {
                Uri uri = Uri.parse("whatsapp://send?phone=+91" +phone);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"WhatsApp is not installed on this device",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
    }

    public class EmailOnClickListener implements View.OnClickListener{
        String email;


        public EmailOnClickListener(String email) {
            this.email = email;
        }

        @Override
        public void onClick(View view) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto",email, null));
            startActivity(Intent.createChooser(emailIntent,"Emails"));
        }
    }

}
