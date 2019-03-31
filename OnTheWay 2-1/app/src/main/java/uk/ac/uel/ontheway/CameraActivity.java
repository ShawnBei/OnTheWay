package uk.ac.uel.ontheway;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.*;
import java.text.*;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class CameraActivity extends AppCompatActivity {

    File createFolder;

    ListView cameraTravelListView;

    ArrayList<String> travelNames = new ArrayList<>();
    ArrayList<String> createTimes = new ArrayList<>();
    ArrayList<String> imageNames = new ArrayList<>();
    TravelAdapter travelAdapter;

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();


    File defaultDir = Environment.getExternalStorageDirectory();

    Uri imageUri;
    ImageView cameraImageview;
    final int REQUEST_IMAGE_CAPTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        cameraTravelListView = findViewById(R.id.cameraTravelListView);
        cameraImageview = findViewById(R.id.cameraImageView);


        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            ContentValues values = new ContentValues();
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }

        travelAdapter = new TravelAdapter(this, travelNames, createTimes);

        // get travel list
        getTravelList();

        // when click the list
        cameraTravelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                getImageNames(i);
                String e = travelNames.get(i);
                createFolder = new File(defaultDir + "/OnTheWayPhotos/" + e);
                if (!createFolder.exists() || !createFolder.isDirectory())
                    createFolder.mkdir();

                uploadFile(e, i);
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                cameraImageview.setImageBitmap(mImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // get travel list
    private void getTravelList() {
        // [start] get travel list and travel time from Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = mAuth.getCurrentUser();

        mDatabase.child(user.getUid()).child("TravelNames")
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String newItem = dataSnapshot.getValue(String.class);
                        Log.d("New Item", newItem);
                        travelNames.add(newItem);
                    }


                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        mDatabase.child(user.getUid()).child("TravelTime")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String newItem = dataSnapshot.getValue(String.class);
                        createTimes.add(newItem);
                        cameraTravelListView.setAdapter(travelAdapter);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        // [end]
    }



//    // get image names
    private void getImageNames(int i){
        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(user.getUid()).child("ImageNames").child(String.valueOf(i)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded (DataSnapshot dataSnapshot, String s){
                String newItem = dataSnapshot.getValue(String.class);
                imageNames.add(newItem);
            }

            @Override
            public void onChildChanged (DataSnapshot dataSnapshot, String s){

            }

            @Override
            public void onChildRemoved (DataSnapshot dataSnapshot){

            }

            @Override
            public void onChildMoved (DataSnapshot dataSnapshot, String s){

            }

            @Override
            public void onCancelled (DatabaseError databaseError){

            }
        });
    }

    private void uploadFile(final String travelName, final int index){



            // input image name
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(CameraActivity.this);
            final View mView = getLayoutInflater().inflate(R.layout.dialog_set_image_name, null);
            final EditText mNewImageName = mView.findViewById(R.id.newImageEditText);
            Button confirmButton = mView.findViewById(R.id.newImageConfirmButton);
            Button cancelButton = mView.findViewById(R.id.newImageCancelButton);
            final DatePickerDialog.OnDateSetListener mDateSetListener;

            final FirebaseUser user = mAuth.getCurrentUser();

            mBuilder.setView(mView);
            final AlertDialog nameDialog = mBuilder.create();
            nameDialog.show();

            // when click confirm button
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!(mNewImageName.getText().toString().isEmpty())){
                        final String newName = mNewImageName.getText().toString();

                        nameDialog.hide(); // test

                        final ProgressDialog progressDialog = new ProgressDialog(CameraActivity.this); // test
                        progressDialog.setTitle("Uploading...");
                        progressDialog.show();

                        StorageReference riversRef = storageRef.child("images/"+ travelName + "/" + newName + ".jpg");

                        riversRef.putFile(imageUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressDialog.dismiss();
                                        mDatabase.child(user.getUid()).child("ImageNames").child(String.valueOf(index)).child(String.valueOf(imageNames.size())).setValue(newName);
                                        Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                                        progressDialog.setMessage(((int) progress) + "% Uploaded...");
                                    }
                                });
                        nameDialog.dismiss(); // test
                        Intent showMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(showMainActivity);
                    }
                    else{
                        Toast.makeText(CameraActivity.this, "Please fill any empty fields!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // when click cancel button
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nameDialog.dismiss();
                }
            });


        }
}

