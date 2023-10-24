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

public class Update extends AppCompatActivity {
    TextView nameview;
    EditText e1;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        nameview = (TextView) findViewById(R.id.tabletname);
        e1 = (EditText) findViewById(R.id.quantity); //quantity


        //Get text from Intent
        Intent intent = getIntent();
        String getName = intent.getStringExtra("ProductName");
        String ID = intent.getStringExtra("ProductID");
        //Set Text
        nameview.setText(getName);

        if(ID.equals("1")){
            node = db.getReference("Inventory/1");
        }
        else if(ID.equals("2")){
            node = db.getReference("Inventory/2");
        }
        else if(ID.equals("3")){
            node = db.getReference("Inventory/3");
        }
        else if(ID.equals("4")){
            node = db.getReference("Inventory/4");
        }

    }
    public void updatequantity(View v)
    {
        String quantity = e1.getText().toString().trim();
        if(quantity.isEmpty())
        {
            e1.setError("Qunatity not be empty");
            e1.requestFocus();
            return;
        }
        node.child("quantity").setValue(quantity);
        e1.setText("");
        Toast.makeText(getApplicationContext(),"Update Successfully.",Toast.LENGTH_LONG).show();
        finish();
    }


}