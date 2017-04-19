package com.shahidfoy.phoneactivities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shahidfoy.phoneactivities.DeviceListAdapter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothMainActivity extends MainActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "BluetoothMainActivity";
    BluetoothAdapter bluetoothAdapter;
    Button btnEnableDisable_Discoverable;

    BluetoothConnectionService mBluetoothConnection;

    Button btnStartConnection;
    Button btnSend;
    EditText chatMsg;
    TextView incomingMessages;
    StringBuilder messages;

    // phone actions
    ImageButton btnFlashAction;
    ImageButton btnBeepAction;
    ImageButton btnShakeAction;

    private Boolean isTorchOn = true;

    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    public DeviceListAdapter deviceListAdapter;
    ListView listView_NewDevices; // = (ListView) findViewById(R.id.listView_NewDevices);

    // broadcastReceiver
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "receiver: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "receiver: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "receiver: STATE TURNING ON");
                        break;
                }

            }
        }
    };

    private final BroadcastReceiver receiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "receiver2: Discoverability Enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "receiver2: Discoverability Disabled. Able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "receiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "receiver2: connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "receiver2: connected");
                        break;

                }

            }
        }
    };


    private BroadcastReceiver receiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, bluetoothDevices);
                listView_NewDevices.setAdapter(deviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver receiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // handles 3 cases:
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
                    mBTDevice = device;
                }
                if(device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING");
                }
                if(device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE");
                }
            }
        }
    };



    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(receiver2);
        unregisterReceiver(receiver3);
        unregisterReceiver(receiver4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_main);


        String receiveAction = getIntent().getAction();
        Bundle extras = getIntent().getExtras();


        Button buttonBluetooth = (Button) findViewById(R.id.buttonBlueTooth);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.buttonDiscoverable);

        listView_NewDevices = (ListView) findViewById(R.id.listView_NewDevices);

        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        chatMsg = (EditText) findViewById(R.id.editText);


        btnFlashAction = (ImageButton) findViewById(R.id.buttonMakeFlash);
        btnBeepAction = (ImageButton) findViewById(R.id.buttonMakeBeep);
        btnShakeAction = (ImageButton) findViewById(R.id.buttonMakeShake);

        incomingMessages = (TextView) findViewById(R.id.incomingMessage);
        incomingMessages.setMovementMethod(new ScrollingMovementMethod());
        messages = new StringBuilder();

        // use local broadcast manager to register broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver4, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listView_NewDevices.setOnItemClickListener(BluetoothMainActivity.this);

        buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: pressed");
                enableDisableBT();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = chatMsg.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);

                chatMsg.setText("");
            }
        });

        // CURRENT WORK AREA!!!!/////
        btnFlashAction.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View view) {
               String stringType = "flash";
               chatMsg.setText(stringType);
               byte[] bytes = chatMsg.getText().toString().getBytes(Charset.defaultCharset());
               mBluetoothConnection.write(bytes);

               chatMsg.setText("");
           }
        });

        btnBeepAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringType = "beep";
                chatMsg.setText(stringType);
                byte[] bytes = chatMsg.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);

                chatMsg.setText("");
            }
        });

        btnShakeAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringType = "shake";
                chatMsg.setText(stringType);
                byte[] bytes = chatMsg.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);

                chatMsg.setText("");
            }
        });


    } // END onCreate



    // broadcast receiver
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            messages.append(text + "\n");

            // set string builder to text view
            incomingMessages.setText(messages);

            if(text.equals("beep")) {
                Toast.makeText(BluetoothMainActivity.this, "BEEP", Toast.LENGTH_SHORT).show();
                //incomingMessages.setText("");
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150);
            }
            else if(text.equals("flash")) {
                Toast.makeText(BluetoothMainActivity.this, "FLASH", Toast.LENGTH_SHORT).show();
                //incomingMessages.setText("");
                try {
                    if(isTorchOn) {
                        turnOffFlashLight();
                        isTorchOn = false;
                    } else {
                        turnOnFlashLight();
                        isTorchOn = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(text.equals("shake")) {

                Toast.makeText(BluetoothMainActivity.this, "SHAKE", Toast.LENGTH_SHORT).show();
                //incomingMessages.setText("");
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(2000);
            }
        }
    };

    // starts connection. make sure phones are paired or connection will fail
    public void startConnection() {
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    // starts chat service
    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");

        mBluetoothConnection.startClient(device, uuid);
    }


    public void enableDisableBT() {
        if(bluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: does not have Bluetooth capabilities");

        }
        if(!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling Bluetooth");
            Intent enableBTintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTintent);

            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(receiver, BTintent);
        }
        if(bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: disabling Bluetooth");
            bluetoothAdapter.disable();

            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(receiver, BTintent);
        }
    }


    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(receiver2, intentFilter);
    }

    public void btnDiscover (View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices");

        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery");



            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver3, discoverDevicesIntent);
        }

        if(!bluetoothAdapter.isDiscovering()) {
            // ask permissions
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver3, discoverDeviceIntent);
        }
    }

    // PERMISSION CHECK
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        bluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device");
        String deviceName = bluetoothDevices.get(i).getName();
        String deviceAddress = bluetoothDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        // create bond
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            bluetoothDevices.get(i).createBond();

            // get bluetooth device
            mBTDevice = bluetoothDevices.get(i);
            // starts connection service
            mBluetoothConnection = new BluetoothConnectionService(BluetoothMainActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        //finish();
    }
}

