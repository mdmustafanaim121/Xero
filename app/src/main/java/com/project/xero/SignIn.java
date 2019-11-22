package com.project.xero;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.xero.Common.Common;
import com.project.xero.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    EditText phoneNo,xPassword;
    Button signIn;
    com.rey.material.widget.CheckBox chkRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        xPassword = (MaterialEditText)findViewById(R.id.xPassword);
        phoneNo = (MaterialEditText)findViewById(R.id.phoneNo);
        signIn = (Button)findViewById(R.id.signIn);
        chkRemember = (CheckBox) findViewById(R.id.xCheckBox);

        //Init Paper
        Paper.init(this);

        //Initializing Firebase for verifying login process

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference db_user = database.getReference("user");

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    //Save user & password
                    if(chkRemember.isChecked())
                    {
                        Paper.book().write(Common.USER_KEY,phoneNo.getText().toString());
                        Paper.book().write(Common.PWD_KEY,xPassword.getText().toString());
                    }


                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("Logging in");
                    mDialog.show();

                    db_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //Code to check if a user exist in db or not
                            if (dataSnapshot.child(phoneNo.getText().toString()).exists()) {


                                //Gather User Information
                                mDialog.dismiss();
                                User user = dataSnapshot.child(phoneNo.getText().toString()).getValue(User.class);
                                user.setPhone(phoneNo.getText().toString()); //To set the phone no
                                if (user.getPassword().equals(xPassword.getText().toString())) {
                                    Intent intent = new Intent(SignIn.this, Home.class);
                                    Common.curUser = user;
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(SignIn.this, "Incorrect Login details", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "User does not exist", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SignIn.this, "Please, check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
