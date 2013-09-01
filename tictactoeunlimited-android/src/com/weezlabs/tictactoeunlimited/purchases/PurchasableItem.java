package com.weezlabs.tictactoeunlimited.purchases;

public class PurchasableItem {
	public String sku;
	public String price;
	public String title;
	public String description;
	
    public PurchasableItem(String sku, String price, String title, String description) {
		super();
		this.sku = sku;
		this.price = price;
		this.title = title;
		this.description = description;
	}
    
    @Override
    public String toString() {
    	return title + " " + description + " " + price;
    }
}
