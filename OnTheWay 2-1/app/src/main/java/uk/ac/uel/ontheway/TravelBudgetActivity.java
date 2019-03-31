package uk.ac.uel.ontheway;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.List;

public class TravelBudgetActivity extends AppCompatActivity {

    TextView totalBudgetTextView;
    ListView travelBudgetView;
    TextView restBudgetTextView;
    Button addNewCost;
    Button budgetBackToTravelDetailButton;
    GeneralBudgetAdapter budgetAdapter;

    ArrayList<String> budgetDescription = new ArrayList<>();
    ArrayList<String> cost = new ArrayList<>();
    int travelIndex = -1;
    String travelName = "";
    int totalBudget = 0;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_budget);

        // get passed index
        if (getIntent().hasExtra("uk.ac.uel.TRAVEL_INDEX")){
            travelIndex = getIntent().getExtras().getInt("uk.ac.uel.TRAVEL_INDEX");
            travelName = getIntent().getExtras().getString("uk.ac.uel.TRAVEL_NAME");
            Log.d("passed index", String.valueOf(travelIndex));
        }
        else
            Toast.makeText(TravelBudgetActivity.this, "Error!", Toast.LENGTH_SHORT).show();

        // define components
        totalBudgetTextView = (TextView) findViewById(R.id.totalBudgetTextView);
        travelBudgetView = (ListView) findViewById(R.id.travelBudgetView);
        restBudgetTextView = (TextView) findViewById(R.id.restBudget);
        addNewCost = (Button) findViewById(R.id.addNewCost);
        budgetBackToTravelDetailButton = (Button) findViewById(R.id.budgetBackToTravelDetailButton);
        budgetAdapter = new GeneralBudgetAdapter(this, budgetDescription, cost);

        //get budget descriptions and cost
        getBudget();

        // display total budget
        getTotalBudget();

        // when long press item
        travelBudgetView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final int index = i;
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(TravelBudgetActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_delete_trip, null);
                final TextView mBudgetDeleteTextView = mView.findViewById(R.id.deleteTripLabel);
                Button confirmButton = mView.findViewById(R.id.confirmButton);
                Button cancelButton = mView.findViewById(R.id.cancelButton);

                mBudgetDeleteTextView.setText("Delete Expense?");

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAuth = FirebaseAuth.getInstance();
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        final FirebaseUser user = mAuth.getCurrentUser();
                        mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(travelIndex)).child(String.valueOf(index)).setValue(null);
                        mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(travelIndex)).child(String.valueOf(index)).setValue(null);

                        Toast.makeText(TravelBudgetActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
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

        // when click add button
        addNewCost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(TravelBudgetActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_addnewcost, null);

                final EditText mNewBudgetDes = mView.findViewById(R.id.newBudgetDesEditText);
                final EditText mNewCost = mView.findViewById(R.id.newCostEditText);
                Button confirmButton = mView.findViewById(R.id.budgetConfirmButton);
                Button cancelButton = mView.findViewById(R.id.budgetCancelButton);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                // when click confirm add button
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAuth = FirebaseAuth.getInstance();
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        final FirebaseUser user = mAuth.getCurrentUser();
                        if(!(mNewBudgetDes.getText().toString().isEmpty()) || !(mNewCost.getText().toString().isEmpty())){
                            String newDes = mNewBudgetDes.getText().toString();
                            String newCost = mNewCost.getText().toString();
                            mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(travelIndex)).child(String.valueOf(budgetDescription.size())).setValue(newDes);
                            mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(travelIndex)).child(String.valueOf(cost.size())).setValue(newCost);
                            Toast.makeText(TravelBudgetActivity.this, "Add Successful!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        else{
                            Toast.makeText(TravelBudgetActivity.this, "Please fill any empty fields!", Toast.LENGTH_SHORT).show();
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

        // when click back button
        budgetBackToTravelDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showTravelDetailActivity = new Intent(getApplicationContext(), TravelDetailActivity.class);
                showTravelDetailActivity.putExtra("uk.ac.uel.TRAVEL_INDEX", travelIndex);
                showTravelDetailActivity.putExtra("uk.ac.uel.TRAVEL_NAME", travelName);
                startActivity(showTravelDetailActivity);
            }
        });
    }


    // get budget descriptions and cost
    private void getBudget(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = mAuth.getCurrentUser();

        mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(travelIndex)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String newItem = dataSnapshot.getValue(String.class);
                budgetDescription.add(newItem);
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

                if (index == budgetDescription.size() - 1) {
                    budgetDescription.remove(index);
                    // set adapter
                } else {
                    if (index < budgetDescription.size() - 1) {
                        budgetDescription.remove(index);
                        for (int i = 0; i < budgetDescription.size(); i++) {
                            mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(travelIndex)).child(String.valueOf(i)).setValue(budgetDescription.get(i));
                        }
                        mDatabase.child(user.getUid()).child("BudgetDes").child(String.valueOf(travelIndex)).child(String.valueOf(budgetDescription.size())).setValue(null);
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

        mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(travelIndex)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String newItem = dataSnapshot.getValue().toString();
                cost.add(newItem);
                // display rest budget
                getRestBudget();
                travelBudgetView.setAdapter(budgetAdapter);
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

                if (index == cost.size() - 1) {
                    cost.remove(index);

                } else {
                    if (index < cost.size() - 1) {
                        cost.remove(index);
                        for (int i = 0; i < cost.size(); i++) {
                            mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(travelIndex)).child(String.valueOf(i)).setValue(cost.get(i));
                        }
                        mDatabase.child(user.getUid()).child("Cost").child(String.valueOf(travelIndex)).child(String.valueOf(cost.size())).setValue(null);
                    } else {
                    }
                }
                // display rest budget
                getRestBudget();
                travelBudgetView.setAdapter(budgetAdapter);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getTotalBudget(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        mDatabase.child(user.getUid()).child("TotalBudget").child(String.valueOf(travelIndex)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String newItem = dataSnapshot.getValue().toString();
                totalBudget = Integer.valueOf(newItem);
                totalBudgetTextView.setText("£"+ newItem);
                getRestBudget();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRestBudget(){
        int restBudget = totalBudget;
        for(int i = 0; i < cost.size(); i ++){
            restBudget -= Integer.valueOf(cost.get(i));
        }
        if(restBudget < 0) {
            restBudgetTextView.setTextColor(Color.RED);
        }else{
            restBudgetTextView.setTextColor(Color.BLACK);
        }
        restBudgetTextView.setText("Remaining: £" + String.valueOf(restBudget));
    }




}
