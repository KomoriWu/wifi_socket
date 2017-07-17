package com.example.wifi_socketclient;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReceiveService extends Service {
    public static final String TAG = ReceiveService.class.getSimpleName();
    private MulticastSocket mSocketReceive;

    @Override
    public IBinder onBind(Intent intent) {
        return new UdpReceiveBinder();
    }

    class UdpReceiveBinder extends Binder implements ReceiveServiceInterface {

        @Override
        public void udpReceive() {
            ReceiveService.this.udpReceive();
        }
    }


    @SuppressLint("StaticFieldLeak")
    private void udpReceive() {
        try {
            mSocketReceive = new MulticastSocket(MainActivity.PORT);
            mSocketReceive.joinGroup(InetAddress.getByName(MainActivity.IP));
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    byte buf[] = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, 1024);
                    while (true) {
                        try {
                            mSocketReceive.receive(dp);
                            Log.d(TAG, "client : " + new String(buf, 0, dp.getLength()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return new String(buf, 0, dp.getLength());
                    }
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);

                }
            }.execute();
//            receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void receive() {
        byte buf[] = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, 1024);
        while (true) {
            try {
                mSocketReceive.receive(dp);
                Log.d(TAG, "client ip : "+new String(buf, 0, dp.getLength()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
