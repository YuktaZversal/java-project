package com.quodd.util;

import static com.quodd.EquityPlusSyncApplication.gson;
import static com.quodd.EquityPlusSyncApplication.logger;
import static com.quodd.EquityPlusSyncApplication.qsAuthorization;
import static com.quodd.EquityPlusSyncApplication.QS_DOMAIN;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.quodd.bean.Entitlement;
import com.quodd.bean.Firm;
import com.quodd.bean.Upstream;
import com.quodd.bean.User;
import com.quodd.exception.QuoddException;

public interface QuoddSupportHelper {

	/**
	 * protocolId, UpstreamBean
	 * 
	 * @throws QuoddException
	 */
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static Map<Long, Upstream> loadUpstreamMap() throws QuoddException {
		Map<Long, Upstream> upstreamMap = new HashMap<>();
		String url = QS_DOMAIN + "/UpstreamApi.php?action=list&module=upstream";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String responseStr = br.readLine();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					Double upstreamId = (Double) lookup.get("upstream_id");
					Double protocolId = (Double) lookup.get("protocol_id");
					String name = (String) lookup.get("name");
					Upstream upstreamBean = new Upstream(protocolId.longValue(), upstreamId.longValue(), name);
					upstreamMap.put(upstreamId.longValue(), upstreamBean);
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return upstreamMap;
	}

