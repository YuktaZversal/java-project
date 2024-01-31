package com.quodd.controller;

import static com.quodd.EquityPlusSyncApplication.logger;
import static com.quodd.EquityPlusSyncApplication.utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.quodd.bean.Entitlement;
import com.quodd.bean.Product;
import com.quodd.bean.Service;
import com.quodd.bean.Upstream;
import com.quodd.exception.QuoddException;
import com.quodd.util.Qss4Helper;
import com.quodd.util.QuoddSupportHelper;

public class EntitlementSyncController {
	private static long successCount = 0;

	public void syncProductServiceMapping() throws QuoddException {
		utility.addProductMappingHeaderToMailList();
		Map<Long, Set<String>> qsEntitlementUpstreamMap = QuoddSupportHelper.loadEntitlementUpstreamMap();
		// Add backend key of entitlements as upstreams
		QuoddSupportHelper.updateEntitlementUpstreamMap(qsEntitlementUpstreamMap);
		Map<Long, Set<Long>> qss4ProductServiceMap = Qss4Helper.loadProductServiceMap();
		Map<Long, String> serviceUpstreamMap = Qss4Helper.loadServiceUpstreamMap();
		Map<Long, Set<Long>> entitlementProductMap = Qss4Helper.loadEntitlementProductMap();
		entitlementProductMap.forEach((entitlement, products) -> {
			if (products == null || products.isEmpty()) {
				logger.warning("ENTITLEMENT_PRODUCT_MISSING " + entitlement);
				utility.addProductMappingToMailList(0l, entitlement, "ENTITLEMENT_PRODUCT_MISSING",
						"No action required");
			} else {
				// All products should have same set of upstream as corresponding entitlement
				Set<String> upstreams = qsEntitlementUpstreamMap.get(entitlement);
				boolean isUpstream = true;
				if (upstreams == null || upstreams.isEmpty()) {
					logger.warning("ENTITLEMENT_UPSTREAM_MISSING " + entitlement);
					utility.addProductMappingToMailList(0l, entitlement, "ENTITLEMENT_UPSTREAM_MISSING",
							"No action required");
					isUpstream = false;
				}
				for (long product : products) {
					Set<Long> services = qss4ProductServiceMap.get(product);
					if (services == null || services.isEmpty()) {
						logger.warning("No services for product " + product);
						if (isUpstream) {
							logger.warning("PRODUCT_SERVICE_MISSING " + product + " entitlement " + entitlement
									+ " upstreams " + upstreams);
							utility.addProductMappingToMailList(product, entitlement, "PRODUCT_SERVICE_MISSING",
									upstreams.toString());
						}
					} else {
						Set<String> productUpstreams = new HashSet<>();
						for (long service : services) {
							String upstrm = serviceUpstreamMap.get(service);
							if (upstrm == null || upstrm.isEmpty()) {
								logger.warning("SERVICE_UPSTREAM_MISSING " + service + " " + product);
								utility.addProductMappingToMailList(product, service, "SERVICE_UPSTREAM_MISSING", "");
							} else {
								productUpstreams.add(upstrm);
							}
						}
						if (isUpstream) {
							Set<String> compareSet = new HashSet<>();
							compareSet.addAll(upstreams);
							compareSet.removeAll(productUpstreams);
							if (!compareSet.isEmpty()) {
								logger.warning("PRODUCT_UPSTREAM_MISSING " + product + " entitlement " + entitlement
										+ " upstreams " + compareSet);
								utility.addProductMappingToMailList(product, entitlement, "PRODUCT_UPSTREAM_MISSING",
										compareSet.toString());
							}
							compareSet = new HashSet<>();
							compareSet.addAll(productUpstreams);
							compareSet.removeAll(upstreams);
							if (!compareSet.isEmpty()) {
								logger.warning("PRODUCT_UPSTREAM_EXTRA " + product + " entitlement " + entitlement
										+ " upstreams " + compareSet);
								utility.addProductMappingToMailList(product, entitlement, "PRODUCT_UPSTREAM_EXTRA",
										compareSet.toString());
							}
						} else {
							if (!productUpstreams.isEmpty()) {
								logger.warning("PRODUCT_UPSTREAM_EXTRA " + product + " entitlement " + entitlement
										+ " upstreams " + productUpstreams);
								utility.addProductMappingToMailList(product, entitlement, "PRODUCT_UPSTREAM_EXTRA",
										productUpstreams.toString());

							}
						}
					}
				}
			}
		});
	}

