package com.example.nursingbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PatientUpdate extends AppCompatActivity {
    TextView nameview;
    EditText e1;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node;
    String mednode= " ";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_update);

        nameview = (TextView) findViewById(R.id.tabletname);
        e1 = (EditText) findViewById(R.id.med);



        //Get text from Intent
        Intent intent = getIntent();
        String getName = intent.getStringExtra("ProductName");
        String ID = intent.getStringExtra("ProductID");
        //Set Text
        nameview.setText(getName);

        if(ID.equals("11")){
            node = db.getReference("Patients/1");
            mednode = "med1";

        }
        else if(ID.equals("12")){
            node = db.getReference("Patients/1");
            mednode = "med2";
        }
    }
    public void updatemed(View v)
    {
        String medname = e1.getText().toString().trim();
        if(medname.isEmpty())
        {
            e1.setError("Medicine Name not be empty");
            e1.requestFocus();
            return;
        }
        node.child(mednode).setValue(medname);
        e1.setText("");
        Toast.makeText(getApplicationContext(),"Update Successfully.",Toast.LENGTH_LONG).show();
    }
}