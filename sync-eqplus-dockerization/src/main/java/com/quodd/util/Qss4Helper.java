package com.quodd.util;

import static com.quodd.EquityPlusSyncApplication.gson;
import static com.quodd.EquityPlusSyncApplication.logger;
import static com.quodd.EquityPlusSyncApplication.qss4Authorization;
import static com.quodd.EquityPlusSyncApplication.QSS4_DOMAIN;
import static com.quodd.EquityPlusSyncApplication.utility;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.quodd.bean.Firm;
import com.quodd.bean.Product;
import com.quodd.bean.Service;
import com.quodd.bean.User;
import com.quodd.exception.QuoddException;

public interface Qss4Helper {

	/**
	 * upstreamId,ServiceBean Map
	 * 
	 * @throws QuoddException
	 */
	DateTimeFormatter qss4TimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a");

	public static Map<Long, Service> loadServicesMap(Map<String, Service> upstreamIdServiceMap) throws QuoddException {
		Map<Long, Service> serviceMap = new HashMap<>();
		String url = QSS4_DOMAIN + "/internal/syncqs/service/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					String serviceName = (String) lookup.get(QuoddSyncConstants.SERVICE_NAME);
					Double serviceId = (Double) lookup.get(QuoddSyncConstants.SERVICE_ID);
					String qsUpstreamId = (String) lookup.get(QuoddSyncConstants.QS_UPSTREAM_ID);
					Service serviceBean = new Service(serviceId.longValue(), serviceName, qsUpstreamId);
					serviceMap.put(serviceId.longValue(), serviceBean);
					if (qsUpstreamId != null && !qsUpstreamId.isEmpty())
						upstreamIdServiceMap.put(qsUpstreamId, serviceBean);
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return serviceMap;
	}

	/**
	 * productId,EntitlementBean Map
	 * 
	 * @throws QuoddException
	 */
	public static Map<Long, Set<Product>> loadProductMap(Map<String, Set<Product>> dbCodeProductMap)
			throws QuoddException {
		Map<Long, Set<Product>> entitlementIdProductMap = new HashMap<>();
		String url = QSS4_DOMAIN + "/internal/syncqs/product/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					String qsDbCode = (String) lookup.get(QuoddSyncConstants.QS_DB_CODE);
					Double productId = (Double) lookup.get(QuoddSyncConstants.PRODUCT_ID);
					Double qsEntitlementIdStr = (Double) lookup.get(QuoddSyncConstants.QS_ENTITLEMENT_ID);
					String feedQuality = (String) lookup.get(QuoddSyncConstants.feedQuality);
					long qsEntitlementId = qsEntitlementIdStr == null ? 0 : qsEntitlementIdStr.longValue();
					String productName = (String) lookup.get(QuoddSyncConstants.PRODUCT_NAME);
					Product productBean = new Product(productId.longValue(), qsDbCode, productName, qsEntitlementId,
							feedQuality);
					if (qsDbCode != null && !qsDbCode.isEmpty())
						dbCodeProductMap.computeIfAbsent(qsDbCode, k -> new HashSet<>()).add(productBean);
					if (qsEntitlementId > 0)
						entitlementIdProductMap.computeIfAbsent(qsEntitlementId, k -> new HashSet<>()).add(productBean);
					if ((qsDbCode == null || qsDbCode.isEmpty()) && qsEntitlementId == 0)
						logger.warning("No Mapping " + productBean);
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return entitlementIdProductMap;
	}

