package com.example.nursingbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    TextView nameview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameview = (TextView) findViewById(R.id.textView);
        //Get text from Intent
        Intent intent = getIntent();
        String getName = intent.getStringExtra("Username");
        //Set Text
        nameview.setText(getName);

        CardView inventoryview=findViewById(R.id.cardview1); //IVM
        CardView callbot=findViewById(R.id.cardview2); //Call Bot
        CardView proreg=findViewById(R.id.cardview3); //Product Registry
        CardView patreg=findViewById(R.id.cardview4); //Patient Registry
        CardView pat1=findViewById(R.id.cardview5); //Patient 1
        CardView pat2=findViewById(R.id.cardview6); //Patient 2
        CardView health=findViewById(R.id.cardview7); //Patient 2

        inventoryview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, InventoryView.class);
                startActivity(i);
            }
        });
        callbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, BotCall.class);
                startActivity(i);
            }
        });
        proreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ProductRegistry.class);
                startActivity(i);
            }
        });
        patreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, PatientRegistry.class);
                startActivity(i);
            }
        });

        pat1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Patient1.class);
                startActivity(i);
            }
        });

        pat2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Patient2.class);
                startActivity(i);
            }
        });
        health.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Health.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finishAffinity();
    }
}