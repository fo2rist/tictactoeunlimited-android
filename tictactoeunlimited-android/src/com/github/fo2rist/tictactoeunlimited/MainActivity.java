package com.github.fo2rist.tictactoeunlimited;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fo2rist.tictactoeunlimited.game.GameLogic;

/**
 * Main screen.
 * Game type chooser.
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);
		
		/*DEBUG*/
		out = (TextView) findViewById(R.id.out);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void startGameVsCpu(View sender) {
		GameLogic.getInstance().initializeGame(8, 10);
		
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		startActivity(new Intent(this, GameActivity.class));
	}

	public void startGameHotSeat(View sender) {
		GameLogic.getInstance().initializeGame(10, 10);
		
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		startActivity(new Intent(this, GameActivity.class));
	}

	public void startGameViaBluetooth(View sender) {
	    btAdapter = BluetoothAdapter.getDefaultAdapter();
	    CheckBTState();
	}

	public void showAbout(View sender) {
		startActivity(new Intent(this, AboutActivity.class));
	}
	
	//TRASH
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;

	// Well known SPP UUID
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	  
	private TextView out;
	
	private void CheckBTState() {
		// Check for Bluetooth support and then check to make sure it is turned on

		// Emulator doesn't support Bluetooth and will return null
		if (btAdapter == null) {
			Toast.makeText(this, "Bluetooth Not supported. Aborting.", Toast.LENGTH_LONG);
			return;
		} else {
			if (btAdapter.isEnabled()) {
				out.append("\n...Bluetooth is enabled...");
				selectDevice();
			} else {
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == RESULT_OK) {
				selectDevice();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void selectDevice() {
		btAdapter.getBondedDevices();
		
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			String btDevicesNames[] = new String[pairedDevices.size()];
			final String btDevicesAddresses[] = new String[pairedDevices.size()];
		    // Loop through paired devices
			int i=0;
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		        btDevicesNames[i] = device.getName();
		        btDevicesAddresses[i] = device.getAddress();
		        i++;
		    }
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setTitle("Make your selection");
		    builder.setItems(btDevicesNames, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int item) {
		    		// Do something with the selection
		    		connectToDevice(btDevicesAddresses[item]);
		    	}
		    });
		    AlertDialog alert = builder.create();
		    alert.show();
		}
	}

	private void connectToDevice(String address) {
		
		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			Toast.makeText(this, "In onResume() and socket create failed: " + e.getMessage() + ".", Toast.LENGTH_LONG);
		}

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection. This will block until it connects.
		try {
			btSocket.connect();
			out.append("\n...Connection established and data link opened...");
			sendMessage();
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Toast.makeText(this,
						"In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".",
						Toast.LENGTH_LONG);
			}
		}
	}

	private void sendMessage() {
		// Create a data stream so we can talk to server.
		out.append("\n...Sending message to server...");

		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			Toast.makeText(this,
					"In onResume() and output stream creation failed:" + e.getMessage() + ".",
					Toast.LENGTH_LONG);
		}

		String message = "Hello from Android.\n";
		byte[] msgBuffer = message.getBytes();
		try {
			outStream.write(msgBuffer);
		} catch (IOException e) {
			String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
			msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		}

//				if (outStream != null) {
//					try {
//						outStream.flush();
//					} catch (IOException e) {
//						AlertBox("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
//					}
//				}
//				try {
//					btSocket.close();
//				} catch (IOException e2) {
//					AlertBox("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
//				}
	}
}
