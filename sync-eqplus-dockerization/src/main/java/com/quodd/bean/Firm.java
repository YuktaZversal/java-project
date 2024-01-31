package com.quodd.bean;

import java.util.Date;

public class Firm {

	private long firmId;
	private long parentFirmId;
	private String firmName;
	private String exchange;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String state;
	private String zip;
	private String country;
	private String email;
	private String phone;
	private long qsFirmId;
	private int userType;
	private Date addedDate;
	private String dataFeedType;
	private String deviceType;
	private int dowjonesBillable;

	public int getDowjonesBillable() {
		return this.dowjonesBillable;
	}

	public void setDowjonesBillable(int dowjonesBillable) {
		this.dowjonesBillable = dowjonesBillable;
	}

	public long getFirmId() {
		return firmId;
	}

	public void setFirmId(long firmId) {
		this.firmId = firmId;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public long getParentId() {
		return parentFirmId;
	}

	public void setParentId(long parentId) {
		parentFirmId = parentId;
	}

	public String getFirmName() {
		return firmName;
	}

	public void setFirmName(String firmName) {
		this.firmName = firmName;
	}

	public long getQsFirmId() {
		return qsFirmId;
	}

	public void setQsFirmId(long clientFirmId) {
		this.qsFirmId = clientFirmId;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public Date getAddedDate() {
		return addedDate;
	}

	public void setAddedDate(Date addedDate) {
		this.addedDate = addedDate;
	}

	public String getDataFeedType() {
		return dataFeedType;
	}

	public void setDataFeedType(String dataFeedType) {
		this.dataFeedType = dataFeedType;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

}
