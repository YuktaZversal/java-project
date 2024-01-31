package com.quodd.bean;

public class Entitlement {

	private long entitlementId;
	private String fullName;
	private String shortCode;
	private String dbFieldName;
	private String backendKey;
	private String billing;
	private String rank;
	private long status;
	private String feedQuality;

	public long getEntitlementId() {
		return entitlementId;
	}

	public void setEntitlementId(long entitlementId) {
		this.entitlementId = entitlementId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getDbFieldName() {
		return dbFieldName;
	}

	public void setDbFieldName(String dbFieldName) {
		this.dbFieldName = dbFieldName;
	}

	public String getBackendKey() {
		return backendKey;
	}

	public void setBackendKey(String backendKey) {
		this.backendKey = backendKey;
	}

	public String getBilling() {
		return billing;
	}

	public void setBilling(String billing) {
		this.billing = billing;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Entitlement [entitlementId=" + entitlementId + ", fullName=" + fullName + ", shortCode=" + shortCode
				+ ", dbFieldName=" + dbFieldName + ", backendKey=" + backendKey + ", billing=" + billing + ", rank="
				+ rank + ", status=" + status + ", feedQuality=" + feedQuality + "]";
	}

	public String getFeedQuality() {
		return feedQuality;
	}

	public void setFeedQuality(String feedQuality) {
		this.feedQuality = feedQuality;
	}

}
