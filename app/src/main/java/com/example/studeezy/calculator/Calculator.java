package com.example.studeezy.calculator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studeezy.R;
import com.example.studeezy.userDashboard.Dashboard;

public class Calculator extends AppCompatActivity {
    private TextView expressionTextView;
    private TextView resultTextView;

    private String input = "";
    private String operation = "";
    private String output = "";
    private boolean isResultCalculated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        expressionTextView = findViewById(R.id.expressionTextView);
        resultTextView = findViewById(R.id.resultTextView);

        Button backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(intent);
                finish();
            }
        });

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        Button button5 = findViewById(R.id.button5);
        Button button6 = findViewById(R.id.button6);
        Button button7 = findViewById(R.id.button7);
        Button button8 = findViewById(R.id.button8);
        Button button9 = findViewById(R.id.button9);
        Button button0 = findViewById(R.id.button0);

        Button buttonAdd = findViewById(R.id.button_add);
        Button buttonSub = findViewById(R.id.button_sub);
        Button buttonMulti = findViewById(R.id.button_multi);
        Button buttonDivide = findViewById(R.id.button_divide);
        Button buttonEqual = findViewById(R.id.button_equal);
        Button buttonClear = findViewById(R.id.button_clear);
        Button buttonDot = findViewById(R.id.button_dot);
        Button buttonSign = findViewById(R.id.button_para2);
        Button buttonPercent = findViewById(R.id.button_para1);

        View.OnClickListener numberClickListener = v -> {
            Button button = (Button) v;
            if (isResultCalculated) {
                input = "";
                isResultCalculated = false;
            }
            input += button.getText().toString();
            expressionTextView.setText(output + " " + operation + " " + input);
        };

        button1.setOnClickListener(numberClickListener);
        button2.setOnClickListener(numberClickListener);
        button3.setOnClickListener(numberClickListener);
        button4.setOnClickListener(numberClickListener);
        button5.setOnClickListener(numberClickListener);
        button6.setOnClickListener(numberClickListener);
        button7.setOnClickListener(numberClickListener);
        button8.setOnClickListener(numberClickListener);
        button9.setOnClickListener(numberClickListener);
        button0.setOnClickListener(numberClickListener);
        buttonDot.setOnClickListener(numberClickListener);

        buttonAdd.setOnClickListener(v -> performOperation("+"));
        buttonSub.setOnClickListener(v -> performOperation("-"));
        buttonMulti.setOnClickListener(v -> performOperation("x"));
        buttonDivide.setOnClickListener(v -> performOperation("÷"));

        buttonEqual.setOnClickListener(v -> calculateResult());

        buttonClear.setOnClickListener(v -> clearAll());

        buttonSign.setOnClickListener(v -> toggleSign());

        buttonPercent.setOnClickListener(v -> applyPercentage());
    }

    private void performOperation(String op) {
        if (!input.isEmpty()) {
            if (isResultCalculated) {
                isResultCalculated = false;
            }
            output = input;
            input = "";
        }
        operation = op;
        expressionTextView.setText(output + " " + operation);
    }

    private void calculateResult() {
        if (output.isEmpty() || input.isEmpty()) {
            return;
        }

        double result = 0;
        double firstOperand = Double.parseDouble(output);
        double secondOperand = Double.parseDouble(input);

        switch (operation) {
            case "+":
                result = firstOperand + secondOperand;
                break;
            case "-":
                result = firstOperand - secondOperand;
                break;
            case "x":
                result = firstOperand * secondOperand;
                break;
            case "÷":
                if (secondOperand != 0) {
                    result = firstOperand / secondOperand;
                } else {
                    resultTextView.setText("Error");
                    return;
                }
                break;
        }

        resultTextView.setText(String.valueOf(result));
        input = "";
        output = String.valueOf(result);
        isResultCalculated = true;
    }

    private void clearAll() {
        input = "";
        output = "";
        operation = "";
        expressionTextView.setText("0");
        resultTextView.setText("0");
        isResultCalculated = false;
    }

    private void toggleSign() {
        if (!input.isEmpty()) {
            double currentValue = Double.parseDouble(input);
            currentValue = -currentValue;
            input = String.valueOf(currentValue);
            expressionTextView.setText(output + " " + operation + " " + input);
        } else {
            Toast.makeText(Calculator.this, "Enter a number to toggle sign", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyPercentage() {
        if (!input.isEmpty()) {
            double currentValue = Double.parseDouble(input);
            if (operation.isEmpty()) {
                input = String.valueOf(currentValue / 100);
            } else {
                input = String.valueOf(currentValue * Double.parseDouble(output) / 100);
            }
            expressionTextView.setText(output + " " + operation + " " + input);
        }
    }
}