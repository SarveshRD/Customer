package com.example.customer;

import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText InEmail;
    private EditText InPassword;
    private Button Loginbutton;
    private FirebaseAuth mAuth;
    private ProgressBar loadingbar;
    private static final String TAG = "LoginActivity";
    private TextView SignUpLabel;
    private boolean check =false;
    private FirebaseAuth.AuthStateListener firebaseauthlistener;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        InEmail = findViewById( R.id.InEmail );
        InPassword = findViewById( R.id.InPassword );
        Loginbutton = findViewById( R.id.LoginButton );
        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        loadingbar =findViewById(R.id.progressBar);
        SignUpLabel = findViewById( R.id.SignupLabel );
        FirebaseUser firebaseUser =FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser!=null ) {
            if (firebaseUser.isEmailVerified()) {
                    chooserighActivity();
            }
        }

        SignUpLabel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getApplicationContext(), SignUpActivity.class );
                startActivity( intent );
                finish();
            }
        });

        Loginbutton.setOnClickListener( new View.OnClickListener() {

            public void onClick(View v)
            {
                String email = InEmail.getText().toString();
                String password = InPassword.getText().toString();

                email = email.trim();
                password = password.trim();

                if (email.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder( LoginActivity.this );
                    builder.setMessage( R.string.login_error_message )
                            .setTitle( R.string.login_error_title )
                            .setPositiveButton( android.R.string.ok, null );
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {
                    loadingbar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword( email, password )
                            .addOnCompleteListener( LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        loadingbar.setVisibility(View.GONE);
                                        cheakemailverification();
                                    }
                                    else
                                    {
                                        loadingbar.setVisibility(View.GONE);
                                        AlertDialog.Builder builder = new AlertDialog.Builder( LoginActivity.this );
                                        builder.setMessage( task.getException().getMessage() )
                                                .setTitle( R.string.login_error_title )
                                                .setPositiveButton( android.R.string.ok, null );
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });
                }
            }

        });
    }


     private void chooserighActivity()
     {
         loadingbar.setVisibility(View.VISIBLE);
         DatabaseReference temp_intent_ref = FirebaseDatabase.getInstance().getReference().child("CustomersAvailable");
         temp_intent_ref.addListenerForSingleValueEvent(new  ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if(dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                 {
                     loadingbar.setVisibility(View.GONE);
                     startActivity(new Intent(getApplicationContext(), MainActivitys.class));
                     finish();
                 }
                 else
                 {
                     loadingbar.setVisibility(View.GONE);
                     startActivity(new Intent(getApplicationContext(),CustomerMapsActivity.class));
                     finish();
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
     }

    void cheakemailverification() {
        loadingbar.setVisibility(View.VISIBLE);
        FirebaseUser firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser.isEmailVerified())
        {
            if(FirebaseAuth.getInstance().getCurrentUser() != null )
            {
                loadingbar.setVisibility(View.GONE);
                    chooserighActivity();
            }
        }
        else
        {
            loadingbar.setVisibility(View.GONE);
            Toast.makeText( getApplicationContext(), "Failure",Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
        }
    }
}

