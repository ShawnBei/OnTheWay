package uk.ac.uel.ontheway;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Comment;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]
    File createRootFile;
    private Uri mCapturedImageURI;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    Button logOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check permissions
        if(checkPermissionsArray(Permissions.PERMISSIONS)){

        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }

        // define components
        final ImageView mapButton = findViewById(R.id.mapButton);
        final ImageView rateButton = findViewById(R.id.rateButton);
        final ImageView cameraButton = findViewById(R.id.cameraButton);
        final ImageView calendarButton = findViewById(R.id.calendarButton);
        final ImageView calculatorButton = findViewById(R.id.calculatorButton);
        final ImageView travelListButton = findViewById(R.id.travelListButton);
        final ImageView photoButton = findViewById(R.id.photoButton);
        final TextView displayName = findViewById(R.id.displayName);
        final ImageView budgetButton = findViewById(R.id.budgetButton);
        logOutBtn  = findViewById(R.id.logOutBtn);

        // create photo folder
        createRootFile = new File(Environment.getExternalStorageDirectory() + "/OnTheWayPhotos");
        if (createRootFile.exists() || createRootFile.isDirectory()){
        }else{
            createRootFile.mkdir();
            Log.d("new folder", "created");
        }

        // display user name
        displayUsername(displayName);


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermission(Permissions.LOCATION_PERMISSION[0])){
                    Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                    MainActivity.this.startActivity(mapIntent);
                }else{
                    Intent restartIntent = new Intent(MainActivity.this, MainActivity.class);
                    MainActivity.this.startActivity(restartIntent);
                }

            }
        });



        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission(Permissions.INTERNET_PERMISSION[0]))
                {
                    Intent currentIntent = new Intent(MainActivity.this, CurrencyActivity.class);
                    MainActivity.this.startActivity(currentIntent);
                }else{
                    Intent restartIntent = new Intent(MainActivity.this, MainActivity.class);
                    MainActivity.this.startActivity(restartIntent);
                }

            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission(Permissions.CAMERA_PERMISSION[0])){
//                    Intent takePictureIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
////                  if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                    ContentValues values = new ContentValues();
//                    mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                    Log.d("uri:", mCapturedImageURI.toString());

                    Intent showIntent = new Intent(MainActivity.this, CameraActivity.class);
//                    showIntent.putExtra("uk.ac.uel.PHOTO_URI", mCapturedImageURI.toString());
                    startActivity(showIntent);

                } else{
                    Intent restartIntent = new Intent(MainActivity.this, MainActivity.class);
                    MainActivity.this.startActivity(restartIntent);
                }



            }


        });


        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent calendarIntent = new Intent(MainActivity.this, CalendarActivity.class);
                MainActivity.this.startActivity(calendarIntent);

            }
        });

        calculatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent calculatorIntent = new Intent(MainActivity.this, CalculatorActivity.class);
                MainActivity.this.startActivity(calculatorIntent);

            }
        });

        travelListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission(Permissions.WRITE_PERMISSION[0])){
                    Intent travelListIntent = new Intent(MainActivity.this, TravelListActivity.class);
                    MainActivity.this.startActivity(travelListIntent);
                }else{
                    Intent restartIntent = new Intent(MainActivity.this, MainActivity.class);
                    MainActivity.this.startActivity(restartIntent);
                }

            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission(Permissions.READ_PERMISSION[0])){
                    Intent photoIntent = new Intent(MainActivity.this, PhotoActivity.class);
                    MainActivity.this.startActivity(photoIntent);
                }else{
                    Intent restartIntent = new Intent(MainActivity.this, MainActivity.class);
                    MainActivity.this.startActivity(restartIntent);
                }


            }
        });


        budgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent budgetIntent = new Intent(MainActivity.this, BudgetActivity.class);
                MainActivity.this.startActivity(budgetIntent);
            }
        });

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showLog = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(showLog);
            }
        });

    }

    // sign out current user
    private void signOut() {
        mAuth.signOut();
    }

    // display user name
    private void displayUsername(TextView displayedName){

        final TextView displayName = displayedName;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child(user.getUid()).child("UserInfo").child("UserName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // Get user name
                        String storedName = dataSnapshot.getValue().toString();
                        displayName.setText("Welcome " + storedName + " !");
                        logOutBtn.setText("Not " + storedName + "? Log Out");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public boolean checkPermissionsArray(String[] permissions){
        for(int i=0; i<permissions.length; i++){
            String check = permissions[i];
            if(!checkPermission(check)){
                return false;
            }
        }
        return true;
    }

    public boolean checkPermission(String permission){
        int permissionRequest = ActivityCompat.checkSelfPermission(MainActivity.this, permission);
        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            return false;
        }else{
            return true;
        }
    }

    public void verifyPermissions(String[] permissions){
        ActivityCompat.requestPermissions(
                MainActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST

        );
    }



}

