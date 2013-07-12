package com.github.fo2rist.tictactoeunlimited;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

public class BtServerThread extends Thread {
		
	private BluetoothAdapter btAdapter = null;
    private final BluetoothServerSocket serverSocket_;
 
    public BtServerThread() {
    	btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothServerSocket tmpServerSocket = null;
        try {
        	// Use a temporary object that is later assigned to mmServerSocket,
        	// because mmServerSocket is final
            tmpServerSocket = btAdapter.listenUsingRfcommWithServiceRecord("Dima", MY_UUID);
        } catch (IOException e) {
        	//Don't care about particular error
        }
        serverSocket_ = tmpServerSocket;
        if (serverSocket_ == null) {
        	Toast.makeText(MainActivity.this, "No server socket", 0).show();
        }
    }
 
    @Override
	public void run() {
    	if (serverSocket_ == null) {
        	return;
        }
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = serverSocket_.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                try {
                	manageConnectedSocket(socket);
					serverSocket_.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
                break;
            }
        }
    }
 
    private void manageConnectedSocket(BluetoothSocket socket) throws IOException {
		InputStream inputStream = socket.getInputStream();
		OutputStream outputStream = socket.getOutputStream();
		while (true) {
			final byte[] buffer = new byte[1024];
			inputStream.read(buffer);
			outputStream.write(buffer);
			MainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this, buffer.toString(), 0).show();
				}
			});
		}
	}

	/** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            serverSocket_.close();
        } catch (IOException e) { }
    }
}
