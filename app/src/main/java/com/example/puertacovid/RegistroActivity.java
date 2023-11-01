package com.example.puertacovid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puertacovid.Common.Common;
import com.example.puertacovid.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegistroActivity extends AppCompatActivity {
    Spinner spinner;
    public static final String[] languages ={"Lenguaje","Español","Ingles"};


    private EditText emailEt, passwordEt1, passwordEt2;
    private Button SignUpButton;
    private TextView SignInTv;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    boolean valid = true;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    CheckBox esAdministradorBox,esUsuarioBox;
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
    @BindView(R.id.register)
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
        setContentView(R.layout.registro);
        mAuth = FirebaseAuth.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        init2();
        setDefaultData();
        emailEt = findViewById(R.id.email);
        passwordEt1 = findViewById(R.id.password1);
        passwordEt2 = findViewById(R.id.password2);
        SignUpButton = findViewById(R.id.register);
        esAdministradorBox = findViewById(R.id.administrador);
        esUsuarioBox = findViewById(R.id.usuario);
        progressDialog = new ProgressDialog(this);
        SignInTv = findViewById(R.id.signInTv);
        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });
        SignInTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        esUsuarioBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    esAdministradorBox.setChecked(false);
                }
            }
        });
        esAdministradorBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    esUsuarioBox.setChecked(false);
                }
            }
        });

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedLang = adapterView.getItemAtPosition(i).toString();
                if(selectedLang.equals("Español")){
                    setLocal(RegistroActivity.this, "es");
                    Intent intent = new Intent(RegistroActivity.this, RegistroActivity.class);
                    startActivity(intent);
                }
                else if(selectedLang.equals("Ingles")){
                    setLocal(RegistroActivity.this, "en");
                    Intent intent = new Intent(RegistroActivity.this, RegistroActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
    public void setLocal(Activity activity, String langCode){
        Locale locale = new Locale(langCode);
        locale.setDefault(locale);

        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
    private void Register() {
        String email = emailEt.getText().toString();
        String password1 = passwordEt1.getText().toString();
        String password2 = passwordEt2.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEt.setError("Ingresa tu email");
            return;
        } else if (TextUtils.isEmpty(password1)) {
            passwordEt1.setError("Ingresa tu contraseña");
            return;
        } else if (TextUtils.isEmpty(password2)) {
            passwordEt2.setError("Confirma tu contraseña");
            return;
        } else if (!password1.equals(password2)) {
            passwordEt2.setError("Diferente contraseña");
            return;
        } else if (password1.length() < 4) {
            passwordEt1.setError("La contraseña debe contener mas de 4 caracteres");
            return;
        } else if (!isVallidEmail(email)) {
            emailEt.setError("Correo invalido");
            return;
        } else if (!(esAdministradorBox.isChecked() || esUsuarioBox.isChecked())){
            Toast.makeText(RegistroActivity.this, "Selecciona el tipo de cuenta", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Porfavor, espere");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth.createUserWithEmailAndPassword(emailEt.getText().toString(), passwordEt1.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("TAG", "Usuario creado con éxito");
                    Toast.makeText(RegistroActivity.this, "Registro Completo, por favor, verifica tu email", Toast.LENGTH_LONG).show();
                    FirebaseUser email = firebaseAuth.getCurrentUser();
                    FirebaseUser user = fAuth.getCurrentUser();
                    email.sendEmailVerification();
                    DocumentReference df = fStore.collection("Usuarios").document(user.getUid());
                    Map<String, Object> userInfo = new HashMap<>();
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    assert firebaseUser != null;
                    String userid = firebaseUser.getUid();

                    userInfo.put("Email", emailEt.getText().toString());
                    userInfo.put("Nombre",edt_first_name.getText().toString());
                    userInfo.put("Apellido",edt_last_name.getText().toString());
                    userInfo.put("Biografia",edt_bio.getText().toString());
                    userInfo.put("Telefono",edt_phone.getText().toString());
                    userInfo.put("Cumple",calendar.getTimeInMillis());
                    userInfo.put("Id",userid);


                    if(esAdministradorBox.isChecked()){
                        userInfo.put("esAdministrador", "1");
                    }
                    if(esUsuarioBox.isChecked()){
                        userInfo.put("esUsuario", "1");
                    }

                    UserModel userModel= new UserModel();
                    userModel.setFirstname(edt_first_name.getText().toString());
                    userModel.setLastname(edt_last_name.getText().toString());
                    userModel.setBio(edt_bio.getText().toString());
                    userModel.setPhone(edt_phone.getText().toString());
                    userModel.setBirthdate(calendar.getTimeInMillis());
                    userModel.setUid(userid);


                    df.set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "Datos guardados con éxito en Firestore");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", "Error al guardar en Firestore: " + e.getMessage());
                        }
                    });

                    userRef.child(userModel.getUid())
                            .setValue(userModel)
                            .addOnFailureListener(e -> {
                                Log.d("TAG", e.getMessage());
                                Toast.makeText(RegistroActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(aVoid -> {
                                Log.d("TAG", "Información del usuario guardada en Realtime Database");
                                Common.currentUser=userModel;
                                startActivity(new Intent(RegistroActivity.this, StartActivity.class));
                                finish();
                            });



                } else {
                    Toast.makeText(RegistroActivity.this, "Registro Fallido: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
    }
    private void setDefaultData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        edt_phone.setEnabled(true);
        edt_date_of_birth.setOnFocusChangeListener((v, hasFocus)->{
            if(hasFocus)
                materialDatePicker.show(getSupportFragmentManager(),materialDatePicker.toString());

        });}

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
    private Boolean isVallidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
