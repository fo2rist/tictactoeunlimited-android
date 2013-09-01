package com.weezlabs.tictactoeunlimited.purchases;

import android.content.Context;
import android.widget.ArrayAdapter;

public class PurchasesAdapter extends ArrayAdapter<PurchasableItem>{

	public PurchasesAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_2, android.R.id.text1);
		
	}

}
