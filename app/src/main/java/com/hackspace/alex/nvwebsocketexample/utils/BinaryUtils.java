package com.hackspace.alex.nvwebsocketexample.utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BinaryUtils {
    // packing an array of 4 bytes to an int, big endian
    public static int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static byte[] intToBytes(int integer) {
        return ByteBuffer.allocate(2).putShort((short) integer).array();
    }

    public static byte[] toPrimitives(List<Byte> byteList)
    {
        List<Byte> list = new ArrayList<>();
        list.addAll(byteList);

        Byte[] oBytes = list.toArray(new Byte[byteList.size()]);
        return toPrimitives(oBytes);
    }

    public static byte[] toPrimitives(Byte[] oBytes)
    {

        byte[] bytes = new byte[oBytes.length];
        for(int i = 0; i < oBytes.length; i++){
            bytes[i] = oBytes[i];
        }
        return bytes;

    }
}
