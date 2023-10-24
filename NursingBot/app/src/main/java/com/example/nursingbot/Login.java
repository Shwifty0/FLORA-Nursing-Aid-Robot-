package com.example.nursingbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    EditText etun,etpw,user;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etun = (EditText) findViewById(R.id.email);
        user = (EditText) findViewById(R.id.username);
        etpw = (EditText) findViewById(R.id.password);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void btn_login_click(View v)
    {
        String email = etun.getText().toString();
        String password = etpw.getText().toString();
        String str = user.getText().toString();
        if(email.isEmpty())
        {
            etun.setError("Email can't be empty");
            Toast.makeText(Login.this,"Enter Email",Toast.LENGTH_LONG).show();
            etun.requestFocus();
            return;
        }
        if(str.isEmpty())
        {
            user.setError("Username can't be empty");
            Toast.makeText(Login.this,"Enter Username",Toast.LENGTH_LONG).show();
            user.requestFocus();
            return;
        }
        if(password.isEmpty())
        {
            etpw.setError("Please enter your password");
            Toast.makeText(Login.this,"Enter Password",Toast.LENGTH_LONG).show();
            etpw.requestFocus();
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(Login.this,"Success !!",Toast.LENGTH_LONG).show();
                    Intent myintent = new Intent(Login.this,MainActivity.class);
                    myintent.putExtra("Username", str);
                    startActivity(myintent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Invalid Username or Password",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}