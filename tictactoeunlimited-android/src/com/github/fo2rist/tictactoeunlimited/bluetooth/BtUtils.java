package com.github.fo2rist.tictactoeunlimited.bluetooth;

import java.util.Set;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

public final class BtUtils {
	/** Default transmission encoding */
	public static final String UTF_8 = "utf-8";
	
	public interface OnDeviceSelectesListener {
		public void onDeviceSelected(String address, String name);
	}
	
	public static class BtEventsListener extends Handler {
		private static final int MSG_CONNECTED = 1;
		private static final int MSG_DISCONNECTED = 2;
		private static final int MSG_DATA_SENT = 3;
		private static final int MSG_DATA_RECEIVED = 4;
		private static final int MSG_ERROR_OCCURED = 5;
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CONNECTED:
				onConnected();
				break;
			case MSG_DISCONNECTED:
				onDisconnected();
				break;
			case MSG_DATA_SENT:
				onDataSent();
				break;
			case MSG_DATA_RECEIVED:
				onDataReceived((String) msg.obj);
				break;
			case MSG_ERROR_OCCURED:
				onErrorOccured((String) msg.obj);
				break;
			default:
				super.dispatchMessage(msg);
				break;
			}
		}
		
		public void notifyAboutConnect() {
			this.sendEmptyMessage(MSG_CONNECTED);
		}
		public void notifyAboutDisconnect() {
			this.sendEmptyMessage(MSG_DISCONNECTED);
		}
		public void notifyAboutDataSent() {
			this.sendEmptyMessage(MSG_DATA_SENT);
		}
		public void notifyAboutDataReceived(String data) {
			Message message = this.obtainMessage(MSG_DATA_RECEIVED, data);
			this.sendMessage(message);
		}
		public void notifyAboutErrorOccured(String errorMessage) {
			Message message = this.obtainMessage(MSG_ERROR_OCCURED, errorMessage);
			this.sendMessage(message);
		}
		
		/** Override in your code to get notified. */
		public void onConnected(){};
		/** Override in your code to get notified. */
		public void onDisconnected(){};
		/** Override in your code to get notified. */
		public void onDataSent(){};
		/** Override in your code to get notified. */
		public void onDataReceived(String data){};
		/** Override in your code to get notified. */
		public void onErrorOccured(String errorMessage){};
	}
	
	// Well known SPP UUID
	static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public static final void showDevicesSelector(Context context, BluetoothAdapter btAdapter, final OnDeviceSelectesListener listener) {
		btAdapter.getBondedDevices();
		
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			final String btDevicesNames[] = new String[pairedDevices.size()];
			final String btDevicesAddresses[] = new String[pairedDevices.size()];
			// Loop through paired devices
			int i = 0;
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a ListView
				btDevicesNames[i] = device.getName();
				btDevicesAddresses[i] = device.getAddress();
				i++;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Make your selection");
			builder.setItems(btDevicesNames, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int itemIndex) {
					// Do something with the selection
					listener.onDeviceSelected(btDevicesAddresses[itemIndex], btDevicesNames[itemIndex]);
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
}
