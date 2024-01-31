package com.quodd.controller;

import static com.quodd.EquityPlusSyncApplication.logger;
import static com.quodd.EquityPlusSyncApplication.utility;
import static com.quodd.EquityPlusSyncApplication.qss4Cache;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import com.quodd.bean.Firm;
import com.quodd.bean.Product;
import com.quodd.bean.User;
import com.quodd.exception.QuoddException;
import com.quodd.util.Qss4Helper;
import com.quodd.util.QuoddSupportHelper;

public class UserSyncController {

	public static final int USER_SEAT_TYPE_TRIAL = 0;
	public static final int USER_SEAT_TYPE_SEAT = 1;
	public static final int USER_STATUS_CANCEL = 6;
	public static final int USER_STATUS_DELETED = 8;
	SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-d hh:mm:ss");

	public UserSyncController() throws Exception {
		utility.addUserHeaderToMailList();
		Map<String, Long> qsExchangeIdFirmMap = new HashMap<>();
		Set<Long> parentFirmSet = new HashSet<>();
		Map<Long, Firm> qsFirmIdBeanMap = QuoddSupportHelper.loadSeatFirmMapList(qsExchangeIdFirmMap, parentFirmSet);
		Map<String, Long> qss4ExchangeIdFirmMap = new HashMap<>();
		Map<Long, Long> qss4QsFirmIdMap = new HashMap<Long, Long>();
		Map<Long, Firm> qss4FirmIdBeanMap = Qss4Helper.loadEqPlusFirms(qss4ExchangeIdFirmMap, qss4QsFirmIdMap);
		Set<Long> tempQsIds = new HashSet<>();
		tempQsIds.addAll(qsFirmIdBeanMap.keySet());
		tempQsIds.removeAll(qss4FirmIdBeanMap.keySet());
		logger.info("QS count " + qsFirmIdBeanMap.size() + " QSS4 count " + qss4FirmIdBeanMap.size() + " To be added "
				+ tempQsIds.size());
		for (long id : parentFirmSet) {
			if (!qss4FirmIdBeanMap.containsKey(id)) {
				long fid = Qss4Helper.addFirm(qsFirmIdBeanMap.get(id), id, null);
				logger.info("PARENT_FIRM_ADDED_IN_QSS4  " + fid);
				utility.addToMailList((int) fid, qsFirmIdBeanMap.get(id).getFirmName(), "PARENT_FIRM_ADDED_IN_QSS4", "",
						"");
			}
		}
		qss4FirmIdBeanMap = Qss4Helper.loadEqPlusFirms(qss4ExchangeIdFirmMap, qss4QsFirmIdMap);
		Map<Long, User> qsCancelledSeatUserMap = new HashMap<>();
		Map<Long, User> cancelledZeroUserMap = new HashMap<>();
		Map<Long, User> internalUserMap = new HashMap<>();
		Map<Long, User> qsActiveSeatUserMap = QuoddSupportHelper.loadSeatUserMapList(qsCancelledSeatUserMap,
				cancelledZeroUserMap, internalUserMap);
		Map<Long, User> qss4SeatUserMap = Qss4Helper.loadUserList();
		Set<String> usernameSet = new HashSet<>();

		// Handle Firms Sync
		syncFirms(qsFirmIdBeanMap, qss4FirmIdBeanMap);

		// Handle Active Users sync
		syncActiveUsers(qsActiveSeatUserMap, qss4SeatUserMap, usernameSet, qss4FirmIdBeanMap, internalUserMap);

		// Handle Cancelled Users Sync
		syncCancelledUsers(qsCancelledSeatUserMap, qss4SeatUserMap, usernameSet, qss4FirmIdBeanMap,
				cancelledZeroUserMap);

		// Handle Trial Users Sync
		syncTrialUsers(qss4SeatUserMap, usernameSet);

		// Handle Deleted Users Sync
		syncDeletedUsers(qsActiveSeatUserMap, qsCancelledSeatUserMap);

		// Handle User Product Sync
		syncUserProduct(qss4SeatUserMap);
	}

