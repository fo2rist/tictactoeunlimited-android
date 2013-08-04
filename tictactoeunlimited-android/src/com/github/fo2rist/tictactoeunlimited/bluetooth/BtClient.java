package com.github.fo2rist.tictactoeunlimited.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.github.fo2rist.tictactoeunlimited.bluetooth.BtUtils.BtEventsListener;

/**
 * Client connection.
 * Launches thread for connection.
 * Allows to reconnect after disconnect, but only one connection may be active at time.
 * {@link #disconnect()} must be called explicitly to start next connections
 */
public class BtClient {
	
	private class ClientLooperThread extends Thread {
		private static final int MSG_CONNECT = 1;
		private static final int MSG_DISCONNECT = 2;
		private static final int MSG_SEND = 3;
		
		private String address_;
		public Handler handler_;
		
		public ClientLooperThread(String address) {
			super("TicTacToe BT client connection");
			this.address_ = address;
		}
				
		public void disconnect() {
			handler_.sendEmptyMessage(MSG_DISCONNECT);
		}
		
		public void send(String data) {
			Message message = handler_.obtainMessage(MSG_SEND, data);
			handler_.sendMessage(message);
		}

		@SuppressLint("HandlerLeak")//We clean pointer to handler
		@Override
		public void run() {
			Looper.prepare();

			handler_ = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case MSG_CONNECT:
						performConnect();
						break;
					case MSG_DISCONNECT:
						performDisconnect();
						Looper.myLooper().quit(); //Stop message processing and exit loop	
						break;
					case MSG_SEND:
						performSend((String) msg.obj);
						break;
					default:
						super.dispatchMessage(msg);
						break;
					}
				}
			};

			//Initialize message queue by CONNECT command before start loop
			handler_.sendEmptyMessage(MSG_CONNECT);
			
			Looper.loop();
			
			//When loop finished clean handler link
			handler_ = null;
		}
		
		private void performConnect() {
			// Set up a pointer to the remote node using it's address.
			BluetoothDevice device = btAdapter_.getRemoteDevice(address_);

			// Two things are needed to make a connection:
			// A MAC address, which we got above.
			// A Service ID or UUID. In this case we are using the
			// UUID for SPP.
			try {
				btSocket_ = device.createRfcommSocketToServiceRecord(BtUtils.MY_UUID);
			} catch (IOException e) {
				listener_.notifyAboutErrorOccured("Socket create failed: " + e.getMessage() + ".");
			}

			// Discovery is resource intensive. Make sure it isn't going on
			// when you attempt to connect and pass your message.
			btAdapter_.cancelDiscovery();

			// Establish the connection. This will block until it connects.
			try {
				btSocket_.connect();
				listener_.notifyAboutConnect();
				send("Test");
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					btSocket_.close();
				} catch (IOException closeException) {
				}
				listener_.notifyAboutErrorOccured("Unable to connect: " + connectException.getMessage() + ".");
			}
		}
		
		private void performDisconnect() {
			try {
				btSocket_.close();
				listener_.notifyAboutDisconnect();
			} catch (IOException e) {
				listener_.notifyAboutErrorOccured("Unable to disconnect: " + e.getMessage() + ".");
			}
		}
		
		private void performSend(String data) {
			// Create a data stream so we can talk to server.
			try {
				outStream_ = btSocket_.getOutputStream();
				inStream_ = btSocket_.getInputStream();
			} catch (IOException e) {
				listener_.notifyAboutErrorOccured("Output stream creation failed:" + e.getMessage() + ".");
			}

			//Send data
			byte[] msgBuffer = data.getBytes(Charset.forName(BtUtils.UTF_8));
			try {
				outStream_.write(msgBuffer);
			} catch (IOException e) {
				listener_.notifyAboutErrorOccured("Exception occurred during write: " + e.getMessage());
			}
			if (outStream_ != null) {
				try {
					outStream_.flush();
					listener_.notifyAboutDataSent();
				} catch (IOException e) {
					listener_.notifyAboutErrorOccured("failed to flush output stream: " + e.getMessage() + ".");
				}
			}
			
			//Read response
			final byte[] buffer = new byte[1024];
			try {
				inStream_.read(buffer);
				listener_.notifyAboutDataReceived(new String(buffer, BtUtils.UTF_8));
			} catch (IOException e) {
				listener_.notifyAboutErrorOccured("Exception occurred during read: " + e.getMessage());
			}
		}
	}
	
	private final BtEventsListener listener_;
	
	private ClientLooperThread connectionThread_ = null;
	
	private BluetoothAdapter btAdapter_ = null;
	private BluetoothSocket btSocket_ = null;
	private OutputStream outStream_ = null;
	private InputStream inStream_ = null;
	
	public BtClient(BtEventsListener listener) throws IllegalStateException {
		listener_ = listener;
		
		btAdapter_ = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter_ == null) {
			throw new IllegalStateException("No bt adapter present");
		}
	}
	
	
	public void startConnection(String address) {
		if (connectionThread_ == null) {
			connectionThread_ = new ClientLooperThread(address);
			connectionThread_.start();
		}
	}
	
	public void disconnect() {
		connectionThread_.disconnect();
		connectionThread_ = null;
	}
	
	public void send(String message) {
		connectionThread_.send(message);
	}
}
