package com.se.bpgc.bookshare.ui.home;



import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.se.bpgc.bookshare.MetadataParser;
import com.se.bpgc.bookshare.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


public class AddByIsbnDialog extends DialogFragment {

    EditText searchField;
    Button addSearchButton;
    Button cancelButton;
    ImageView cover;
    TextView title;
    TextView author;
    View metadataContainer;
    TextView error;
    View infoContainer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_by_isbn_dialog, container, false);

        // Do all the stuff to initialize your custom view
        searchField = v.findViewById(R.id.isbn_edit);
        addSearchButton = v.findViewById(R.id.add_button_isbn);
        cancelButton = v.findViewById(R.id.cancel_button_isbn);
        cover = v.findViewById(R.id.isbn_cover);
        title = v.findViewById(R.id.isbn_title);
        author = v.findViewById(R.id.isbn_author);
        metadataContainer = v.findViewById(R.id.isbn_metadata_container);
        error = v.findViewById(R.id.invalid_input_text);
        infoContainer = v.findViewById(R.id.isbn_info_card_container);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        addSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                error.setVisibility(View.GONE);
                String isbn_str = searchField.getText().toString().replaceAll("[^a-zA-Z0-9]","");
                GoogleApiRequest apiRequest = new GoogleApiRequest();
                apiRequest.execute(isbn_str);
            }

        });

        return v;
    }

    class GoogleApiRequest extends AsyncTask<String, Object, JSONObject> {
        String isbn_val;
        @Override
        protected void onPreExecute() {
            // Check network connection.
            if(isNetworkConnected() == false){
                // Cancel request.
                Log.i(getClass().getName(), "Not connected to the internet");
                cancel(true);
                return;
            }
        }
        @Override
        protected JSONObject doInBackground(String... isbns) {
            // Stop if cancelled
            if(isCancelled()){
                return null;
            }

            String apiUrlString = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbns[0];
            isbn_val = isbns[0];
            try{
                HttpURLConnection connection = null;
                // Build Connection.
                try{
                    URL url = new URL(apiUrlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(5000); // 5 seconds
                    connection.setConnectTimeout(5000); // 5 seconds
                } catch (MalformedURLException e) {
                    // Impossible: The only two URLs used in the app are taken from string resources.
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    // Impossible: "GET" is a perfectly valid request method.
                    e.printStackTrace();
                }
                int responseCode = connection.getResponseCode();
                if(responseCode != 200){
                    Log.w(getClass().getName(), "GoogleBooksAPI request failed. Response Code: " + responseCode);
                    connection.disconnect();
                    return null;
                }

                // Read data from response.
                StringBuilder builder = new StringBuilder();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = responseReader.readLine();
                while (line != null){
                    builder.append(line);
                    line = responseReader.readLine();
                }
                String responseString = builder.toString();
                Log.d(getClass().getName(), "Response String: " + responseString);
                JSONObject responseJson = new JSONObject(responseString);
                // Close connection and return response code.
                connection.disconnect();
                return responseJson;
            } catch (SocketTimeoutException e) {
                Log.w(getClass().getName(), "Connection timed out. Returning null");
                return null;
            } catch(IOException e){
                Log.d(getClass().getName(), "IOException when connecting to Google Books API.");
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                Log.d(getClass().getName(), "JSONException when connecting to Google Books API.");
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(JSONObject responseJson) {
            if(isCancelled()){
                // Request was cancelled due to no network connection.
                // showNetworkDialog();

            } else if(responseJson == null){
                //showSimpleDialog(getResources().getString(R.string.dialog_null_response));
            }
            else{
                // All went well. Do something with your new JSONObject.

                final JSONObject jsn = responseJson;
                final MetadataParser mData = new MetadataParser(responseJson);
                if(!mData.isValid()){
                    error.setText(R.string.dialog_error);
                    error.setVisibility(View.VISIBLE);
                    infoContainer.setVisibility(View.VISIBLE);
                }
                else{
                    try {
                        Picasso.get().load(mData.getImageLink()).fit().into(cover);
                    }
                    catch (Exception e){
                        cover.setImageResource(R.drawable.ic_book_white_80dp);
                    }
                    title.setText(mData.getTitle());
                    author.setText(mData.getAuthor());
                    metadataContainer.setVisibility(View.VISIBLE);
                    infoContainer.setVisibility(View.VISIBLE);
                    addSearchButton.setText("ADD");
                    addSearchButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

                            String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())*-1);
                            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("catalog").child(isbn_val);
                            mRef.child("title").setValue(mData.getTitle());
                            mRef.child("titleLowerCase").setValue(mData.getTitle().toLowerCase());
                            mRef.child("author").setValue(mData.getAuthor());
                            mRef.child("category").setValue(mData.getCategory());
                            mRef.child("description").setValue(mData.getDescription());
                            mRef.child("thumbnail").setValue(mData.getImageLink());
                            mRef.child("averageRating").setValue(mData.getRating());
                            mRef.child("isbn").setValue(isbn_val);
                            mRef.child("timestamp").setValue(timestamp);
                            mRef.child("userList").child(mAuth.getUid()).child("uid").setValue(mAuth.getUid());
                            mRef.child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists()){
                                        dataSnapshot.getRef().setValue(1);
                                    }
                                    else{
                                        dataSnapshot.getRef().setValue((long)dataSnapshot.getValue()+1);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            mRef = FirebaseDatabase.getInstance().getReference("userData").child(mAuth.getUid()).child("bookList").child(isbn_val);
                            mRef.child("title").setValue(mData.getTitle());
                            mRef.child("titleLowerCase").setValue(mData.getTitle().toLowerCase());
                            mRef.child("author").setValue(mData.getAuthor());
                            mRef.child("category").setValue(mData.getCategory());
                            mRef.child("description").setValue(mData.getDescription());
                            mRef.child("thumbnail").setValue(mData.getImageLink());
                            mRef.child("averageRating").setValue(mData.getRating());
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
                    });

                }









            }
        }

        protected boolean isNetworkConnected(){

            // Instantiate mConnectivityManager if necessary
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            // Is device connected to the Internet?
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                return true;
            } else {
                return false;
            }
        }

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
