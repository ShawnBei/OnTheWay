package uk.ac.uel.ontheway;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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


public class BudgetActivity extends AppCompatActivity {

    ListView myListView;
    TextView totalSpendingTextView;
    Button budgetBackToMainButton;
    ArrayList<String> travelNames = new ArrayList<>();
    ArrayList<String> costs = new ArrayList<>();
    GeneralBudgetAdapter budgetAdapter;

    double totalSpending = 0.0;
    int sum = 0;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // define components
        myListView = findViewById(R.id.budgetView);
        totalSpendingTextView = findViewById(R.id.totalSpendingTextView);
        budgetBackToMainButton = (Button) findViewById(R.id.budgetBackToMainButton);

        // get list
        getList();




        budgetAdapter = new GeneralBudgetAdapter(this, travelNames, costs);
        myListView.setAdapter(budgetAdapter);

        totalSpendingTextView.setText("Total Spending is " + getTotalSpending());


        budgetBackToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showMain = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(showMain);
            }
        });


    }


    // get travel names
    private void getList() {
        // [start] get travel list and travel time from Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = mAuth.getCurrentUser();

        mDatabase.child(user.getUid()).child("TravelNames")
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String newItem = dataSnapshot.getValue(String.class);
                        sum = 0;
                        travelNames.add(newItem);
                        final String key = dataSnapshot.getKey().toString();
                        costs.add("0");
                        getCost(key);
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

    }
    private  void getCost(String key1){
        final String key = key1;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabase.child(user.getUid()).child("Cost").child(key).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String newItem = dataSnapshot.getValue().toString();
                sum = 0;
                sum += Integer.valueOf(newItem);
                int n = Integer.valueOf(costs.get(Integer.valueOf(key))) + sum;
                costs.set(Integer.valueOf(key), String.valueOf(n));
                myListView.setAdapter(budgetAdapter);
                totalSpendingTextView.setText("Total Spending is " + getTotalSpending());
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
    }

    private double getTotalSpending(){
        // calculate total spending
        totalSpending = 0.0;
        for (int i = 0; i < costs.size(); i++) {
            totalSpending += Double.parseDouble(costs.get(i));
        }
        return totalSpending;
    }
}
