package com.weezlabs.tictactoeunlimited;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.weezlabs.tictactoeunlimited.purchases.PurchasableItem;
import com.weezlabs.tictactoeunlimited.purchases.PurchasesAdapter;

public class PurchasesActivity extends Activity implements OnItemClickListener {
	private static final int BILLING_RESPONSE_RESULT_OK = 0;//	Success
	private static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1; // User pressed back or canceled a dialog
	private static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3; // Billing API version is not supported for the type requested
	private static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4; // Requested product is not available for purchase
	private static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5; // Invalid arguments provided to the API. This error can also indicate that the application was not correctly signed or properly set up for In-app Billing in Google Play, or does not have the necessary permissions in its manifest
	private static final int BILLING_RESPONSE_RESULT_ = 6; // Fatal error during the API action
	private static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7; // Failure to purchase since item is already owned
	private static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8; //Failure to consume since item is not owned
	private static final int BILLING_RESPONSE_RESULT_STUB = -1; //value unused by platform as a result. 
	
	private static final String PURCHASE_MODES_ALL = "modes_all";
	private static final String PURCHASE_MODE_ONLINE = "mode_online";
	private static final String PURCHASE_MODE_VS_FRIEND = "mode_vs_friend";
    
	IInAppBillingService billingService_;

	ServiceConnection billingServiceConnection_ = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			billingService_ = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			billingService_ = IInAppBillingService.Stub.asInterface(service);
			startPurhcasesUpdate();
		}
	};
	
	private ListView purchasesListView;
	
	private PurchasesAdapter purchasesAdapter_;
	
	private static class PurchasesLoadingTask extends AsyncTask<Void, PurchasableItem, Exception> {
		private Context context_;
		private IInAppBillingService service_;
		
		public PurchasesLoadingTask(Context context, IInAppBillingService billingService) {
			this.context_ = context;
			this.service_ = billingService;
		}
		
		@Override
		protected Exception doInBackground(Void... params) {
			try {
				ArrayList<String> activePurchases = getActivePurchases();
				getAvailablePurchases(activePurchases);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * Gets list of available purchases, updated it by info about active purchases and post data via progress. 
		 */
		private void getAvailablePurchases(ArrayList<String> activePurchases) throws RemoteException {
			ArrayList<String> skuList = new ArrayList<String>();
			skuList.add(PURCHASE_MODES_ALL);
			skuList.add(PURCHASE_MODE_ONLINE);
			skuList.add(PURCHASE_MODE_VS_FRIEND);
			/* DEBUG */skuList.add("android.test.purchased");
			/* DEBUG */skuList.add("android.test.canceled");
			/* DEBUG */skuList.add("android.test.refunded");
			/* DEBUG */skuList.add("android.test.item_unavailable");

			Bundle querySkus = new Bundle();
			querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

			Bundle skuDetails = service_.getSkuDetails(3, context_.getPackageName(), "inapp", querySkus);

			int response = skuDetails.getInt("RESPONSE_CODE");
			if (response == BILLING_RESPONSE_RESULT_OK) {
				ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
				try {
					for (String thisResponse : responseList) {
						JSONObject object = new JSONObject(thisResponse);
						String sku = object.getString("productId");
						String price = object.getString("price");
						String title = object.getString("title");
						String description = object.getString("description");

						PurchasableItem item = new PurchasableItem(sku, price, title, description);
						//TODO update item if present in activePurchases 
						publishProgress(item);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Get purchase already made by user.
		 * @return list of owned SKU or null.
		 */
		private ArrayList<String> getActivePurchases() throws RemoteException {
			Bundle ownedItems = service_.getPurchases(3, context_.getPackageName(), "inapp", null);

			int response = ownedItems.getInt("RESPONSE_CODE");
			if (response == BILLING_RESPONSE_RESULT_OK) {
				ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
				ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
				ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
				String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

				// for (int i = 0; i < purchaseDataList.size(); ++i) {
				// 	String purchaseData = purchaseDataList.get(i);
				// 	String sku = ownedSkus.get(i);
				// }
				if (continuationToken != null) {
					// call getPurchases again
					// and pass in the token to retrieve more items
					// but now we don't support too many items
				}

				return ownedSkus;
			}
			
			return null;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_purchases);
		
		purchasesListView = (ListView) findViewById(R.id.list_purchases);
		
		purchasesAdapter_ = new PurchasesAdapter(this);
		purchasesListView.setAdapter(purchasesAdapter_);
		purchasesListView.setEmptyView(findViewById(android.R.id.empty));
		purchasesListView.setOnItemClickListener(this);
		
		bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"),
				billingServiceConnection_,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		if (billingServiceConnection_ != null) {
			unbindService(billingServiceConnection_);
		}
		super.onPause();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1001) {
			if (resultCode == RESULT_OK) {
				int responseCode = data.getIntExtra("RESPONSE_CODE", BILLING_RESPONSE_RESULT_STUB);
				String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
				String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

				if (responseCode == BILLING_RESPONSE_RESULT_OK) {
//					try {
//						JSONObject jo = new JSONObject(purchaseData);
//						String sku = jo.getString("productId");
//						// alert("You have bought the " + sku + ". Excellent choice, adventurer!");
//					} catch (JSONException e) {
//						// alert("Failed to parse purchase data.");
//						e.printStackTrace();
//					}
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	/**
	 * Handle click on puchasable item.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		PurchasableItem purchasableItem = purchasesAdapter_.getItem(position);
		purchaseItem(purchasableItem.sku);
	}
	
	protected void startPurhcasesUpdate() {
		PurchasesLoadingTask loadingTask = new PurchasesLoadingTask(this, billingService_) {
			@Override
			protected void onProgressUpdate(PurchasableItem... items) {
				for (PurchasableItem purchasableItem : items) {
					purchasesAdapter_.add(purchasableItem);
				}
			};
			@Override
			protected void onPostExecute(Exception result) {
				// TODO Handle errors
			}
		};
		loadingTask.execute();
	}
	
	private void purchaseItem(String sku) {
		Bundle buyIntentBundle;
		try {
			buyIntentBundle = billingService_.getBuyIntent(3,
					getPackageName(),
					sku,
					"inapp",
					"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");//TODO Change payload
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		
		PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
		if (pendingIntent == null) {
			//TODO Warn users
			Toast.makeText(this, "Unable to purchase", Toast.LENGTH_SHORT).show();
			return;
		}
		
		try {
			startIntentSenderForResult(pendingIntent.getIntentSender(),
					   1001,
					   new Intent(),
					   Integer.valueOf(0),
					   Integer.valueOf(0),
					   Integer.valueOf(0));
		} catch (SendIntentException e) {
			e.printStackTrace();
		}
	}
}
