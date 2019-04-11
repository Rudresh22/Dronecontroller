package com.example.dronecontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button mtakeOff, mLand, disconnect;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ProgressDialog progress;
    private ImageView mHome;
    // SPP UUID service - this should work for most devices
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isBtConnected = false;
    private static String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHome=(ImageView)findViewById(R.id.homebtn);

        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,home.class);
                startActivity(intent);

            }
        });

        //Get MAC address and device name from the DeviceList via EXTRA after creating Intent
        Intent intent = getIntent();
        address = intent.getStringExtra(Devicelist.EXTRA_ADDRESS);


        new ConnectBT().execute();



        //Car Controls
        mtakeOff = findViewById(R.id.button);
        mLand = findViewById(R.id.button2);


        disconnect = findViewById(R.id.button3);

        // Car Button Listeners
        mtakeOff.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    droneCommand("F");
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    droneCommand("D"); //to stop car when button is released
                }
                return true;

            }
        });

        mLand.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    droneCommand("S");
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    droneCommand("D"); //to stop car when button is released
                }
                return true;
            }
        });




        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                droneCommand("S");
                disconnectBT();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        setContentView(R.layout.activity_main);
        disconnectBT();
    }

    // For Drone Commands
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    btAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                finish();
                msg("Connection Failed. Please try again.");
            } else {
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }


    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }


    // For Drone Commands: (Upward - "F" Downward - "S" Stop = "D")
    private void droneCommand(String s) {
        if(btSocket!=null) {
            try {
                btSocket.getOutputStream().write(s.getBytes()); // sends Command
            } catch (IOException E) {
                msg("Error");
            }
        }
    }

    private void disconnectBT() {
        try {
            btSocket.close();
            msg("Disconnected successfully.");
            finish();
        } catch(java.io.IOException E) {
            msg("Error while closing BT connection.");
        }
    }
}
