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

	int updateRewardStoryContentList(Reward reward);

	Reward getRewardCommentInfo(Map<String, Object> param);

	int insertRewardComment(Map<String, Object> param);

	List<Map<String, Object>> insertRewardRecomment(Map<String, Object> param);

	List<Map<String, Object>> reloadRewardRecomment(Map<String, Object> param);

	List<Map<String, Object>> reloadRewardComment(Map<String, Object> param);

	Reward getRewardNoticeInfo(Map<String, Object> param);

	Reward getRewardStoryInfo(Map<String, Object> param);

	Map<String, Object> clickRewardLike(Map<String, Object> param);

}
