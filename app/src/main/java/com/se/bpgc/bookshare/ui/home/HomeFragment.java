package com.se.bpgc.bookshare.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.se.bpgc.bookshare.BarcodeScannerActivity;
import com.se.bpgc.bookshare.BookActivity;
import com.se.bpgc.bookshare.BookMetadataModel;
import com.se.bpgc.bookshare.MainActivity;
import com.se.bpgc.bookshare.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    RecyclerView topRatedRecycler;
    FirebaseRecyclerAdapter<BookMetadataModel,HomeViewHolder> topRatedAdapter;
    RecyclerView newAdditionsRecycler;
    FirebaseRecyclerAdapter<BookMetadataModel,HomeViewHolder> newAdditionAdapter;
    ProgressBar topProg;
    ProgressBar newAdditionProgress;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        topProg = view.findViewById(R.id.top_rated_prog);
        newAdditionProgress = view.findViewById(R.id.new_additions_progress);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

        if (ContextCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            getActivity(),
                            new String[] { Manifest.permission.SEND_SMS,Manifest.permission.CAMERA },
                            3);
        }


        final FloatingActionButton scanButton = root.findViewById(R.id.scan_barcode_button_home);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent barcodeScannerIntent = new Intent(getActivity(), BarcodeScannerActivity.class);
                startActivity(barcodeScannerIntent);
            }
        });

        final FloatingActionButton addByISBN = root.findViewById(R.id.add_by_isbn_button_home);
        addByISBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("Add By ISBN");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment dialogFragment = new AddByIsbnDialog();
                dialogFragment.show(ft, "Add By ISBN");
            }
        });

        final FloatingActionButton addManually = root.findViewById(R.id.add_manually_button);
        addManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("Add Manually");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment dialogFragment = new AddManually();
                dialogFragment.show(ft, "Add Manually");
            }
        });


        // Top-Rated Recycler

        topRatedRecycler = root.findViewById(R.id.top_rated_recycler);
        LinearLayoutManager tm = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        tm.setStackFromEnd(true);
        tm.setReverseLayout(true);
        topRatedRecycler.setLayoutManager(tm);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("catalog");

        Query top_rated_query = mRef.orderByChild("averageRating").limitToLast(10);


        FirebaseRecyclerOptions<BookMetadataModel> topRatedOptions =
                new FirebaseRecyclerOptions.Builder<BookMetadataModel>()
                        .setQuery(top_rated_query, BookMetadataModel.class)
                        .build();


        topRatedAdapter = new FirebaseRecyclerAdapter<BookMetadataModel, HomeViewHolder>(topRatedOptions) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                topProg.setVisibility(View.GONE);
                topRatedRecycler.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onBindViewHolder(@NonNull HomeViewHolder holder, int position, @NonNull final BookMetadataModel model) {
                    try {
                        Picasso.get().load(model.getThumbnail()).into(holder.cover);
                    }
                    catch(Exception e){
                        holder.cover.setImageResource(R.drawable.ic_book_white_80dp);
                    }
                    holder.title.setText(model.getTitle());
                    holder.mainView.setOnClickListener(new View.OnClickListener() {
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


            }

            @NonNull
            @Override
            public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_grid, parent, false);
                return new HomeViewHolder(view);
            }
        };

        topRatedRecycler.addItemDecoration(new ListingItemDecoration(18));
        topRatedRecycler.setAdapter(topRatedAdapter);
        topRatedRecycler.setItemViewCacheSize(20);
        topRatedRecycler.setDrawingCacheEnabled(true);


        //New Additions
        newAdditionsRecycler = root.findViewById(R.id.new_addition_recycler);

        Query new_addition_query = FirebaseDatabase.getInstance().getReference("catalog").orderByChild("timestamp").limitToFirst(10);

        FirebaseRecyclerOptions<BookMetadataModel> newAdditionOptions =
                new FirebaseRecyclerOptions.Builder<BookMetadataModel>()
                        .setQuery(new_addition_query,BookMetadataModel.class)
                        .build();


        LinearLayoutManager lm = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        lm.setReverseLayout(true);
        lm.setStackFromEnd(true);
        newAdditionsRecycler.setLayoutManager(lm);

        newAdditionAdapter = new FirebaseRecyclerAdapter<BookMetadataModel, HomeViewHolder>(newAdditionOptions) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                newAdditionProgress.setVisibility(View.GONE);
                newAdditionsRecycler.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onBindViewHolder(@NonNull HomeViewHolder holder, int position, @NonNull final BookMetadataModel model) {
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
            }

            @NonNull
            @Override
            public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_grid, parent, false);
                return new HomeViewHolder(view);
            }
        };

        newAdditionsRecycler.addItemDecoration(new ListingItemDecoration(18));
        newAdditionsRecycler.setAdapter(newAdditionAdapter);
        newAdditionsRecycler.setItemViewCacheSize(20);
        newAdditionsRecycler.setDrawingCacheEnabled(true);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        topRatedAdapter.startListening();
        newAdditionAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        topRatedAdapter.stopListening();
        newAdditionAdapter.stopListening();
    }


    class HomeViewHolder extends RecyclerView.ViewHolder{
        View mainView;
        ImageView cover;
        TextView title;
        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            cover = itemView.findViewById(R.id.cover_grid);
            title = itemView.findViewById(R.id.title_grid);
        }
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
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.left = space;
            } else {
                outRect.left = 0;
            }
        }
    }

}