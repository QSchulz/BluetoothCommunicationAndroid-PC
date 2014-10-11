package com.wordpress.tricksandprojects.bluetoothcommunication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Server {

    private final BluetoothAdapter bluetoothAdapter;
    private final Activity activity;
    protected final TextView receivedText;

    public Server(Activity activity, BluetoothAdapter adapter) {

        bluetoothAdapter = adapter;
        this.activity = activity;
        receivedText = (TextView) this.activity.findViewById(R.id.receivedText);
        new AcceptThread().start();

    }

    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;

            try {

                // MY_UUID is the app's UUID string, also used by the client code
                UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
                //Start new server.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothCommunicationServer", MY_UUID);

            } catch (IOException e) { }

            mmServerSocket = tmp;

        }

        public void run() {

            BluetoothSocket socket = null;

            // Keep listening until exception occurs or a socket is returned
            while (true) {

                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    new ClientConnectedThread(socket).start();

                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                }

            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {

            try {
                mmServerSocket.close();
            } catch (IOException e) { }

        }
    }

    private class ClientConnectedThread extends Thread {

        private final BluetoothSocket mSocket;
        private final InputStream mInput;

        public ClientConnectedThread(BluetoothSocket socket){

            mSocket = socket;
            InputStream tmpInputStream = null;

            try{
                tmpInputStream = mSocket.getInputStream();
            }catch (IOException e){}

            mInput = tmpInputStream;

        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            String receivedMessage = "";

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try{

                    bytes = mInput.read(buffer);
                    receivedMessage += new String(buffer, "UTF-8").substring(0,bytes);

                }catch (IOException e){break;}

                //Update the textView with the received text.
                activity.runOnUiThread(new TextViewRunnable(receivedText, receivedMessage));
            }

            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
