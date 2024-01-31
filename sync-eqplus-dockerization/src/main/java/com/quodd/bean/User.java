package com.quodd.bean;

public class User {

	private long clientUserId; // double
	private int userType; // double
	private int status; // double
	private long firmId; // double
	private long parentId; // double
	private String firmName;
	private String firmType;
	private String userName;
	private String password;
	private String firstname;
	private String lastName;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String state;
	private String zip;
	private String country;
	private String email;
	private String phone;
	private String exchange;
	private long creationTime;
	private long updateTime;
	private long cancelledDate = 0l;
	private long cancelledEffective = 0l;
	private String cancelledTime;
	private String cancelledEffectiveTime;
	private boolean allowChild;
	private long clientFirmId;
	private String exchangeId;
	private String dataFeedType;
	private String deviceType;
	private int seatType;
	
	public String getCancelledTime() {
		return this.cancelledTime;
	}
	
	public void setCancelledTime(String cancelledTime) {
		this.cancelledTime = cancelledTime;
	}
	
	public String getCancelledEffectiveTime() {
		return this.cancelledEffectiveTime;
	}
	
	public void setCancelledEffectiveTime(String cancelledEffectiveTime) {
		this.cancelledEffectiveTime = cancelledEffectiveTime;
	}
	
	public long getClientUserId() {
		return this.clientUserId;
	}

	public void setClientUserId(long clientUserId) {
		this.clientUserId = clientUserId;
	}

	public int getUserType() {
		return this.userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public int getSeatType() {
		return this.seatType;
	}

	public void setSeatType(int seatType) {
		this.seatType = seatType;
	}
	
	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getFirmId() {
		return this.firmId;
	}

	public void setFirmId(long firmId) {
		this.firmId = firmId;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAddressLine1() {
		return this.addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return this.addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return this.zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public long getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public long getCancelledDate() {
		return this.cancelledDate;
	}

	public void setCancelledDate(long cancelledDate) {
		this.cancelledDate = cancelledDate;
	}
	
	public long getCancelledEffectiveDate() {
		return this.cancelledEffective;
	}

	public void setCancelledEffectiveDate(long cancelledEffective) {
		this.cancelledEffective = cancelledEffective;
	}

	public long getParentId() {
		return this.parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public String getFirmName() {
		return this.firmName;
	}

	public void setFirmName(String firmName) {
		this.firmName = firmName;
	}

	public String getFirmType() {
		return this.firmType;
	}

	public void setFirmType(String firmType) {
		this.firmType = firmType;
	}

	public boolean isAllowChild() {
		return this.allowChild;
	}

	public void setAllowChild(boolean allowChild) {
		this.allowChild = allowChild;
	}

	@Override
	public String toString() {
		return "User [clientUserId=" + this.clientUserId + ", userType=" + this.userType + ", status=" + this.status
				+ ", firmId=" + this.firmId + ", userName=" + this.userName + ", password=" + this.password
				+ ", firstname=" + this.firstname + ", lastName=" + this.lastName + ", addressLine1="
				+ this.addressLine1 + ", addressLine2=" + this.addressLine2 + ", city=" + this.city + ", state="
				+ this.state + ", zip=" + this.zip + ", country=" + this.country + ", email=" + this.email + ", phone="
				+ this.phone + ", creationTime=" + this.creationTime + ", updateTime=" + this.updateTime
				+ ", cancelledDate=" + this.cancelledDate + ", clientFirmId=" + this.clientFirmId + "]";
	}

	public long getClientFirmId() {
		return this.clientFirmId;
	}

	public void setClientFirmId(long clientFirmId) {
		this.clientFirmId = clientFirmId;
	}

	public String getExchange() {
		return this.exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getExchangeId() {
		return this.exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDataFeedType() {
		return dataFeedType;
	}

	public void setDataFeedType(String dataFeedType) {
		this.dataFeedType = dataFeedType;
	}
}
