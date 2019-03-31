package uk.ac.uel.ontheway;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class TravelListActivity extends AppCompatActivity {

    File createFolder;

    ListView travelListView;
    Button addNewTravelButton;
    Button backToMainButton;

    ArrayList<String> travelNames = new ArrayList<>();
    ArrayList<String> createTimes = new ArrayList<>();
    ArrayList<String> diaryNames = new ArrayList<>();
    ArrayList<String> imageNames = new ArrayList<>();
    ArrayList<String> budgetDes = new ArrayList<>();
    ArrayList<String> cost = new ArrayList<>();
    TravelAdapter travelAdapter;

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    SensorManager sm;
    private float acelVal;
    private float acelLat;
    private float shake;

    File defaultDir = Environment.getExternalStorageDirectory();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_list);

        travelListView = findViewById(R.id.travelListView);
        addNewTravelButton = findViewById(R.id.addNewTravelButton);
        backToMainButton = findViewById(R.id.backToMainButton);

        travelAdapter = new TravelAdapter(this, travelNames, createTimes);

        // get travel list
        getTravelList();

        // when click the list
        travelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                String e = travelNames.get(i);
                Intent showDetailActivity = new Intent(getApplicationContext(), TravelDetailActivity.class);
                showDetailActivity.putExtra("uk.ac.uel.TRAVEL_INDEX", i);
                showDetailActivity.putExtra("uk.ac.uel.TRAVEL_NAME", e);
                createFolder = new File(defaultDir + "/OnTheWayPhotos/" + e);

                // create folder to store photos
                if (createFolder.exists() || createFolder.isDirectory()) {
                } else {
                    createFolder.mkdir();
                }
                startActivity(showDetailActivity);
            }
        });

        // when long click the list
        travelListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final int index = i;
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(TravelListActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_delete_trip, null);
                Button confirmButton = mView.findViewById(R.id.confirmButton);
                Button cancelButton = mView.findViewById(R.id.cancelButton);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                // get diary names
                getDiaryNames(index);

                // get image names
                getImageNames(index);

                // get budget des
                getBudgetDes(index);

                // get cost
                getBudgetCost(index);

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAuth = FirebaseAuth.getInstance();
                        final FirebaseUser user = mAuth.getCurrentUser();
                        mDatabase = FirebaseDatabase.getInstance().getReference();

                        int m = travelNames.size();
                        String w = travelNames.get(index);

                        // delete travel names and travel times
                        mDatabase.child(user.getUid()).child("TravelNames").child(String.valueOf(index)).setValue(null);
                        mDatabase.child(user.getUid()).child("TravelTime").child(String.valueOf(index)).setValue(null);

                        // delete diary names
                        for (int i1 = diaryNames.size() - 1; i1 > -1; i1 --) {
                            mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(index)).child(String.valueOf(i1)).setValue(null);
                        }

                        // adjust diary index
                        for (int o = index + 1; o < m; o ++){

                            getDiaryNames(o);

                            final int p = o - 1;
                            final int q = o;

                            mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(o)).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(p)).setValue(dataSnapshot.getValue());
                                    for (int i1 = diaryNames.size() - 1; i1 > -1; i1--) {
                                        mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(q)).child(String.valueOf(i1)).setValue(null);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        // delete diary content
                        mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(index)).setValue(null);

                        // adjust diary content index
                        for (int o = index + 1; o < m; o ++){
                            final int p = o - 1;
                            final int q = o;
                            mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(o)).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(p)).setValue(dataSnapshot.getValue());
                                    mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(q)).setValue(null);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }



                        // delete images in local files
                        File deleteFileDir = new File(defaultDir + "/OnTheWayPhotos/" + w);
                        deletePhotos(deleteFileDir);

                        // delete images in storage
                        for(int u = 0; u < imageNames.size(); u ++) {
                            StorageReference deleteImageRef = storageRef.child("images/" + w + "/" + imageNames.get(u) + ".jpg");
                            deleteImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(TravelListActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(TravelListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        // delete images names on database
                        mDatabase.child(user.getUid()).child("ImageNames").child(String.valueOf(index)).setValue(null);

                        // delete total budget
                        mDatabase.child(user.getUid()).child("TotalBudget").child(String.valueOf(index)).setValue(null);

                        // adjust total budget index
                        for (int o = index + 1; o < m; o ++){
                            final int x = o - 1;
                            final int y = o;
                            mDatabase.child(user.getUid()).child("TotalBudget").child(String.valueOf(o)).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mDatabase.child(user.getUid()).child("TotalBudget").child(String.valueOf(x)).setValue(dataSnapshot.getValue());
                                    mDatabase.child(user.getUid()).child("TotalBudget").child(String.valueOf(y)).setValue(null);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }

                        // delete budget des
                        for (int i1 = budgetDes.size() - 1; i1 > -1; i1 --) {
                            mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(index)).child(String.valueOf(i1)).setValue(null);
                        }
                        budgetDes = new ArrayList<>();

                        // adjust budget index
                        for (int o = index + 1; o < m; o ++){

                            getBudgetDes(o);

                            final int p = o - 1;
                            final int q = o;

                            mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(o)).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(p)).setValue(dataSnapshot.getValue());
                                    for (int i1 = budgetDes.size() - 1; i1 > -1; i1--) {
                                        mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(q)).child(String.valueOf(i1)).setValue(null);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        // delete cost
                        for (int i1 = cost.size() - 1; i1 > -1; i1 --) {
                            mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(index)).child(String.valueOf(i1)).setValue(null);
                        }
                        cost = new ArrayList<>();

                        // adjust cost index
                        for (int o = index + 1; o < m; o ++){

                            getBudgetCost(o);

                            final int p = o - 1;
                            final int q = o;

                            mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(o)).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(p)).setValue(dataSnapshot.getValue());
                                    for (int i1 = cost.size() - 1; i1 > -1; i1--) {
                                        mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(q)).child(String.valueOf(i1)).setValue(null);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        dialog.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                return false;
            }
        });

        // when click the add button
        addNewTravelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(TravelListActivity.this);
                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_addnewtravel, null);

                final EditText mNewTripName = dialogView.findViewById(R.id.inutNewTripName);
                final TextView mNewTripDate = dialogView.findViewById(R.id.inputNewTripDate);
                final EditText mNewTripBudget = dialogView.findViewById(R.id.addBudget);
                Button confirmButton = dialogView.findViewById(R.id.confirmButton);
                Button cancelButton = dialogView.findViewById(R.id.cancelButton);
                final DatePickerDialog.OnDateSetListener mDateSetListener;


                mBuilder.setView(dialogView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();


                mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;

                        String date = month + "/" + dayOfMonth + "/" + year;
                        mNewTripDate.setText(date);

                    }
                };


                mNewTripDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dateDialog = new DatePickerDialog(
                                TravelListActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                mDateSetListener,
                                year, month, day);

                        dateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dateDialog.show();
                    }
                });


                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!((mNewTripName.getText().toString().isEmpty()) || (mNewTripDate.getText().toString().isEmpty())) || !(mNewTripBudget.getText().toString().isEmpty())){
                            mAuth = FirebaseAuth.getInstance();
                            FirebaseUser user = mAuth.getCurrentUser();
                            mDatabase = FirebaseDatabase.getInstance().getReference();
                            String newTrip = mNewTripName.getText().toString();
                            String newDate = mNewTripDate.getText().toString();
                            int totalBudget = Integer.parseInt(mNewTripBudget.getText().toString());
                            createFolder = new File(defaultDir + "/" + "OnTheWayPhotos/" + newTrip);

                            int i = 0;
                            if(travelNames != null && !travelNames.isEmpty()){
                                i = travelNames.size();
                            }else{
                                i = 0;
                            }

                            mDatabase.child(user.getUid()).child("TotalBudget").child(String.valueOf(travelNames.size())).setValue(String.valueOf(totalBudget));
                            mDatabase.child(user.getUid()).child("TravelNames").child(String.valueOf(travelNames.size())).setValue(newTrip);
                            mDatabase.child(user.getUid()).child("TravelTime").child(String.valueOf(createTimes.size())).setValue(newDate);

                            Toast.makeText(TravelListActivity.this, "Add Successful!", Toast.LENGTH_SHORT).show();

                            // create folder to store photos
                            if (createFolder.exists() || createFolder.isDirectory()) {
                            } else {
                                createFolder.mkdir();
                            }

                            String e = newTrip;
                            Intent showDetailActivity = new Intent(getApplicationContext(), TravelDetailActivity.class);
                            showDetailActivity.putExtra("uk.ac.uel.TRAVEL_INDEX", i);
                            showDetailActivity.putExtra("uk.ac.uel.TRAVEL_NAME", e);
                            startActivity(showDetailActivity);

                            dialog.dismiss();
                        } else {
                            Toast.makeText(TravelListActivity.this, "Please fill any empty fields!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

            }
        });

        // shake phone to delete all items
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        acelLat = SensorManager.GRAVITY_EARTH;
        acelVal = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;

        // when back button is clicked
        backToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(showMainActivity);
            }
        });

    }
    // on create ends


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

                        String stringIndex = dataSnapshot.getKey();
                        Log.d("delete index", stringIndex);

                        String stringValue = (String) dataSnapshot.getValue();
                        Log.d("delete value", stringValue);

                        int index = Integer.parseInt(stringIndex);

