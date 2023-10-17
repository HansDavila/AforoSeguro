package com.example.puertacovid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class Historial extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.inicio){
            Intent intent = new Intent(Historial.this, InicioActivity.class);
            startActivity(intent);
        }else if (id == R.id.cuenta){
            Intent intent = new Intent(Historial.this, Cuenta.class);
            startActivity(intent);
        }else if (id == R.id.salir){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Historial.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.inicio){
            Intent intent = new Intent(Historial.this, Historial.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}