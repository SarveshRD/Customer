package com.example.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText upEmail;
    private EditText upPassword;
    private Button SignUpButton;
    private ProgressDialog loadingbar;
    private static final String TAG = "SignUpActivity";
    private TextView LoginLabel;
    private FirebaseAuth firebaseauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_sign_up );


        upEmail = findViewById( R.id.UpEmail );
        upPassword = findViewById( R.id.UpPassword );
        SignUpButton = findViewById( R.id.SignUpButton );
        loadingbar = new ProgressDialog( this );
        LoginLabel = findViewById( R.id.LoginLabel );

        firebaseauth = FirebaseAuth.getInstance();



        LoginLabel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getApplicationContext(), LoginActivity.class );
                startActivity( intent );
                finish();
            }
        } );

        SignUpButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String email = upEmail.getText().toString().trim();
                String password = upPassword.getText().toString().trim();

                firebaseauth.createUserWithEmailAndPassword( email, password )
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user =firebaseauth.getCurrentUser();
                                    user.sendEmailVerification().addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(getApplicationContext(), "Email verification message send" ,Toast.LENGTH_LONG).show();
                                                String custid=firebaseauth.getCurrentUser().getUid();
                                                DatabaseReference custiddb= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(custid);
                                                custiddb.setValue(true);
                                                finish();
                                            }
                                            else
                                            {
                                                Toast.makeText(getApplicationContext(), "Email not valid" ,Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    } );
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder( SignUpActivity.this );
                                    builder.setMessage( task.getException().getMessage() )
                                            .setTitle( R.string.SignUp_error_title )
                                            .setPositiveButton( android.R.string.ok, null );
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            }
                        } );
            }
        } );


    }

}









