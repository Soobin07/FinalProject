package com.spring.test.account.service;

import java.util.List;
import java.util.Map;

public interface AccountService {
	List<Map<String, Object>> selectRewardAccount(int rewardNo);
	int updateRewardAccount(Map<String, Object> param);
}
