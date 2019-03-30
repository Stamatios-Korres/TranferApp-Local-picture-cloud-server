package com.example.timos.photo_sharing.Runnables;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.timos.photo_sharing.Activities.LoginActivity;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by timos on 10/4/2018.
 */


public class ConnectToServerRunnable implements Runnable  {

    private String command;
    private int rangeIpStart;
    private int rangeIpEnd;
    private Socket my_sock;
    private String startingIp;
    private final String TAG = "ThreadLog";
    private  AppCompatActivity LoginActivity;
    private AtomicBoolean myBoolean = new AtomicBoolean(false);
    public ConnectToServerRunnable(int start,int finish, AppCompatActivity LoginActivity,String s) {
        this.command = s;
        this.rangeIpStart = start;
        this.rangeIpEnd = finish;
        this.LoginActivity = LoginActivity;
        create_starting_ip();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start. Command = " + command);
        try {
            connect_to_Server();
        } catch (com.example.timos.photo_sharing.Activities.LoginActivity.NetworkExceptions networkExceptions) {
            networkExceptions.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " End.");
    }

    public void create_starting_ip(){
        int[] ipArray = new int[4];
        ipArray[0] = 192;
        ipArray[1]=168;
        ipArray[2] = rangeIpStart;
        ipArray[3] = 0;
        startingIp = "";
        for(int i=0;i<4;i++) {
            startingIp += String.valueOf(ipArray[i]);
            if(i!=3)
                startingIp+=".";
        }
    }

    public boolean connect_to_Ip(String ip, int port, int timeout) {
        try {
            my_sock = new Socket();
            my_sock.connect(new InetSocketAddress(ip, port), timeout);
            Log.e(TAG,"Connected");
            return true;
        } catch (Exception ex) {
            my_sock = null;
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String return_next_domain(String prev_ip) throws LoginActivity.NetworkExceptions {
        String nextIp = "";
        String[] parts = prev_ip.split("\\.");
        int [] intparts = new int[parts.length];
        for(int i=0;i<4;i++)
            intparts[i] = Integer.parseInt(parts[i]);
        if(intparts[3]==254){
            if(intparts[2]== rangeIpEnd || intparts[2]>=255)
                throw new LoginActivity.NetworkExceptions("All Ip Ranges were scanned");
            else {
                intparts[2]++;
                intparts[3]=0;
            }
        }
        else
            intparts[3]++;
        for(int i=0;i<4;i++) {
            parts[i] = String.valueOf(intparts[i]);
            nextIp += parts[i];
            if(i!=3)
                nextIp+=".";
        }
        return nextIp ;
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void connect_to_Server() throws com.example.timos.photo_sharing.Activities.LoginActivity.NetworkExceptions {
        while (true) {
            if (!Thread.interrupted()) {
                Log.e(TAG, "Attempting ip:  " + startingIp);
                if (connect_to_Ip(startingIp, 8081, 200) == true) {
                    Log.e(TAG, "Connected to: " + startingIp);
                    break;
                }
                startingIp = return_next_domain(startingIp);
            }

        }
    }
    public interface ThreadListener{

    }
}