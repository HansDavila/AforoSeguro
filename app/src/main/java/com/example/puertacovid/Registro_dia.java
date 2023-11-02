package com.example.puertacovid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class Registro_dia extends AppCompatActivity {
    private TextView mTxtReceive,personas_edit;
    FirebaseFirestore fStore;
    private static final String TAG = "BlueTest5-MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_dia);
        mTxtReceive = (TextView) findViewById(R.id.people_day);
        fStore = FirebaseFirestore.getInstance();

        Button btnUpdateAforo = findViewById(R.id.btnUpdateAforo);
        EditText peopleDayEditText = findViewById(R.id.people_day);

        // Crea una referencia al documento de Firestore
        DocumentReference aforoRef = fStore.collection("Estado").document("Aforo");

        // Añade un EventListener para escuchar los cambios en tiempo real
        aforoRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // Obtiene el valor de Maximo y actualiza el TextView
                    String maximo = snapshot.getString("Maximo");
                    peopleDayEditText.setText(maximo);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });



        btnUpdateAforo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String peopleDayValue = peopleDayEditText.getText().toString();
                updateFirestoreAforo(peopleDayValue);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DocumentReference df = fStore.collection("Estado").document("People");
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
                    mTxtReceive.setText(snapshot.getString("PeopleDay"));

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void updateFirestoreAforo(String value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference aforoRef = db.collection("Estado").document("Aforo");

        aforoRef.update("Maximo", value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Registro_dia.this, "Aforo actualizado", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Registro_dia.this, InicioActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
    }
}