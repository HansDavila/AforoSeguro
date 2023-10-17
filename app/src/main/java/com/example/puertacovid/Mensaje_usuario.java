package com.example.puertacovid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class Mensaje_usuario extends AppCompatActivity {

    FirebaseFirestore fStore;
    private TextView mTxtReceive;
    private static final String TAG = "BlueTest5-MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensaje_usuario);
        fStore = FirebaseFirestore.getInstance();
        mTxtReceive = (TextView) findViewById(R.id.editTextTextMultiLine2);

    }

    @Override
    protected void onResume() {
        super.onResume();
        DocumentReference df = fStore.collection("Estado").document("Mensaje");
        df.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    mTxtReceive.setText(snapshot.getString("Dia"));

                } else {
                    Log.d(TAG, "Current data: null");
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
            Intent intent = new Intent(Mensaje_usuario.this, HomeActivity.class);
            startActivity(intent);
        }else if (id == R.id.salir){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Mensaje_usuario.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.cuenta){
            Intent intent = new Intent(Mensaje_usuario.this, Cuenta.class);
            startActivity(intent);
        }else if (id == R.id.videocall){
            Intent intent = new Intent(Mensaje_usuario.this, Mensaje_usuario.class);
            startActivity(intent);
        }else if (id == R.id.peopleday){
            Intent intent = new Intent(Mensaje_usuario.this, Registro_dia.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}