package uk.ac.uel.ontheway;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TravelDetailActivity extends AppCompatActivity {

    int passedIndex = -1;
    String travelName = "";
    int imageCount = 0;
    ImageView diaryButton;
    ImageView albumButton;
    ImageView budgetButton;
    Button backToTravelListButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_detail);

        // get passed index
        if (getIntent().hasExtra("uk.ac.uel.TRAVEL_INDEX")){
            passedIndex = getIntent().getExtras().getInt("uk.ac.uel.TRAVEL_INDEX");
            travelName = getIntent().getExtras().getString("uk.ac.uel.TRAVEL_NAME");
            Log.d("passed index", String.valueOf(passedIndex));
        }
        else
            Toast.makeText(TravelDetailActivity.this, "Error!", Toast.LENGTH_SHORT).show();

        // get images count
        getImageCount(passedIndex);

        // define button
        diaryButton = (ImageView) findViewById(R.id.diaryButton);
        albumButton = (ImageView) findViewById(R.id.albumButton);
        budgetButton = (ImageView) findViewById(R.id.budgetButton);
        backToTravelListButton = (Button) findViewById(R.id.backToTravelListButton);

        // when click diary button
        diaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent diaryListIntent = new Intent(TravelDetailActivity.this, DiaryListActivity.class);
                diaryListIntent.putExtra("uk.ac.uel.TRAVEL_INDEX", passedIndex);
                diaryListIntent.putExtra("uk.ac.uel.TRAVEL_NAME", travelName);
                startActivity(diaryListIntent);
            }
        });

        // when click album button
        albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showAlbumListActivity = new Intent(getApplicationContext(), AlbumListActivity.class);
                showAlbumListActivity.putExtra("uk.ac.uel.TRAVEL_INDEX", passedIndex);
                showAlbumListActivity.putExtra("uk.ac.uel.TRAVEL_NAME", travelName);
                showAlbumListActivity.putExtra("uk.ac.uel.IMAGE_COUNT", imageCount);
                startActivity(showAlbumListActivity);
            }
        });

        // when click budget button
        budgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showBudgetActivity = new Intent(getApplicationContext(), TravelBudgetActivity.class);
                showBudgetActivity.putExtra("uk.ac.uel.TRAVEL_INDEX", passedIndex);
                showBudgetActivity.putExtra("uk.ac.uel.TRAVEL_NAME", travelName);
                startActivity(showBudgetActivity);
            }
        });

        // when click back button
        backToTravelListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showTravelListActivity = new Intent(getApplicationContext(), TravelListActivity.class);
                startActivity(showTravelListActivity);
            }
        });
    }

    // get image count
    private void getImageCount(int i){
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(user.getUid()).child("ImageNames").child(String.valueOf(i)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded (DataSnapshot dataSnapshot, String s){
                imageCount ++;
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
