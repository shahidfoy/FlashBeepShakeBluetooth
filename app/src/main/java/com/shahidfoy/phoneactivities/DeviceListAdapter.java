package com.shahidfoy.phoneactivities;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by shahi on 4/15/2017.
 */

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater layoutInflater;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private int viewResourceId;

    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices) {
        super(context, tvResourceId, devices);
        this.bluetoothDevices = devices;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewResourceId = tvResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(viewResourceId, null);
        BluetoothDevice device = bluetoothDevices.get(position);

        if(device != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAddress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);

            if(deviceName != null) {
                deviceName.setText(device.getName());

            }
            if(deviceAddress != null) {
                deviceAddress.setText(device.getAddress());
            }
        }
        return convertView;
    }
}
