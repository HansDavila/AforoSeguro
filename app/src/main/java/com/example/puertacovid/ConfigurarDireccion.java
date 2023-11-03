package com.example.puertacovid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfigurarDireccion extends AppCompatActivity {

    private EditText editTextUrl;
    private Button buttonContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_direccion);

        editTextUrl = findViewById(R.id.editTextUrl);
        buttonContinuar = findViewById(R.id.buttonContinuar);

        buttonContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = editTextUrl.getText().toString();

                if (!url.isEmpty()) {
                    // Muestra un ProgressDialog mientras esperas la respuesta
                    final ProgressDialog progressDialog = new ProgressDialog(ConfigurarDireccion.this);
                    progressDialog.setMessage("Cargando...");
                    progressDialog.show();

                    // Configura Retrofit con la URL ingresada
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ApiService apiService = retrofit.create(ApiService.class);
                    Call<ResponseBody> call = apiService.getAforo();

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            progressDialog.dismiss(); // Oculta el ProgressDialog

                            if (response.isSuccessful()) {
                                // Si la respuesta es exitosa, inicia InicioActivity
                                Intent intent = new Intent(ConfigurarDireccion.this, InicioActivity.class);
                                intent.putExtra("URL", url);
                                startActivity(intent);
                            } else {
                                // Si la respuesta no es exitosa, muestra un mensaje
                                Toast.makeText(ConfigurarDireccion.this, "Conexión fallida", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            progressDialog.dismiss(); // Oculta el ProgressDialog

                            // Si la solicitud falla, muestra un mensaje
                            Toast.makeText(ConfigurarDireccion.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Muestra un mensaje si el campo de texto está vacío
                    Toast.makeText(ConfigurarDireccion.this, "Por favor, ingrese una URL", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}