	public void syncServices() throws QuoddException {
		// check if service is in qss4 and has upstream id mapped but not in
		// quoddsupport
		utility.addServiceHeaderToMailList();
		Map<String, Service> upstreamIdServiceMap = new HashMap<>();
		Map<Long, Service> serviceMap = Qss4Helper.loadServicesMap(upstreamIdServiceMap);
		Map<Long, Upstream> upstreamMap = QuoddSupportHelper.loadUpstreamMap();
		upstreamMap.forEach((upstreamId, upstreamBean) -> {
			Service qss4Service = upstreamIdServiceMap.get(upstreamId.toString());
			if (qss4Service == null) {
				long protocolId = upstreamBean.getProtocolId();
				qss4Service = serviceMap.get(protocolId);
				if (qss4Service == null) {
					qss4Service = new Service(upstreamBean.getProtocolId(), upstreamBean.getName(),
							upstreamId.toString());
					utility.addServicesToMailList((int) qss4Service.getServiceId(), qss4Service.getServiceName(),
							qss4Service.getQuoddUpstreamId(), "SERVICE_MISSING In QSS4", "");
					logger.warning("SERVICE_MISSING " + qss4Service);
				} else {
					logger.warning("MANUAL_SERVICE_MAPPING_MISSING " + qss4Service + " " + upstreamBean);
					utility.addServicesToMailList((int) qss4Service.getServiceId(), qss4Service.getServiceName(),
							qss4Service.getQuoddUpstreamId(), "MANUAL_SERVICE_MAPPING_MISSING",
							"NEW_QUODD_UPSTREAM_ID: " + upstreamBean.getUpstreamId() + "<br> NEW_SERVICE_NAME: "
									+ upstreamBean.getName());
				}
			} else {
				if (qss4Service.getServiceId() != upstreamBean.getProtocolId()) {
					utility.addServicesToMailList((int) qss4Service.getServiceId(), qss4Service.getServiceName(),
							qss4Service.getQuoddUpstreamId(), "MISMATCH_SERVICE_ID_PROTOCOL_ID",
							"NEW_SERVICE_ID : " + upstreamBean.getProtocolId());
					logger.warning("MISMATCH_SERVICE_ID_PROTOCOL_ID " + qss4Service + " " + upstreamBean);
				}
				if (!qss4Service.getServiceName().equals(upstreamBean.getName())) {
					utility.addServicesToMailList((int) qss4Service.getServiceId(), qss4Service.getServiceName(),
							qss4Service.getQuoddUpstreamId(), "MISMATCH_SERVICE_NAME_PROTOCOL_NAME",
							"NEW_SERVICE_NAME : " + upstreamBean.getName());
					logger.warning("MISMATCH_SERVICE_NAME_PROTOCOL_NAME " + qss4Service + " " + upstreamBean);
				}
				successCount++;
			}
		});
		Set<String> cancelEntitlementMap = new HashSet<>();
		Map<String, Entitlement> backendKeyEntitlementMap = QuoddSupportHelper.loadUpstreamFromEntitlement(cancelEntitlementMap);
		backendKeyEntitlementMap.forEach((backendKey, entitlementBean) -> {
			Service qss4Service = upstreamIdServiceMap.get(backendKey);
			if (qss4Service == null) {
				qss4Service = new Service(entitlementBean.getFullName(), backendKey);
				utility.addServicesToMailList((int) qss4Service.getServiceId(), qss4Service.getServiceName(),
						backendKey, "SERVICE_MISSING_BACKEND_KEY", "");
				logger.warning("SERVICE_MISSING_BACKEND_KEY " + qss4Service);
			} else {
				if (!qss4Service.getServiceName().equals(entitlementBean.getFullName())) {
					// Commented as there are name mismatch on Nanex entitlements
//					utility.addServicesToMailList((int) qss4Service.getServiceId(), qss4Service.getServiceName(),
//							backendKey, "MISMATCH_SERVICE_NAME_BACKEND_KEY_NAME", entitlementBean.getFullName());
					logger.warning("MISMATCH_SERVICE_NAME_BACKEND_KEY_NAME " + qss4Service + " " + entitlementBean);
				}
				successCount++;
			}
		});
		Set<String> qss4UpstreamSet = new HashSet<>();
		qss4UpstreamSet.addAll(upstreamIdServiceMap.keySet());
		upstreamMap.keySet().forEach(id -> qss4UpstreamSet.remove(id.toString()));
		qss4UpstreamSet.removeAll(backendKeyEntitlementMap.keySet());
		if (!qss4UpstreamSet.isEmpty())
			logger.warning("Qss4 upstreams not in QuoddSupport " + qss4UpstreamSet);
	}

