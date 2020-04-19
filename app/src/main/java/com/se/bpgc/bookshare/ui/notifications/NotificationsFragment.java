package com.se.bpgc.bookshare.ui.notifications;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.se.bpgc.bookshare.R;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private RecyclerView notificationRecycler;
    private RecyclerView.Adapter<NotificationViewHolder> notificationAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationRecycler = root.findViewById(R.id.notif_recycler);

        final FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = mAuth.getUid();
        Query query = FirebaseDatabase.getInstance().getReference("userData").child(uid).child("notifications").orderByChild("timestamp");
        final ArrayList<NotificationModel> notificationSet = new ArrayList<>();

        notificationRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationRecycler.addItemDecoration(new ListingItemDecoration(10));
        notificationAdapter = new NotificationAdapter(notificationSet,getActivity());
        notificationRecycler.setAdapter(notificationAdapter);
        final ProgressBar pBar= root.findViewById(R.id.progressBar);

        final View emptyContainer = root.findViewById(R.id.empty_container_notif);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotOut) {
                if(!dataSnapshotOut.exists()){
                    emptyContainer.setVisibility(View.VISIBLE);
                }
                else {
                    pBar.setVisibility(View.GONE);
                    notificationRecycler.setVisibility(View.VISIBLE);
                    for (DataSnapshot snap : dataSnapshotOut.getChildren()) {
                        final DataSnapshot notifRef = snap;
                        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("notifications").child(snap.child("notifID").getValue().toString());
                        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshotIn) {

                                String notifID = dataSnapshotIn.child("notifID").getValue().toString();

                                String isbn = dataSnapshotIn.child("isbn").getValue().toString();
                                String title = dataSnapshotIn.child("title").getValue().toString();
                                String author = dataSnapshotIn.child("author").getValue().toString();
                                String description = dataSnapshotIn.child("description").toString();
                                String thumbnail = dataSnapshotIn.child("thumbnail").getValue().toString();
                                String category = dataSnapshotIn.child("category").getValue().toString();
                                double averageRating = (double) Double.parseDouble(dataSnapshotIn.child("averageRating").getValue().toString());

                                String requestName = dataSnapshotIn.child("requestName").getValue().toString();
                                String requestPhone = dataSnapshotIn.child("requestPhone").getValue().toString();
                                String requestEmail = dataSnapshotIn.child("requestEmail").getValue().toString();
                                String requestUid = dataSnapshotIn.child("requestUid").getValue().toString();

                                String responseName = dataSnapshotIn.child("responseName").getValue().toString();
                                String responsePhone = dataSnapshotIn.child("responsePhone").getValue().toString();
                                String responseEmail = dataSnapshotIn.child("responseEmail").getValue().toString();
                                String responseUid = dataSnapshotIn.child("responseUid").getValue().toString();

                                String isPending = dataSnapshotIn.child("isPending").getValue().toString();
                                String type = dataSnapshotIn.child("type").getValue().toString();
                                String scheduleDelete = dataSnapshotIn.child("scheduleDelete").getValue().toString();
                                long count = (long) dataSnapshotIn.child("count").getValue();

                                NotificationModel model =
                                        new NotificationModel(notifID,
                                                isbn, title, author, thumbnail, category, averageRating, description
                                                , requestUid, requestName, requestPhone, requestEmail
                                                , responseUid, responseName, responsePhone, responseEmail
                                                , isPending, type, scheduleDelete, count, mAuth.getUid());


                                notificationSet.add(model);
                                notificationAdapter.notifyDataSetChanged();
                            }

                            ;

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        return root;
    }


    public class ListingItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public ListingItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
        }
    }

}