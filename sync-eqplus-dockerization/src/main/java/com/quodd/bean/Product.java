package com.quodd.bean;

public class Product {

	private long productId;
	private String quoddDbName;
	private String productName;
	private long quoddEntitlementId;
	private String feedQuality;

	public Product(long productId, String quoddDbName, String productName, long quoddEntitlementId,String feedQuality) {
		this.productId = productId;
		this.quoddDbName = quoddDbName;
		this.productName = productName;
		this.quoddEntitlementId = quoddEntitlementId;
		this.feedQuality=feedQuality;
	}

	public Product(String quoddDbName, String productName, long quoddEntitlementId,String feedQuality) {
		this.productId = 0;
		this.quoddDbName = quoddDbName;
		this.productName = productName;
		this.quoddEntitlementId = quoddEntitlementId;
		this.feedQuality=feedQuality;
	}

	public long getProductId() {
		return this.productId;
	}

	public String getQuoddDbName() {
		return this.quoddDbName;
	}

	public String getProductName() {
		return this.productName;
	}

	public long getQuoddEntitlementId() {
		return this.quoddEntitlementId;
	}

	@Override
	public String toString() {
		return "Product [productId=" + productId + ", quoddDbName=" + quoddDbName + ", productName=" + productName
				+ ", quoddEntitlementId=" + quoddEntitlementId + ", feedQuality=" + feedQuality + "]";
	}

	public String getFeedQuality() {
		return feedQuality;
	}

	public void setFeedQuality(String feedQuality) {
		this.feedQuality = feedQuality;
	}
}