	public static void syncFirms(Map<Long, Firm> qsFirmIdBeanMap, Map<Long, Firm> qss4FirmIdBeanMap) {
		for (long id : qsFirmIdBeanMap.keySet()) {
			Map<String, Object> lookup = new HashMap<String, Object>();
			if (!qss4FirmIdBeanMap.containsKey(id)) {
				Firm bean = qsFirmIdBeanMap.get(id);
				Long parentId = null;
				if (bean.getParentId() != 0) {
					Firm parentFirm = qss4FirmIdBeanMap.get(bean.getParentId());
					if (parentFirm != null)
						parentId = parentFirm.getFirmId();
					else {
						logger.warning("NO PARENT FIRM for : " + id + ", " + bean.getParentId());
					}
				}
				long fid = Qss4Helper.addFirm(bean, id, parentId);
				logger.info("Added  " + fid);
				utility.addToMailList((int) fid, qsFirmIdBeanMap.get(id).getFirmName(), "FIRM_ADDED_IN_QSS4", "", "");
			} else {
				Firm qsBean = qsFirmIdBeanMap.get(id);
				Firm qss4Bean = qss4FirmIdBeanMap.get(id);
				String temp = (qsBean.getFirmName() == null) ? "" : qsBean.getFirmName();
				if (!temp.equals(qss4Bean.getFirmName()) && (temp != null && qss4Bean.getFirmName() != null)
						&& (qss4Bean.getFirmName().substring(0, qss4Bean.getFirmName().length()).equals(temp)
								&& qss4Bean.getFirmName().charAt(temp.length()) == '_'
								&& qss4Bean.getFirmName().substring(temp.length() + 1, qss4Bean.getFirmName().length())
										.equals(Long.toString(qsBean.getQsFirmId())))) {
					logger.warning("MISMATCH_FIRM_NAME : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_NAME ",
							qsBean.getFirmName(), qss4Bean.getFirmName());
				}

				temp = (qsBean.getExchange() == null) ? "" : qsBean.getExchange();
				if (!temp.equals(qss4Bean.getExchange()) && qss4Bean.getExchange() != null) {
					logger.warning("MISMATCH_FIRM_EXCHANGE_ID : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_EXCHANGE_ID ",
							qsBean.getExchange(), qss4Bean.getExchange());
					lookup.put("exchange_id", temp);
				}

				temp = (qsBean.getAddressLine1() == null) ? "" : qsBean.getAddressLine1();
				if (!temp.equals(qss4Bean.getAddressLine1()) && qss4Bean.getAddressLine1() != null) {
					logger.warning("MISMATCH_FIRM_ADDRESS1 : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_ADDRESS1 ",
							qsBean.getAddressLine1(), qss4Bean.getAddressLine1());
					lookup.put("address_line_1", temp);
				}

				temp = (qsBean.getAddressLine2() == null) ? "" : qsBean.getAddressLine2();
				if (!temp.equals(qss4Bean.getAddressLine2()) && qss4Bean.getAddressLine2() != null) {
					logger.warning("MISMATCH_FIRM_ADDRESS2 : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_ADDRESS2  ",
							qsBean.getAddressLine2(), qss4Bean.getAddressLine2());
					lookup.put("address_line_2", temp);
				}

				temp = (qsBean.getCity() == null) ? "" : qsBean.getCity();
				if (!temp.equals(qss4Bean.getCity()) && qss4Bean.getCity() != null) {
					logger.warning("MISMATCH_FIRM_CITY : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_CITY ",
							qsBean.getCity(), qss4Bean.getCity());
					lookup.put("city", temp);
				}
				temp = (qsBean.getState() == null) ? "" : qsBean.getState();
				if (!temp.equals(qss4Bean.getState()) && qss4Bean.getState() != null) {
					logger.warning("MISMATCH_FIRM_STATE : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_STATE ",
							qsBean.getState(), qss4Bean.getState());
					lookup.put("state", temp);
				}
				temp = (qsBean.getZip() == null) ? "" : qsBean.getZip();
				if (!temp.equals(qss4Bean.getZip()) && qss4Bean.getZip() != null) {
					logger.warning("MISMATCH_FIRM_ZIP : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_ZIP ",
							qsBean.getZip(), qss4Bean.getZip());
					lookup.put("zip", temp);
				}

				temp = (qsBean.getEmail() == null) ? "" : qsBean.getEmail();
				if (!temp.equals(qss4Bean.getEmail()) && qss4Bean.getEmail() != null) {
					logger.warning("MISMATCH_FIRM_EMAIL : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_EMAIL ",
							qsBean.getEmail(), qss4Bean.getEmail());
					lookup.put("email", temp);
				}
				temp = (qsBean.getDeviceType() == null) ? "" : qsBean.getDeviceType();
				if (!qsBean.getDeviceType().equals(qss4Bean.getDeviceType()) && qss4Bean.getDeviceType() != null) {
					logger.warning("MISMATCH_FIRM_DEVICETYPE : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_DEVICETYPE ",
							qsBean.getDeviceType(), qss4Bean.getDeviceType());
					lookup.put("device_type", temp);
				}

				if (qsBean.getDowjonesBillable() != qss4Bean.getDowjonesBillable()) {
					logger.warning("MISMATCH_FIRM_DOWJONES_BILLABLE : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(),
							"MISMATCH_FIRM_DOWJONES_BILLABLE ", String.valueOf(qsBean.getDowjonesBillable()),
							String.valueOf(qss4Bean.getDowjonesBillable()));
					lookup.put("is_dowjones_billable", qsBean.getDowjonesBillable());
				}

				if (qsBean.getUserType() != qss4Bean.getUserType()) {
					logger.warning("MISMATCH_FIRM_USERTYPE : " + id);
					utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(), "MISMATCH_FIRM_USERTYPE ",
							String.valueOf(qsBean.getUserType()), String.valueOf(qss4Bean.getUserType()));
					lookup.put("firm_type", qsBean.getUserType());
				}
				if (qsBean.getParentId() > 0) {
					Firm parentFirm = qss4FirmIdBeanMap.get(qsBean.getParentId());
					if (parentFirm != null) {
						long mappedQsParentId = parentFirm.getFirmId();
						if (mappedQsParentId != qss4Bean.getParentId()) {
							logger.warning("MISMATCH_FIRM_PARENT_ID : " + id);
							utility.addToMailList((int) qsBean.getFirmId(), qsBean.getFirmName(),
									"MISMATCH_FIRM_PARENT_ID ", String.valueOf(qsBean.getParentId()),
									String.valueOf(qss4Bean.getParentId()));
						}
					} else {
						logger.warning("NO PARENT FIRM for " + qsBean.getParentId());
					}
				}

				if (!lookup.isEmpty()) {
					Qss4Helper.updateFirmInfo(lookup, id);

				}
			}
		}
	}

	public static void syncActiveUsers(Map<Long, User> qsActiveSeatUserMap, Map<Long, User> qss4SeatUserMap,
			Set<String> usernameSet, Map<Long, Firm> qss4FirmIdBeanMap, Map<Long, User> internalUserMap) {
		utility.addToMailList(4, "Active Users", "", "", "");
		for (Long id : qsActiveSeatUserMap.keySet()) {
			Map<String, Object> lookup = new HashMap<String, Object>();
			User userBean = qsActiveSeatUserMap.get(id);
			usernameSet.add(userBean.getUserName().toLowerCase());
			if (!qss4SeatUserMap.containsKey(id)) {
				Firm firmBean = qss4FirmIdBeanMap.get(userBean.getFirmId());
				if (firmBean != null) {
					Qss4Helper.addQsUser(userBean, firmBean, firmBean.getFirmId());
					utility.addToMailList((int) userBean.getClientUserId(), userBean.getUserName(),
							"USER_ADDED_IN_QSS4", "", "");
				} else {
					logger.warning("No firm for : " + userBean.getUserName());
				}
			} else {
				User qss4User = qss4SeatUserMap.get(id);
				if (userBean.getStatus() != qss4User.getStatus()) {
					logger.warning("MISMATCH_USER_STATUS : " + id);
					utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_STATUS", String.valueOf(userBean.getStatus()),
							String.valueOf(qss4User.getStatus()));
				}

				int seatType = qss4User.getSeatType();
				if (seatType == USER_SEAT_TYPE_SEAT) {
					long qsFirmId = userBean.getFirmId();
					long qss4FirmId = qss4User.getClientFirmId();
					if (qss4FirmId == 0) {
						logger.warning("MISMATCH_USER_CLIENT_FIRM_ID_MISSING : " + id);
						utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_CLIENT_FIRM_ID_MISSING", String.valueOf(qsFirmId),
								String.valueOf(qss4FirmId));
						System.out.println("ClientFirmId should be : " + qsFirmId + " as qss4FirmId is 0s");
						if (qsFirmId != 0)
							lookup.put("client_firm_id", qsFirmId);
					}
					if (qss4FirmIdBeanMap.containsKey(qss4FirmId)) {
						Firm qss4Firm = qss4FirmIdBeanMap.get(qss4FirmId);
						long qss4QsFirmId = qss4Firm.getFirmId();
						if (qsFirmId != qss4QsFirmId) {
							logger.warning("MISMATCH_USER_CLIENT_FIRM_ID : " + id);
							utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
									"MISMATCH_USER_CLIENT_FIRM_ID", String.valueOf(qsFirmId),
									String.valueOf(qss4QsFirmId));
							lookup.put("client_firm_id", qsFirmId);
						}
					}
				}
				String temp = (userBean.getUserName() == null) ? "" : userBean.getUserName();
				String qss4temp = (qss4User.getUserName() == null) ? "" : qss4User.getUserName();
				if (!temp.equals(qss4temp)) {
					logger.warning("MISMATCH_USER_USERNAME : " + id);
					utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_USERNAME", temp, qss4temp);
				}

				temp = (userBean.getFirstname() == null) ? "" : userBean.getFirstname();
				qss4temp = (qss4User.getFirstname() == null) ? "" : qss4User.getFirstname();
				if (!temp.equals(qss4temp)) {
					logger.warning("MISMATCH_USER_FIRSTNAME : " + id);
					utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_FIRSTNAME", temp, qss4temp);
					if (!temp.isEmpty())
						lookup.put("first_name", temp);
				}

				temp = (userBean.getLastName() == null) ? "" : userBean.getLastName();
				qss4temp = (qss4User.getLastName() == null) ? "" : qss4User.getLastName();
				if (!temp.equals(qss4temp)) {
					logger.warning("MISMATCH_USER_LASTNAME : " + id);
					utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_LASTNAME", temp, qss4temp);
					if (!temp.isEmpty())
						lookup.put("last_name", temp);
				}

				temp = (userBean.getAddressLine1() == null) ? "" : userBean.getAddressLine1();
				qss4temp = (qss4User.getAddressLine1() == null) ? "" : qss4User.getAddressLine1();
				if (!temp.equals(qss4temp)) {
					logger.warning("MISMATCH_USER_ADDRESS_LINE_1 : " + id);
					utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_ADDRESS_LINE_1", temp, qss4temp);
					if (!temp.isEmpty())
						lookup.put("address_line_1", temp);
				}

				temp = (userBean.getAddressLine2() == null) ? "" : userBean.getAddressLine2();
				qss4temp = (qss4User.getAddressLine2() == null) ? "" : qss4User.getAddressLine2();
				if (!temp.equals(qss4temp)) {
					logger.warning("MISMATCH_USER_ADDRESS_LINE_2 : " + id);
					utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_ADDRESS_LINE_2", temp, qss4temp);
					if (!temp.isEmpty())
						lookup.put("address_line_2", temp);
				}

				temp = (userBean.getCity() == null) ? "" : userBean.getCity();
				qss4temp = (qss4User.getCity() == null) ? "" : qss4User.getCity();
				if (!temp.equals(qss4temp)) {
					logger.warning("MISMATCH_USER_CITY : " + id);
					utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_CITY",
							temp, qss4temp);
					if (!temp.isEmpty())
						lookup.put("city", temp);
				}

				temp = (userBean.getState() == null) ? "" : userBean.getState();
				qss4temp = (qss4User.getState() == null) ? "" : qss4User.getState();
				if (!temp.equals(qss4User.getState())) {
					logger.warning("MISMATCH_USER_STATE : " + id);
					utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_STATE",
							temp, qss4User.getState());
					if (!temp.isEmpty())
						lookup.put("state", temp);
				}

				temp = (userBean.getZip() == null) ? "" : userBean.getZip();
				qss4temp = (qss4User.getZip() == null) ? "" : qss4User.getZip();
				if (!temp.equals(qss4temp)) {
					logger.warning("MISMATCH_USER_ZIP : " + id);
					utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(), "MISMATCH_USER_ZIP",
							temp, qss4temp);
					if (!temp.isEmpty())
						lookup.put("zip", temp);
				}

				try {
					temp = (userBean.getCountry() == null) ? "" : userBean.getCountry();
					qss4temp = (qss4User.getCountry() == null) ? "" : qss4User.getCountry();
					if (!temp.equals(qss4temp)) {
						String country = qss4Cache.getCountryCodeMap().get(temp);

						if (!country.equalsIgnoreCase(qss4temp)) {
							logger.warning("MISMATCH_USER_COUNTRY : " + id);
							utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
									"MISMATCH_USER_COUNTRY", temp, qss4temp);
							if (!temp.isEmpty())
								lookup.put("country", temp);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				if (userBean.getUserType() != qss4User.getUserType()) {
					logger.warning("MISMATCH_USER_TYPE : " + id);
					utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_TYPE ",
							String.valueOf(userBean.getUserType()), String.valueOf(qss4User.getUserType()));
					lookup.put("user_type", userBean.getUserType());
				}

				temp = (userBean.getExchangeId() == null) ? "" : userBean.getExchangeId();
				qss4temp = (qss4User.getExchangeId() == null) ? "" : qss4User.getExchangeId();
				if (!temp.equals(qss4temp)) {
					logger.warning("MISMATCH_USER_EXCHANGEID : " + id + ", QS : " + temp + ", QSS4 : "
							+ qss4User.getExchangeId());
					utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_EXCHANGEID ",
							String.valueOf(temp), String.valueOf(qss4temp));
					if (!temp.isEmpty())
						lookup.put("exchange_id", temp);
				}

				temp = (userBean.getDeviceType() == null) ? "" : userBean.getDeviceType();
				qss4temp = (qss4User.getDeviceType() == null) ? "" : qss4User.getDeviceType();
				if (!temp.equals(qss4temp)) {
					logger.warning("MISMATCH_USER_DEVICETYPE : " + id);
					utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_DEVICETYPE ",
							String.valueOf(temp), String.valueOf(qss4temp));
					if (!temp.isEmpty())
						lookup.put("device_type", temp);
				}

				if (internalUserMap.containsKey(id)) {
					String dftemp = "I";
					if ((!dftemp.equals(qss4User.getDataFeedType()))) {
						logger.warning("MISMATCH_USER_DATAFEEDTYPE : " + id + ", QS : " + dftemp + ", QSS4 : "
								+ qss4User.getDataFeedType());
						utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_DATAFEEDTYPE ",
								dftemp, String.valueOf(qss4User.getDataFeedType()));
					}
				} else {
					String dtemp = (userBean.getDataFeedType() == null) ? "" : userBean.getDataFeedType();
					if (userBean.getDeviceType().equals("D")) {
						if (!dtemp.equals(qss4User.getDataFeedType())) {
							logger.warning("MISMATCH_USER_DATAFEEDTYPE : " + id);
							utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
									"MISMATCH_USER_DATAFEEDTYPE ",
									String.valueOf(userBean.getDataFeedType()),
									String.valueOf(qss4User.getDataFeedType()));
						}
					} else {
						if (dtemp.equalsIgnoreCase("B") && qss4User.getDataFeedType().equalsIgnoreCase("B")) {
							logger.warning("MISMATCH_USER_DEVICE_DATAFEEDTYPE : " + id);
							utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
									"MISMATCH_USER_DEVICE_DATAFEEDTYPE : ",
									String.valueOf(userBean.getDataFeedType() + "-E"),
									String.valueOf(qss4User.getDataFeedType()));
						}
					}
				}
				if (!lookup.isEmpty()) {
					Qss4Helper.updateUserInfo(id, lookup);
				}
			}
		}

	}

	public static void syncCancelledUsers(Map<Long, User> qsCancelledSeatUserMap, Map<Long, User> qss4SeatUserMap,
			Set<String> usernameSet, Map<Long, Firm> qss4FirmIdBeanMap, Map<Long, User> cancelledZeroUserMap) {
		utility.addToMailList(6, "Cancelled Users", "", "", "");
		logger.info("Cancelled Users");
		Random ran = new Random();
		for (Long id : qsCancelledSeatUserMap.keySet()) {
			Map<String, Object> updateFieldsLookup = new HashMap<String, Object>();
			User userBean = qsCancelledSeatUserMap.get(id);
			while (!usernameSet.add(userBean.getUserName().toLowerCase()))
				userBean.setUserName(userBean.getUserName() + "_" + ran.nextInt(1000));
			if (!qss4SeatUserMap.containsKey(id)) {
				Firm firmBean = qss4FirmIdBeanMap.get(userBean.getFirmId());
				if (firmBean == null) {
					logger.warning("NO_USER_FIRM ON qss4 for seat ID : " + id);
					utility.addToMailList(id.intValue(), "", "NO_USER_FIRM ON qss4 for seat ID ", "", "");
					continue;
				}
				Qss4Helper.addQsUser(userBean, firmBean, firmBean.getFirmId());
				utility.addToMailList(id.intValue(), userBean.getUserName(), "User Added for Firm in QSS4", "", "");
			} else {
				User qss4User = qss4SeatUserMap.get(id);
				if (userBean.getStatus() != qss4User.getStatus()) {
					logger.warning("MISMATCH_USER_STATUS : " + id);
					utility.addToMailList((int) userBean.getClientUserId(), "No Action Required",
							"MISMATCH_USER_STATUS ", String.valueOf(userBean.getStatus()),
							String.valueOf(qss4User.getStatus()));
					Qss4Helper.cancelUser(id);
				}
				if (!userBean.getUserName().equals(qss4User.getUserName())) {
					logger.warning("MISMATCH_USER_USERNAME : " + id);
					utility.addToMailList((int) userBean.getClientUserId(), "", "MISMATCH_USER_USERNAME ",
							userBean.getUserName(), qss4User.getUserName());
					updateFieldsLookup.put("user_name", userBean.getUserName());
				}
				if (userBean.getUserType() != qss4User.getUserType()) {
					logger.warning("MISMATCH_USER_TYPE : " + id);
					utility.addToMailList((int) userBean.getClientUserId(), "", "MISMATCH_USER_TYPE ",
							String.valueOf(userBean.getUserType()), String.valueOf(qss4User.getUserType()));
					updateFieldsLookup.put("user_type", userBean.getUserType());
				}

				if (!userBean.getExchangeId().equals(qss4User.getExchangeId())) {
					logger.warning("MISMATCH_USER_EXCHANGEID : " + id);
					utility.addToMailList((int) userBean.getClientUserId(), "", "MISMATCH_USER_EXCHANGEID ",
							String.valueOf(userBean.getExchangeId()), String.valueOf(qss4User.getExchangeId()));
					updateFieldsLookup.put("exchange_id", userBean.getExchangeId());
				}

				if (userBean.getCancelledDate() != 0) {
					long qss4CancelledDate = qss4User.getCancelledDate();
					long qsCancelledDate = userBean.getCancelledDate();
					LocalDate dateLimit = LocalDate.parse("2020-01-01");
					long dateLimitMs = dateLimit.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

					if (qsCancelledDate < dateLimitMs)
						continue;
					String qsCancelledTime = userBean.getCancelledTime();
					String qss4CancelledTime = qss4User.getCancelledTime();

					if (qsCancelledDate != qss4CancelledDate) {
						logger.warning("MISMATCH_USER_CANCELLED_DATE : " + id + "----- QS : " + qsCancelledDate
								+ ", Qss4 : " + qss4CancelledDate);
						utility.addToMailList((int) userBean.getClientUserId(), "", "MISMATCH_USER_CANCELLED_DATE ",
								String.valueOf(qsCancelledTime), String.valueOf(qss4CancelledTime));
						updateFieldsLookup.put("deactivation_time", qsCancelledTime);
					}
				}
				if (!updateFieldsLookup.isEmpty()) {
					Qss4Helper.updateUserInfo(id, updateFieldsLookup);
				}

			}
		}

		logger.log(Level.WARNING, " QS : Cancelled Zero Users");
		utility.addToMailList(6, "Cancelled Zero Users", "", "", "");
		for (Long id : cancelledZeroUserMap.keySet()) {
			User userBean = cancelledZeroUserMap.get(id);
			while (!usernameSet.add(userBean.getUserName().toLowerCase()))
				userBean.setUserName(userBean.getUserName() + "_" + ran.nextInt(1000));
			if (!qss4SeatUserMap.containsKey(id)) {
				logger.warning("NO_USER ON qss4 for seat ID : " + id);
				Firm firmBean = qss4FirmIdBeanMap.get(userBean.getFirmId());
				if (firmBean == null) {
					logger.warning("NO_USER_FIRM ON qss4 for seat ID " + id);
					utility.addToMailList(id.intValue(), "", "NO_USER_FIRM ON qss4 for seat ID ", "", "");
					continue;
				}
			} else {
				User qss4User = qss4SeatUserMap.get(id);
				if (userBean.getStatus() != qss4User.getStatus()) {
					logger.warning("MISMATCH_USER_STATUS : " + id);
					utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
							"MISMATCH_USER_STATUS ", String.valueOf(userBean.getStatus()),
							String.valueOf(qss4User.getStatus()));
					Qss4Helper.cancelUser(id);
				}
			}
		}
	}

	public static void syncTrialUsers(Map<Long, User> qss4SeatUserMap, Set<String> usernameSet) {
		Map<Long, User> qsCancelledTrialUserMap = new HashMap<>();
		Map<Long, User> qsActiveTrialUserMap;
		try {
			qsActiveTrialUserMap = QuoddSupportHelper.loadTrialUserMapList(qsCancelledTrialUserMap);

			utility.addToMailList(10, "Trial Users", "", "", "");
			logger.info("TRIAL USERS");
			for (Long id : qsActiveTrialUserMap.keySet()) {
				Map<String, Object> lookup = new HashMap<String, Object>();
				User userBean = qsActiveTrialUserMap.get(id);
				usernameSet.add(userBean.getUserName().toLowerCase());
				if (!qss4SeatUserMap.containsKey(id)) {
					logger.warning("No trial user on QSS4 :" + id);
				} else {
					User qss4User = qss4SeatUserMap.get(id);
					if (userBean.getStatus() != qss4User.getStatus()) {
						logger.warning("MISMATCH_USER_STATUS " + id);
						utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_STATUS", String.valueOf(userBean.getStatus()),
								String.valueOf(qss4User.getStatus()));
					}

					String temp = (userBean.getUserName() == null) ? "" : userBean.getUserName();
					String qss4temp = (qss4User.getUserName() == null) ? "" : qss4User.getUserName();
					if (!temp.equals(qss4temp)) {
						logger.warning("MISMATCH_USER_USERNAME : " + id);
						utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_USERNAME", temp, qss4User.getUserName());
					}

					temp = (userBean.getAddressLine1() == null) ? "" : userBean.getAddressLine1();
					qss4temp = (qss4User.getAddressLine1() == null) ? "" : qss4User.getAddressLine1();
					if (!temp.equals(qss4temp)) {
						logger.warning("MISMATCH_USER_ADDRESS_LINE_1 : " + id);
						utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_ADDRESS_LINE_1", temp, qss4temp);
						if (!temp.isEmpty())
							lookup.put("address_line_1", temp);
					}

					temp = (userBean.getAddressLine2() == null) ? "" : userBean.getAddressLine2();
					qss4temp = (qss4User.getAddressLine2() == null) ? "" : qss4User.getAddressLine2();
					if (!temp.equals(qss4temp)) {
						logger.warning("MISMATCH_USER_ADDRESS_LINE_2 : " + id);
						utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_ADDRESS_LINE_2", temp, qss4temp);
						if (!temp.isEmpty())
							lookup.put("address_line_2", temp);
					}

					temp = (userBean.getCity() == null) ? "" : userBean.getCity();
					qss4temp = (qss4User.getCity() == null) ? "" : qss4User.getCity();
					if (!temp.equals(qss4temp)) {
						logger.warning("MISMATCH_USER_CITY : " + id);
						utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_CITY", temp, qss4temp);
						if (!temp.isEmpty())
							lookup.put("city", temp);
					}

					temp = (userBean.getState() == null) ? "" : userBean.getState();
					qss4temp = (qss4User.getState() == null) ? "" : qss4User.getState();
					if (!temp.equals(qss4temp)) {
						logger.warning("MISMATCH_USER_STATE : " + id);
						utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_STATE", temp, qss4temp);
						if (!temp.isEmpty())
							lookup.put("state", temp);
					}

					temp = (userBean.getZip() == null) ? "" : userBean.getZip();
					qss4temp = (qss4User.getZip() == null) ? "" : qss4User.getZip();
					if (!temp.equals(qss4temp)) {
						logger.warning("MISMATCH_USER_ZIP : " + id);
						utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_ZIP", temp, qss4temp);
						if (!temp.isEmpty())
							lookup.put("zip", temp);
					}

					temp = (userBean.getCountry() == null) ? "" : userBean.getCountry();
					qss4temp = (qss4User.getCountry() == null) ? "" : qss4User.getCountry();
					if (!temp.equals(qss4temp)) {
						logger.warning("MISMATCH_USER_COUNTRY : " + id);
						utility.addToMailList((int) qss4User.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_COUNTRY", temp, qss4temp);
						if (!temp.isEmpty())
							lookup.put("country", temp);
					}

					if (userBean.getUserType() != qss4User.getUserType()) {
						logger.warning("MISMATCH_USER_TYPE : " + id);
						utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_TYPE ", String.valueOf(userBean.getUserType()),
								String.valueOf(qss4User.getUserType()));
					}

					temp = (userBean.getExchangeId() == null) ? "" : userBean.getExchangeId();
					qss4temp = (qss4User.getExchangeId() == null) ? "" : qss4User.getExchangeId();
					if (!temp.equals(qss4temp)) {
						logger.warning("MISMATCH_USER_EXCHANGEID : " + id + ", QS : " + temp + ", QSS4 : "
								+ qss4User.getExchangeId());
						utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_EXCHANGEID ", String.valueOf(userBean.getExchangeId()),
								String.valueOf(qss4User.getExchangeId()));
						if (!temp.isEmpty())
							lookup.put("exchange_id", temp);
					}

					temp = (userBean.getDeviceType() == null) ? "" : userBean.getDeviceType();
					qss4temp = (qss4User.getDeviceType() == null) ? "" : qss4User.getDeviceType();
					if (!temp.equals(qss4temp)) {
						logger.warning("MISMATCH_USER_DEVICETYPE : " + id);
						utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_DEVICETYPE ", String.valueOf(userBean.getDeviceType()),
								String.valueOf(qss4User.getDeviceType()));
						if (!temp.isEmpty())
							lookup.put("device_type", userBean.getDeviceType());
					}

					String dtemp = (userBean.getDataFeedType() == null) ? "" : userBean.getDataFeedType();
					if (userBean.getDeviceType().equals("D")) {
						if (!dtemp.equals(qss4User.getDataFeedType())) {
							logger.warning("MISMATCH_USER_DATAFEEDTYPE : " + id);
							utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
									"MISMATCH_USER_DATAFEEDTYPE ", String.valueOf(userBean.getDataFeedType()),
									String.valueOf(qss4User.getDataFeedType()));
							if (!dtemp.isEmpty())
								lookup.put("datafeed_type", dtemp);
						}
					} else if (dtemp.equalsIgnoreCase("B") && qss4User.getDataFeedType().equalsIgnoreCase("B")) {
						logger.warning("MISMATCH_USER_DEVICE_DATAFEEDTYPE :  " + id);
						utility.addToMailList((int) userBean.getClientUserId(), qss4User.getUserName(),
								"MISMATCH_USER_DEVICE_DATAFEEDTYPE ", String.valueOf(userBean.getDataFeedType() + "-E"),
								String.valueOf(qss4User.getDataFeedType()));
					}
					if (!lookup.isEmpty()) {
						Qss4Helper.updateUserInfo(id, lookup);
					}
				}
			}
		} catch (QuoddException e) {
			e.printStackTrace();
		}
	}

	public static void syncDeletedUsers(Map<Long, User> qsCidUserBeanMap, Map<Long, User> qsCancelledSeatUserMap) {
		utility.addToMailList(8, "Deleted Users", "", "", "");
		HashMap<Long, User> qss4DeletedUserCidBean;
		try {
			qss4DeletedUserCidBean = Qss4Helper.loadQss4DeletedUsers();
			HashMap<String, Object> qsLookup = new HashMap<String, Object>();
			for (long cid : qss4DeletedUserCidBean.keySet()) {
				User qsBean = null;
				User qss4Bean = qss4DeletedUserCidBean.get(cid);
				if (qsCidUserBeanMap.containsKey(cid)) {
					int qss4Status = qss4Bean.getStatus();
					qsBean = qsCidUserBeanMap.get(cid);
					int qsStatus = qsBean.getStatus();

					if (qsStatus != USER_STATUS_CANCEL) {
						utility.addToMailList((int) qss4Bean.getClientUserId(), qss4Bean.getUserName(),
								"MISMATCH_USER_STATUS", String.valueOf(qsStatus), String.valueOf(qss4Status));
						logger.warning(
								"MISMATCH_USER_STATUS : " + cid + " -- QS : " + qsStatus + ", Qss4 : " + qss4Status);
						logger.warning("Deleting user on QS: " + cid);
						QuoddSupportHelper.cancelQuoddSupportUser(qsBean);
						utility.addToMailList((int) qss4Bean.getClientUserId(), qss4Bean.getUserName(),
								"CANCELLED_USER_ON_QS", String.valueOf(USER_STATUS_CANCEL),
								String.valueOf(USER_STATUS_DELETED));
					} else {
						logger.warning("QSS4 Deleted user : " + cid + " -- CANCELLED_ON_QS");
					}
				} else if (qsCancelledSeatUserMap.containsKey(cid)) {
					logger.warning("QSS4 Deleted user : " + cid + " -- Already CANCELLED_ON_QS");
					qsBean = qsCancelledSeatUserMap.get(cid);
				}
				if (qsBean != null) {
					String temp = qsBean.getFirstname();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("first", "N/A");
					}
					temp = qsBean.getLastName();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("last", "N/A");
					}
					temp = qsBean.getEmail();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("email", "N/A");
					}
					temp = qsBean.getPassword();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("password", "N/A");
					}
					temp = qsBean.getCity();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("city", "N/A");
					}
					temp = qsBean.getState();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("state", "N/A");
					}
					temp = qsBean.getCountry();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("country", "N/A");
					}
					temp = qsBean.getAddressLine1();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("address1", "N/A");
					}
					temp = qsBean.getAddressLine2();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("address2", "N/A");
					}
					temp = qsBean.getZip();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("zip", "N/A");
					}
					temp = qsBean.getPhone();
					if (temp != null && !temp.equalsIgnoreCase("N/A")) {
						qsLookup.put("phone", "N/A");
					}
					if (!qsLookup.isEmpty()) {
						QuoddSupportHelper.updateUserInfo(qsBean.getUserName(), qsLookup);
						logger.warning("UPDATED_USER_ON_QS : " + cid);
						utility.addToMailList((int) qss4Bean.getClientUserId(), qss4Bean.getUserName(),
								"UPDATED_USER_ON_QS", "", "N/A");
					}
					logger.warning("DELETED_USER_ON_QS : " + cid);
				} else {
					logger.info("User not present on qs : " + cid + " -- NO_ACTION");
				}

			}
		} catch (QuoddException e) {
			logger.warning(e.getLocalizedMessage());
		}
	}

	public static void syncUserProduct(Map<Long, User> qss4SeatUserMap) {
		Map<String, Set<Product>> dbCodeProductMap = new HashMap<>();
		try {
			Qss4Helper.loadProductMap(dbCodeProductMap);

			Map<String, Set<Long>> dbCodeProductIdMap = new HashMap<>();
			for (String code : dbCodeProductMap.keySet()) {
				Set<Product> productSet = dbCodeProductMap.get(code);
				Set<Long> productIdSet = dbCodeProductIdMap.getOrDefault(code, new HashSet<>());
				for (Product product : productSet) {
					productIdSet.add(product.getProductId());
				}
				dbCodeProductIdMap.put(code, productIdSet);
			}
			Set<String> cancelEntitlementSet = new HashSet<>();
			QuoddSupportHelper.loadUpstreamFromEntitlement(cancelEntitlementSet);

			qss4SeatUserMap = Qss4Helper.loadUserList();
			for (long cid : qss4SeatUserMap.keySet()) {
				if (qss4SeatUserMap.get(cid).getSeatType() == USER_SEAT_TYPE_TRIAL) {
					logger.info("Dropping trial user : " + cid);
					continue;
				}
				Map<String, Object> qsSeatEntitlementMap = QuoddSupportHelper.getUserSeatEntitlementMap(cid);
				List<Map<String, Object>> qss4ProductStatusMap;
				try {
					cancelEntitlementSet.forEach(cancelledKey -> {
						if (qsSeatEntitlementMap.containsKey(cancelledKey))
							qsSeatEntitlementMap.remove(cancelledKey);
					});
					qss4ProductStatusMap = Qss4Helper.loadProductByuser(cid);

					Map<Long, Integer> qss4EntitlementMap = new HashMap<>();
					for (Map<String, Object> pMap : qss4ProductStatusMap) {
						qss4EntitlementMap.put(((Double) pMap.get("product_id")).longValue(),
								((Double) pMap.get("status")).intValue());
					}
					Set<Long> qsActiveProductSet = new HashSet<>();
					for (String dbCode : qsSeatEntitlementMap.keySet()) {
						if (dbCodeProductMap.containsKey(dbCode)
								&& ((Double) qsSeatEntitlementMap.get(dbCode)).intValue() == 1) {
							qsActiveProductSet.addAll(dbCodeProductIdMap.get(dbCode));
						}
					}

					StringBuilder activeProducts = new StringBuilder();
					StringBuilder cancelledProducts = new StringBuilder();
					for (long qss4EntitlementId : qss4EntitlementMap.keySet()) {
						if (!qsActiveProductSet.contains(qss4EntitlementId)) {
							cancelledProducts.append("," + qss4EntitlementId);
						}
					}
					if (qss4SeatUserMap.get(cid).getStatus() != 6) {
						for (Long product : qsActiveProductSet) {
							Integer status = qss4EntitlementMap.get(product);
							if (status == null || status != 3)
								activeProducts.append("," + product);
							else
								qss4EntitlementMap.remove(product);
						}
					}
					for (Long product : qss4EntitlementMap.keySet()) {
						Integer status = qss4EntitlementMap.get(product);
						if (status == null || status != 6)
							cancelledProducts.append("," + product);
					}
					if (qss4SeatUserMap.get(cid).getStatus() != 6) {
						if (activeProducts.length() > 1) {
							logger.info("To be Active Products : " + activeProducts);
							Qss4Helper.addUserProduct(cid, activeProducts.substring(1));
						}
						if (cancelledProducts.length() > 1) {
							logger.info("To be Cancelled Products : " + cancelledProducts);
							Qss4Helper.deleteUserProduct(cid, cancelledProducts.substring(1));
							logger.info("Products Mapping " + cid + " active " + activeProducts.toString()
									+ " cancelled " + cancelledProducts.toString());
						}
					}
				} catch (QuoddException e) {
					e.printStackTrace();
				}

			}
		} catch (QuoddException e1) {
			e1.printStackTrace();
		}
	}
}
