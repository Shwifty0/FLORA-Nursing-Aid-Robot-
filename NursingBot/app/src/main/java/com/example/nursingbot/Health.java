package com.example.nursingbot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Health extends AppCompatActivity {
    TextView tempview,spoview,bpmview;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node;
    DatabaseReference tempnode,spo2node,bpmnode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);

        tempview = (TextView) findViewById(R.id.temp);
        bpmview = (TextView) findViewById(R.id.heartrate);
        spoview = (TextView) findViewById(R.id.spo2);
        node = db.getReference("Health");
        tempnode = node.child("temp");
        bpmnode = node.child("bpm");
        spo2node = node.child("spo2");

        tempnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); // Patient Name
                tempview.setText(value+" °C");
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Health.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        bpmnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); // Patient Name
                bpmview.setText(value+" Bpm");
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Health.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        spo2node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); // Patient Name
                spoview.setText(value+" %");
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Health.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }
}