package com.quodd.bean;

public class Service {

	private long serviceId = 0;
	private String serviceName;
	private String quoddUpstreamId;

	public Service(long serviceId, String serviceName, String quoddUpstreamId) {
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.quoddUpstreamId = quoddUpstreamId;
	}

	public Service(String serviceName, String quoddUpstreamId) {
		this.serviceId = 0;
		this.serviceName = serviceName;
		this.quoddUpstreamId = quoddUpstreamId;
	}

	public long getServiceId() {
		return this.serviceId;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public String getQuoddUpstreamId() {
		return this.quoddUpstreamId;
	}

	@Override
	public String toString() {
		return "Service [serviceId=" + this.serviceId + ", serviceName=" + this.serviceName + ", quoddUpstreamId="
				+ this.quoddUpstreamId + "]";
	}
}
