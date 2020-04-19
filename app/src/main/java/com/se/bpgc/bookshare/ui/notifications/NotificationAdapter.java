package com.se.bpgc.bookshare.ui.notifications;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.se.bpgc.bookshare.BookActivity;
import com.se.bpgc.bookshare.R;
import com.se.bpgc.bookshare.ui.my_library.MyLibraryFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {
    ArrayList<NotificationModel> set;
    Context context;

    public NotificationAdapter(ArrayList<NotificationModel> set,Context context){
        this.set = set;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder holder, int position) {
        final NotificationModel notif = set.get(position);
        holder.title.setText(notif.getTitle());

        holder.detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationModel model = notif;
                Intent bookIntent = new Intent(context, BookActivity.class);
                bookIntent.putExtra("title",model.getTitle());
                bookIntent.putExtra("author",model.getAuthor());
                bookIntent.putExtra("thumbnail",model.getThumbnail());
                bookIntent.putExtra("category",model.getCategory());
                bookIntent.putExtra("isbn",model.getIsbn());
                bookIntent.putExtra("description",model.getDescription());
                bookIntent.putExtra("averageRating",Double.toString(model.getAverageRating()));
                context.startActivity(bookIntent);
            }
        });

        holder.author.setText(notif.getAuthor());
        try {
            Picasso.get().load(notif.getThumbnail()).fit().into(holder.cover);
            Log.e("Picasso",notif.getThumbnail());
        }
        catch(Exception e){
            holder.cover.setImageResource(R.drawable.ic_book_white_80dp);
        }

        if(notif.getRequestUid().equals(notif.getUserUid())){
            if(notif.getIsPending().equals("true") && notif.getCount() == 0){
                holder.requestText.setText("No Provider Found:");
                holder.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        Toast.makeText(context,"The request has been cancelled successfully",Toast.LENGTH_SHORT);

                        FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).removeValue();
                        FirebaseDatabase.getInstance().getReference("userData").child(notif.getUserUid()).child("notifications").child(notif.getNotifID()).removeValue();
                    }
                });
                holder.cancel.setVisibility(View.VISIBLE);
                holder.infoContainer.setVisibility(View.VISIBLE);

            }
            else if(notif.getIsPending().equals("true")){

                holder.requestText.setText("Looking For Provider:");

                holder.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        Toast.makeText(context,"The request has been cancelled successfully",Toast.LENGTH_SHORT).show();
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).child("userList");
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userData");
                                for(DataSnapshot snap : dataSnapshot.getChildren()){
                                    String remUid = snap.child("uid").getValue().toString();
                                    ref.child(remUid).child("notifications").child(notif.getNotifID()).removeValue();
                                }
                                dataSnapshot.getRef().removeValue();
                                ref.child(notif.getUserUid()).child("notifications").child(notif.getNotifID()).removeValue();
                                FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                });
                holder.cancel.setVisibility(View.VISIBLE);
                holder.infoContainer.setVisibility(View.VISIBLE);
            }
            else{

                holder.requestText.setText(notif.getResponseName()+" has accepted your request for:");

                if(!notif.getResponsePhone().isEmpty()){

                    holder.phone.setOnClickListener(new PhoneOnClickListener(notif.getResponsePhone()));
                    holder.sms.setOnClickListener(new SmsOnClickListener(notif.getResponsePhone()));
                    holder.phone.setVisibility(View.VISIBLE);
                    holder.sms.setVisibility(View.VISIBLE);

                }

                if(!notif.getResponseEmail().isEmpty()){
                    holder.email.setOnClickListener(new EmailOnClickListener(notif.getResponseEmail()));
                    holder.email.setVisibility(View.VISIBLE);
                }

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        FirebaseDatabase.getInstance().getReference("userData").child(notif.getUserUid()).child("notifications").child(notif.getNotifID()).removeValue();
                        if(notif.getScheduleDelete() == "true"){
                            FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).removeValue();
                        }
                        else{
                            FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).child("scheduleDelete").setValue("true");

                        }

                    }
                });

                holder.delete.setVisibility(View.VISIBLE);

                holder.contactContainer.setVisibility(View.VISIBLE);

            }
        }
        else if(notif.getResponseUid().equals(notif.getUserUid())){
            holder.requestText.setText("You have accepted request of "+notif.getRequestName()+" for:");

            if(!notif.getRequestPhone().isEmpty()){
                holder.phone.setOnClickListener(new PhoneOnClickListener(notif.getRequestPhone()));
                holder.sms.setOnClickListener(new SmsOnClickListener(notif.getRequestPhone()));
                holder.phone.setVisibility(View.VISIBLE);
                holder.sms.setVisibility(View.VISIBLE);
            }

            if(!notif.getRequestEmail().isEmpty()){
                holder.email.setOnClickListener(new EmailOnClickListener(notif.getRequestEmail()));
                holder.email.setVisibility(View.VISIBLE);
            }

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    set.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    FirebaseDatabase.getInstance().getReference("userData").child(notif.getUserUid()).child("notifications").child(notif.getNotifID()).removeValue();

                    if(notif.getScheduleDelete() == "true"){
                        FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).removeValue();
                    }
                    else{
                        FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).child("scheduleDelete").setValue("true");

                    }
                }
            });

            holder.delete.setVisibility(View.VISIBLE);

            holder.contactContainer.setVisibility(View.VISIBLE);

        }
        else{
            holder.requestText.setText(notif.getRequestName()+" is interested in "+notif.getType()+"ing:");

            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    holder.acceptDeclineContainer.setVisibility(View.GONE);

                    FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

                    final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID());
                    mRef.child("responseUid").setValue(mAuth.getUid());
                    mRef.child("responseName").setValue(mAuth.getDisplayName());
                    mRef.child("responsePhone").setValue(mAuth.getPhoneNumber()!= null?mAuth.getPhoneNumber():"");
                    mRef.child("responseEmail").setValue(mAuth.getEmail()!=null ?mAuth.getEmail():"");
                    mRef.child("isPending").setValue("false");

                    DatabaseReference gRef = FirebaseDatabase.getInstance().getReference("userData").child(notif.getUserUid()).child("phoneNumber");

                    gRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                String phoneN = dataSnapshot.getValue().toString();
                                mRef.child("responsePhone").setValue(phoneN);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    notif.setResponseUid(mAuth.getUid());
                    notif.setResponseName(mAuth.getDisplayName());
                    notif.setResponseEmail(mAuth.getEmail());
                    notif.setResponsePhone(mAuth.getPhoneNumber());
                    notif.setIsPending("false");

                    holder.requestText.setText("You have accepted request of "+notif.getRequestName()+" for:");

                    if(!notif.getRequestPhone().isEmpty()){
                        holder.phone.setOnClickListener(new PhoneOnClickListener(notif.getRequestPhone()));
                        holder.sms.setOnClickListener(new SmsOnClickListener(notif.getRequestPhone()));
                        holder.phone.setVisibility(View.VISIBLE);
                        holder.sms.setVisibility(View.VISIBLE);
                    }

                    if(!notif.getRequestEmail().isEmpty()){
                        holder.email.setOnClickListener(new EmailOnClickListener(notif.getRequestEmail()));
                        holder.email.setVisibility(View.VISIBLE);
                    }

                    holder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            set.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                            FirebaseDatabase.getInstance().getReference("userData").child(notif.getUserUid()).child("notifications").child(notif.getNotifID()).removeValue();

                            if(notif.getScheduleDelete() == "true"){
                                FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).removeValue();
                            }
                            else{
                                FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).child("scheduleDelete").setValue("true");

                            }

                        }
                    });

                    holder.delete.setVisibility(View.VISIBLE);

                    holder.contactContainer.setVisibility(View.VISIBLE);

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID()).child("userList");

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot snap : dataSnapshot.getChildren()){
                                String remUid = snap.child("uid").getValue().toString();
                                if(remUid.equals(notif.getRequestUid()) || remUid.equals(notif.getResponseUid())){
                                    continue;
                                }
                                FirebaseDatabase.getInstance().getReference("userData").child(remUid).child("notifications").child(notif.getNotifID()).removeValue();
                            }
                            dataSnapshot.getRef().removeValue();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            });

            holder.decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    set.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    FirebaseDatabase.getInstance().getReference("userData").child(notif.getUserUid()).child("notifications").child(notif.getNotifID()).removeValue();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications").child(notif.getNotifID());
                    ref.child("count").setValue(notif.getCount()-1);
                    ref.child("userList").child(notif.getUserUid()).removeValue();

                }
            });
            holder.acceptDeclineContainer.setVisibility(View.VISIBLE);
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
            context.startActivity(intent);
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
            context.startActivity(it);
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
            context.startActivity(Intent.createChooser(emailIntent,"Emails"));
        }
    }


    @Override
    public int getItemCount() {
        return set.size();
    }
}
