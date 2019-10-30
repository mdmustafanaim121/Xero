package com.project.xero;



import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.xero.Common.Common;
import com.project.xero.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignUp extends AppCompatActivity {

    MaterialEditText xPhone,xName,xPassword;
    Button signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        xPhone = (MaterialEditText)findViewById(R.id.phoneNo);
        xName = (MaterialEditText) findViewById(R.id.Name);
        xPassword = (MaterialEditText) findViewById(R.id.xPassword);

        //Initializing Firebase for verifying login process

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference db_user = database.getReference("user");

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                    mDialog.setMessage("Loggin in");
                    mDialog.show();

                    db_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Check if somone with the same number already exist
                            if (dataSnapshot.child(xPhone.getText().toString()).exists()) {
                                mDialog.dismiss();
                                Toast.makeText(SignUp.this, "Number already exist", Toast.LENGTH_SHORT).show();
                            } else {
                                mDialog.dismiss();
                                User user = new User(xName.getText().toString(), xPassword.getText().toString());
                                db_user.child(xPhone.getText().toString()).setValue(user);
                                Toast.makeText(SignUp.this, "Signed Up Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                else {
                    Toast.makeText(SignUp.this, "Please, check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
