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
			/*DEBUG*/queryPurchasesList();
			/*DEBUG*/getPurchases();
		}
	};
	
	private ListView purchasesListView;
	
	private PurchasesAdapter purchasesAdapter_;
	
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
				int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
				String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
				String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

//				BILLING_RESPONSE_RESULT_OK	0	Success
//				BILLING_RESPONSE_RESULT_USER_CANCELED	1	User pressed back or canceled a dialog
//				BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE	3	Billing API version is not supported for the type requested
//				BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE	4	Requested product is not available for purchase
//				BILLING_RESPONSE_RESULT_DEVELOPER_ERROR	5	Invalid arguments provided to the API. This error can also indicate that the application was not correctly signed or properly set up for In-app Billing in Google Play, or does not have the necessary permissions in its manifest
//				BILLING_RESPONSE_RESULT_ERROR	6	Fatal error during the API action
//				BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED	7	Failure to purchase since item is already owned
//				BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED	8	Failure to consume since item is not owned
				if (responseCode == 0) {
					try {
						JSONObject jo = new JSONObject(purchaseData);
						String sku = jo.getString("productId");
						// alert("You have bought the " + sku + ". Excellent choice, adventurer!");
					} catch (JSONException e) {
						// alert("Failed to parse purchase data.");
						e.printStackTrace();
					}
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
	
	/**
	 * DO NOT CALL ON THE MAIN THREAD
	 */
	private void queryPurchasesList() {
		ArrayList<String> skuList = new ArrayList<String>();
		skuList.add(PURCHASE_MODES_ALL);
		skuList.add(PURCHASE_MODE_ONLINE);
		skuList.add(PURCHASE_MODE_VS_FRIEND);
		/*DEBUG*/skuList.add("android.test.purchased");
		/*DEBUG*/skuList.add("android.test.canceled");
		/*DEBUG*/skuList.add("android.test.refunded");
		/*DEBUG*/skuList.add("android.test.item_unavailable");
//		"android.test.purchased"
//		"android.test.canceled"
//		"android.test.refunded"
//		"android.test.item_unavailable"
		
		Bundle querySkus = new Bundle();
		querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
		
		Bundle skuDetails;
		try {
			skuDetails = billingService_.getSkuDetails(3, 
					   getPackageName(),
					   "inapp",
					   querySkus);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		int response = skuDetails.getInt("RESPONSE_CODE");
		if (response == 0) {
		   ArrayList<String> responseList 
		      = skuDetails.getStringArrayList("DETAILS_LIST");
		   try {
			   for (String thisResponse : responseList) {
			      JSONObject object = new JSONObject(thisResponse);
			      String sku = object.getString("productId");
			      String price = object.getString("price");
			      String title = object.getString("title");
			      String description = object.getString("description");
			      
			      PurchasableItem item = new PurchasableItem(sku, price, title, description);
			      purchasesAdapter_.add(item);
			   }
		   } catch (JSONException e) {
			e.printStackTrace();
		}
		}
	}
	
	private void purchaseItem(String sku) {
		Bundle buyIntentBundle;
		try {
			buyIntentBundle = billingService_.getBuyIntent(3,
					getPackageName(),
					sku,
					"inapp",
					"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
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
	
	private void getPurchases() {
		Bundle ownedItems;
		try {
			ownedItems = billingService_.getPurchases(3, getPackageName(), "inapp", null);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		}
		
		int response = ownedItems.getInt("RESPONSE_CODE");
		if (response == 0) {
		   ArrayList<String> ownedSkus = 
		      ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
		   ArrayList<String> purchaseDataList = 
		      ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
		   ArrayList<String> signatureList = 
		      ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
		   String continuationToken = 
		      ownedItems.getString("INAPP_CONTINUATION_TOKEN");
		   
		   for (int i = 0; i < purchaseDataList.size(); ++i) {
		      String purchaseData = purchaseDataList.get(i);
//		      String signature = signatureList.get(i);
//		      String sku = ownedSkus.get(i);
		  
		      // do something with this purchase information
		      // e.g. display the updated list of products owned by user
		   } 

		   // if continuationToken != null, call getPurchases again 
		   // and pass in the token to retrieve more items
		}
	}
}
