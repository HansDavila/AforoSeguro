package com.example.puertacovid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Cuenta extends AppCompatActivity {
    private EditText memail;
    private Button mbrestablecer;
    private String correo = "";
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

        memail = (EditText) findViewById(R.id.email);
        mbrestablecer = (Button) findViewById(R.id.brestablecer);

        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(this);

        mbrestablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correo = memail.getText().toString();

                if(!correo.isEmpty()){
                    mDialog.setMessage("Espera un momento");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    resetPassword();
                }
                else{
                    Toast.makeText(Cuenta.this, "Debe ingresar el correo electronico", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.inicio){
            Intent intent = new Intent(Cuenta.this, ConectarBt.class);
            startActivity(intent);
        }else if (id == R.id.salir){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Cuenta.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.cuenta){
            Intent intent = new Intent(Cuenta.this, Cuenta.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    private void resetPassword() {
        mAuth.setLanguageCode("es");
        mAuth.sendPasswordResetEmail(correo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Cuenta.this, "Se ha enviado un correo para reestablecer tu contraseña", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Cuenta.this, MainActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(Cuenta.this, "No se pudo enviar el correo de reestablecer contraseña", Toast.LENGTH_SHORT).show();
                }
                mDialog.dismiss();
            }
        });
    }
}