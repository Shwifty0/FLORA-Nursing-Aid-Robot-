package com.example.nursingbot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientRegistry extends AppCompatActivity {
    EditText e1,e2,e3,e4;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node;
    long node_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_registry);

        e1 = (EditText) findViewById(R.id.patientname); // tablet name
        e2 = (EditText) findViewById(R.id.mrtag); //quantity
        e3 = (EditText) findViewById(R.id.medicine1); // tablet name
        e4 = (EditText) findViewById(R.id.medicine2); //quantity

        node = db.getReference("Patients");
        node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    node_id = (dataSnapshot.getChildrenCount());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PatientRegistry.this,"Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void savePatient(View v)
    {
        String name = e1.getText().toString().trim();
        String code = e2.getText().toString().trim();
        String medi1 = e3.getText().toString().trim();
        String medi2 = e4.getText().toString().trim();

        if(name.isEmpty())
        {
            e1.setError("Please enter your name");
            e2.requestFocus();
            return;
        }
        if(code.isEmpty())
        {
            e2.setError("MR Tag ID not be empty");
            e2.requestFocus();
            return;
        }
        if(medi1.isEmpty())
        {
            e3.setError("Medicine Name not be empty");
            e3.requestFocus();
            return;
        }
        if(medi2.isEmpty())
        {
            e4.setError("Medicine Name not be empty");
            e4.requestFocus();
            return;
        }

        Patients obj = new Patients(name,code,medi1,medi2);


        // Writing data to Firebase
        node.child(String.valueOf(node_id + 1)).setValue(obj);

//        Log.e("Error: ", String.valueOf(obj));
        e1.setText("");
        e2.setText("");
        e3.setText("");
        e4.setText("");
        Toast.makeText(getApplicationContext(),"Saved Successfully.",Toast.LENGTH_LONG).show();
    }
}