package com.example.barcode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.example.barcode.R.id.button;
import static com.example.barcode.R.id.goForSpeech;
import static com.example.barcode.R.id.textView;



public class MainActivity extends AppCompatActivity {

    public static TextView textView;
    Button button;
    static User user;
    static DatabaseReference myRef;
    static public Float bmiRes = 0.0f;

    static DatabaseReference usersRef;
    static FirebaseDatabase database;

    //BMI Related
    Spinner Sweight;
    Spinner Sheight;
    EditText weight;
    EditText height;
    TextView calculatedBMI;
    TextView speechRes;
    Button goForSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bmiRes=0.0f;

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        usersRef = myRef.child("users");

        //bmi parameters
        Sweight = (Spinner) findViewById(R.id.SpinnerWeight);
        Sheight = (Spinner) findViewById(R.id.SpinnerHeight);
        weight = (EditText) findViewById(R.id.TxtWeight);
        height = (EditText) findViewById(R.id.TxtHeight);
        calculatedBMI = (TextView) findViewById(R.id.TxtResult);
        speechRes = (TextView) findViewById(R.id.speechRes);
        goForSpeech = findViewById(R.id.goForSpeech);

        weight.requestFocus();


        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bmiRes>0){
                    startActivity(new Intent(MainActivity.this, Main2Activity.class));
                }
                else {
                    weight.requestFocus();
                    Toast.makeText(getApplicationContext(), "Please calculate the BMI to proceed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //BMI
        Button calculate   = (Button)findViewById(R.id.bmi);
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (IsValid()){
                    Float weight = ConvertWeight();
                    Float height = ConvertHeight();

                    DecimalFormat df = new DecimalFormat("#.##");

                    calculatedBMI.setText("Your BMI is " + df.format(Result(weight , height)).toString() + "\n" + "Scan the bar code to see results");

                }
            }


        });

        goForSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bmiRes>0) {
                    String temp =  (String) speechRes.getText();
                    temp.toLowerCase();
                    if(temp.equals("")){
                        Toast.makeText(getApplicationContext(), "Speech Not Recognized", Toast.LENGTH_SHORT).show();

                    }
                    else {
                        User userD = new User(true, temp, bmiRes);
                        usersRef.setValue(userD);
                        startActivity(new Intent(MainActivity.this, Data.class));
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "First Calculate BMI", Toast.LENGTH_SHORT).show();
                }

            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK && null!=data){
                    ArrayList<String> words = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speechRes.setText(words.get(0));
                    goForSpeech.requestFocus();

                }
                break;
        }
    }

    public static class User {

        public String productId;
        public Integer ifDetectedSend0;
        public Float bmi;
        public String outMessage;
        public String suggestion;
        public String speech;
        public Boolean trueForSpeech;


        public User(String id, Float kbmi) {
            this.productId = id;
            this.ifDetectedSend0 = 1;
            this.bmi = kbmi;
            this.outMessage = "";
            this.suggestion = "";
            this.speech = "";
            this.trueForSpeech=false;

            // ...
        }

        public User(Boolean trueForSpeech, String speech, Float kbmi) {
            this.productId = "";
            this.ifDetectedSend0 = 1;
            this.bmi = kbmi;
            this.outMessage = "";
            this.suggestion = "";
            this.speech = speech.toLowerCase();
            this.trueForSpeech=trueForSpeech;

            // ...
        }


    }

    // for calculation of BMI

    private Float ConvertHeight() {
        Float ans = 0.0f;
        Float heightResult = 0.0f;
        String unit = Sheight.getSelectedItem().toString();

        switch (unit){
            case "ft":
                heightResult = Float.parseFloat(height.getText().toString());
                ans = heightResult * 12.0f;
                break;
            case "cm":
                heightResult = Float.parseFloat(height.getText().toString());
                ans = heightResult * 0.3937f;
                break;
            default:
                ans = 0.0f;
        }
        return ans;
    }

    private Float ConvertWeight() {
        Float ans = 0.0f;
        String unit = Sweight.getSelectedItem().toString();

        switch (unit){
            case "kg":
                Float weightResult = Float.parseFloat(weight.getText().toString());
                ans = weightResult * 2.2f;
                break;
            case "lbs":
                ans = Float.parseFloat(weight.getText().toString());
                break;
            default:
                ans = 0.0f;
        }
        return ans;
    }

    private Float Result(Float weight, Float height) {
        Float resultWeight;
        Float resultHeight;

        resultWeight = weight * 0.45f;
        resultHeight = height * 0.025f;

        resultHeight = resultHeight * resultHeight;

        bmiRes = resultWeight/resultHeight;

        return bmiRes;
    }

    private boolean IsValid() {
        boolean ret = true;

        if (weight.getText().toString().trim().length() == 0){
            weight.setError("Enter your weight");
            return false;
        }

        if (height.getText().toString().trim().length() == 0){
            height.setError("Enter your height");
            return false;
        }

        return ret;
    }

    //speech
    public void speechR(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi Speak Something");

        try{
            startActivityForResult(intent, 1);

        }catch (ActivityNotFoundException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }



    }





}
