package com.example.nursingbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InventoryView extends AppCompatActivity {
    TextView productin, productoutview, med1, med2, med3, med4, name1, name2, name3, name4;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node, node1, node2, node3, node4;
    DatabaseReference productoutnode, med1node, med2node, med3node, med4node, name1node, name2node, name3node, name4node;
    int med1count = 0, med2count = 0, med3count = 0, med4count = 0, total = 0, productout = 0;
    String productname1, productname2, productname3, productname4;
    long node_id = 0;

//    String to Int : Integer.parseInt(String)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_view);

        name1 = (TextView) findViewById(R.id.text1);
        med1 = (TextView) findViewById(R.id.med1);
        name2 = (TextView) findViewById(R.id.text2);
        med2 = (TextView) findViewById(R.id.med2);
        name3 = (TextView) findViewById(R.id.text3);
        med3 = (TextView) findViewById(R.id.med3);
        name4 = (TextView) findViewById(R.id.text4);
        med4 = (TextView) findViewById(R.id.med4);
        productin = (TextView) findViewById(R.id.textView3);
//        productoutview = (TextView) findViewById(R.id.textView7);

        node = db.getReference("Inventory");
        node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    node_id = (dataSnapshot.getChildrenCount());
                    Log.e("IDs: ", String.valueOf(node_id));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(InventoryView.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });

    }
    public void med1update(View v) {
        String str = "1";
        Intent myintent = new Intent(InventoryView.this, Update.class);
        myintent.putExtra("ProductID", str);
        startActivity(myintent);
    }

    public void med2update(View v) {
        String str = "2";
        Intent myintent = new Intent(InventoryView.this, Update.class);
        myintent.putExtra("ProductID", str);
        startActivity(myintent);
    }

    public void med3update(View v) {
        String str = "3";
        Intent myintent = new Intent(InventoryView.this, Update.class);
        myintent.putExtra("ProductName", productname3);
        myintent.putExtra("ProductID", str);
        startActivity(myintent);
    }

    public void med4update(View v) {
        String str = "4";
        Intent myintent = new Intent(InventoryView.this, Update.class);
        myintent.putExtra("ProductName", productname4);
        myintent.putExtra("ProductID", str);
        startActivity(myintent);
    }


}