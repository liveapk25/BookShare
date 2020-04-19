package com.se.bpgc.bookshare.ui.my_library;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.se.bpgc.bookshare.BookActivity;
import com.se.bpgc.bookshare.BookMetadataModel;
import com.se.bpgc.bookshare.R;
import com.squareup.picasso.Picasso;


public class MyLibraryFragment extends Fragment {

    RecyclerView catalogRecycler;
    FirebaseRecyclerAdapter<BookMetadataModel, CardViewHolder> catalogAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        catalogRecycler = root.findViewById(R.id.library_recycler_view);

        final FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("userData").child(mAuth.getUid()).child("bookList").orderByChild("title");

        FirebaseRecyclerOptions<BookMetadataModel> options =
                new FirebaseRecyclerOptions.Builder<BookMetadataModel>()
                        .setQuery(query,BookMetadataModel.class)
                        .build();

        catalogRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        final View empty_container = root.findViewById(R.id.empty_dash);

        final ProgressBar progressBar = root.findViewById(R.id.progressBar);

        catalogAdapter= new FirebaseRecyclerAdapter<BookMetadataModel, CardViewHolder>(options) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                int count = getItemCount();
                progressBar.setVisibility(View.GONE);
                if(count == 0){
                    catalogRecycler.setVisibility(View.GONE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                           if(catalogRecycler.getVisibility() == View.GONE){
                               empty_container.setVisibility(View.VISIBLE);
                           }
                        }
                    }, 2000);


                }
                else{
                    catalogRecycler.setVisibility(View.VISIBLE);
                    empty_container.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull final CardViewHolder holder, final int position, @NonNull final BookMetadataModel model) {
                try {
                    Picasso.get().load(model.getThumbnail()).into(holder.cover);
                }
                catch(Exception e){
                    holder.cover.setImageResource(R.drawable.ic_book_white_80dp);
                }
                holder.title.setText(model.getTitle());
                holder.author.setText(model.getAuthor());
                holder.category.setText(model.getCategory());
                holder.rating.setRating((float)model.getAverageRating());
                holder.detailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent bookIntent = new Intent(getActivity(), BookActivity.class);
                        bookIntent.putExtra("title",model.getTitle());
                        bookIntent.putExtra("author",model.getAuthor());
                        bookIntent.putExtra("thumbnail",model.getThumbnail());
                        bookIntent.putExtra("category",model.getCategory());
                        bookIntent.putExtra("isbn",model.getIsbn());
                        bookIntent.putExtra("description",model.getDescription());
                        bookIntent.putExtra("averageRating",Double.toString(model.getAverageRating()));
                        startActivity(bookIntent);
                    }
                });
                holder.removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getRef(holder.getAdapterPosition()).removeValue();
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position,catalogAdapter.getItemCount());
                        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("catalog").child(model.getIsbn());
                        mRef.child("userList").child(mAuth.getUid()).removeValue();

                        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                long val = (long)dataSnapshot.child("count").getValue();
                                if(val == 1){
                                    dataSnapshot.getRef().removeValue();
                                }
                                else{
                                    dataSnapshot.child("count").getRef().setValue(val-1);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        DatabaseReference uRef = FirebaseDatabase.getInstance().getReference("userData");

                        uRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                long val = (long) dataSnapshot.child("totalCount").getValue();
                                dataSnapshot.child("totalCount").getRef().setValue(val-1);

                                val = (long) dataSnapshot.child(mAuth.getUid()).child("count").getValue();
                                dataSnapshot.child(mAuth.getUid()).child("count").getRef().setValue(val-1);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                });
            }

            @NonNull
            @Override
            public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_card, parent, false);
                return new CardViewHolder(view);
            }
        };



        catalogRecycler.addItemDecoration(new ListingItemDecoration(18));
        catalogRecycler.setAdapter(catalogAdapter);
        catalogRecycler.setItemViewCacheSize(20);
        catalogRecycler.setDrawingCacheEnabled(true);

        SearchView searchView = root.findViewById(R.id.library_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Query query = FirebaseDatabase.getInstance().getReference("user").child(mUid).orderByChild("titleLowerCase").startAt(s.toLowerCase()).endAt(s.toLowerCase()+"\uf8ff");
                Log.e("TITLE",s);
                FirebaseRecyclerOptions<BookMetadataModel> options =
                        new FirebaseRecyclerOptions.Builder<BookMetadataModel>()
                                .setQuery(query,BookMetadataModel.class)
                                .build();
                catalogAdapter.updateOptions(options);
                catalogAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return root;
    }

    class CardViewHolder extends RecyclerView.ViewHolder{
        View mainView;
        ImageView cover;
        TextView title;
        TextView author;
        TextView category;
        RatingBar rating;
        Button detailButton;
        Button removeButton;
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            cover = itemView.findViewById(R.id.card_cover);
            title = itemView.findViewById(R.id.title_card);
            author = itemView.findViewById(R.id.author_card);
            category = itemView.findViewById(R.id.category_card);
            rating = itemView.findViewById(R.id.rating_card);
            detailButton = itemView.findViewById(R.id.detail_button_card);
            removeButton = itemView.findViewById(R.id.remove_button_card);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        catalogAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        catalogAdapter.stopListening();
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
