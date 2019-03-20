package com.example.dronecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class Devicelist extends AppCompatActivity {

    ListView deviceList;
    private BluetoothAdapter btAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    private EditText IPText = null;
    public static String EXTRA_ADDRESS = "device_address";
    public static String EXTRA_NAME = "device_name";
    public static String EXTRA_IP = "device_ip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Select Device");
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBTState();
        setContentView(R.layout.activity_devicelist);
        deviceList = findViewById(R.id.listView);


        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter.isEnabled()) {
            pairedDevicesList();
        }
    }

    private void pairedDevicesList()
    {
        pairedDevices = btAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0) {
            for(BluetoothDevice bt : pairedDevices) {
                list.add("Name: " + bt.getName() + "\nMAC: " + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
                checkBTState();
                // Get the device MAC address, the last 17 chars in the View
                String info = ((TextView) v).getText().toString();
                String address = info.substring(info.length() - 17);


                // Make an intent to start next activity.
                Intent i = new Intent(Devicelist.this, MainActivity.class);

                //Change the activity.
                i.putExtra(EXTRA_ADDRESS, address); //this will be received in MainActivity
                startActivity(i);

        }
    };

    private void checkBTState() {
        btAdapter= BluetoothAdapter.getDefaultAdapter();
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!btAdapter.isEnabled()) {
                //Ask user to turn on BT
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

}
