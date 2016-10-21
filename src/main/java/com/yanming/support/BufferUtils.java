package com.yanming.support;

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;

import java.io.UnsupportedEncodingException;

/**
 * Created by allan on 16/10/18.
 */
public final class BufferUtils {

    private BufferUtils() {

    }

    public static long readEncodedLenInt(ByteBuf buffer) {
        int firstByte = buffer.readUnsignedByte();
        return readEncodedLenInt(buffer, firstByte);
    }

    public static long readEncodedLenInt(ByteBuf buffer, int firstByte) {
        if (firstByte < 251) {
            return firstByte;
        } else if (firstByte == 0xfc) {
            return buffer.readUnsignedShortLE();//2个字节
        } else if (firstByte == 0xfd) {
            return buffer.readUnsignedMediumLE();//3个字节
        } else if (firstByte == 0xfe) {//8个字节
            return buffer.readLongLE();
        }
        return -1;
    }

    public static String readEncodedLenString(ByteBuf buffer) {
        int len = (int) readEncodedLenInt(buffer);
        byte[] data = new byte[len];
        buffer.readBytes(data);
        try {
            return new String(data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("not support utf-8");
        }
    }

    public static void readNullString(ByteBuf from, ByteBuf to) {
        int len = from.forEachByte(ByteProcessor.FIND_NUL) - from.readerIndex();
        to.ensureWritable(len);
        from.readBytes(to, len);
        from.skipBytes(1);
    }

    public static void writeEncodedLenInt(ByteBuf buffer, long length) {
        if (length < 251) {
            buffer.writeByte((byte) length);
        } else if (length < 65536L) {//2个字节
            buffer.writeByte((byte) 252);
            buffer.writeShortLE((int) length);
        } else if (length < 16777216L) {//3个字节
            buffer.writeByte((byte) 253);
            buffer.writeMediumLE((int) length);
        } else {//8个字节
            buffer.writeByte((byte) 254);
            buffer.writeLongLE(length);
        }
    }

    public static byte[] toBytes(String str, String encoding) {
        try {
            return str.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("not support" + encoding);
        }
    }

    public static byte[] toBytes(String str) {
        return toBytes(str, "UTF-8");
    }

    public static String toString(byte[] b) {
        return toString(b, "UTF-8");
    }

    public static String toString(byte[] b, String encoding) {
        try {
            return new String(b, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("not support" + encoding);
        }
    }

    public static boolean isEOF(short b){
        return b==0xfe;
    }

    public static boolean isEOFPacket(ByteBuf in){
        return in.getUnsignedByte(in.readerIndex()+5)==0xFE;
    }
    public static boolean isOKPacket(ByteBuf in){
        return in.getUnsignedByte(in.readerIndex()+5)==0x00;
    }

    public static boolean isErrorPacket(ByteBuf in){
        return in.getUnsignedByte(in.readerIndex()+5)==0xFF;
    }
}
