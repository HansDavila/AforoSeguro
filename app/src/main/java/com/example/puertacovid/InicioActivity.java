package com.example.puertacovid;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import okhttp3.ResponseBody;


import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InicioActivity extends AppCompatActivity {
    Spinner spinner;
    public static final String[] languages ={"Lenguaje","Español","Ingles"};
    private Button mBtnConectar, mBtnAbrir, mBtnCerrar;
    private static final String TAG = "BlueTest5-MainActivity";
    private int mMaxChars = 50000;//Default
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    CheckBox esAdministradorBox,esUsuarioBox;
    FirebaseAuth mAuth;    //private UUID mDeviceUUID;
    //private BluetoothSocket mBTSocket;
    //private ReadInput mReadThread = null;
    //private boolean mIsUserInitiatedDisconnect = false;

    private TextView mTxtReceive;
    private CheckBox chkReceiveText;
    private TextView campoTxt;

    private long tiempoUltimaNotificacion = 0;

    private int aforoActual;
    private boolean notificacionEnviada = false; // Variable para mantener el estado de la notificación
    String maximo;

    //private boolean mIsBluetoothConnected = false;

    //private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;
    String command;
    //private OutputStream outputStream;

    private Handler handler = new Handler();
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            fetchAforo(); // Método que hace la solicitud GET
            handler.postDelayed(this, 10000); // Reprograma el runnable cada 10 segundos
        }
    };



    @Override
    protected void onResume() {
        super.onResume();

        // Localiza el TextView de capacidad
        TextView textViewCapacidad = findViewById(R.id.capacidad);

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
                    maximo = snapshot.getString("Maximo");
                    textViewCapacidad.setText("Capacidad: " + maximo + " personas");

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        DocumentReference df = fStore.collection("Estado").document("EstadoPuerta");
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
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio);

        campoTxt = findViewById(R.id.CampoTxt);

        handler.post(runnableCode);

        spinner = findViewById(R.id.spinner);
        mAuth = FirebaseAuth.getInstance();
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedLang = adapterView.getItemAtPosition(i).toString();
                if(selectedLang.equals("Español")){
                    setLocal(InicioActivity.this, "es");
                    Intent intent = new Intent(InicioActivity.this, ConectarBt.class);
                    startActivity(intent);
                }
                else if(selectedLang.equals("Ingles")){
                    setLocal(InicioActivity.this, "en");
                    Intent intent = new Intent(InicioActivity.this, ConectarBt.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ActivityHelper.initialize(this);
        //Intent intent = getIntent();
        //Bundle b = intent.getExtras();
        //mDevice = b.getParcelable(ConectarBt.DEVICE_EXTRA);
        //mDeviceUUID = UUID.fromString(b.getString(ConectarBt.DEVICE_UUID));
        //mMaxChars = b.getInt(ConectarBt.BUFFER_SIZE);
        Log.d(TAG, "Ready");
        mTxtReceive = (TextView) findViewById(R.id.aforo);
        mBtnConectar = findViewById(R.id.conectar);
        chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
        mBtnAbrir = findViewById(R.id.abrirpuerta);
        mBtnCerrar = findViewById(R.id.cerrarpuerta);
        /*mBtnAbrir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command = "1";
                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        mBtnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command = "2";
                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });*/

mBtnConectar.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        DocumentReference df = fStore.collection("Estado").document("Aforo");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("Maximo", mTxtReceive.getText().toString());
        df.set(userInfo);

    }
});

        mBtnAbrir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mBtnAbrir.getText().equals("Open Door")){
                    mBtnAbrir.setBackgroundColor(Color.parseColor("#ae0000"));
                    mBtnAbrir.setText("Close Door");
                    DocumentReference df = fStore.collection("Estado").document("EstadoPuerta");
                    Map<String, Object> userInfo = new HashMap<>();

                    userInfo.put("Puerta", "Abierta");
                    df.set(userInfo);

                }else if(mBtnAbrir.getText().equals("Abrir Puerta")){
                    mBtnAbrir.setBackgroundColor(Color.parseColor("#ae0000"));
                    mBtnAbrir.setText("Cerrar Puerta");
                    DocumentReference df = fStore.collection("Estado").document("EstadoPuerta");
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("Puerta", "Abierta");
                    df.set(userInfo);

                }else  if(mBtnAbrir.getText().equals("Close Door")){
                    mBtnAbrir.setBackgroundColor(Color.parseColor("#71ae00"));
                    mBtnAbrir.setText("Open Door");
                    DocumentReference df = fStore.collection("Estado").document("EstadoPuerta");
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("Puerta", "Cerrada");
                    df.set(userInfo);

                }else if(mBtnAbrir.getText().equals("Cerrar Puerta")){
                    mBtnAbrir.setBackgroundColor(Color.parseColor("#71ae00"));
                    mBtnAbrir.setText("Abrir Puerta");
                    DocumentReference df = fStore.collection("Estado").document("EstadoPuerta");
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("Puerta", "Cerrada");
                    df.set(userInfo);
                }
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
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.inicio){
            Intent intent = new Intent(InicioActivity.this, ChatActivity.class);
            startActivity(intent);
        }else if (id == R.id.salir){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(InicioActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.cuenta){
            Intent intent = new Intent(InicioActivity.this, Cuenta.class);
            startActivity(intent);
        }else if (id == R.id.videocall){
            Intent intent = new Intent(InicioActivity.this, Mensaje_advertencia.class);
            startActivity(intent);
        }else if (id == R.id.peopleday){
            Intent intent = new Intent(InicioActivity.this, Registro_dia.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    private void fetchAforo() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.7:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<ResponseBody> call = apiService.getAforo();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        aforoActual = jsonObject.getInt("aforo");
                        mTxtReceive.setText(String.valueOf(aforoActual));

                        int actualPersonas = mTxtReceive.getText().toString().equals("") ? 0 : Integer.parseInt(mTxtReceive.getText().toString());
                        if (actualPersonas >= Integer.parseInt(maximo)) {
                            if (chkReceiveText.isChecked()) { // Si el CheckBox está activo
                                if (!notificacionEnviada || (System.currentTimeMillis() - tiempoUltimaNotificacion) >= 60000) {
                                    mostrarNotificacionAforoMaximo();
                                    notificacionEnviada = true;
                                    tiempoUltimaNotificacion = System.currentTimeMillis(); // Guarda el tiempo actual
                                }
                            } else if (!notificacionEnviada) { // Si el CheckBox no está activo y no se ha enviado notificación
                                mostrarNotificacionAforoMaximo();
                                notificacionEnviada = true;
                            }
                        } else if (actualPersonas < Integer.parseInt(maximo)) {
                            notificacionEnviada = false; // Restablece la marca si el aforo está por debajo del máximo
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        Log.e("API_ERROR", "Error al procesar la respuesta", e);
                    }
                } else {
                    Log.e("API_ERROR", "Respuesta no exitosa: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e("API_ERROR", "Error al hacer la llamada", t);
                campoTxt.setText("Error: " + t.getMessage());
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detiene cualquier callback pendiente al destruir la actividad
        handler.removeCallbacks(runnableCode);
    }

    // Método para mostrar la notificación
    private void mostrarNotificacionAforoMaximo() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "aforoMaximo"; // Usar el mismo ID para el canal y la notificación

        // Crear el canal de notificación para versiones de Android Oreo o superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH; // Configura la importancia
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Aforo Máximo", importance);
            channel.setDescription("Notificaciones de aforo máximo alcanzado");
            channel.setShowBadge(true);
            channel.enableVibration(true); // Habilitar vibración
            channel.enableLights(true); // Habilitar luces
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            notificationManager.createNotificationChannel(channel);
        }

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name) // Asegúrate de que este es un icono válido
                .setContentTitle("Aforo máximo alcanzado")
                .setContentText("El aforo actual ha superado el límite permitido.")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Para versiones anteriores a Oreo
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true) // La notificación se cancela al tocarla
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // Para mostrar en la pantalla de bloqueo y en la barra de estado

        // Mostrar la notificación
        notificationManager.notify(1, builder.build());
    }


    /*private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554

                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);

                        /*
                         * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix


                        if (chkReceiveText.isChecked()) {
                            mTxtReceive.post(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtReceive.setText(strInput);
                                }
                            });
                        }

                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
// TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(InicioActivity.this, "Hold on", "Connecting");
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
// Unable to connect to device
                e.printStackTrace();
                mConnectSuccessful = false;
            }

            if(mConnectSuccessful)
            {
                try
                {
                    outputStream = mBTSocket.getOutputStream(); //gets the output stream of the socket
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }*/


}
