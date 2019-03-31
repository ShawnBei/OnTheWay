package uk.ac.uel.ontheway;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CalculatorActivity extends AppCompatActivity {

    private Button addButton, subButton, multiButton, divButton;
    private EditText num1, num2;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        addButton = findViewById(R.id.addButton);
        subButton = findViewById(R.id.subButton);
        multiButton = findViewById(R.id.multiplyButton);
        divButton = findViewById(R.id.divideButton);
        num1 = findViewById(R.id.firstEditText);
        num2 = findViewById(R.id.secondEditText);
        result = findViewById(R.id.resultTextView);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(num1.getText().toString().equals("")||num2.getText().toString().equals("")){
                    Toast.makeText(CalculatorActivity.this, "Please enter numbers ", Toast.LENGTH_SHORT).show();
                }else{
                    num1.getText().toString();
                    num2.getText().toString();
                    double a1 = Double.valueOf(num1.getText().toString());
                    double a2 = Double.valueOf(num2.getText().toString());
                    double a3;
                    a3 = a1+a2;
                    result.setText(String.valueOf(a3));
                }
            }
        });

        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(num1.getText().toString().equals("")||num2.getText().toString().equals("")){
                    Toast.makeText(CalculatorActivity.this, "Please enter numbers ", Toast.LENGTH_SHORT).show();
                }else{
                    num1.getText().toString();
                    num2.getText().toString();
                    double a1 = Double.valueOf(num1.getText().toString());
                    double a2 = Double.valueOf(num2.getText().toString());
                    double a3;
                    a3 = a1-a2;
                    result.setText(String.valueOf(a3));
                }
            }
        });

        multiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(num1.getText().toString().equals("")||num2.getText().toString().equals("")){
                    Toast.makeText(CalculatorActivity.this, "Please enter numbers ", Toast.LENGTH_SHORT).show();
                }else{
                    num1.getText().toString();
                    num2.getText().toString();
                    double a1 = Double.valueOf(num1.getText().toString());
                    double a2 = Double.valueOf(num2.getText().toString());
                    double a3;
                    a3 = a1 * a2;
                    result.setText(String.valueOf(a3));
                }
            }
        });

        divButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(num1.getText().toString().equals("")||num2.getText().toString().equals("")){
                    Toast.makeText(CalculatorActivity.this, "Please enter numbers ", Toast.LENGTH_SHORT).show();
                }else{
                    num1.getText().toString();
                    num2.getText().toString();
                    double a1 = Double.valueOf(num1.getText().toString());
                    double a2 = Double.valueOf(num2.getText().toString());
                    double a3;
                    a3 = a1 / a2;
                    result.setText(String.valueOf(a3));
                }
            }
        });

    }

}
