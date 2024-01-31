package com.quodd.bean;

public class UpstreamLink {
	
	private int protocolId;
	private String upstreamId;
	private int entitlementId;
	
	public UpstreamLink(int protocolId, String upstreamId, int entitlementId) {
		this.entitlementId = entitlementId;
		this.protocolId = protocolId;
		this.upstreamId = upstreamId;
	}

	public int getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}

	public String getUpstreamId() {
		return upstreamId;
	}

	public void setUpstreamId(String upstreamId) {
		this.upstreamId = upstreamId;
	}

	public int getEntitlementId() {
		return entitlementId;
	}

	public void setEntitlementId(int entitlementId) {
		this.entitlementId = entitlementId;
	}
	
	
	@Override
	public String toString() {
		return "UpstreamLink [protocolId=" + protocolId + ", upstreamId=" + upstreamId + ", entitlementId="
				+ entitlementId + "]";
	}
	
	
}
