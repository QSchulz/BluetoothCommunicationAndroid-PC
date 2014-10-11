package com.wordpress.tricksandprojects.bluetoothcommunication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.EditText;

import java.io.IOException;
import java.util.UUID;

public class Client {

    private final Activity activity;
    private final BluetoothDevice device;
    private final BluetoothAdapter adapter;

    public Client(Activity activity, BluetoothDevice device, BluetoothAdapter adapter){

        this.activity = activity;
        this.adapter = adapter;
        this.device = device;

        new ConnectThread(device).start();

    }

    private class  ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

        public ConnectThread(BluetoothDevice device){
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }

            mmSocket = tmp;

        }

        @Override
        public void run() {
            // Cancel discovery because it will slow down the connection
            adapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            new ConnectedThread(mmSocket).start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket){
            this.mmSocket = socket;
        }

        @Override
        public void run() {

            EditText editText = (EditText)activity.findViewById(R.id.sendText);

            try {
                mmSocket.getOutputStream().write(editText.getText().toString().getBytes());
            }catch(IOException e){}

            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
