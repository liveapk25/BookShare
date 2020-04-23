package com.se.bpgc.bookshare.ui.dashboard;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.se.bpgc.bookshare.BookActivity;
import com.se.bpgc.bookshare.BookMetadataModel;
import com.se.bpgc.bookshare.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class DashboardFragment extends Fragment {

    RecyclerView catalogRecycler;
    FirebaseRecyclerAdapter<BookMetadataModel,GridViewHolder> catalogAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView catalogText = root.findViewById(R.id.catalog_text);
        catalogText.setText("Catalog:");

        SearchView sView = root.findViewById(R.id.library_search_view);
        sView.setQueryHint("Search from Catalog");

        catalogRecycler = root.findViewById(R.id.library_recycler_view);

        Query query = FirebaseDatabase.getInstance().getReference("catalog").orderByChild("title");

        FirebaseRecyclerOptions<BookMetadataModel> options =
                new FirebaseRecyclerOptions.Builder<BookMetadataModel>()
                        .setQuery(query,BookMetadataModel.class)
                        .build();

        catalogRecycler.setLayoutManager(new GridLayoutManager(getActivity(),3));

        final View empty_container = root.findViewById(R.id.empty_dash);
        final ProgressBar progressBar = root.findViewById(R.id.progressBar);

        catalogAdapter= new FirebaseRecyclerAdapter<BookMetadataModel, GridViewHolder>(options) {

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
            protected void onBindViewHolder(@NonNull GridViewHolder holder, int position, @NonNull final BookMetadataModel model) {
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
            public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_grid, parent, false);
                return new GridViewHolder(view);
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
                Query query = FirebaseDatabase.getInstance().getReference("catalog").orderByChild("titleLowerCase").startAt(s.toLowerCase()).endAt(s.toLowerCase()+"\uf8ff");
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

    class GridViewHolder extends RecyclerView.ViewHolder{
        View mainView;
        ImageView cover;
        TextView title;
        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            cover = itemView.findViewById(R.id.cover_grid);
            title = itemView.findViewById(R.id.title_grid);
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
            outRect.bottom = space;

        }
    }

}