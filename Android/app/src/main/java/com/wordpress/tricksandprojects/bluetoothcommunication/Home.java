package com.wordpress.tricksandprojects.bluetoothcommunication;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity {

    private static final int BLUETOOTH_DISCOVERY_DURATION = 60;
    protected Button send, listen;
    protected TextView receivedText;
    protected BluetoothAdapter bluetoothAdapter;
    private final int BLUETOOTH_DISCOVERY = 0;
    private ArrayAdapter<String> arrayAdapter;
    private boolean turnedOnByApp = false;

    //Broadcast receiver for device found by the Bluetooth module
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                arrayAdapter.add(device.getName() + ": " + device.getAddress());
                arrayAdapter.notifyDataSetChanged();

            }

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        send = (Button)findViewById(R.id.sendButton);
        listen = (Button)findViewById(R.id.listenButton);
        receivedText = (TextView) findViewById(R.id.receivedText);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    /**
     * Method called when clicking on the "Listen" button.
     * It enables Bluetooth, makes the phone discoverable and launches the server.
     */
    public void listen(View view){

        //If the Bluetooth module is off, turn it on. This is possible thanks to android.permission.BLUETOOTH_ADMIN.
        //Otherwise we should have asked the user to turn it on via a Dialog.
        if (!bluetoothAdapter.isEnabled()){

            bluetoothAdapter.enable();
            turnedOnByApp = true;

        }

        receivedText.setText("No text has been received yet.");

        //Check if the phone is already discoverable.
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){

            //Make the phone discoverable on Bluetooth network for 60s.
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERY_DURATION);
            startActivityForResult(discoverableIntent, BLUETOOTH_DISCOVERY);

        }
        else{
            Toast.makeText(this, "Your device is still discoverable and is currently connectable", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BLUETOOTH_DISCOVERY){
            if (resultCode == BLUETOOTH_DISCOVERY_DURATION){
                //Start a new server when the device's discovery has been turned on.
                new Server(this, bluetoothAdapter);
            }
        }
    }

    /**
     * Method called when clicking on "Send" button.
     * It enables Bluetooth, starts discovery of Bluetooth devices, add the result to a dialog in which you
     * can chose the device to connect to and launches the client for this device.
     */
    public void send(View view){

        //If the Bluetooth module is off, turn it on. This is possible thanks to android.permission.BLUETOOTH_ADMIN.
        //Otherwise we should have asked the user to turn it on via a Dialog.
        if (!bluetoothAdapter.isEnabled()){

            bluetoothAdapter.enable();
            turnedOnByApp = true;

        }

        //Register the BroadcastReceiver when Bluetooth devices have been found.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        //Start discovery of Bluetooth devices on network.
        bluetoothAdapter.startDiscovery();

        //Ask the user to chose which device he wants to connect to.
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                this);
        builderSingle.setTitle("Select One Device:-");
        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.select_dialog_singlechoice);

        builderSingle.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        //Create an onClickListener on an item of the list displayed.
        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //The click is received. It retrieves the clicked item and launches the client for this device.
                        //Stop the discovery of new devices on network.
                        bluetoothAdapter.cancelDiscovery();
                        String device = arrayAdapter.getItem(which);
                        dialog.dismiss();
                        String address = device.substring(device.indexOf(": ")+2);
                        new Client(Home.this, bluetoothAdapter.getRemoteDevice(address), bluetoothAdapter);

                    }
                });

        builderSingle.show();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(mReceiver);

        //If Bluetooth was not on before launching the app, stop it.
        if (turnedOnByApp) {
            bluetoothAdapter.disable();
        }

    }

}