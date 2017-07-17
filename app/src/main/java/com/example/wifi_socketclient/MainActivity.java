package com.example.wifi_socketclient;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private MulticastSocket mSocketSend;

    private Button mBtnSend;
    public static final String IP = "224.0.0.1";
    public static final int PORT = 8888;
    private String mSendStr = "hello";
    private DatagramPacket dataPacket;
    private InetAddress mAddress;
    private Intent mIntent;
    private ReceiveServiceInterface mServiceInterface;
    private MyServiceConn mServiceConn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        try {
            mSocketSend = new MulticastSocket();
            mAddress = InetAddress.getByName(IP);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    udpSend();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        initService();
    }
    private class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mServiceInterface = (ReceiveServiceInterface) iBinder;
            mServiceInterface.udpReceive();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
    private void initService() {
        mServiceConn = new MyServiceConn();
        mIntent = new Intent(this, ReceiveService.class);
        startService(mIntent);
        bindService(mIntent, mServiceConn, BIND_AUTO_CREATE);
    }


    private void udpSend() throws IOException {
        //发送的数据包，局网内的所有地址都可以收到该数据包
        mSocketSend.setTimeToLive(4);
        byte[] data = mSendStr.getBytes();
        //这个地方可以输出判断该地址是不是广播类型的地址
        Log.d(TAG, "address:" + mAddress.isMulticastAddress() );
        dataPacket = new DatagramPacket(data, data.length, mAddress, PORT);
        mSocketSend.send(dataPacket);
//        mSocketSend.close();
    }

    private String getLocalIp() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if (ipAddress == 0) return "未连接wifi";
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }
}
