package com.quodd.util;

import java.util.ArrayList;
import java.util.List;

public class EquityUtility {

	private final List<String> mailList = new ArrayList<>();

	public void addProductHeaderToMailList() {
		this.mailList.add(
				"<b><tr style=background-color:#FFFF00><td>productName</td><td>qs_db_code</td><td>qs_entitlement_id</td><td>status</td><td>additionalInfo</td><tr></b>");
	}

	public void addProductToMailList(String productName, String qsDbCode, String qsEntitlementId, String status,
			String additionalInfo) {
		this.mailList.add("<tr><td>" + productName + "</td><td>" + qsDbCode + "</td><td>" + qsEntitlementId
				+ "</td><td>" + status + "</td><td>" + additionalInfo + "</td><td>");
	}

	public void addProductMappingHeaderToMailList() {
		this.mailList.add(
				"<b><tr style=background-color:#FFFF00><td>productId</td><td>entitlementId</td><td>status</td><td>additionalInfo</td><tr></b>");
	}

	public void addProductMappingToMailList(Long productId, Long entitlementId, String status, String additionalInfo) {
		this.mailList.add("<tr><td>" + productId + "</td><td>" + entitlementId + "</td><td>" + status + "</td><td>"
				+ additionalInfo + "</td><td>");
	}

	public void addServiceHeaderToMailList() {
		this.mailList.add(
				"<b><tr style=background-color:#FFFF00><td>serviceId</td><td>serviceName</td><td>upstreamId</td><td>status</td><td>additionalInfo</td><tr></b>");
	}

	public void addServicesToMailList(int serviceId, String serviceName, String upstreamId, String status,
			String additionalInfo) {
		this.mailList.add("<tr><td>" + serviceId + "</td><td>" + serviceName + "</td><td>" + upstreamId + "</td><td>"
				+ status + "</td><td>" + additionalInfo + "</td><td>");
	}

	public void addUserHeaderToMailList() {
		this.mailList.add(
				"<b><tr style=background-color:#FFFF00><td>AccountNumber</td><td>AccountName</td><td>Status</td><td>QSValue</td><td>QSS4Value</td><tr></b>");
	}

	public void addToMailList(int accountNumber, String accountName, String status, String qsValue, String qss4Value) {
		this.mailList.add("<tr><td>" + accountNumber + "</td><td>" + accountName + "</td><td>" + status + "</td><td>"
				+ qsValue + "</td><td>" + qss4Value + "</td></tr>");
	}

	public String getEmailBody() {
		StringBuilder sb = new StringBuilder();
		if (this.mailList.isEmpty()) {
			sb.append("No differences found for sync");
		} else {
			sb.append("<table border = 1px>");
			this.mailList.forEach(record -> sb.append(record));
			sb.append("</table>");
		}

		return sb.toString();
	}

}
