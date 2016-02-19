package com.tomcat.onbobletestapp;

import java.util.HashMap;

/**
 * Created by tomcat on 2016/2/2.
 */
public class BLEGattAttributes
{
    private static HashMap<String, String>  attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT       = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String MLC_BLE_SERVICE   = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String MLC_BLE_WRTE      = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static String MLC_BLE_READ      = "0000fff2-0000-1000-8000-00805f9b34fb";
    public static String MLC_BLE_UPDATE_FW = "0000fff3-0000-1000-8000-00805f9b34fb";
    public static String MLC_BLE_XXX1      = "0000fff4-0000-1000-8000-00805f9b34fb";
    public static String MLC_BLE_XXX2      = "0000fff5-0000-1000-8000-00805f9b34fb";

    static
    {
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(MLC_BLE_SERVICE, "Microlife BLE Device Service");

        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(MLC_BLE_WRTE, "Microlife BLE Device Write");
        attributes.put(MLC_BLE_READ, "Microlife BLE Device Read");
    }

    public static String lookup(String uuid, String defaultName)
    {
        String  name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
