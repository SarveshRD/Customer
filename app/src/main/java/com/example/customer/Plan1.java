package com.example.customer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Plan1 extends AppCompatActivity {

    Button Plan_btn, Plan_btn2;
    TextView time,time2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan1);

        Plan_btn=findViewById(R.id.btn_plan);
        Plan_btn2=findViewById(R.id.btn_plan2);
        time = findViewById(R.id.time);
        time2 = findViewById(R.id.time2);


        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("TimeStamp");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long timestamp = (Long) snapshot.getValue();
                System.out.println(timestamp);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ref.setValue(ServerValue.TIMESTAMP);



        //button coding


        Plan_btn2.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            ref1.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Long timestamp = 0L;

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (Objects.equals(child.getKey(), "TimeStamp")) {
                                timestamp = Long.valueOf(child.getValue().toString());
                            }
                        }
                        time.setText("TimeStamp : " + getDate(timestamp));

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (Objects.equals(child.getKey(), "TimeStamp")) {
                                timestamp = Long.valueOf(child.getValue().toString());
                            }
                        }
                        time2.setText("UpdateDate : " + getUpdateDate(timestamp));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

        }
    });



        Plan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Plan1.this,MainActivity.class));

            }
        });



    }


    private String getUpdateDate (Long timestamp){

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.add(Calendar.DATE, 30);
        Date d2 = cal.getTime();
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        if(new Date().before(d2))
        {
            Toast.makeText(Plan1.this,"Your Plan is currently working ",Toast.LENGTH_SHORT).show();
        }
        else if(new Date().after(d2)){

            final DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("TimeStamp");
            ref2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        ref2.push().setValue("");
                        startActivity(new Intent(Plan1.this,Error.class));
                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            Toast.makeText(Plan1.this,"your plan is finished",Toast.LENGTH_SHORT).show();
        }else if(new Date().compareTo(d2) == 0)
        {
            Toast.makeText(Plan1.this,"Today is a last day of plan",Toast.LENGTH_SHORT).show();

        }else
        {
            Toast.makeText(Plan1.this,"Sorry",Toast.LENGTH_SHORT).show();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        return sdf.format(d2);

    }

    private String getDate (Long timestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        Date d1 = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        return sdf.format(d1);
    }


}