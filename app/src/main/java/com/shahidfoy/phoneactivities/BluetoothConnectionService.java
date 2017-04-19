package com.shahidfoy.phoneactivities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by shahi on 4/15/2017.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "CHATTER";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;

    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }


    // listens for incoming connections. behaves like a server-side client.
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // creates new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.d(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            mmServerSocket = tmp;

        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try {
                // will return on a successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start....");

                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection");
            } catch (IOException e) {
                Log.d(TAG, "run: IOException: " + e.getMessage());
            }


            if(socket != null) {
                connected(socket, mmDevice);
            }

            Log.i(TAG, "END mAcceptThread");

        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread");

            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }

    }



    // runs thread while accepting to make an outgoing connection
    // with a device. the connection will either succeed or fail.
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread");

            // get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e){
                Log.d(TAG, "ConnectThread: Could not create InsecureRfcommSocket: " + e.getMessage());
            }

            mmSocket = tmp;

            // cancels discovery IMPORTANT
            mBluetoothAdapter.cancelDiscovery();

            // connect to BluetoothSocket

            // blocking call will only return on a
            // successful connection or an exception
            try {
                Log.d(TAG, "run: ConnectThread connected");
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    Log.d(TAG, "run: Closed Socket");
                    mmSocket.close();
                } catch (IOException e1) {
                    Log.d(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
            }


            connected(mmSocket, mmDevice);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }


    // start chat service. starts AcceptThread to begain a
    // session in listening (server) mode. called by Activity onResume()
    public synchronized void start() {
        Log.d(TAG, "start");

        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    // AcceptThread starts and sits waiting for a connection
    // ConnectThread starts and attempts to make a connection with
    // other divices AcceptThread.
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started.");

        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection established
            try {
                mProgressDialog.dismiss();
            } catch(NullPointerException e) {
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()

            // keep listening to the InputStream until an exception occurs
            while(true) {
                // read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    // important info for phone activites app
                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage" , incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

                } catch (IOException e) {
                    Log.d(TAG, "write: Error reading inputStream: " + e.getMessage());
                    break;
                }
            }
        }

        // call this from main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.d(TAG, "write: Error writing to outputstream: " + e.getMessage());
            }
        }

        // call this from main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {

            }
        }
    }


    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting");

        // start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    // write to the ConnectedThread in an unsynchronized manner
    public void write(byte[] out) {
        ConnectedThread r;

        // synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called");
        // write
        mConnectedThread.write(out);
    }
}