	/**
	 * add service
	 */
	public static boolean addService(Service service) {
		String addUrl = QSS4_DOMAIN + "/internal/syncqs/service/add";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + addUrl);
		try {
			URL obj = new URL(addUrl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			JsonObject data = new JsonObject();
			if (service.getServiceId() != 0)
				data.addProperty(QuoddSyncConstants.SERVICE_ID, service.getServiceId());
			data.addProperty(QuoddSyncConstants.SERVICE_NAME, service.getServiceName());
			data.addProperty(QuoddSyncConstants.QS_UPSTREAM_ID, service.getQuoddUpstreamId());
			con.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream());) {
				wr.writeBytes(data.toString());
				wr.flush();
			}
			int responseCode = con.getResponseCode();
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_POST_REQUEST + addUrl + " " + responseCode);
			logger.info(() -> "Post parameters for Add Service : " + data.toString());
			if (responseCode != HttpURLConnection.HTTP_OK) {
				return false;
			}
		} catch (Exception e) {
			logger.warning(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * add product
	 */
	public static boolean addProduct(Product product) {
		String url = QSS4_DOMAIN + "/internal/syncqs/product/add";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		utility.addProductHeaderToMailList();
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			JsonObject data = new JsonObject();
			data.addProperty(QuoddSyncConstants.PRODUCT_NAME, product.getProductName());
			data.addProperty(QuoddSyncConstants.QS_DB_CODE, product.getQuoddDbName());
			data.addProperty(QuoddSyncConstants.QS_ENTITLEMENT_ID, product.getQuoddEntitlementId());
			con.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream());) {
				wr.writeBytes(data.toString());
				wr.flush();
			}
			int responseCode = con.getResponseCode();
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_POST_REQUEST + url + " " + responseCode);
			logger.info(() -> "Post parameters : " + data.toString());
			if (responseCode != HttpURLConnection.HTTP_OK) {
				return false;
			}
			utility.addProductToMailList(product.getProductName(), product.getQuoddDbName(),
					product.getQuoddEntitlementId() + "", "NEW_PRODUCT_ADDED", "");
		} catch (Exception e) {
			logger.warning(e.getMessage());
			return false;
		}
		return true;
	}

	public static Map<Long, Set<Long>> loadProductServiceMap() throws QuoddException {
		Map<Long, Set<Long>> productServicesMap = new HashMap<>();
		String url = QSS4_DOMAIN + "/internal/syncqs/productservice/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					Double productId = (Double) lookup.get(QuoddSyncConstants.PRODUCT_ID);
					Double serviceId = (Double) lookup.get(QuoddSyncConstants.SERVICE_ID);
					productServicesMap.computeIfAbsent(productId.longValue(), k -> new HashSet<>())
							.add(serviceId.longValue());
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return productServicesMap;
	}

	public static Map<Long, String> loadServiceUpstreamMap() throws QuoddException {
		Map<Long, String> serviceUpstreamMap = new HashMap<>();
		String url = QSS4_DOMAIN + "/internal/syncqs/service/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					Double serviceId = (Double) lookup.get(QuoddSyncConstants.SERVICE_ID);
					String qsUpstreamId = (String) lookup.get(QuoddSyncConstants.QS_UPSTREAM_ID);
					if (qsUpstreamId != null && !qsUpstreamId.isEmpty())
						serviceUpstreamMap.put(serviceId.longValue(), qsUpstreamId);
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return serviceUpstreamMap;
	}

	public static Map<Long, Set<Long>> loadEntitlementProductMap() throws QuoddException {
		Map<Long, Set<Long>> entitlementProductMap = new HashMap<>();
		String url = QSS4_DOMAIN + "/internal/syncqs/product/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					Double productId = (Double) lookup.get(QuoddSyncConstants.PRODUCT_ID);
					Double qsEntitlementIdStr = (Double) lookup.get(QuoddSyncConstants.QS_ENTITLEMENT_ID);
					long qsEntitlementId = qsEntitlementIdStr == null ? 0 : qsEntitlementIdStr.longValue();
					if (qsEntitlementId > 0)
						entitlementProductMap.computeIfAbsent(qsEntitlementId, k -> new HashSet<>())
								.add(productId.longValue());
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return entitlementProductMap;
	}

	/**
	 * add product SeviceLink
	 */
	public static void addProductService(long productId, long serviceId) {
		String url = QSS4_DOMAIN + "/internal/syncqs/product/" + productId + "/servicelink/add/" + serviceId;
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_POST_REQUEST + url + " " + responseCode);
		} catch (Exception e) {
			logger.warning(e.getMessage());
			// utility.addToMailList(0, url, "Failed", e.getMessage(), "");
		}
	}

	/**
	 * List of products
	 * 
	 * @throws QuoddException
	 */
	public static List<Long> loadProductServiceLink(long productId) throws QuoddException {
		ArrayList<Long> serviceList = new ArrayList<>();
		String url = QSS4_DOMAIN + "/internal/syncqs/product/" + productId + "/servicelink/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<Long>>() {
				}.getType();
				serviceList = gson.fromJson(responseStr, type);
			}
			con.disconnect();
		} catch (QuoddException e) {
			logger.warning(e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return serviceList;
	}

	// add qs users to qss4
	public static void addQsUser(User user, Firm qsFirm, long qss4FirmId) {

		String url = QSS4_DOMAIN + "/internal/syncqs/user/seat";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url + " cid : " + user.getClientUserId());
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			JsonObject data = new JsonObject();
			data.addProperty("client_user_id", user.getClientUserId());
			data.addProperty("client_firm_id", qss4FirmId);
			data.addProperty("user_name", user.getUserName());
			data.addProperty("password", user.getPassword());
			data.addProperty("user_type", user.getUserType());
			data.addProperty("status", user.getStatus());
			data.addProperty("creation_time", user.getCreationTime());
			data.addProperty("update_time", user.getUpdateTime());
			if (user.getCancelledDate() != 0)
				data.addProperty("cancelled_time", user.getCancelledDate());
			data.addProperty("first_name", user.getFirstname());
			data.addProperty("last_name", user.getLastName());
			data.addProperty("address_line_1", user.getAddressLine1());
			data.addProperty("address_line_2", user.getAddressLine2());
			data.addProperty("city", user.getCity());
			data.addProperty("state", user.getState());
			data.addProperty("zip", user.getZip());
			data.addProperty("country", user.getCountry());
			data.addProperty("email", user.getEmail());
			data.addProperty("phone", user.getPhone());
			data.addProperty("exchange_id", qsFirm.getExchange());
			data.addProperty("phone", user.getPhone());
			data.addProperty("exchange_id", qsFirm.getExchange());
			data.addProperty("device_Type", user.getDeviceType());
			data.addProperty("dataFeed_Type", qsFirm.getDataFeedType());
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(data.toString());
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				String responseStr = "";
				StringBuilder sb = new StringBuilder();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream()));) {
					while ((responseStr = br.readLine()) != null) {
						sb.append(responseStr);
					}
				}
				logger.warning("FAILED_ADD_USER " + user.getClientUserId() + ", Response : " + sb.toString());
			}
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_POST_REQUEST + url + " " + responseCode);
			logger.info(() -> "Post parameters : " + data.toString());
		} catch (IOException e) {
			logger.warning(e.getMessage());
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}

	// add user Product
	public static void addUserProduct(long clientUserId, String products) {
		String url = QSS4_DOMAIN + "/internal/syncqs/cid/" + clientUserId + "/product/" + products;
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_POST_REQUEST + url + " " + responseCode);
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}

	public static Map<Long, User> loadUserList() throws QuoddException {
		HashMap<Long, User> activeUserBeanMap = new HashMap<>();
		String url = QSS4_DOMAIN + "/internal/syncqs/user/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				long count = 0;
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>();
					lookup.putAll(bean);
					User user = new User();
					Double clientUserId = (Double) lookup.get("client_user_id");
					user.setClientUserId(clientUserId.longValue());
					String username = (String) lookup.get("user_name");
					user.setUserName(username);
					int status = ((Double) lookup.get("status")).intValue();
					user.setStatus(status);
					user.setUserType(((Double) lookup.get("user_type")).intValue());
					user.setSeatType(((Double) lookup.get("seat_type")).intValue());
					String deviceType = (String) lookup.get("device_type");
					user.setDeviceType(deviceType);
					String datafeedType = (String) lookup.get("datafeed_type");
					user.setDataFeedType(datafeedType);
					String exchangeId = (String) lookup.get("exchange_id");
					user.setExchangeId(exchangeId);
					String firstName = (String) lookup.get("first_name");
					user.setFirstname(firstName);
					String lastName = (String) lookup.get("last_name");
					user.setLastName(lastName);
					String email = (String) lookup.get("email");
					user.setEmail(email);
					String addressLine1 = (String) lookup.get("address_line_1");
					user.setAddressLine1(addressLine1);
					String addressLine2 = (String) lookup.get("address_line_2");
					user.setAddressLine2(addressLine2);
					String city = (String) lookup.get("city");
					user.setCity(city);
					String state = (String) lookup.get("state");
					user.setState(state);
					String zip = (String) lookup.get("zip");
					user.setZip(zip);
					String country = (String) lookup.get("country");
					user.setCountry(country);
					Double tempClientFirmId = (Double) lookup.get("client_firm_id");
					long clientFirmId = tempClientFirmId == null ? 0 : tempClientFirmId.longValue();
					user.setClientFirmId(clientFirmId);
					String cancelled = (String) lookup.get("deactivation_time");
					if (cancelled != null) {
						LocalDate deactivationTime = LocalDateTime.parse(cancelled, qss4TimeFormatter).toLocalDate();
						Long deactivationTimeMs = deactivationTime.atStartOfDay(ZoneOffset.UTC).toInstant()
								.toEpochMilli();
						user.setCancelledDate(deactivationTimeMs);
					} else {
						user.setCancelledDate(0l);
					}
					user.setCancelledTime(cancelled);
					activeUserBeanMap.put(clientUserId.longValue(), user);

					count++;
				}
				logger.info("QSS4 count " + count);
			}
			con.disconnect();
		} catch (QuoddException e) {
			logger.warning(e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return activeUserBeanMap;
	}

	public static List<Long> activeUserList() {
		ArrayList<Long> clientUserIds = new ArrayList<>();
		String url = QSS4_DOMAIN + "/internal/syncqs/user/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>();
					lookup.putAll(bean);
					User user = new User();
					Double clientUserId = (Double) lookup.get("client_user_id");
					user.setClientUserId(clientUserId.longValue());
					String username = (String) lookup.get("user_name");
					user.setUserName(username);
					int status = ((Double) lookup.get("status")).intValue();
					user.setStatus(status);
					if (status == 4) {
						clientUserIds.add(clientUserId.longValue());
					}
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return clientUserIds;
	}

	public static void cancelUser(long cid) {
		String url = QSS4_DOMAIN + "/api/cid/" + cid + "/user/cancel";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}

	public static void reactivateUser(long cid) {
		String url = QSS4_DOMAIN + "/api/cid/" + cid + "/user/reactivate";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}

	/*
	 * List of Product By User (cid)
	 */

	public static List<Map<String, Object>> loadProductByuser(long clientUserId) throws QuoddException {
		List<Map<String, Object>> productStatusMap = new ArrayList<>();
		String url = QSS4_DOMAIN + "/api/cid/" + clientUserId + "/product/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<List<Map<String, Object>>>() {
				}.getType();
				productStatusMap = gson.fromJson(responseStr, type);
			}
			con.disconnect();
		} catch (QuoddException e) {
			logger.warning(e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return productStatusMap;
	}

	/*
	 * delete user Product
	 */

	public static void deleteUserProduct(long clientUserId, String products) {
		String url = QSS4_DOMAIN + "/api/cid/" + clientUserId + "/product/" + products + "/delete";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}

	public static void updateUserName(String username, long cid) {
		String url = QSS4_DOMAIN + "/internal/syncqs/user/update";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			JsonObject data = new JsonObject();
			data.addProperty("user_name", username);
			data.addProperty("client_user_id", cid);
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(data.toString());
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_POST_REQUEST + url + " " + responseCode);
			logger.info(() -> "Post parameters : " + data.toString());
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}

	public static void updateUserInfo(long cid, Map<String, Object> lookup) {
		String url = QSS4_DOMAIN + "/api/cid/" + cid + "/user/update";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			JsonObject data = new JsonObject();
			for (Map.Entry<String, Object> entry : lookup.entrySet()) {
				if (entry.getKey().equals("user_type")) {
					data.addProperty(entry.getKey(), ((Integer) entry.getValue()).intValue());
				} else if (entry.getKey().equals("client_firm_id")) {
					data.addProperty(entry.getKey(), ((Long) entry.getValue()).longValue());
				} else {
					data.addProperty(entry.getKey(), entry.getValue().toString());
				}
			}
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(data.toString());
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_POST_REQUEST + url + " " + responseCode);
			logger.info(() -> "Post parameters : " + data.toString());
			con.disconnect();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}

	// public static Map<String, Long> getUpstreamIdServiceIdMap() {
	// HashMap<String, Long> upstreamIdServiceIDMap = new HashMap<>();
	// String url = QSS4_DOMAIN + "/internal/syncqs/service/list";
	// logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
	// try {
	// URL obj = new URL(url);
	// HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	// con.setRequestMethod("GET");
	// con.setDoOutput(true);
	// con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
	// int responseCode = con.getResponseCode();
	// logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " +
	// responseCode);
	// try (BufferedReader br = new BufferedReader(new
	// InputStreamReader(con.getInputStream()));) {
	// String responseStr = br.readLine();
	// Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
	// }.getType();
	// ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
	// for (HashMap<String, Object> bean : list) {
	// Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	// lookup.putAll(bean);
	// Double serviceId = (Double) lookup.get(QuoddSyncConstants.SERVICE_ID);
	// String quoddUpstreamId = (String)
	// lookup.get(QuoddSyncConstants.QS_UPSTREAM_ID);
	// upstreamIdServiceIDMap.put(quoddUpstreamId, serviceId.longValue());
	//
	// }
	// }
	// con.disconnect();
	// } catch (Exception e) {
	// logger.log(Level.WARNING, e.getMessage(), e);
	// }
	// return upstreamIdServiceIDMap;
	// }

	// public static Map<Long, Long> getEntitlementIdProductIdMap() {
	// HashMap<Long, Long> entitlementIdProductIdMap = new HashMap<>();
	// String url = QSS4_DOMAIN + "/internal/syncqs/product/list";
	// logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
	// try {
	// URL obj = new URL(url);
	// HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	// con.setRequestMethod("GET");
	// con.setDoOutput(true);
	// con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
	// int responseCode = con.getResponseCode();
	// logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " +
	// responseCode);
	// try (BufferedReader br = new BufferedReader(new
	// InputStreamReader(con.getInputStream()));) {
	// String responseStr = br.readLine();
	// Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
	// }.getType();
	// ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
	// for (HashMap<String, Object> bean : list) {
	// Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	// lookup.putAll(bean);
	// Double productId = (Double) lookup.get(QuoddSyncConstants.PRODUCT_ID);
	// Double quoddEntitlementId = (Double)
	// lookup.get(QuoddSyncConstants.QS_ENTITLEMENT_ID);
	// entitlementIdProductIdMap.put(quoddEntitlementId.longValue(),
	// productId.longValue());
	// }
	// }
	// con.disconnect();
	// } catch (Exception e) {
	// logger.log(Level.WARNING, e.getMessage(), e);
	// }
	// return entitlementIdProductIdMap;
	// }

	public static void updateFirmInfo(Map<String, Object> lookup, long firmId) {
		try {
			String url = QSS4_DOMAIN + "/api/firm/firmid/" + firmId + "/update?is_qs_firm=1";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			JsonObject data = new JsonObject();
			for (Map.Entry<String, Object> entry : lookup.entrySet()) {
				if (entry.getKey().equalsIgnoreCase("is_dowjones_billable")
						|| entry.getKey().equalsIgnoreCase("firm_type"))
					data.addProperty(entry.getKey(), (int) entry.getValue());
				else
					data.addProperty(entry.getKey(), entry.getValue().toString());
			}
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(data.toString());
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			logger.info(() -> QuoddSyncConstants.SENDING_POST_REQUEST + url + " " + responseCode);
			logger.info(() -> "Post parameters : " + data.toString());
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}

	public static long addFirm(Firm firm, long qsFirmId, Long parentId) {
		long firmId = 0;
		try {
			String url = QSS4_DOMAIN + "/internal/syncqs/firm/add";
			logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url + " fid : " + firm.getQsFirmId());
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			JsonObject data = new JsonObject();
			data.addProperty("firm_name", firm.getFirmName());
			data.addProperty("address_line_1", firm.getAddressLine1());
			data.addProperty("address_line_2", firm.getAddressLine2());
			data.addProperty("city", firm.getCity());
			data.addProperty("state", firm.getState());
			data.addProperty("zip", firm.getZip());
			data.addProperty("country", firm.getCountry());
			data.addProperty("email", firm.getEmail());
			data.addProperty("phone", firm.getPhone());
			data.addProperty("exchange_id", String.valueOf(firm.getExchange()));
			data.addProperty("datafeed_type", firm.getDataFeedType());
			data.addProperty("device_type", firm.getDeviceType());
			data.addProperty("firm_type", firm.getUserType());
			if (parentId != null)
				data.addProperty("parent_firm_id", parentId);
			data.addProperty("client_firm_id", qsFirmId);
			// Send post request
			con.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream());) {
				wr.writeBytes(data.toString());
				wr.flush();
			}
			int responseCode = con.getResponseCode();
			logger.info(() -> QuoddSyncConstants.SENDING_POST_REQUEST + url + " " + responseCode);
			logger.info(() -> "Post parameters : " + data.toString());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<HashMap<String, String>>() {
				}.getType();
				HashMap<String, String> resultMap = gson.fromJson(responseStr, type);
				String status = resultMap.get("status");
				if (status.equals("SUCCESS")) {
					String tempString = resultMap.get("firmId");
					firmId = Long.valueOf(tempString);
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return firmId;
	}

	public static Map<Long, Firm> loadEqPlusFirms(Map<String, Long> exchangeIdFirmMap, Map<Long, Long> qss4QsFirmIdMap)
			throws QuoddException {
		Map<Long, Firm> qss4FirmIdBeanMap = new HashMap<>();
		String url = QSS4_DOMAIN + "/internal/syncqs/firm/list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qss4Authorization);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					Double firmId = (Double) lookup.get(QuoddSyncConstants.FIRM_ID);
					Double qsFirmId = (Double) lookup.get(QuoddSyncConstants.QS_FIRM_ID);
					String firmName = (String) lookup.get(QuoddSyncConstants.FIRM_NAME);
					String zip = (String) lookup.get(QuoddSyncConstants.ZIP);
					String phone = (String) lookup.get(QuoddSyncConstants.PHONE);
					String email = (String) lookup.get(QuoddSyncConstants.EMAIL);
					String address1 = (String) lookup.get(QuoddSyncConstants.ADDRESS_LINE_1);
					String address2 = (String) lookup.get(QuoddSyncConstants.ADDRESS_LINE_2);
					String city = (String) lookup.get(QuoddSyncConstants.CITY);
					String state = (String) lookup.get(QuoddSyncConstants.STATE);
					String country = (String) lookup.get(QuoddSyncConstants.COUNTRY);
					String exchange = (String) lookup.get(QuoddSyncConstants.EXCHANGE_ID);
					Double parentFirmId = (Double) lookup.get("parent_firm_id");
					String deviceType = (String) lookup.get(QuoddSyncConstants.DEVICE_TYPE);
					String dataFeedType = (String) lookup.get(QuoddSyncConstants.DATAFEED_TYPE);
					Double userType = (Double) lookup.get(QuoddSyncConstants.FIRM_TYPE);
					int dowjonesBillable = ((Double) lookup.get("is_dowjones_billable")).intValue();
					Firm firm = new Firm();
					firm.setFirmName(firmName);
					firm.setExchange(exchange);
					firm.setAddressLine1(address1);
					firm.setAddressLine2(address2);
					firm.setCity(city);
					firm.setCountry(country);
					firm.setPhone(phone);
					firm.setEmail(email);
					firm.setState(state);
					firm.setZip(zip);
					firm.setFirmId(firmId.longValue());
					firm.setDeviceType(deviceType);
					firm.setDataFeedType(dataFeedType);
					firm.setDowjonesBillable(dowjonesBillable);
					if (parentFirmId != null)
						firm.setParentId(parentFirmId.longValue());
					if (userType != null)
						firm.setUserType(userType.intValue());
					if (qsFirmId != null) {
						qss4FirmIdBeanMap.put(qsFirmId.longValue(), firm);
						qss4QsFirmIdMap.put(firmId.longValue(), qsFirmId.longValue());
					}
					if (exchange != null) {
						exchangeIdFirmMap.put(exchange, firmId.longValue());
					}
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return qss4FirmIdBeanMap;
	}

	public static HashMap<Long, User> loadQss4DeletedUsers() throws QuoddException {
		HashMap<Long, User> qss4ClientUserIdBeanMap = new HashMap<>();
		try {
			String userListUrl = QSS4_DOMAIN + "/api/user/deleted/list";
			URL obj = new URL(userListUrl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setRequestProperty("Authorization", qss4Authorization);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException("NO_RESPONSE_FOUND");
			}
			logger.info("Sending 'GET' request to URL :" + userListUrl + " : " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();

				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					User user = new User();
					Double clientUserId = (Double) lookup.get("client_user_id");
					user.setClientUserId(clientUserId.longValue());
					String username = (String) lookup.get("user_name");
					user.setUserName(username);
					int status = ((Double) lookup.get("status")).intValue();
					user.setStatus(status);
					Double seatType = (Double) lookup.get("seat_type");
					user.setSeatType(seatType.intValue());
					String creationTime = (String) lookup.get("creation_time");
					if (creationTime != null) {
						LocalDate temp = LocalDateTime.parse(creationTime, qss4TimeFormatter).toLocalDate();
						Long creationTimeMs = temp.atStartOfDay(ZoneOffset.UTC).toInstant()
								.toEpochMilli();
						user.setCreationTime(creationTimeMs);
					}
					String deactivationTime = (String) lookup.get("deactivation_time");
					if (deactivationTime != null) {
						LocalDate temp = LocalDateTime.parse(creationTime, qss4TimeFormatter).toLocalDate();
						Long deactivationTimeMs = temp.atStartOfDay(ZoneOffset.UTC).toInstant()
								.toEpochMilli();
						user.setCancelledDate(deactivationTimeMs);
					}
					qss4ClientUserIdBeanMap.put(clientUserId.longValue(), user);
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return qss4ClientUserIdBeanMap;
	}
}