	public static Map<String, Entitlement> loadUpstreamFromEntitlement(Set<String> cancelEntitlementMap)
			throws QuoddException {
		Map<String, Entitlement> backendKeyEntitlementMap = new HashMap<>();
		try {
			String url = QS_DOMAIN + "/EntitlementApi.php?module=entitlement&action=list";
			logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(sb.toString(), type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					Double entitlementId = (Double) lookup.get("entitlement_id"); // it is now double.
					String fullName = (String) lookup.get("full_name");
					String dbName = (String) lookup.get("db_name");
					String backendKey = (String) lookup.get("backend_key");
					String feedQuality = (String) lookup.get("real_time");
					Entitlement entitlementBean = new Entitlement();
					entitlementBean.setEntitlementId(entitlementId.longValue());
					entitlementBean.setFullName(fullName);
					entitlementBean.setDbFieldName(dbName);
					entitlementBean.setStatus(((Double) lookup.get("status")).longValue());
					entitlementBean.setBackendKey(backendKey);
					entitlementBean.setFeedQuality(feedQuality);
					if (entitlementBean.getStatus() == 1) {
						if (backendKey != null && !backendKey.isEmpty())
							backendKeyEntitlementMap.put(backendKey, entitlementBean);
					} else {
						cancelEntitlementMap.add(dbName);
						logger.info(
								() -> "Entitlement Disabled. Name: " + fullName + "entitlementId: " + entitlementId
										+ "dbName: " + dbName);
					}
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return backendKeyEntitlementMap;
	}

	/**
	 * EntitlementId,EntitlementBean Map
	 * 
	 * @throws QuoddException
	 */
	public static Map<Long, Entitlement> loadEntitlementMap() throws QuoddException {
		Map<Long, Entitlement> entitlementMap = new HashMap<>();
		try {
			String url = QS_DOMAIN + "/EntitlementApi.php?module=entitlement&action=list";
			logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(sb.toString(), type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					Double entitlementId = (Double) lookup.get("entitlement_id"); // it is now double.
					String fullName = (String) lookup.get("full_name");
					String dbName = (String) lookup.get("db_name");
					String backendKey = (String) lookup.get("backend_key");
					String feedQuality = (String) lookup.get("real_time");
					Entitlement entitlementBean = new Entitlement();
					entitlementBean.setEntitlementId(entitlementId.longValue());
					entitlementBean.setFullName(fullName);
					entitlementBean.setDbFieldName(dbName);
					entitlementBean.setStatus(((Double) lookup.get("status")).longValue());
					entitlementBean.setBackendKey(backendKey);
					entitlementBean.setFeedQuality("Y".equalsIgnoreCase(feedQuality) ? "R" : "D");
					if (entitlementBean.getStatus() == 1) {
						entitlementMap.put(entitlementId.longValue(), entitlementBean);
					} else {
						logger.info(
								() -> "Entitlement Disabled. Name: " + fullName + "entitlementId: " + entitlementId
										+ "dbName: " + dbName);
					}
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
		return entitlementMap;
	}

	/**
	 * upstreamId,EntitlementId Map
	 * 
	 * @throws QuoddException
	 */
	public static Map<Long, Set<String>> loadEntitlementUpstreamMap() throws QuoddException {
		HashMap<Long, Set<String>> entitlementUpstreamMap = new HashMap<>();
		String url = QS_DOMAIN + "/UpstreamLinkApi.php?action=list&module=upstream_link";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String line;
				StringBuilder sb = new StringBuilder();
				// added this , otherwise it is giving null pointer.
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				String responseStr = sb.toString();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					Double upstreamId = (Double) lookup.get("upstream_id");
					Double entitlementId = (Double) lookup.get("entitlement_id");
					entitlementUpstreamMap.computeIfAbsent(entitlementId.longValue(), k -> new HashSet<>())
							.add(Long.toString(upstreamId.longValue()));
				}
			}
			con.disconnect();
		} catch (QuoddException e) {
			logger.warning(e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return entitlementUpstreamMap;
	}

	public static void updateEntitlementUpstreamMap(Map<Long, Set<String>> entitlementUpstreamMap)
			throws QuoddException {
		try {
			String url = QS_DOMAIN + "/EntitlementApi.php?module=entitlement&action=list";
			logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> list = gson.fromJson(sb.toString(), type);
				for (HashMap<String, Object> bean : list) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					Double entitlementId = (Double) lookup.get("entitlement_id"); // it is now double.
					String backendKey = (String) lookup.get("backend_key");
					long status = ((Double) lookup.get("status")).longValue();
					if (status != 1)
						entitlementUpstreamMap.remove(entitlementId.longValue());
					else if (backendKey != null && !backendKey.isEmpty())
						entitlementUpstreamMap.computeIfAbsent(entitlementId.longValue(), k -> new HashSet<>())
								.add(backendKey);
				}
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			throw new QuoddException(e.getMessage());
		}
	}

	public static Map<String, Object> getUserSeatEntitlementMap(long seatId) {
		HashMap<String, Object> userSeatEntitlementMap = new HashMap<>();
		String url = QS_DOMAIN + "/SeatApi.php?action=seat_detail&module=seat&s_seat_id=" + seatId;
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new Exception(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				Type type = new TypeToken<HashMap<String, Object>>() {
				}.getType();
				userSeatEntitlementMap = gson.fromJson(sb.toString(), type);
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return userSeatEntitlementMap;
	}

	public static Map<Long, User> loadSeatUserMapList(Map<Long, User> cancelledSeatUserMap,
			Map<Long, User> cancelledZeroUserMap, Map<Long, User> internalUserMap) throws QuoddException {
		Map<Long, User> activeSeatUserMap = new HashMap<>();
		String url = QS_DOMAIN + "/SeatApi.php?action=list&module=seat";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		Set<String> activeUsernameSet = new HashSet<>();
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new Exception(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				String responseStr = sb.toString();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> userMapList = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : userMapList) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					User user = new User();
					Double seatId = (Double) lookup.get("seat_id");
					long seatIdL = seatId.longValue();
					long firmId = ((Double) lookup.get("firm_id")).longValue();
					Double statusD = (Double) lookup.get("status");
					String zip = (String) lookup.get("zip");
					Double userType = (Double) lookup.get("pro");
					String first = (String) lookup.get("first");
					String last = (String) lookup.get("last");
					String phone = (String) lookup.get("phone");
					String email = (String) lookup.get("email");
					String userName = (String) lookup.get("username");
					if (userName == null || userName.trim().isEmpty())
						continue;
					String passWord = (String) lookup.get("password");
					String address1 = (String) lookup.get("address1");
					String address2 = (String) lookup.get("address2");
					String city = (String) lookup.get("city");
					String state = (String) lookup.get("state");
					String country = (String) lookup.get("country");
					String addedStr = (String) lookup.get("added");
					String editedStr = (String) lookup.get("edited");
					String cancelledStr = (String) lookup.get("canceled");
					String datafeed = (String) lookup.get("datafeed");
					String deviceType = (datafeed.equalsIgnoreCase("1")) ? "D" : "C";
					String contractEffective = (String) lookup.get("effective");
					String firmType = (String) lookup.get("firm_type");
					String exchangeId = (String) lookup.get("exchange_id");
					user.setClientUserId(seatIdL);
					if (userType != null)
						user.setUserType(userType.intValue());
					else
						user.setUserType(1);
					user.setFirmId(firmId);
					user.setFirstname(first);
					user.setLastName(last);
					user.setAddressLine1(address1);
					user.setAddressLine2(address2);
					user.setCity(city);
					user.setCountry(country);
					user.setPassword(passWord);
					user.setPhone(phone);
					user.setEmail(email);
					user.setUserName(userName);
					user.setState(state);
					user.setZip(zip);
					user.setDeviceType(deviceType);
					user.setDataFeedType(firmType);
					user.setExchangeId(exchangeId);
					user.setCancelledTime(cancelledStr);
					user.setCancelledEffectiveTime(contractEffective);
					int status = statusD.intValue();
					if (status == 1 || status == 3) {
						if (status == 3) {
							internalUserMap.put(seatIdL, user);
						}
						// status 3 is Internal, 6 in backup-active
						status = 4;// active
						activeSeatUserMap.put(seatIdL, user);
						activeUsernameSet.add(user.getUserName().toLowerCase());
					} else if (status == 9 || status == 5) {
						if (status == 5 && "0000-00-00".equals(cancelledStr))
							logger.log(Level.INFO, "Backup Users :" + seatId.toString() + " : userName : " + userName);
						status = 6;// cancelled
						cancelledSeatUserMap.put(seatIdL, user);
					} else if (status == 8) {
						// Early - Access
						user.setSeatType(QuoddSyncConstants.USER_TYPE_TRIAL);
					} else {
						// could be Internal, Backup, backup-Active or early - Access
						// if (status == 5) // Backup - ignore them
						// continue;
						status = 4;// active
						activeUsernameSet.add(user.getUserName().toLowerCase());
					}
					user.setStatus(status);
					if (status == 6 && "0000-00-00".equals(cancelledStr)) {
						// logger.info("Dropping_cancelled_zero " + firmId + "," + seatIdL);
						cancelledZeroUserMap.put(seatIdL, user);
						cancelledSeatUserMap.remove(seatIdL);
						continue;
					}
					if (contractEffective == null)
						contractEffective = "0000-00-00";
					if (status == 4 && contractEffective.equals("0000-00-00")) {
						logger.warning("Dropping epoch time " + firmId);
						activeSeatUserMap.remove(seatIdL);
						continue;
					}
					if (contractEffective.equals("0000-00-00")) {
						contractEffective = (addedStr.split(" "))[0];
					}
					if (addedStr.equals("0000-00-00 00:00:00")) {
						addedStr = contractEffective + " 12:00:00";
					}

					LocalDate effectiveDate = LocalDate.parse(contractEffective, dateFormatter);
					long effectiveDateMs = effectiveDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

					LocalDate cancelledDate;
					long cancelledDateMs = 0l;
					if (!cancelledStr.equals("0000-00-00")) {
						cancelledDate = LocalDate.parse(cancelledStr, dateFormatter);
						cancelledDateMs = cancelledDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
					} else {
						cancelledDate = LocalDate.MIN;
					}

					LocalDateTime addedDateTime = LocalDateTime.parse(addedStr, dateTimeFormatter);
					long addedDateMs = addedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

					LocalDateTime editDateTime;
					long editDateMs = 0l;
					if (!editedStr.equals("0000-00-00 00:00:00")) {
						editDateTime = LocalDateTime.parse(editedStr, dateTimeFormatter);
						editDateMs = editDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
					} else {
						editDateTime = LocalDateTime.MIN;
					}

					if (effectiveDate.isAfter(addedDateTime.toLocalDate()))
						user.setCreationTime(effectiveDateMs);
					else
						user.setCreationTime(addedDateMs);
					if (editDateTime.isAfter(addedDateTime))
						user.setUpdateTime(editDateMs);
					else
						user.setUpdateTime(user.getCreationTime());
					if (status == 4)
						user.setCancelledDate(0);
					else
						user.setCancelledDate(cancelledDateMs);

					if (user.getCreationTime() <= 0)
						logger.warning("WRONG_TIME_CREATION " + firmId + "," + seatIdL);
					if (user.getUpdateTime() <= 0)
						logger.warning("WRONG_TIME_UPDATE " + firmId + "," + seatIdL);
					if (user.getCancelledDate() <= 0 && status == 6)
						logger.warning("WRONG_TIME_CANCEL " + firmId + "," + seatIdL);
					if (exchangeId == null || exchangeId.isEmpty())
						logger.warning("No exchange Id for " + firmId + "," + seatIdL + "," + status);
				}
			}
			con.disconnect();
			// Set<Long> cancelledSeatToDelete = new HashSet<>();
			// cancelledSeatUserMap.forEach((id, user) -> {
			// if (activeUsernameSet.contains(user.getUserName().toLowerCase()))
			// cancelledSeatToDelete.add(id);
			// });
			// logger.info("Duplicate cancel user to be removed " +
			// cancelledSeatToDelete.size());
			// cancelledSeatToDelete.forEach(cancelledSeatUserMap::remove);

			// quoddSupportCache.setQuoddSupportActiveUser(activeSeatUserMap);
			// quoddSupportCache.setQuoddSupportCancelledUser(cancelledSeatUserMap);
			logger.info("Active user QS Cache: " + activeSeatUserMap.size());
			logger.info("cancelled user QS Cache: " + cancelledSeatUserMap.size());
		} catch (QuoddException e) {
			logger.warning(e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return activeSeatUserMap;
		// Need to handle firms with datafeed only contract as well - default will be
		// pro q_contracts.contract_type = 2
		// For contract type 3, we need to report datafeed users separately
		// We need to discuss regarding entitlements of all datafeed type users
	}

	public static Map<Long, Firm> loadSeatFirmMapList(Map<String, Long> exchangeIdFirmMap, Set<Long> childAllowedSet)
			throws QuoddException {
		Map<Long, Set<Long>> parentChildMap = new HashMap<>();
		Map<Long, Firm> qsFirmIdBeanMap = new HashMap<>();

		SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String url = QS_DOMAIN + "/FirmApi.php?module=firm&action=list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();

			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				String responseStr = sb.toString();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> firmMapList = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : firmMapList) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					long firmId = ((Double) lookup.get("firm_id")).longValue();
					String firmType = (String) lookup.get("firm_type");
					long parentId = ((Double) lookup.get("parent_id")).longValue();
					long firmClass = ((Double) lookup.get("class")).longValue();
					String firmName = (String) lookup.get("firm");
					String exchangeId = (String) lookup.get("exchange_id");
					String address1 = (String) lookup.get("address1");
					String address2 = (String) lookup.get("address2");
					String city = (String) lookup.get("city");
					String state = (String) lookup.get("state");
					String zip = (String) lookup.get("zip");
					String country = (String) lookup.get("country");
					String phone = (String) lookup.get("phone");
					String email = (String) lookup.get("email");
					String datafeed = (String) lookup.get("datafeed");
					int dowjonesBill = 0;
					if (lookup.get("news_billing") != null)
						dowjonesBill = ((Double) lookup.get("news_billing")).intValue();
					String deviceType = (datafeed.equalsIgnoreCase("1")) ? "D" : "C";
					int allowChild = ((Double) lookup.get("children")).intValue();
					if (lookup.get("pro") == null) {
						logger.warning("No Contract for " + firmId);
						continue;
					}
					if (exchangeId == null || exchangeId.isEmpty()) {
						logger.warning("No exchangeid  for " + firmId);
					} else {
						if (exchangeIdFirmMap.containsKey(exchangeId))
							logger.warning("Duplicate ExchangeId " + exchangeId + ", " + firmId + ", "
									+ exchangeIdFirmMap.get(exchangeId));
						else
							exchangeIdFirmMap.put(exchangeId, firmId);
					}
					int userType = ((Double) lookup.get("pro")).intValue();
					String addedStr = (String) lookup.get("added");
					String contractEffective = (String) lookup.get("effective");
					Date addedDate;
					Date contractEffectiveDate;
					if (addedStr == null)
						addedDate = new Date(0);
					else
						addedDate = sdfTime.parse(addedStr);
					if (contractEffective == null)
						contractEffectiveDate = new Date(0);
					else
						contractEffectiveDate = sdf.parse(contractEffective);
					if (contractEffectiveDate.getTime() == 0)
						logger.warning("Bad Contract effective date  " + firmId);
					if (addedDate.getTime() == 0 && contractEffectiveDate.getTime() == 0)
						logger.warning("Bad timestamps " + firmId);
					if (allowChild == 1)
						childAllowedSet.add(firmId);
					if (parentId != 0) {
						parentChildMap.computeIfAbsent(parentId, id -> new HashSet()).add(firmId);
					}
					Firm firmBean = new Firm();
					firmBean.setFirmId(firmId);
					firmBean.setParentId(parentId);
					firmBean.setFirmName(firmName);
					firmBean.setExchange(exchangeId);
					firmBean.setAddressLine1(address1);
					firmBean.setAddressLine2(address2);
					firmBean.setCity(city);
					firmBean.setState(state);
					firmBean.setZip(zip);
					firmBean.setCountry(country);
					firmBean.setPhone(phone);
					firmBean.setEmail(email);
					firmBean.setUserType(userType);
					firmBean.setDataFeedType(firmType);
					firmBean.setDeviceType(deviceType);
					firmBean.setDowjonesBillable(dowjonesBill);
					if (contractEffectiveDate.getTime() > addedDate.getTime())
						firmBean.setAddedDate(contractEffectiveDate);
					else
						firmBean.setAddedDate(addedDate);
					qsFirmIdBeanMap.put(firmId, firmBean);
				}
				Set<Long> testSet = new HashSet<>();
				testSet.addAll(childAllowedSet);
				testSet.removeAll(parentChildMap.keySet());
				testSet.forEach(id -> logger.warning("No child for parent firm " + id));

				testSet = new HashSet<>();
				testSet.addAll(parentChildMap.keySet());
				testSet.removeAll(childAllowedSet);
				testSet.forEach(id -> logger.warning("No allowChild for parent firm with children " + id));

				parentChildMap.forEach((pid, cids) -> {
					Firm parentBean = qsFirmIdBeanMap.get(pid);
					cids.forEach(cid -> {
						Firm childBean = qsFirmIdBeanMap.get(cid);
						if (parentBean.getUserType() != childBean.getUserType())
							logger.warning("User Type Mismatch parent: " + pid + "," + parentBean.getUserType()
									+ " child: " + cid + "," + childBean.getUserType());
					});
				});

			}
			con.disconnect();

		} catch (QuoddException e) {
			logger.warning(e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return qsFirmIdBeanMap;

		// Need to handle firms with datafeed only contract as well - default will be
		// pro q_contracts.contract_type = 2
		// For contract type 3, we need to report datafeed users separately
		// We need to discuss regarding entitlements of all datafeed type users
	}

	public static Map<Long, User> loadTrialUserMapList(Map<Long, User> cancelledTrialUserMap) throws QuoddException {
		Map<Long, User> activeTrialUserMap = new HashMap<>();
		SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String url = QS_DOMAIN + "/TrialApi.php?module=trial&action=list";
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		Set<String> activeUsernameSet = new HashSet<>();
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new QuoddException(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new Exception(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				String responseStr = sb.toString();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> userMapList = gson.fromJson(responseStr, type);
				for (HashMap<String, Object> bean : userMapList) {
					Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
					lookup.putAll(bean);
					User user = new User();
					Double trialId = (Double) lookup.get("id");
					String id = "10".concat(String.valueOf(trialId.longValue()));
					long trialIdL = Long.parseLong(id);
					long firmId = ((Double) lookup.get("firm_id")).longValue();
					Double statusD = (Double) lookup.get("status");
					int status = statusD.intValue();
					Double userType = (Double) lookup.get("pro");
					String email = (String) lookup.get("email");
					String userName = (String) lookup.get("username");
					if (userName == null || userName.trim().isEmpty())
						continue;
					String passWord = (String) lookup.get("password");
					String address1 = (String) lookup.get("address1");
					String address2 = (String) lookup.get("address2");
					String city = (String) lookup.get("city");
					String state = (String) lookup.get("state");
					String zip = (String) lookup.get("zip");
					String country = (String) lookup.get("country");
					String addedStr = (String) lookup.get("date_issued");
					String extendedStr = (String) lookup.get("date_extended");
					String cancelledStr = (String) lookup.get("date_expires");
					String datafeed = (String) lookup.get("datafeed");
					String deviceType = (datafeed.equalsIgnoreCase("1")) ? "D" : "C";
					String datafeed_type = (String) lookup.get("firm_type");
					String exchangeId = (String) lookup.get("exchange_id");
					user.setClientUserId(trialIdL);
					if (userType != null)
						user.setUserType(userType.intValue());
					else
						user.setUserType(0); // For trial default non-pro
					user.setFirmId(firmId);
					user.setAddressLine1(address1);
					user.setAddressLine2(address2);
					user.setCity(city);
					user.setCountry(country);
					user.setPassword(passWord);
					user.setEmail(email);
					user.setUserName(userName);
					user.setState(state);
					user.setZip(zip);
					user.setDeviceType(deviceType);
					user.setDataFeedType(datafeed_type);
					user.setExchangeId(exchangeId);
					try {
						if (cancelledStr != null) {
							user.setCancelledDate((sdf.parse(cancelledStr)).getTime());
						}
					} catch (ParseException pe) {
						logger.log(Level.WARNING, pe.getMessage(), pe);
					}
					if (status == 3 || status == 5) {
						Date cancelledDate = new Date(user.getCancelledDate());
						Date dateLimit = new Date();
						if (status == 5 && cancelledDate.before(dateLimit)) {
							cancelledTrialUserMap.put(trialIdL, user);
						} else {
							status = 4;
							activeTrialUserMap.put(trialIdL, user);
							activeUsernameSet.add(user.getUserName().toLowerCase());
						}
					} else if (status == 9) {
						status = 6;
						cancelledTrialUserMap.put(trialIdL, user);
					}
					user.setStatus(status);
					Date addedDate = sdf.parse(addedStr);
					user.setCreationTime(addedDate.getTime());
					if (user.getCreationTime() <= 0)
						logger.warning("WRONG_TIME_CREATION " + firmId + "," + trialIdL);
					if (user.getCancelledDate() <= 0 && status == 6)
						logger.warning("WRONG_TIME_CANCEL " + firmId + "," + trialIdL);
				}
			}
			con.disconnect();
			logger.info("Active Trial User QS Cache: " + activeTrialUserMap.size());
			logger.info("Cancelled Trial User QS Cache: " + cancelledTrialUserMap.size());
		} catch (QuoddException e) {
			logger.warning(e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return activeTrialUserMap;

	}

	public static Firm getFirmDetailsByFirmId(long firmId) {
		String url = QS_DOMAIN + "/FirmApi.php?action=firm_detail&module=firm&firm_id=" + firmId;
		logger.info(() -> QuoddSyncConstants.GETTING_RESPONSE_FROM_URL + url);
		Firm firm = new Firm();
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty(QuoddSyncConstants.AUTHORIZATION, qsAuthorization);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			logger.info(() -> QuoddSyncConstants.SENDING_GET_REQUEST + url + " " + responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new Exception(QuoddSyncConstants.NO_RESPONSE_FOUND);
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				String responseStr = sb.toString();
				Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
				}.getType();
				ArrayList<HashMap<String, Object>> userMapList = gson.fromJson(responseStr, type);
				HashMap<String, Object> bean = userMapList.get(0);
				Map<String, Object> lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
				lookup.putAll(bean);

				Double parentId = (Double) lookup.get("parent_id");
				String firmName = (String) lookup.get("firm");
				String firmType = (String) lookup.get("firm_type");
				String zip = (String) lookup.get("zip");
				String phone = (String) lookup.get("phone");
				String email = (String) lookup.get("email");
				String address1 = (String) lookup.get("address1");
				String address2 = (String) lookup.get("address2");
				String city = (String) lookup.get("city");
				String state = (String) lookup.get("state");
				String country = (String) lookup.get("country");
				String exchange = (String) lookup.get("exchange_id");
				int userType = ((Double) lookup.get("pro")).intValue();

				firm.setQsFirmId(firmId);
				firm.setFirmName(firmName);
				firm.setExchange(exchange);
				firm.setParentId(parentId.longValue());
				// firm.setFirmType(firmType);
				firm.setAddressLine1(address1);
				firm.setAddressLine2(address2);
				firm.setCity(city);
				firm.setCountry(country);
				firm.setPhone(phone);
				firm.setEmail(email);
				firm.setState(state);
				firm.setZip(zip);
				firm.setUserType(userType);
				firm.setDataFeedType(firmType);
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		return firm;
	}

	public static void cancelQuoddSupportUser(User userBean) {
		try {
			String userName = userBean.getUserName();
			String url = QS_DOMAIN + "/cancelUserApi.php?username=" + userName;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			logger.info("Sending 'GET' request to QSS URL : " + url + " Response Code : " + responseCode);
			String responseStr = "";
			StringBuilder sb = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				while ((responseStr = br.readLine()) != null) {
					sb.append(responseStr);
				}
				logger.info("QSS response: " + userName + " : " + sb);
				con.disconnect();
			}
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}

	public static void updateUserInfo(String userName, HashMap<String, Object> lookup) {
		String url = QS_DOMAIN + "/updateUserApi.php?username=" + userName + "&purgeUser=Y";
		logger.info("Getting response from : " + url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			JsonObject data = new JsonObject();
			for (Map.Entry<String, Object> entry : lookup.entrySet()) {
				data.addProperty(entry.getKey(), entry.getValue().toString());
			}
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(data.toString());
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			logger.info("SENDING_RESPONSE_TO_URL : " + url + " : " + responseCode);
			if (responseCode != 200) {
				logger.info("UPDATE_USER_FAILED");
				throw new QuoddException("Update User FAILED : " + userName);
			} else {
				logger.info("REQUEST_SUCCESSFUL : " + responseCode + " -- Post Parameters : " + data.toString());
			}
			con.disconnect();
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
	}
}
