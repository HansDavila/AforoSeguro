package com.example.puertacovid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

public class Mensaje_advertencia extends AppCompatActivity {
    private Button btnmen;
    FirebaseFirestore fStore;
    private TextView mTxtReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensaje_advertencia);
        btnmen = findViewById(R.id.mensaje);
        fStore = FirebaseFirestore.getInstance();
        mTxtReceive = (TextView) findViewById(R.id.editTextTextMultiLine2);
        btnmen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference df = fStore.collection("Estado").document("Mensaje");
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("Dia", mTxtReceive.getText().toString());
                df.set(userInfo);
                Intent intent = new Intent(Mensaje_advertencia.this, InicioActivity.class);
                startActivity(intent);            }
        });
    }
    private void init2() {
        ButterKnife.bind(this);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.inicio){
            Intent intent = new Intent(Mensaje_advertencia.this, HomeActivity.class);
            startActivity(intent);
        }else if (id == R.id.salir){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Mensaje_advertencia.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.cuenta){
            Intent intent = new Intent(Mensaje_advertencia.this, Cuenta.class);
            startActivity(intent);
        }else if (id == R.id.videocall){
            Intent intent = new Intent(Mensaje_advertencia.this, Mensaje_advertencia.class);
            startActivity(intent);
        }else if (id == R.id.peopleday){
            Intent intent = new Intent(Mensaje_advertencia.this, Registro_dia.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}