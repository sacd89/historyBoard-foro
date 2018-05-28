package com.example.hector.history;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String userFind = "";

    final RabbitController mensaje = new RabbitController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        if(true){
//            startActivity(new Intent(MainActivity.this, LoginActivity.class));
//        }
        String value = "";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("user");
            //The key argument here must match that used in the other activity
        }


        JSONObject jsonObj = new JSONObject();
        setContentView(R.layout.activity_main);
        TextView user = (TextView) findViewById(R.id.nombreUser);
        try {
            jsonObj = new JSONObject(value);
            user.append(jsonObj.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button enviar = (Button) findViewById(R.id.button);
        final EditText text = (EditText) findViewById(R.id.editText);
        final ListView lista = (ListView) findViewById(R.id.lista);
        ArrayList<Mensaje> arrayOfMensajes = new ArrayList<Mensaje>();
        final MensajesAdapter adapter = new MensajesAdapter(this, arrayOfMensajes);
        lista.setAdapter(adapter);
        mensaje.publishToAMQP();

        final Handler incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                try {
                    final JSONObject mensajeNuevoJson = new JSONObject(message);
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get("http://192.168.100.6:3000/users/android/getUserById/" + mensajeNuevoJson.getString("user"), new AsyncHttpResponseHandler() {

                        @Override
                        public void onStart() {
                            // called before request is started
                        }

                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {

                            try {

                                String usuarioEncontrado = new String(responseBody, "UTF-8");
                                JSONObject usuarioFindJson = new JSONObject(usuarioEncontrado);
                                Mensaje newMensaje = new Mensaje(usuarioFindJson.getString("name"), mensajeNuevoJson.getString("date"), mensajeNuevoJson.getString("mensaje"));
                                adapter.add(newMensaje);

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                        }
                        @Override
                        public void onRetry(int retryNo) {
                            // called when request is retried
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        mensaje.subscribe(incomingMessageHandler);

        final JSONObject finalJsonObj = jsonObj;
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensajeJson = "";
                JSONObject json = new JSONObject();
                String m = text.getText().toString();
                try {
                    json.put("mensaje", m);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    String dateString = formatter.format(new Date());
                    json.put("date", dateString);
                    json.put("user", finalJsonObj.getString("_id"));
                    
                    mensajeJson = json.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!m.equals("")){
                    mensaje.publishMessage(mensajeJson);
                    text.setText("");
//                    lista.setSelection(adapter.getCount() - 1);

                }
            }
        });

    }

    public String getUsuarioById(String id){
//        final RequestParams params = new RequestParams();
//        params.put("id", id);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://192.168.100.6:3000/users/android/getUserById/" + id, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {

                try {

                    userFind = new String(responseBody, "UTF-8");


                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
//                onLoginFailed();
//                progressDialog.dismiss();
            }
            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

        return userFind;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mensaje.publishThread.interrupt();
        mensaje.subscribeThread.interrupt();
    }
}
