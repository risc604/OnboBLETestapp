package com.tomcat.onbobletestapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by tomcat on 2016/1/30.
 */
public class BluetoothLeService extends Service
{
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager    mBluetoothManager;
    private BluetoothAdapter    mBluetoothAdapter;
    private String              mBluetoothDeviceAddress;
    private BluetoothGatt       mBluetoothGatt;
    private int                 mConnectionState = STATE_DISCONNECTED;

    private static final int    STATE_DISCONNECTED = 0;
    private static final int    STATE_CONNECTING   = 1;
    private static final int    STATE_CONNECTED    = 2;

    public final static String  ACTION_GATT_CONNECTED = "com.tomcat.onbobletestapp.le.ACTION_GATT_CONNECTED";
    public final static String  ACTION_GATT_DISCONNECTED = "com.tomcat.onbobletestapp.le.ACTION_GATT_DISCONNECTED";
    public final static String  ACTION_GATT_SERVICES_DISCOVERED = "com.tomcat.onbobletestapp.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String  ACTION_DATA_AVAILABLE = "com.tomcat.onbobletestapp.le.ACTION_DATA_AVAILABLE";
    public final static String  EXTRA_DATA = "com.tomcat.onbobletestapp.le.ACTION_EXTRA_DATA";

    public final static UUID    UUID_HEART_RATE_MEASUREMENT = UUID.fromString(BLEGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID    UUID_MLC_BLE_SERVICE        = UUID.fromString(BLEGattAttributes.MLC_BLE_SERVICE);
    public final static UUID    UUID_MLC_BLE_WRITE          = UUID.fromString(BLEGattAttributes.MLC_BLE_WRTE);
    public final static UUID    UUID_MLC_BLE_READ           = UUID.fromString(BLEGattAttributes.MLC_BLE_READ);

    private BluetoothLeScanner  mBluetoothLeScanner;

    private ScanSettings        scanSettings;
    List<ScanFilter>            filters;
    
    public String               mBluetoothGattAdress;
    public boolean              mBluetoothGattServiceDiscover;
    public boolean              mBluetoothGattConnected;

    private final BluetoothGattCallback mGatCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            //super.onConnectionStateChange(gatt, status, newState);
            String  intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTING)
            {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            switch (status)
            {
                case BluetoothGatt.GATT_SUCCESS:
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    break;

                case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
                    broadcastUpdate("GATT_INVALID_ATTRIBUTE_LENGTH");
                    break;

                default:
                    Log.w(TAG, "onServiceDiscovered received: " + status);
                    break;
            }
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            switch (status)
            {
                case BluetoothGatt.GATT_SUCCESS:
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    break;

                default:
                    Log.w(TAG, "onCharacteristicRead received: " + status);
                    break;
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            switch (status)
            {
                case BluetoothGatt.GATT_SUCCESS:
                    //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    break;

                default:
                    Log.w(TAG, "onCharacteristicWrite received: " + status);
                    break;
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action)
    {
        final Intent    intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
    {
        final Intent    intent = new Intent(action);

        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()))
        {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0)
            {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else
            {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }
        else
        {
            final byte[]    data = characteristic.getValue();
            if ((data != null) && (data.length > 0))
            {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder
    {
        BluetoothLeService getService()
        {
            return BluetoothLeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public boolean onUnbind(Intent intent)
    {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize()
    {
        if (mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
            {
                Log.e(TAG, "Unable to obtain a BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return  true;
    }

    //GATT server hosted on BLE device.
    public boolean connect(final String address)
    {
        if ((mBluetoothAdapter == null) || (address == null))
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (    (mBluetoothDeviceAddress != null) &&
                address.equals(mBluetoothDeviceAddress) &&
                (mBluetoothGatt != null))
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect())
            {
                mConnectionState = STATE_CONNECTING;
                return true;
            }
            else
            {
                return false;
            }
        }

        final BluetoothDevice   device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null)
        {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, false, mGatCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return  true;
    }

    public void disconnect()
    {
        if ((mBluetoothAdapter == null) || (mBluetoothGatt == null) )
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close()
    {
        if (mBluetoothGatt == null)
        {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if ((mBluetoothAdapter == null) || (mBluetoothGatt == null))
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if ((mBluetoothAdapter == null) || (mBluetoothGatt == null))
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable)
    {
        if ((mBluetoothAdapter == null) || (mBluetoothGatt == null))
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()))
        {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        if (UUID_MLC_BLE_WRITE.equals(characteristic.getUuid()))
        {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

    }

    public List<BluetoothGattService> getSupportedGattServices()
    {
        if (mBluetoothGatt == null)     return null;
        return mBluetoothGatt.getServices();
    }
}


