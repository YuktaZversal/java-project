package com.quodd.bean;

public class Upstream {
	private long protocolId;
	private long upstreamId;
	private String name;

	public Upstream(long protocolId, long upstreamId, String name) {
		this.protocolId = protocolId;
		this.upstreamId = upstreamId;
		this.name = name;
	}

	public long getProtocolId() {
		return this.protocolId;
	}

	public long getUpstreamId() {
		return this.upstreamId;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return "Upstream [protocolId=" + this.protocolId + ", upstreamId=" + this.upstreamId + ", name=" + this.name
				+ "]";
	}

}