//                        diaryIndex.remove(index); // diary index changed

                        if (index == travelNames.size() - 1) {
                            travelNames.remove(index);
                            // set adapter
                        } else {
                            if (index < travelNames.size() - 1) {
                                travelNames.remove(index);
                                for (int i = 0; i < travelNames.size(); i++) {
                                    mDatabase.child(user.getUid()).child("TravelNames").child(String.valueOf(i)).setValue(travelNames.get(i));
                                }
                                mDatabase.child(user.getUid()).child("TravelNames").child(String.valueOf(travelNames.size())).setValue(null);
                            } else {
                            }
                        }

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
                        travelListView.setAdapter(travelAdapter);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        String stringIndex = dataSnapshot.getKey();
                        Log.d("delete index", stringIndex);

                        String stringValue = (String) dataSnapshot.getValue();
                        Log.d("delete value", stringValue);

                        int index = Integer.parseInt(stringIndex);

                        if (index == createTimes.size() - 1) {
                            createTimes.remove(index);
                            // set adapter
                        } else {
                            if (index < createTimes.size() - 1) {
                                createTimes.remove(index);
                                for (int i = 0; i < createTimes.size(); i++) {
                                    mDatabase.child(user.getUid()).child("TravelTime").child(String.valueOf(i)).setValue(createTimes.get(i));
                                }
                                mDatabase.child(user.getUid()).child("TravelTime").child(String.valueOf(createTimes.size())).setValue(null);
                            } else {
                            }
                        }
                        travelListView.setAdapter(travelAdapter);
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

    // sensor code
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {


            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            acelLat = acelVal;
            acelVal = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = acelVal - acelLat;
            shake = shake * 0.9f + delta;

            if (shake > 12) {

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(TravelListActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_delete_trip, null);
                TextView deleteAllTextView = mView.findViewById(R.id.deleteTripLabel);
                Button confirmButton = mView.findViewById(R.id.confirmButton);
                Button cancelButton = mView.findViewById(R.id.cancelButton);

                deleteAllTextView.setText("Delete All Trips?");

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();
                        mDatabase = FirebaseDatabase.getInstance().getReference();

                        int indexFixed = travelNames.size();

                        for (int i = indexFixed - 1; i > -1; i--) {
                            mDatabase.child(user.getUid()).child("TravelNames").child(String.valueOf(i)).setValue(null);
                            mDatabase.child(user.getUid()).child("TravelTime").child(String.valueOf(i)).setValue(null);
                        }
                        mDatabase.child(user.getUid()).child("DiaryNames").setValue(null);
                        mDatabase.child(user.getUid()).child("DiaryContent").setValue(null);
                        mDatabase.child(user.getUid()).child("ImageNames").setValue(null);
                        mDatabase.child(user.getUid()).child("TotalBudget").setValue(null);
                        mDatabase.child(user.getUid()).child("Cost").setValue(null);

                        //delete storage...
                        StorageReference deleteImageRef = storageRef.child("images");
                        deleteImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(TravelListActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(TravelListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        Toast.makeText(TravelListActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private void getDiaryNames(int i) {

        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(i)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded (DataSnapshot dataSnapshot, String s){
                String newItem = dataSnapshot.getValue(String.class);
                diaryNames.add(newItem);
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

    // delete photo folder on local memory
    private void deletePhotos(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deletePhotos(child);

        fileOrDirectory.delete();
    }

    // get image names
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

    private void getBudgetDes(int i){
        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(i)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded (DataSnapshot dataSnapshot, String s){
                String newItem = dataSnapshot.getValue(String.class);
                budgetDes.add(newItem);
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

    private void getBudgetCost (int i){
        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(i)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded (DataSnapshot dataSnapshot, String s){
                String newItem = dataSnapshot.getValue().toString();
                cost.add(newItem);
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
}
