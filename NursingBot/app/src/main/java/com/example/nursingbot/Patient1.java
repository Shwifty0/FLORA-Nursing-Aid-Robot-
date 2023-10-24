package com.example.nursingbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Patient1 extends AppCompatActivity {
    TextView mainread,nameview,mrcodeview,med1view,med2view;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node;
    DatabaseReference namenode,mrcodenode,med1node,med2node;
    String productname1,productname2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient1);

        CardView med1=findViewById(R.id.cardview1); //med 1
        CardView med2=findViewById(R.id.cardview2); //med 2

        nameview = (TextView) findViewById(R.id.name1);
        mrcodeview = (TextView) findViewById(R.id.mrcode);
        med1view = (TextView) findViewById(R.id.med1);
        med2view = (TextView) findViewById(R.id.med2);
        node = db.getReference("Patients/1");
        namenode = node.child("name");
        mrcodenode = node.child("mrcode");
        med1node = node.child("med1");
        med2node = node.child("med2");

        namenode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); // Patient Name
                nameview.setText(value);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Patient1.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        mrcodenode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); // Patient Name
                mrcodeview.setText(value);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Patient1.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        med1node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productname1 = dataSnapshot.getValue(String.class); // Patient Name
                med1view.setText(productname1);

            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Patient1.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        med2node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productname2 = dataSnapshot.getValue(String.class); // Patient Name
                med2view.setText(productname2);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Patient1.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

        med1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str= "11";
                Intent i = new Intent(Patient1.this, PatientUpdate.class);
                i.putExtra("ProductName", productname1);
                i.putExtra("ProductID", str);
                startActivity(i);
            }
        });
        med2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str= "12";
                Intent i = new Intent(Patient1.this, PatientUpdate.class);
                i.putExtra("ProductName", productname2);
                i.putExtra("ProductID", str);
                startActivity(i);
            }
        });

    }
}