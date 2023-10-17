package com.example.puertacovid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.puertacovid.Common.Common;
import com.example.puertacovid.Model.UserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    @BindView(R.id.edt_first_name)
    TextInputEditText edt_first_name;

    @BindView(R.id.edt_last_name)
    TextInputEditText edt_last_name;

    @BindView(R.id.edt_phone)
    TextInputEditText edt_phone;

    @BindView(R.id.edt_bio)
    TextInputEditText edt_bio;

    @BindView(R.id.edt_date_of_birth)
    TextInputEditText edt_date_of_birth;
    @BindView(R.id.btn_register)
    Button btn_register;

    FirebaseDatabase database;
    DatabaseReference userRef;
    MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .build();

    SimpleDateFormat simpleDateFormat= new SimpleDateFormat("dd-MM-yyyy");
    Calendar calendar = Calendar.getInstance();
    boolean isSelectBirthDate = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        init2();
        setDefaultData();
    }

    private void setDefaultData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        edt_phone.setEnabled(true);
        edt_date_of_birth.setOnFocusChangeListener((v, hasFocus)->{
            if(hasFocus)
                materialDatePicker.show(getSupportFragmentManager(),materialDatePicker.toString());

        });
        btn_register.setOnClickListener(v ->{
            if(!isSelectBirthDate){
                return;
            }


            mAuth.createUserWithEmailAndPassword(edt_first_name.getText().toString(), edt_last_name.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                assert firebaseUser != null;
                                String userid = firebaseUser.getUid();
                                UserModel userModel= new UserModel();
                                userModel.setFirstname(edt_first_name.getText().toString());
                                userModel.setLastname(edt_last_name.getText().toString());
                                userModel.setBio(edt_bio.getText().toString());
                                userModel.setPhone(edt_phone.getText().toString());
                                userModel.setBirthdate(calendar.getTimeInMillis());
                                userModel.setUid(userid);

                                userRef.child(userModel.getUid())
                                        .setValue(userModel)
                                        .addOnFailureListener(e ->{
                                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnSuccessListener(aVoid ->{
                                            Toast.makeText(RegisterActivity.this, "Registro completo", Toast.LENGTH_SHORT).show();
                                            Common.currentUser=userModel;
                                            startActivity(new Intent(RegisterActivity.this,HomeActivity.class));
                                            finish();
                                        });


                            } else {
                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });





        });

    }

    private void init2()
    {
        ButterKnife.bind(this);
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference(Common.USER_REFERENCES);
        materialDatePicker.addOnPositiveButtonClickListener(selection ->{
            calendar.setTimeInMillis(selection);
            edt_date_of_birth.setText(simpleDateFormat.format(selection));
            isSelectBirthDate=true;
        });

    }
}