package com.example.wifi_socketclient;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReceiveService extends Service {
    public static final String TAG = ReceiveService.class.getSimpleName();
    public static final String DATA="data";
    private MulticastSocket mSocketReceive;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            EventBus.getDefault().post(new FirstEvent( msg.getData().getString(DATA)));
            Log.d(TAG, "client data : " + msg.getData().getString(DATA));
        }
    };

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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    receive();
                }
            }).start();
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
                String data = new String(buf, 0, dp.getLength());
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString(DATA, data);
                message.setData(bundle);
                mHandler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
