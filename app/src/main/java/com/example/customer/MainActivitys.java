package com.example.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivitys extends AppCompatActivity {
    private static final String TAG ="MainActivitys" ;
    private ProgressDialog loadingBar;
    FirebaseAuth mAuth;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitys_main);

        b= findViewById(R.id.LogButt);
        loadingBar =new ProgressDialog(getApplicationContext());

        /**************registerListener***********************/
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("this","onClick called");
                loadingBar.setTitle("Message box");
                loadingBar.setMessage("Please Wait");
                mAuth= FirebaseAuth.getInstance();

                try {
                    mAuth.signOut();
                    loadingBar.dismiss();
                    finish();
                    Intent i =new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(i);
                    Toast.makeText(getApplicationContext(),"Signed out Successfully",Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
