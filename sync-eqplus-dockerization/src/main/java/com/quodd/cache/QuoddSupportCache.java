package com.quodd.cache;

import java.util.HashMap;
import java.util.Map;

import com.quodd.bean.User;

public class QuoddSupportCache {

	private Map<Long, User> quoddSupportActiveUser = new HashMap<>();
	private Map<Long, User> quoddSupportCancelledUser = new HashMap<>();

	public Map<Long, User> getQuoddSupportActiveUser() {
		return this.quoddSupportActiveUser;
	}

	public void setQuoddSupportActiveUser(Map<Long, User> quoddSupportActiveUser) {
		this.quoddSupportActiveUser = quoddSupportActiveUser;
	}

	public Map<Long, User> getQuoddSupportCancelledUser() {
		return this.quoddSupportCancelledUser;
	}

	public void setQuoddSupportCancelledUser(Map<Long, User> quoddSupportCancelledUser) {
		this.quoddSupportCancelledUser = quoddSupportCancelledUser;
	}

}
