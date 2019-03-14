package com.spring.test.reward.model.service;

import java.util.List;
import java.util.Map;

import com.spring.test.reward.model.vo.Reward;
import com.spring.test.reward.model.vo.RewardItem;

public interface RewardService {

	int selectNextRewardNo();

	List<Map<String, Object>> selectRewardCategoryList();

	int selectCurRewardNo();

	int createProjectReward(Map<String, Object> basicInfo);

	int updateRewardBasicInfo(Map<String, Object> param);

	Reward selectReward(int rewardNo);

	int insertRewardItem(RewardItem rewardItem);

	int deleteRewardItem(int itemNo);

	int updateRewardItem(RewardItem rewardItem);

	Reward getRewardCommentInfo(int rewardNo);

	Reward getRewardNoticeInfo(int rewardNo);

	Reward getRewardStoryInfo(int rewardNo);

}
