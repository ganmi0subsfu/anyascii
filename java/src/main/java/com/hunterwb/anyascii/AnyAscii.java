package com.hunterwb.anyascii;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class AnyAscii {

    private AnyAscii() {}

    private static final String[][][] planes = new String[3][][];

    public static String transliterate(String s) {
        if (isAscii(s)) return s;
        StringBuilder sb = new StringBuilder(s.length());
        transliterate(s, sb);
        return sb.toString();
    }

    public static boolean isAscii(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isAscii(s.charAt(i))) return false;
        }
        return true;
    }

    public static boolean isAscii(char c) {
        return isAscii((int) c);
    }

    public static boolean isAscii(int codePoint) {
        return (codePoint >>> 7) == 0;
    }

    public static void transliterate(String s, StringBuilder dst) {
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            transliterate(cp, dst);
            i += Character.charCount(cp);
        }
    }

    public static void transliterate(CharSequence s, StringBuilder dst) {
        for (int i = 0; i < s.length();) {
            int cp = Character.codePointAt(s, i);
            transliterate(cp, dst);
            i += Character.charCount(cp);
        }
    }

    public static void transliterate(int codePoint, StringBuilder dst) {
        if (isAscii(codePoint)) {
            dst.append((char) codePoint);
            return;
        }
        int planeNum = codePoint >>> 16;
        switch (planeNum) {
            case 0x0:
            case 0x1:
            case 0x2:
                transliteratePlane(planeNum, codePoint, dst);
                break;
            case 0xE:
                transliteratePlaneE(codePoint, dst);
                break;
        }
    }

    private static void transliteratePlane(int planeNum, int codePoint, StringBuilder dst) {
        String[][] plane = planes[planeNum];
        if (plane == null) plane = planes[planeNum] = new String[256][];
        int blockNum = (codePoint >>> 8) & 0xFF;
        String[] block = plane[blockNum];
        if (block == null) block = loadBlock(planeNum, blockNum);
        int lo = codePoint & 0xFF;
        if (block.length <= lo) return;
        String s = block[lo];
        if (s == null) return;
        dst.append(s);
    }

    private static void transliteratePlaneE(int codePoint, StringBuilder dst) {
        int c = codePoint & 0xFFFF;
        if (c >= 0x20 && c <= 0x7E) {
            dst.append((char) c);
        }
    }

    private static String[] loadBlock(int planeNum, int blockNum) {
        InputStream input = AnyAscii.class.getResourceAsStream(String.format("%03x", (planeNum << 16) + blockNum));
        String[] block;
        if (input == null) {
            block = new String[0];
        } else {
            try {
                try {
                    block = split(new BufferedInputStream(input, 1024));
                } finally {
                    input.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return planes[planeNum][blockNum] = block;
    }

    @SuppressWarnings("deprecation")
    private static String[] split(InputStream input) throws IOException {
        String[] block = new String[256];
        int blockLen = 0;
        byte[] buf = new byte[4];
        int bufLen = 0;
        int b;
        while ((b = input.read()) != -1) {
            if (b == 0) {
                if (bufLen != 0) {
                    block[blockLen] = new String(buf, 0, 0, bufLen).intern();
                    bufLen = 0;
                }
                blockLen++;
            } else {
                if (bufLen == buf.length) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[bufLen++] = (byte) b;
            }
        }
        if (blockLen < 256) block = Arrays.copyOf(block, blockLen);
        return block;
    }
}