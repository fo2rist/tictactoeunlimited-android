package com.github.fo2rist.tictactoeunlimited;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.github.fo2rist.tictactoeunlimited.BtUtils.BtEventsListener;

public class BtServer extends Thread {

	private BluetoothAdapter btAdapter = null;
	private final BluetoothServerSocket serverSocket_;
	private final BtEventsListener listener_;

	public BtServer(BtEventsListener listener) throws IllegalStateException {
		super("TicTacToe BT server");
		listener_ = listener;

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			throw new IllegalStateException("No bt adapter present");
		}
		BluetoothServerSocket tmpServerSocket = null;
		try {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			tmpServerSocket = btAdapter.listenUsingRfcommWithServiceRecord("Dima", BtUtils.MY_UUID);
		} catch (IOException e) {
			// Don't care about particular error
		}
		serverSocket_ = tmpServerSocket;
		if (serverSocket_ == null) {
			throw new IllegalStateException("Unable to open Bluetooth server socket");
		}
	}

	@Override
	public void run() {
		if (serverSocket_ == null) {
			return;
		}
		BluetoothSocket socket = null;
		// Keep listening until exception occurs or a socket is returned
		try {
			socket = serverSocket_.accept();
		} catch (IOException e) {
			return;
		}
		// If a connection was accepted
		if (socket != null) {
			// Do work to manage the connection (in a separate thread)
			try {
				listener_.notifyAboutConnect();
				manageConnectedSocket(socket);
				serverSocket_.close();
			} catch (IOException e) {
				listener_.notifyAboutErrorOccured(e.getLocalizedMessage());
			}
			return;
		}
	}
 
    private void manageConnectedSocket(BluetoothSocket socket) throws IOException {
		InputStream inputStream = socket.getInputStream();
		OutputStream outputStream = socket.getOutputStream();
		while (true) {
			final byte[] buffer = new byte[1024];
			inputStream.read(buffer);
			outputStream.write("UNDSTD".getBytes());
			
			listener_.notifyAboutDataReceived(new String(buffer, BtUtils.UTF_8));
		}
	}

	/** Will cancel the listening socket, and cause the thread to finish */
	public void cancel() {
		try {
			serverSocket_.close();
		} catch (IOException e) {
		}
	}
}
