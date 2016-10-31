package com.yanming.support;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by allan on 16/10/18.
 */
public final class BufferUtils {

    private final static ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

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

    public static String readFully(ByteBuf buffer) {
        return buffer.toString(Charset.forName("UTF-8"));
    }

    public static byte[] readEncodedLenBytes(ByteBuf buffer) {
        int start = buffer.readerIndex();
        buffer.markReaderIndex();//标记初始位置
        int len = (int) readEncodedLenInt(buffer);
        int lenSize = buffer.readerIndex() - start;//获取长度占用字节数
        byte[] data = new byte[len + lenSize];
        buffer.resetReaderIndex();
        buffer.readBytes(data);
        return data;
    }

    public static void writeLenString(ByteBuf packet, String str) {
        byte[] data = toBytes(str);
        writeLenBytes(packet, data);
    }

    public static void writeLenBytes(ByteBuf packet, byte[] data) {
        writeEncodedLenInt(packet, data.length);
        packet.writeBytes(data);
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

    public static ByteBuf wrapString(String text) {
        ByteBuf data = allocator.buffer();
        data.writeCharSequence(text, Charset.forName("UTF-8"));
        return data;
    }

    public static ByteBuf newBuffer() {
        return allocator.buffer();
    }

    public static boolean isEOF(short b) {
        return b == 0xfe;
    }

    public static boolean isEOFPacket(ByteBuf in) {
        return in.getUnsignedByte(in.readerIndex() + 4) == 0xFE;
    }

    public static boolean isOKPacket(ByteBuf in) {
        return in.getUnsignedByte(in.readerIndex() + 4) == 0x00;
    }

    public static boolean isErrorPacket(ByteBuf in) {
        return in.getUnsignedByte(in.readerIndex() + 5) == 0xFF;
    }

    public static byte getByte(byte[] memory, int index) {
        return memory[index];
    }

    public static int getUnsignedByte(byte[] memory, int index) {
        return memory[index] & 0xff;
    }

    public static short getShortLE(byte[] memory, int index) {
        return (short) (memory[index] & 0xff | memory[index + 1] << 8);
    }

    public static int getUnsignedShortLE(byte[] memory, int index) {
        return getShortLE(memory, index) & 0xFFFF;
    }

    public static int getIntLE(byte[] memory, int index) {
        return memory[index] & 0xff |
                (memory[index + 1] & 0xff) << 8 |
                (memory[index + 2] & 0xff) << 16 |
                (memory[index + 3] & 0xff) << 24;
    }

    public static long getUnsignedIntLE(byte[] memory, int index) {
        return getIntLE(memory, index) & 0xFFFFFFFFL;
    }

}