	public void syncProducts() throws QuoddException {
		utility.addProductHeaderToMailList();
		Map<Long, Entitlement> qsEntitlements = QuoddSupportHelper.loadEntitlementMap();
		Map<String, Set<Product>> dbCodeProductMap = new HashMap<>();
		Map<Long, Set<Product>> entitlementIdProductMap = Qss4Helper.loadProductMap(dbCodeProductMap);
		qsEntitlements.forEach((entitlementId, entitlementBean) -> {
			Set<Product> qss4ProductByid = entitlementIdProductMap.get(entitlementId);
			Set<Product> qss4ProductByCode = dbCodeProductMap.get(entitlementBean.getDbFieldName());
			if ((qss4ProductByid == null || qss4ProductByid.isEmpty())
					&& (qss4ProductByCode == null || qss4ProductByCode.isEmpty())) {
				logger.warning("PRODUCT_MISSING " + entitlementBean);
				utility.addProductToMailList(entitlementBean.getFullName(), entitlementBean.getDbFieldName(),
						entitlementBean.getEntitlementId() + "", "PRODUCT_MISSING", "");
			} else {
				if (qss4ProductByid != null) {
					qss4ProductByid.forEach(product -> {
						if (qss4ProductByCode == null || !qss4ProductByCode.contains(product)) {
							utility.addProductToMailList(product.getProductName(), product.getQuoddDbName(),
									product.getQuoddEntitlementId() + "", "MANUAL_PRODUCT_MAPPING_MISSING_CODE", "");
							logger.warning("MANUAL_PRODUCT_MAPPING_MISSING_CODE " + entitlementBean + " " + product);
						} else {
							String backendKey = entitlementBean.getBackendKey();
							if (backendKey == null || backendKey.isEmpty()) {
								if (!entitlementBean.getFeedQuality().equalsIgnoreCase(product.getFeedQuality())) {
									utility.addProductToMailList(product.getProductName(), product.getQuoddDbName(),
											product.getQuoddEntitlementId() + "", "PRODUCT_MISMATCH_DATAFEED",
											"QS = " + entitlementBean.getFeedQuality() + " QSS4 - "
													+ product.getFeedQuality());
									logger.warning("PRODUCT_MISMATCH_DATAFEED : Product_id " + product.getProductId()
											+ " Enitlement_id " + entitlementBean.getEntitlementId());
								}
							}
						}
					});
				} else {
					utility.addProductToMailList(entitlementBean.getFullName(), entitlementBean.getDbFieldName(),
							entitlementBean.getEntitlementId() + "", "MANUAL_PRODUCT_MAPPING_MISSING_ID", "");
					logger.warning("MANUAL_PRODUCT_MAPPING_MISSING_ID " + entitlementBean);
				}
				if (qss4ProductByCode != null) {
					qss4ProductByCode.forEach(product -> {
						if (qss4ProductByid == null || !qss4ProductByid.contains(product)) {
							utility.addProductToMailList(product.getProductName(), product.getQuoddDbName(),
									product.getQuoddEntitlementId() + "", "MANUAL_PRODUCT_MAPPING_MISSING_ID",
									"NEW_ENTITLEMENT_ID: " + entitlementId);
							logger.warning("MANUAL_PRODUCT_MAPPING_MISSING_ID " + entitlementBean + " " + product);
						} else {
							String backendKey = entitlementBean.getBackendKey();
							if (backendKey == null || backendKey.isEmpty()) {
								if (!entitlementBean.getFeedQuality().equalsIgnoreCase(product.getFeedQuality())) {
									utility.addProductToMailList(product.getProductName(), product.getQuoddDbName(),
											product.getQuoddEntitlementId() + "", "PRODUCT_MISMATCH_DATAFEED",
											"QS = " + entitlementBean.getFeedQuality() + " QSS4 - "
													+ product.getFeedQuality());
									logger.warning("PRODUCT_MISMATCH_DATAFEED : Product_id " + product.getProductId()
											+ " Enitlement_id " + entitlementBean.getEntitlementId());
								}
							}
						}
					});
				} else {
					utility.addProductToMailList(entitlementBean.getFullName(), entitlementBean.getDbFieldName(),
							entitlementBean.getEntitlementId() + "", "MANUAL_PRODUCT_MAPPING_MISSING_CODE", "");
					logger.warning("MANUAL_PRODUCT_MAPPING_MISSING_CODE " + entitlementBean);
				}
			}
		});
	}
}
