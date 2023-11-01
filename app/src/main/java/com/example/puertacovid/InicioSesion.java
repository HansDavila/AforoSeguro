package com.example.puertacovid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.core.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;

public class InicioSesion extends AppCompatActivity {
    Spinner spinner;
    public static final String[] languages ={"Lenguaje","Español","English"};


    private EditText emailEt, passwordEt;
    private Button SignInButton;
    private TextView SignUpTv;
    private TextView olvido;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
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
        setContentView(R.layout.activity_inicio_sesion);
        firebaseAuth = FirebaseAuth.getInstance();
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        SignInButton = findViewById(R.id.login);
        progressDialog = new ProgressDialog(this);
        olvido = findViewById(R.id.olvido);

        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });


        olvido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InicioSesion.this, CuentaUsuario.class);
                startActivity(intent);
                finish();
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
                    setLocal(InicioSesion.this, "es");
                    Intent intent = new Intent(InicioSesion.this, InicioSesion.class);
                    startActivity(intent);
                }
                else if(selectedLang.equals("Ingles")){
                    setLocal(InicioSesion.this, "en");
                    Intent intent = new Intent(InicioSesion.this, InicioSesion.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void Login(){
        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEt.setError("Ingresa tu email");
            return;
        } else if (TextUtils.isEmpty(password)) {
            passwordEt.setError("Ingresa tu contraseña");
            return;
        }
        progressDialog.setMessage("Porfavor, espere");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(InicioSesion.this, "Inicio de sesion completo", Toast.LENGTH_LONG).show();
                                checkUserAccessLevel(authResult.getUser().getUid());
                            } else {
                                Toast.makeText(InicioSesion.this, "Error al enviar el correo de verificación", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                startActivity(new Intent(InicioSesion.this,UsuarioActivity.class));
            }
        });

        progressDialog.dismiss();
    }
    public void setLocal(Activity activity, String langCode){
        Locale locale = new Locale(langCode);
        locale.setDefault(locale);

        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
    private void checkUserAccessLevel(String uid){
        DocumentReference df = fStore.collection("Usuarios").document(uid);
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("TAG", "onSuccess: " + documentSnapshot.getData());
                UserModel userModel= new UserModel();
                userModel.setFirstname(documentSnapshot.getString("Nombre"));
                userModel.setLastname(documentSnapshot.getString("Apellido"));
                userModel.setBio(documentSnapshot.getString("Biografia"));
                userModel.setPhone(documentSnapshot.getString("Telefono"));
                userModel.setBirthdate(calendar.getTimeInMillis());
                userModel.setUid(documentSnapshot.getString("Id"));

                Common.currentUser=userModel;
                if(documentSnapshot.getString("esAdministrador") != null){
                    startActivity(new Intent(InicioSesion.this,ConectarBt.class));
                    finish();
                }
                if(documentSnapshot.getString("esUsuario") != null){

                    startActivity(new Intent(InicioSesion.this,UsuarioActivity.class));
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "Error al obtener el documento: ", e);
            }
        });
    }
}