package com.example.nursingbot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProductRegistry extends AppCompatActivity {
    EditText e1,e2;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node;
    long node_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_registry);

        e1 = (EditText) findViewById(R.id.tabletname); // tablet name
        e2 = (EditText) findViewById(R.id.quantity); //quantity

        node = db.getReference("Inventory");
        node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    node_id = (dataSnapshot.getChildrenCount());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProductRegistry.this,"Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void addproduct(View v)
    {
        String tabletname = e1.getText().toString().trim();
        String quantity = e2.getText().toString().trim();

        if(tabletname.isEmpty())
        {
            e1.setError("Please enter your name");
            e2.requestFocus();
            return;
        }
        if(quantity.isEmpty())
        {
            e2.setError("Qunatity not be empty");
            e2.requestFocus();
            return;
        }

        Medicines obj = new Medicines(tabletname,quantity);

        // Writing data to Firebase
        node.child(String.valueOf(node_id + 1)).setValue(obj);
        e1.setText("");
        e2.setText("");
        Toast.makeText(getApplicationContext(),"Saved Successfully.",Toast.LENGTH_LONG).show();
    }
}