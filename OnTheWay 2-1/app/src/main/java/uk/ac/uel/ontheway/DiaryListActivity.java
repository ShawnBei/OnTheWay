package uk.ac.uel.ontheway;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DiaryListActivity extends AppCompatActivity {

    int passedIndex = -1;
    String travelName = "";
    ArrayList<String> diaryNames = new ArrayList<>();
    ListView diaryListView;
    Button addNewDiaryButton;
    Button backToTravelDetailButton;
    ArrayAdapter<String> diaryAdapter;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser user = mAuth.getCurrentUser();

    SensorManager sm;
    private float acelVal;
    private float acelLat;
    private float shake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);

        // get passed index
        if (getIntent().hasExtra("uk.ac.uel.TRAVEL_INDEX")){
            passedIndex = getIntent().getExtras().getInt("uk.ac.uel.TRAVEL_INDEX");
            travelName = getIntent().getExtras().getString("uk.ac.uel.TRAVEL_NAME");
            Log.d("passed index", String.valueOf(passedIndex));
        }
        else
            Toast.makeText(DiaryListActivity.this, "Error!", Toast.LENGTH_SHORT).show();


        // define button and list view
        diaryListView = (ListView) findViewById(R.id.diaryListView);
        addNewDiaryButton = (Button) findViewById(R.id.addDiaryButton);
        backToTravelDetailButton = (Button) findViewById(R.id.backToTravelDetailButton);

        // define list view adapter
        diaryAdapter = new ArrayAdapter<String>(this, R.layout.diarylist_view_single, R.id.diaryListTextView, diaryNames);


        // get diary list from database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(passedIndex)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String newItem = dataSnapshot.getValue(String.class);
                Log.d("New Item", newItem);
                diaryNames.add(newItem);
                diaryListView.setAdapter(diaryAdapter);
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

                if(index == diaryNames.size()-1){
                    diaryNames.remove(index);
                    // set adapter
                }
                else{
                    if(index < diaryNames.size()-1) {
                        diaryNames.remove(index);
                        for (int i = 0; i < diaryNames.size(); i++) {
                            mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(passedIndex)).child(String.valueOf(i)).setValue(diaryNames.get(i));
                        }
                        mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(passedIndex)).child(String.valueOf(diaryNames.size())).setValue(null);
                    }
                    else{
                    }
                }
                diaryListView.setAdapter(diaryAdapter);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // when click on item
        diaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                Intent showDiaryContentActivity = new Intent(getApplicationContext(), DiaryContentActivity.class);
                showDiaryContentActivity.putExtra("uk.ac.uel.TRAVEL_INDEX", passedIndex);
                showDiaryContentActivity.putExtra("uk.ac.uel.DIARY_INDEX", i);
                showDiaryContentActivity.putExtra("uk.ac.uel.TRAVEL_NAME", travelName);
                startActivity(showDiaryContentActivity);
            }
        });

        // when click add button
        addNewDiaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(DiaryListActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_addnewdiary, null);

                final EditText mNewDiaryName = mView.findViewById(R.id.newDiaryEditText);
                Button confirmButton = mView.findViewById(R.id.confirmNewDiaryButton);
                Button cancelButton = mView.findViewById(R.id.cancelNewdiaryButton);
                final DatePickerDialog.OnDateSetListener mDateSetListener;

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                // when click confirm add button
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!(mNewDiaryName.getText().toString().isEmpty())){
                            String newDiary = mNewDiaryName.getText().toString();
                            mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(passedIndex)).child(String.valueOf(diaryNames.size())).setValue(newDiary);
                            Toast.makeText(DiaryListActivity.this, "Add Successful!", Toast.LENGTH_SHORT).show();

                            int i = 0;
                            if(diaryNames != null && !diaryNames.isEmpty()){
                                i = diaryNames.size();
                            }else{
                                i = 0;
                            }


                            Intent showDiaryContentActivity = new Intent(getApplicationContext(), DiaryContentActivity.class);
                            showDiaryContentActivity.putExtra("uk.ac.uel.TRAVEL_INDEX", passedIndex);
                            showDiaryContentActivity.putExtra("uk.ac.uel.DIARY_INDEX", i);
                            showDiaryContentActivity.putExtra("uk.ac.uel.TRAVEL_NAME", travelName);
                            startActivity(showDiaryContentActivity);

                            dialog.dismiss();
                        }
                        else{
                            Toast.makeText(DiaryListActivity.this, "Please fill any empty fields!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                // when click cancel button
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });


        // when long press item (delete one item)
        diaryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final int index = i;
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(DiaryListActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_delete_diary, null);
                Button confirmButton = mView.findViewById(R.id.confirmDeleteDiaryButton);
                Button cancelButton = mView.findViewById(R.id.cancelDeleteDiaryButton);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                final int m = diaryNames.size();

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(passedIndex)).child(String.valueOf(index)).setValue(null);
                        mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(passedIndex)).child(String.valueOf(index)).setValue(null);

                        // adjust diary content index
                        for (int o = index + 1; o < m; o ++){
                            final int p = o - 1;
                            final int q = o;
                            mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(passedIndex)).child(String.valueOf(o)).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(passedIndex)).child(String.valueOf(p)).setValue(dataSnapshot.getValue());
                                    mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(passedIndex)).child(String.valueOf(q)).setValue(null);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }

                        Toast.makeText(DiaryListActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
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


        // when shake the phone (delete all item)
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        acelLat = SensorManager.GRAVITY_EARTH;
        acelVal = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;


        // when back button is clicked
        backToTravelDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showTravelDetailActivity = new Intent(getApplicationContext(), TravelDetailActivity.class);
                showTravelDetailActivity.putExtra("uk.ac.uel.TRAVEL_INDEX", passedIndex);
                showTravelDetailActivity.putExtra("uk.ac.uel.TRAVEL_NAME", travelName);
                startActivity(showTravelDetailActivity);
            }
        });

    }

    // shake code
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            acelLat = acelVal;
            acelVal = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = acelVal - acelLat;
            shake = shake * 0.9f + delta;

            if (shake > 12){

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(DiaryListActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_delete_diary, null);
                TextView deleteAllTextView = mView.findViewById(R.id.deleteDiaryTextView);
                Button confirmButton = mView.findViewById(R.id.confirmDeleteDiaryButton);
                Button cancelButton = mView.findViewById(R.id.cancelDeleteDiaryButton);

                deleteAllTextView.setText("Delete All Diaries?");

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int indexFixed = diaryNames.size();
                        for(int i = indexFixed-1; i > -1; i --) {
                            mDatabase.child(user.getUid()).child("DiaryNames").child(String.valueOf(passedIndex)).child(String.valueOf(i)).setValue(null);
                        }
                        mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(passedIndex)).setValue(null);
                        Toast.makeText(DiaryListActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
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
}
