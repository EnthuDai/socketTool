package com.sinolab.tool.socket.util;

import java.nio.charset.Charset;
import java.util.Calendar;

public class Hex {
    /**
     * 数组转换成十六进制字符串
     * @param splitStr 分隔符
     * @param appendLast 是否在文末加上分隔符
     * @return HexString
     */
    public static String bytesToHexString(byte[] bArray, String splitStr, boolean appendLast) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
            if(i<bArray.length-1)   sb.append(splitStr);
            else if(appendLast) sb.append(splitStr);
        }
        return sb.toString();
    }

    /**
     * 16进制字符串转byte数组，字符串数组不带0x
     * @param str 字符串数组
     * @return  字符数组
     */
    public static byte[] HexStringToBytes(String str){
        byte[] bytes = new byte[str.length()/2];
        for(int i=0;i<str.length()/2;i++){
            byte high = (byte) (Character.digit(str.charAt(i*2), 16) & 0xff);
            byte low = (byte) (Character.digit(str.charAt(i*2 + 1), 16) & 0xff);
            bytes[i] = (byte) (high << 4 | low);
        }
        return bytes;
    }

    /**
     * 大于0的int转byte数组，分大小端
     * @param a 需要转换的int
     * @param isSmallEndian 是否小端在前
     * @return byte数组形式的int值
     */
    public static byte[] intToByteArray(int a ,boolean isSmallEndian){
        if(isSmallEndian) return intToByteArray(a);
        byte[] result = new byte[4];
        //低位至高位
        result[0] = (byte) ((a >> 24) & 0xFF);
        result[1] = (byte) ((a >> 16) & 0xFF);
        result[2] = (byte) ((a >> 8) & 0xFF);
        result[3] = (byte) (a & 0xFF);
        return result;
    }

    /**
     * int到byte[] 小端在前
     * @param i
     * @return
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //低位至高位
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }
    /**
     * 单个byte转为16进制字符串
     * @param value byte值
     * @return 长度为2，自动补零的16进制字符串，不含'0x'
     */
    public static String byteToHexStr(int value){
        byte b = (byte)value;
        return String.format("%02X",b);
    }

    /**
     * byte数组转int，数组长度不能大于4
     * @param bytes byte数组
     * @param beginIndex 从byte数组的哪一位开始
     * @param length 需要计算的数组长度
     * @param isSmallEndian 是否小端在前
     * @return Int
     */
    public static int byteArrayToInt(byte[] bytes,int beginIndex, int length, boolean isSmallEndian){
        length = length<4?length:4;         //如果长度超出4，则按4计算
        int value = 0;
        if(isSmallEndian){
            for(int i=beginIndex+length-1;i>=beginIndex;i--){
                value = (value << 8)|(bytes[i]&0xff);
            }
            return value;
        }
        else{
            for(int i=beginIndex;i<length+beginIndex;i++){
                value = (value << 8)|(bytes[i]&0xff);
            }
            return value;
        }
    }

    /**
     * 大于0的int转byte数组，分大小端
     * @param a 需要转换的int
     * @param isSmallEndian 是否小端在前
     * @return
     */
    public static byte[] longToByteArray(long a ,boolean isSmallEndian){
        if(isSmallEndian) return longToByteArray(a);
        byte[] result = new byte[4];
        //低位至高位
        result[0] = (byte) ((a >> 24) & 0xFF);
        result[1] = (byte) ((a >> 16) & 0xFF);
        result[2] = (byte) ((a >> 8) & 0xFF);
        result[3] = (byte) (a & 0xFF);
        return result;
    }

    /**
     * int到byte[] 小端在前
     * @param i
     * @return
     */
    public static byte[] longToByteArray(long i) {
        byte[] result = new byte[4];
        //低位至高位
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }


    public static long byteArrayToLong(byte[] bytes,int beginIndex, int length, boolean isSmallEndian){
        length = length<4?length:4;         //如果长度超出4，则按4计算
        long value = 0;
        if(isSmallEndian){
            for(int i=beginIndex+length-1;i>=beginIndex;i--){
                value = (value << 8)|(bytes[i]&0xff);
            }
            return value;
        }
        else{
            for(int i=beginIndex;i<length+beginIndex;i++){
                value = (value << 8)|(bytes[i]&0xff);
            }
            return value;
        }
    }

    /**
     * 累加校验，取低八位
     * @param bytes 源字节数组
     * @param beginIndex 从第几位开始
     * @param length    计算长度
     * @return 累加值的低八位
     */
    public static byte generateSumCheckBit(byte[] bytes, int beginIndex, int length){
        int sum = 0;
        for(int i=beginIndex; i<beginIndex+length;i++){
            sum += bytes[i]&0xFF;
        }
        return (byte)sum;
    }

    /**
     * 将两个byte数组进行或运算返回结果
     * @param bytes1 数组一
     * @param bytes2 数组二
     * @return 结果，长度与数组一 一致
     */
    public static byte[] ORArray(byte[] bytes1, byte[] bytes2){
        byte[] result = new byte[bytes1.length];
        for(int i=0;i<Math.min(bytes1.length, bytes2.length);i++){
            result[i] = (byte) (bytes1[i]|bytes2[i]);
        }
        return result;
    }
    /**
     * 以以下形式返回时间
     *      当时时间(YYYYMMDDHHMMSS)
     *      7字节
     *      yy yy mm dd hh mm ss
     * 单个字节都是ascall码形式的byte
     * @return 时间字节数组
     */
    public static byte[] getCurrentDateTimeBytes(){
        Calendar calendar = Calendar.getInstance();
        byte[] datetime = new byte[7];
        datetime[0] = (byte)(calendar.get(Calendar.YEAR) / 100);//设置时间
        datetime[1] = (byte)(calendar.get(Calendar.YEAR) % 100);
        datetime[2] = (byte)(calendar.get(Calendar.MONTH) + 1);
        datetime[3] = (byte)(calendar.get(Calendar.DAY_OF_MONTH));
        datetime[4] = (byte)(calendar.get(Calendar.HOUR_OF_DAY));
        datetime[5] = (byte)(calendar.get(Calendar.MINUTE));
        datetime[6] = (byte)(calendar.get(Calendar.SECOND));
        return datetime;
    }

    /**
     * 截取byte数组里的一部分
     * @param bytes 源数组
     * @param startIndex 起始位
     * @param length 需要截取的长度
     * @return 截取出来的数组
     */
    public static byte[] split(byte[] bytes, int startIndex, int length){
        byte[] result = new byte[length];
        System.arraycopy(bytes, startIndex, result, 0, length);
        return result;
    }

    /**
     * 将中文字符串转gb2312字符数组
     * @param str 中文
     * @param targetLength 返回目标数组的长度
     * @return 字符数组
     */
    public static byte[] stringToGB2312(String str, int targetLength){
        byte[] emptyBytes = new byte[targetLength];
        byte[] gb2312s = str.getBytes(Charset.forName("GB2312"));
        for(int i =0;i<gb2312s.length;i+=2){
            if(gb2312s[i] != 0x00){
                gb2312s = split(gb2312s, i, gb2312s.length -i);
                break;
            }
        }
        return Hex.ORArray(emptyBytes, gb2312s);
    }

    /**
     * 累加求和
     * @param bytes
     * @return
     */
    public static int  checkSum(byte[] bytes){
        return checkSum(bytes,0,bytes.length);
    }

    /**
     * 累加求和
     * @param bytes
     * @param startIndex
     * @param length
     * @return
     */
    public static int checkSum(byte[] bytes, int startIndex, int length){
        int sum = 0;
        for(int i=0;i<length;i++){
            sum+=bytes[startIndex+i]&0xFF;
        }
        return sum;
    }

    public static byte[] strToAsciiByteArray(String str, int length){
        byte[] result = new byte[length];
        for(int i=0;i<length;i++){
            try{
                result[i] = (byte)(48 + (str.charAt(i) - '0'));
            }catch (Exception e){
                e.printStackTrace();
                result[i] = 0x00;
            }
        }
        return result;
    }
}
