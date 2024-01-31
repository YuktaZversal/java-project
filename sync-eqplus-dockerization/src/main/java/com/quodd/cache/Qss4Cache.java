package com.quodd.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.quodd.bean.Firm;
import com.quodd.bean.Product;
import com.quodd.bean.User;

public class Qss4Cache {

	private final Map<Long, Product> productMap = new HashMap<>();
	private final Map<Long, User> qss4ActiveUser = new HashMap<>();
	private final Map<Long, User> qss4CancelledUser = new HashMap<>();
	private final Map<String, Long> qss4UserMap = new HashMap<>();
	private final Map<Long, Firm> qss4FirmMap = new HashMap<>();
	private final Map<String, String> countryCodeMap = new HashMap<String, String>();

	public void addProduct(Long productId, Product product) {
		this.productMap.put(productId, product);
	}

	public Map<String, String> getCountryCodeMap() {
		this.countryCodeMap.put("IN", "INDIA");
		this.countryCodeMap.put("US", "United States");
		this.countryCodeMap.put("CA", "Cananda");
		this.countryCodeMap.put("Eg", "Egypt");
		this.countryCodeMap.put("LU", "Luxembourg");
		this.countryCodeMap.put("RU", "Russia");
		this.countryCodeMap.put("AR", "Argentina");
		this.countryCodeMap.put("CH", "Switzerland");
		this.countryCodeMap.put("DE", "Germany");
		this.countryCodeMap.put("SK", "Slovakia");
		this.countryCodeMap.put("GB", "United Kingdom");
		this.countryCodeMap.put("ES", "Spain");
		return this.countryCodeMap;
	}

	@Deprecated
	public Map<Long, User> getQss4ActiveUser() {
		return this.qss4ActiveUser;
	}

	public void addQss4ActiveUser(long clientUserId, User user) {
		this.qss4ActiveUser.put(clientUserId, user);
	}

	@Deprecated
	public Map<Long, User> getQss4CancelledUser() {
		return this.qss4CancelledUser;
	}

	public void addQss4CancelledUser(long clientUserId, User user) {
		this.qss4CancelledUser.put(clientUserId, user);
	}

	@Deprecated
	public Map<String, Long> getQss4UserMap() {
		return this.qss4UserMap;
	}

	public void addQss4User(String username, Long clientUserId) {
		this.qss4UserMap.put(username, clientUserId);
	}
	@Deprecated
	public Map<Long, Firm> getQss4FirmMap() {
		return this.qss4FirmMap;
	}

	public void addQss4Firm(long qsFirmId, Firm firm) {
		this.qss4FirmMap.put(qsFirmId, firm);
	}

	/**
	 * dbName, ProductId Map
	 */
	@Deprecated
	public Map<String, Long> getDbNameAndProductIdMapping() {
		HashMap<String, Long> productEntitlementMap = new HashMap<>();
		for (Entry<Long, Product> product : productMap.entrySet()) {
			String dbname = product.getValue().getQuoddDbName();
			Long productId = product.getKey();
			productEntitlementMap.put(dbname, productId);
		}
		return productEntitlementMap;
	}
}
