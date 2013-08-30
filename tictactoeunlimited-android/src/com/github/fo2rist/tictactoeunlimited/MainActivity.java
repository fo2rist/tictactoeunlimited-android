package com.github.fo2rist.tictactoeunlimited;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fo2rist.tictactoeunlimited.bluetooth.BtClient;
import com.github.fo2rist.tictactoeunlimited.bluetooth.BtServer;
import com.github.fo2rist.tictactoeunlimited.bluetooth.BtUtils;
import com.github.fo2rist.tictactoeunlimited.bluetooth.BtUtils.BtEventsListener;
import com.github.fo2rist.tictactoeunlimited.bluetooth.BtUtils.OnDeviceSelectesListener;
import com.github.fo2rist.tictactoeunlimited.controls.HorizontalPagerWidget;
import com.github.fo2rist.tictactoeunlimited.controls.HorizontalPagerWidget.OnPageSelectedListener;
import com.github.fo2rist.tictactoeunlimited.controls.MapSizesAdapter;
import com.github.fo2rist.tictactoeunlimited.controls.ModesAdapter;
import com.github.fo2rist.tictactoeunlimited.controls.ModesAdapter.GameMode;
import com.github.fo2rist.tictactoeunlimited.game.GameLogic;

/**
 * Main screen.
 * Game type chooser.
 */
public class MainActivity extends FragmentActivity {
	
	private enum BluetoothMode {
		Server,
		Client
	}
	
	private static final int REQUEST_ENABLE_BT_FOR_CLIENT = 1;
	private static final int REQUEST_ENABLE_BT_FOR_SERVER = 2;
	
	//Controls
	HorizontalPagerWidget modeSelector;
	HorizontalPagerWidget mapSizeSelector;
	View mapSizeSelectorBlock;
	ImageButton buttonLocalGame;
	ImageButton buttonCreateGame;
	ImageButton buttonJoinGame;
	//Trash
	private TextView out;
	
	private ModesAdapter modesAdapter_;
	private MapSizesAdapter mapSizesAdapter_;
	
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

		//Initialize game modes
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		modesAdapter_ = new ModesAdapter(getSupportFragmentManager());
		mapSizesAdapter_ = new MapSizesAdapter(getSupportFragmentManager(), dm);
		btAdapter_ = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter_ == null) {
			//Disable modes/buttons if necessary
			modesAdapter_.disableMode(GameMode.ViaBluetooth);
			findViewById(R.id.bt_send_button).setEnabled(false);
		}
		modesAdapter_.disableMode(GameMode.ViaBluetooth);
		modesAdapter_.disableMode(GameMode.ViaNetwork);
		
		//Setup ui
		buttonLocalGame = (ImageButton) findViewById(R.id.button_local_game);
		buttonCreateGame = (ImageButton) findViewById(R.id.button_create_game);
		buttonJoinGame = (ImageButton) findViewById(R.id.button_join_game);
		
		modeSelector = (HorizontalPagerWidget) findViewById(R.id.mode_selector);
		modeSelector.setAdapter(modesAdapter_);
		modeSelector.setButtons(findViewById(R.id.button_mode_left),
				findViewById(R.id.button_mode_right));
		
		mapSizeSelectorBlock = findViewById(R.id.map_size_selector_block);
		mapSizeSelector = (HorizontalPagerWidget) findViewById(R.id.map_size_selector);
		mapSizeSelector.setAdapter(mapSizesAdapter_);
		mapSizeSelector.setButtons(findViewById(R.id.button_map_left),
				findViewById(R.id.button_map_right));
		
		//Setup listeners for selectors
		modeSelector.setOnPageSelectedListener(new OnPageSelectedListener() {
			@Override
			public void onPageSelected(int position) {
				setupMode(modesAdapter_.getMode(position));
			}
		});
		
		//Setup default mode
		setupMode(modesAdapter_.getMode(modeSelector.getCurrentItem()));
		
		/*DEBUG*/
		out = (TextView) findViewById(R.id.out);
		out.setVisibility(View.GONE);
		findViewById(R.id.bt_send_button).setVisibility(View.GONE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
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

	public void onStartLocalGameClicked(View sender) {
		GameMode gameMode = modesAdapter_.getMode(modeSelector.getCurrentItem());
		int[] mapSize = mapSizesAdapter_.getMapSize(mapSizeSelector.getCurrentItem());
		
		GameLogic.GameMode playersMode;
		switch (gameMode) {
		case VsCpu:
			playersMode = GameLogic.GameMode.GameModeSinglePlayer;
			break;
		case VsFriend:
		case ViaBluetooth:
		case ViaNetwork:
			playersMode = GameLogic.GameMode.GameModeTwoPlayers;
			break;
		default:
			throw new IllegalStateException("Unsupported game mode");
		}
				
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		GameActivity.launch(this, mapSize[0], mapSize[1], playersMode);
	}
	
	public void onCreateGameClicked(View sender) {
		GameMode gameMode = modesAdapter_.getMode(modeSelector.getCurrentItem());
		//TODO use mode
		checkBtAndStartGame(BluetoothMode.Server);
	}

	public void onJoinGameClicked(View sender) {
		GameMode gameMode = modesAdapter_.getMode(modeSelector.getCurrentItem());
		//TODO use mode
		checkBtAndStartGame(BluetoothMode.Client);
	}
	
	public void onSendAnotherClicked(View sender)  {
		btClient_.send("Hello from Android");
	}

	public void onShowAboutClicked(View sender) {
		startActivity(new Intent(this, AboutActivity.class));
	}

	private void setupMode(GameMode gameMode) {
		switch (gameMode) {
		case VsCpu:
		case VsFriend:
			buttonLocalGame.setVisibility(View.VISIBLE);
			buttonCreateGame.setVisibility(View.GONE);
			buttonJoinGame.setVisibility(View.GONE);
			mapSizeSelectorBlock.setVisibility(View.VISIBLE);
			break;
		case ViaBluetooth:
		case ViaNetwork:
			buttonLocalGame.setVisibility(View.GONE);
			buttonCreateGame.setVisibility(View.VISIBLE);
			buttonJoinGame.setVisibility(View.VISIBLE);
			mapSizeSelectorBlock.setVisibility(View.GONE);
			break;
		default:
			throw new IllegalArgumentException();
		}
		
	}
	
	private void checkBtAndStartGame(BluetoothMode mode) {
		// Check for Bluetooth support and then check to make sure it is turned on
		// Emulator doesn't support Bluetooth and will return null
		if (btAdapter_ == null) {
			Toast.makeText(this, "Bluetooth Not supported. Aborting.", Toast.LENGTH_LONG);
			return;
		}

		if (btAdapter_.isEnabled()) {
			switch (mode) {
			case Server:				
				startBluetoothServer();
				break;
			case Client:
				selectDeviceToConnect();
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
