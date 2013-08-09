package com.github.fo2rist.tictactoeunlimited;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fo2rist.tictactoeunlimited.bluetooth.BtClient;
import com.github.fo2rist.tictactoeunlimited.bluetooth.BtServer;
import com.github.fo2rist.tictactoeunlimited.bluetooth.BtUtils;
import com.github.fo2rist.tictactoeunlimited.bluetooth.BtUtils.BtEventsListener;
import com.github.fo2rist.tictactoeunlimited.bluetooth.BtUtils.OnDeviceSelectesListener;
import com.github.fo2rist.tictactoeunlimited.controls.HorizontalFlipperWidget;
import com.github.fo2rist.tictactoeunlimited.controls.MapSizesAdapter;
import com.github.fo2rist.tictactoeunlimited.controls.ModesAdapter;
import com.github.fo2rist.tictactoeunlimited.game.GameLogic;

/**
 * Main screen.
 * Game type chooser.
 */
public class MainActivity extends FragmentActivity {
	private enum GameMode {
		Server,
		Client
	}
	
	private static final int REQUEST_ENABLE_BT_FOR_CLIENT = 1;
	private static final int REQUEST_ENABLE_BT_FOR_SERVER = 2;
	
	//Controls
	HorizontalFlipperWidget modeSelector;
	HorizontalFlipperWidget mapSizeSelector;
	//Trash
	private TextView out;
	
	private BluetoothAdapter btAdapter_ = null;
	
	private BtClient btClient_ = null;
	private BtServer btServer_ = null;
	
	private final BtEventsListener btClientEventsListener_ = new BtEventsListener() {
		@Override
		public void onConnected() {
			Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
		};
		
		@Override
		public void onDisconnected() {
			Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
		};
		
		@Override
		public void onDataSent() {
			
		};
		
		@Override
		public void onDataReceived(String data) {
			out.append(" > Got: " + data);
		};
		
		@Override
		public void onErrorOccured(String errorMessage) {
			Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
			if (btClient_ != null) {
				btClient_.disconnect();
				btClient_ = null;
			}
		};
	};
	
	private final BtEventsListener btServerEventsListener_ = new BtEventsListener() {
		@Override
		public void onConnected() {
			Toast.makeText(MainActivity.this, "Client connected", Toast.LENGTH_SHORT).show();
		};
		
		@Override
		public void onDisconnected() {
			Toast.makeText(MainActivity.this, "Client disconnected", Toast.LENGTH_SHORT).show();
		};
		
		@Override
		public void onDataReceived(String data) {
			out.append(data);
		};
		
		@Override
		public void onErrorOccured(String errorMessage) {
			Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
			if (btServer_ != null) {
				btServer_.cancel();
				btServer_ = null;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);
		
		modeSelector = (HorizontalFlipperWidget) findViewById(R.id.mode_selector);
		modeSelector.setAdapter(new ModesAdapter(getSupportFragmentManager()));
		modeSelector.setButtons(findViewById(R.id.button_mode_left),
				findViewById(R.id.button_mode_right));
		mapSizeSelector = (HorizontalFlipperWidget) findViewById(R.id.map_size_selector);
		mapSizeSelector.setAdapter(new MapSizesAdapter(getSupportFragmentManager()));
		mapSizeSelector.setButtons(findViewById(R.id.button_map_left),
				findViewById(R.id.button_map_right));
		
		btAdapter_ = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter_ == null) {
			//Disable button
			findViewById(R.id.bt_server_button).setEnabled(false);
			findViewById(R.id.bt_client_button).setEnabled(false);
			findViewById(R.id.bt_send_button).setEnabled(false);
		}
		
		/*DEBUG*/
		out = (TextView) findViewById(R.id.out);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT_FOR_CLIENT:
			if (resultCode == RESULT_OK) {
				selectDeviceToConnect();
			}
			break;
		case REQUEST_ENABLE_BT_FOR_SERVER:
			if (resultCode == RESULT_OK) {
				startBluetoothServer();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void onStartGameVsCpuClicked(View sender) {
		GameLogic.getInstance().initializeGame(8, 10);
		
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		startActivity(new Intent(this, GameActivity.class));
	}

	public void onStartGameHotSeatClicked(View sender) {
		GameLogic.getInstance().initializeGame(10, 10);
		
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		startActivity(new Intent(this, GameActivity.class));
	}
	
	public void onStartBluetoothServerClicked(View sender) {
		checkBtAndStartGame(GameMode.Server);
	}

	public void onStartGameViaBluetoothClicked(View sender) {
		checkBtAndStartGame(GameMode.Client);
	}
	
	public void onSendAnotherClicked(View sender)  {
		btClient_.send("Hello from Android");
	}

	public void onShowAboutClicked(View sender) {
		startActivity(new Intent(this, AboutActivity.class));
	}

	private void checkBtAndStartGame(GameMode mode) {
		// Check for Bluetooth support and then check to make sure it is turned on
		// Emulator doesn't support Bluetooth and will return null
		if (btAdapter_ == null) {
			Toast.makeText(this, "Bluetooth Not supported. Aborting.", Toast.LENGTH_LONG);
			return;
		}

		if (btAdapter_.isEnabled()) {
			switch (mode) {
			case Server:				
				selectDeviceToConnect();
				break;
			case Client:
				startBluetoothServer();
				break;
			}
		} else {
			// Prompt user to turn on Bluetooth to select device when done
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			
			switch (mode) {
			case Server:				
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_FOR_SERVER);
				break;
			case Client:
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_FOR_CLIENT);
				break;
			}
		}
	} 
		
	private void startBluetoothServer() {
		if (btServer_ == null) {
			btServer_ = new BtServer(btServerEventsListener_);
			btServer_.start();
		}
	}
	
	private void selectDeviceToConnect() {
		BtUtils.showDevicesSelector(this, btAdapter_, new OnDeviceSelectesListener() {
			@Override
			public void onDeviceSelected(String address, String name) {
				if (btClient_ == null) {
					btClient_ = new BtClient(btClientEventsListener_);
				}
				btClient_.startConnection(address);
			}
		});
	}
}
