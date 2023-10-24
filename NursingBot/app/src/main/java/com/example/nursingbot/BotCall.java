package com.example.nursingbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BotCall extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node,node1;
    DatabaseReference movementnode, Originnode, PointBnode, PointCnode;
    boolean pointA=false,pointB=false,pointC=false,Origin=true,movement=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_call);

        CardView cardView1=findViewById(R.id.cardview1);
        CardView cardView2=findViewById(R.id.cardview2);
        CardView cardView3=findViewById(R.id.cardview3);
        CardView cardView4  =findViewById(R.id.cardview4);

        node = db.getReference("Loc");
        node1 = db.getReference("Location");
        movementnode = node.child("movement");
        Originnode = node.child("notOrigin");

        movementnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); //load status
                if(value.equals("1")) {
                    movement=true;
                }
                else if(value.equals("0")){
                    movement=false;
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Error: ", String.valueOf(error));
            }
        });

        Originnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); //load status
                if(value.equals("1")) {
                    Origin=false;
                }
                else if(value.equals("0")){
                    Origin=true;
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Error: ", String.valueOf(error));
            }
        });

        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Origin == true && movement == false){
                    Toast.makeText(getApplicationContext(), "Moving Towards Point A ", Toast.LENGTH_LONG).show();
                    node1.child("PointA").setValue("1");
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Location Demand", Toast.LENGTH_LONG).show();
                }
            }
        });
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Origin == true && movement == false){
                    Toast.makeText(getApplicationContext(), "Moving Towards Point B ", Toast.LENGTH_LONG).show();
                    node1.child("PointB").setValue("1");
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Location Demand", Toast.LENGTH_LONG).show();
                }
            }
        });
        cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Origin == true && movement == false){
                    Toast.makeText(getApplicationContext(), "Moving Towards Point C ", Toast.LENGTH_LONG).show();
                    node1.child("PointC").setValue("1");
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Location Demand", Toast.LENGTH_LONG).show();
                }
            }
        });
        cardView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Origin == false && movement == false){
                    Toast.makeText(getApplicationContext(), "Moving Towards Origin ", Toast.LENGTH_LONG).show();
                    node1.child("Origin").setValue("1");
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid Location Demand", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}