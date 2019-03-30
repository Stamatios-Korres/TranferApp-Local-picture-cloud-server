package com.example.timos.photo_sharing.Activities;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.timos.photo_sharing.R;
import com.example.timos.photo_sharing.Runnables.ConnectToServerRunnable;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "WifiState";
    private Socket my_sock;
    private String ipOfServer;
    private int port = 8081;
    private ExecutorService executor;
   @RequiresApi(api = Build.VERSION_CODES.O)
   @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_login);
       executor = Executors.newFixedThreadPool(17);
       for (int i = 0; i < 255; i+=17) {
           Thread thread = new Thread(new ConnectToServerRunnable(i,i+15,this,"DoIt"));
           executor.execute(thread);
       }
       executor.shutdown();

        try {
//            if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_NETWORK_STATE)
//                    != PackageManager.PERMISSION_GRANTED)
//                Log.e("Permissions", "Access denied");
//
//            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//            if (mWifi.isConnected()) {
//                Log.e(TAG, "Wifi Conntected");
//                connect_to_Server();
//            } else {
//                Log.e(TAG, "Wifi Not Conntected");
//                long start = System.nanoTime();
//                connect_to_Server();
//                long elapsedTime = System.nanoTime() - start;
//                Log.e(TAG, String.valueOf(elapsedTime));
//            }
//        }catch(NetworkExceptions e){
//            Log.e(TAG,e.showMessage());
        }catch(Exception e){
            Log.e(TAG," Exception: ",e);
            e.printStackTrace();
       }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "Application is destroyed");
        super.onDestroy();
    }

    public void login(View view){
        EditText usernameText = (EditText) findViewById(R.id.editText2);
        String username= usernameText.getText().toString();
        EditText passwordTExt = (EditText) findViewById(R.id.editText);
        String password= usernameText.getText().toString();
    }

    public static class NetworkExceptions extends Exception {
        private String CauseofException;
        public NetworkExceptions(String ExceptionInformation){
            CauseofException = ExceptionInformation;
        }
        public String showMessage(){
            return CauseofException;
        }
    }

    public void  set_ip(String ipFound){
        ipOfServer = ipFound;
        executor.shutdownNow();

    }


}
