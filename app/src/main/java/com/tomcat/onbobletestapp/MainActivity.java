package com.tomcat.onbobletestapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String MLC_BP = "3MW1-4B";
    private static final long   SCAN_PERIOD = 10000;    //10s


    private LeDeviceListAdapter mLeDevicesListAdapter;
    private BluetoothAdapter    mBluetoothAdapter;
    private BluetoothLeService  mBluetoothLeService;
    private boolean             mScanning;
    private Handler             mHandler;

    private TextView    mTextView;
    private ListView    mListView;
    private Button      mTestButton;

    private boolean     mConnected = false;
    private boolean     SearchBLE = false;
    private boolean     BLUETOOTH_RECONNECT = false;


    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {

        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {

        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                //Toast.makeText(this, "GATT Connected.", Toast.LENGTH_SHORT).show();
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                displayGattDevice(mBluetoothLeService.getSupportedGattServices());
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, R.string.ble_not_support, Toast.LENGTH_SHORT).show();
            finish();
        }

        Intent gettServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gettServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        startService(gettServiceIntent);

        final BluetoothManager  bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            Toast.makeText(this, R.string.error_bluetooth_not_support, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
            {}
            else
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
        }

        mTextView = (TextView)findViewById(R.id.Devices);
        mTextView.setText("");
        mTextView.willNotDraw();
        mTestButton = (Button)findViewById(R.id.testbutton);
        mTestButton.setText("SCAN");
        mListView = (ListView)findViewById(R.id.listView);


    }

    protected void onResume()
    {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled())     // Does check phone BT enable ?
        {
            if(!mBluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
        registerReceiver(m)
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if((requestCode == RESULT_OK) && (resultCode == Activity.RESULT_CANCELED))
        {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void updateConnectionState(final int resourceId)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mTextView.setText(resourceId);
            }
        });
    }

    //get BLE device GATT profile.
    private void displayGattDevice(List<BluetoothGattService> gattServices)
    {
        String address = deviceAddress.replaceAll(":", "");

        if (deviceName == null)
        {
            deviceName = "Unknow Device";
        }

        if (!SearchBLE && (!BLUETOOTH_RECONNECT))
        {
            mLeDevicesListAdapter.notifyDataSetChanged();
        }
    }


    private void setmTestButtonClick()
    {
        //mTestButton.setText("STOP");
        mTestButton.setVisibility(View.GONE);
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable)
    {
        if (enable)
        {
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    private class LeDeviceListAdapter extends BaseAdapter
    {
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<String>   mMacAddress;
        private ArrayList<String>   mRSSI;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter()        //constructor
        {
            super();
            mLeDevices  = new ArrayList<>();
            //mMacAddress = new ArrayList<String>();
            //mRSSI       = new ArrayList<String>();
            mInflator   = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device)
        {
            if (!mLeDevices.contains(device))
            {
                mLeDevices.add(device);
                //mMacAddress.add(MacAddress);
                //mRSSI.add(RSSI);
            }
        }

        public BluetoothDevice getDevice(int position)
        {
            return mLeDevices.get(position);
        }

        public void clear()
        {
            mLeDevices.clear();
            //mMacAddress.clear();
            //mRSSI.clear();
        }

        @Override
        public int getCount()
        {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i)
        {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i)
        {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            ViewHolder  viewHolder;

            if (view == null)
            {
                //view = (ListView) findViewById(R.id.listView);
                view = mInflator.inflate(R.layout.activity_main, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(R.id.listView);
                //viewHolder.deviceAddress = (TextView) view.findViewById(R.id.listView);
                //viewHolder.deviceRSSI = (TextView) view.findViewById(R.id.listView);
                view.setTag(viewHolder);
                //view = getLayoutInflater().inflate(R.layout.activity_main, null);
            }
            else
            {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String    deviceName = device.getName();
            if ((deviceName != null) && (deviceName.length() > 0))
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    //if(device.getName().equalsIgnoreCase(MLC_BP))
                    if (device.getName().equals(MLC_BP))
                    {
                        mLeDevicesListAdapter.addDevice(device);
                        mLeDevicesListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    static class ViewHolder
    {
        TextView    deviceName;
        TextView    deviceAddress;
        TextView    deviceRSSI;
    }
}
