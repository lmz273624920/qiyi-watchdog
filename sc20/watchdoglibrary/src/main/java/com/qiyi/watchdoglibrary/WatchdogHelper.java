package com.qiyi.watchdoglibrary;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class WatchdogHelper {
    private String TAG = "WatchdogHelper";
    private static final WatchdogHelper ourInstance = new WatchdogHelper();

    public static WatchdogHelper getInstance() {
        return ourInstance;
    }

    private WatchdogHelper() {
    }

    private SerialPort mSerialPort;
    private boolean isOpen;

    public WatchdogHelper init() {
        try {
            mSerialPort = new SerialPort(new File("/dev/ttyHSL1"), 115200, 0);
            isOpen = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "qiyi watchdog init error!");
        }
        return ourInstance;
    }

    public void openSend() {
        new SerialPortSendThread().start();
    }

    public void openRead() {
        new SerialPortReadThread().start();
    }

    public void close() {
        isOpen = false;
        if (mSerialPort!=null) {
            mSerialPort.close();
        }
    }

    private class SerialPortSendThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isOpen) {
//                byte[] buff = {0x55, (byte) 0xaa, 0x00, 0x02, 0x02, 0x01, (byte) 0x84, 0x5A, (byte) 0xA5};
                byte[] buff = {0x55, (byte) 0xAA, 0x00, 0x04, 0x01, 0x00, 30, 0x00, (byte) 0xF3, 0x5A, (byte) 0xA5};
                try {
                    OutputStream outputStream = mSerialPort.getOutputStream();
                    outputStream.write(buff);
                    outputStream.flush();
                    Log.d(TAG, "qiyi watchdog send");
                    try {
                        sleep(60000 * 2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "qiyi watchdog send error!");
                }
            }
        }
    }

    private class SerialPortReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            byte[] readBuff = new byte[16];
            while (isOpen) {
                try {
                    int size = mSerialPort.getInputStream().read(readBuff);
                    Log.d(TAG, "qiyi watchdog read size:"+size);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "qiyi watchdog read error!");
                }
            }
        }
    }
}
