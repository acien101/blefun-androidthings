package com.nilhcem.blefun.common;

import java.nio.charset.Charset;
import java.util.UUID;

public class AwesomenessProfile {

    // UUID for the UART BTLE client characteristic which is necessary for notifications.
    public static UUID DESCRIPTOR_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static UUID DESCRIPTOR_USER_DESC = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");

    public static UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID CHARACTERISTIC_COUNTER_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID CHARACTERISTIC_INTERACTOR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    public static byte[] getUserDescription(UUID characteristicUUID) {
        String desc;

        if (CHARACTERISTIC_COUNTER_UUID.equals(characteristicUUID)) {
            desc = "Indicates the number of time you have been awesome so far";
        } else if (CHARACTERISTIC_INTERACTOR_UUID.equals(characteristicUUID)) {
            desc = "Write any value here to move the cat’s paw and increment the awesomeness counter";
        } else {
            desc = "";
        }

        return desc.getBytes(Charset.forName("UTF-8"));
    }
}
