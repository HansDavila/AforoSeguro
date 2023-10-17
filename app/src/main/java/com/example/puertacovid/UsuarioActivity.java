package com.example.puertacovid;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puertacovid.sdktest.videoLlamada;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UsuarioActivity extends AppCompatActivity {
    Spinner spinner;
    public static final String[] languages ={"Lenguaje","Español","Ingles"};
    private TextView mTxtReceive,personas_edit;
    FirebaseFirestore fStore;
    private static final String TAG = "BlueTest5-MainActivity";
    private Button mBtnConectar, mBtnAbrir, mBtnCerrar;
public static String Maximo;
    @Override
    protected void onResume() {
        super.onResume();
        DocumentReference df = fStore.collection("Estado").document("Aforo");
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>(){

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("TAG", "onSuccess: " + documentSnapshot.getData());
               mTxtReceive.setText(documentSnapshot.getString("Maximo"));
            }
        });

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
                    mTxtReceive.setText(snapshot.getString("Maximo"));

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
        DocumentReference df2 = fStore.collection("Estado").document("People");
        df2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    personas_edit.setText(snapshot.getString("Inside"));

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        DocumentReference df3 = fStore.collection("Estado").document("EstadoPuerta");
        df3.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
    String estado;
    estado=snapshot.getString("Puerta");
                    if(estado.equals("Cerrada")){
            mBtnConectar.setEnabled(false);
            mBtnConectar.setBackgroundColor(Color.parseColor("#a6a6a6"));
                    }else{
                        mBtnConectar.setEnabled(true);
                        mBtnConectar.setBackgroundColor(Color.parseColor("#37aaed"));
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        spinner = findViewById(R.id.spinner);
        mTxtReceive = (TextView) findViewById(R.id.aforo);
        personas_edit= (TextView) findViewById(R.id.personas_edit);
        fStore = FirebaseFirestore.getInstance();
        mBtnConectar = findViewById(R.id.conectar);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        mBtnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference df = fStore.collection("Estado").document("People");
                DocumentReference df2 = fStore.collection("Estado").document("Aforo");
                Map<String, Object> userInfo = new HashMap<>();
                df2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d("TAG", "onSuccess: " + documentSnapshot.getData());
 Maximo=documentSnapshot.getString("Maximo");
                        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>(){

                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Log.d("TAG", "onSuccess: " + documentSnapshot.getData());
                                if(Integer.parseInt(Maximo)>Integer.parseInt(documentSnapshot.getString("Inside"))){
                                        if(mBtnConectar.getText().equals("Enter")){
                                            mBtnConectar.setText("Exit");
                                            userInfo.put("Inside",(String.valueOf(Integer.parseInt(documentSnapshot.getString("Inside"))+1)));
                                            userInfo.put("PeopleDay",(String.valueOf(Integer.parseInt(documentSnapshot.getString("PeopleDay"))+1)));

                                            df.set(userInfo);
                                        }else if(mBtnConectar.getText().equals("Entrar")){
                                            mBtnConectar.setText("Salir");
                                            userInfo.put("Inside",(String.valueOf(Integer.parseInt(documentSnapshot.getString("Inside"))+1)));
                                            userInfo.put("PeopleDay",(String.valueOf(Integer.parseInt(documentSnapshot.getString("PeopleDay"))+1)));

                                            df.set(userInfo);
                                        }  else if(mBtnConectar.getText().equals("Exit")){
                                            mBtnConectar.setText("Enter");
                                            userInfo.put("Inside",(String.valueOf(Integer.parseInt(documentSnapshot.getString("Inside"))-1)));
                                            userInfo.put("PeopleDay",(String.valueOf(Integer.parseInt(documentSnapshot.getString("PeopleDay")))));

                                            df.set(userInfo);
                                        } else if(mBtnConectar.getText().equals("Salir")){
                                            mBtnConectar.setText("Exit");
                                            userInfo.put("Inside",(String.valueOf(Integer.parseInt(documentSnapshot.getString("Inside"))-1)));
                                            userInfo.put("PeopleDay",(String.valueOf(Integer.parseInt(documentSnapshot.getString("PeopleDay")))));

                                            df.set(userInfo);
                                        }
                                    }


                                 else{
                                        Toast.makeText(UsuarioActivity.this, "El aforo maximo se ha alcanzado", Toast.LENGTH_SHORT).show();

                                    }

                                }







                        });
                    }
                });



            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedLang = adapterView.getItemAtPosition(i).toString();
                if(selectedLang.equals("Español")){
                    setLocal(UsuarioActivity.this, "es");
                    Intent intent = new Intent(UsuarioActivity.this, UsuarioActivity.class);
                    startActivity(intent);
                }
                else if(selectedLang.equals("Ingles")){
                    setLocal(UsuarioActivity.this, "en");
                    Intent intent = new Intent(UsuarioActivity.this, UsuarioActivity.class);
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
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.inicio){
            Intent intent = new Intent(UsuarioActivity.this, HomeActivity.class);
            startActivity(intent);
        }else if (id == R.id.salir){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UsuarioActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.cuenta){
            Intent intent = new Intent(UsuarioActivity.this, Cuenta.class);
            startActivity(intent);
        }else if (id == R.id.videocall){
            Intent intent = new Intent(UsuarioActivity.this, Mensaje_usuario.class);
            startActivity(intent);
        }else if (id == R.id.peopleday){
            Intent intent = new Intent(UsuarioActivity.this, Registro_dia.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
