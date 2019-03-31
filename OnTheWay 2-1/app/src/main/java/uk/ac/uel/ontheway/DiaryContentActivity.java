package uk.ac.uel.ontheway;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DiaryContentActivity extends AppCompatActivity {

    int travelIndex = -1;
    int diaryIndex = -1;
    String travelName = "";
    EditText diaryContent;
    Button saveButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser user = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_content);

        diaryContent = (EditText) findViewById(R.id.diaryContentEditText);
        saveButton = (Button) findViewById(R.id.saveButton);

        // get passed index
        if (getIntent().hasExtra("uk.ac.uel.TRAVEL_INDEX")){
            travelIndex = getIntent().getExtras().getInt("uk.ac.uel.TRAVEL_INDEX");
            diaryIndex = getIntent().getExtras().getInt("uk.ac.uel.DIARY_INDEX");
            travelName = getIntent().getExtras().getString("uk.ac.uel.TRAVEL_NAME");
        }
        else
            Toast.makeText(DiaryContentActivity.this, "Error!", Toast.LENGTH_SHORT).show();

        // get text from database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(travelIndex)).child(String.valueOf(diaryIndex)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String content = dataSnapshot.getValue(String.class);
                diaryContent.setText(content);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // when click save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = diaryContent.getText().toString();
                mDatabase.child(user.getUid()).child("DiaryContent").child(String.valueOf(travelIndex)).child(String.valueOf(diaryIndex)).setValue(content);
                Intent showDiaryListActivity = new Intent(getApplicationContext(), DiaryListActivity.class);
                showDiaryListActivity.putExtra("uk.ac.uel.TRAVEL_INDEX", travelIndex);
                showDiaryListActivity.putExtra("uk.ac.uel.TRAVEL_Name", travelName);
                startActivity(showDiaryListActivity);
            }
        });
    }
}
