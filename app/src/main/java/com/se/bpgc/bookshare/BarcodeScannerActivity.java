package com.se.bpgc.bookshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

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

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    String isbn_val;
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        GoogleApiRequest apiRequest = new GoogleApiRequest();
        isbn_val = rawResult.toString();
        apiRequest.execute(rawResult.toString());
    }

    class GoogleApiRequest extends AsyncTask<String, Object, JSONObject> {

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

                MetadataParser mData = new MetadataParser(responseJson);

                if(!mData.getTitle().isEmpty()) {


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

                    finish();

                    Toast toast = Toast.makeText(getApplicationContext(),"Book Added Successfully",Toast.LENGTH_LONG);
                    toast.show();

                }
                else{
                    finish();
                    Toast.makeText(getApplicationContext(),"Error occurred While Adding The Book",Toast.LENGTH_SHORT).show();
                }



            }
        }

        protected boolean isNetworkConnected(){

            // Instantiate mConnectivityManager if necessary
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            // Is device connected to the Internet?
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                return true;
            } else {
                return false;
            }
        }



    }

}
