package com.spring.test.user.controller;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.spring.test.common.util.FileUtil;
import com.spring.test.common.util.StringUtil;
import com.spring.test.user.model.service.UserService;

@Controller
public class UserController {
	
	@Autowired
	UserService service;
	
	@Autowired
	BCryptPasswordEncoder pwEncoder;
	
	@Autowired
	StringUtil stringUtil;
	
	@Autowired
	FileUtil fileUtil;

	
	//회원가입
		//이동
	@RequestMapping("/welcome")
	public String goRegist(HttpServletRequest request)
	{
		String loc = "redirect:/mainPage";
		if(request.getSession().getAttribute("userNo") == null)
		{
			loc="user/regist";
		}
		return loc;
	}
			//베이직 이동
	@RequestMapping("/regist/basic")
	public String goBasicRegist(HttpServletRequest request)
	{
		String loc = "redirect:/mainPage";
		if(request.getSession().getAttribute("userNo") == null)
		{
			loc="user/registFrm";
		}
		return loc;
	}
			//약관페이지로 이동
	@RequestMapping("/terms")
	public String goTerms()
	{
		return "user/terms";
	}
		//기능
			//아이디 중복 확인
	@ResponseBody
	@RequestMapping("/regist/checkEmail")
	public Map checkEmail(String email)
	{
		Map map=new HashMap();
		int check=service.selectUserEmailCount(email);
		
		map.put("check", check);
		
		return map;
	}
			//이메일 인증
				//보내기
	@ResponseBody
	@RequestMapping("/sendEmail")
	public void sendEmail(String email, String type, HttpServletRequest request)
	{
		//랜덤키(인증번호) 만들어서 세션에 넣음
		int tempKey=service.getTempKey();
		request.getSession().setAttribute("tempKey", tempKey);
		
		//랜덤키 담은 email 넣음
		System.out.println(email);
		service.sendEmailKey(email, tempKey,type);
	}
				//시간초과
	@ResponseBody
	@RequestMapping("/confirm/timeUp")
	public void timeUp(HttpServletRequest request)
	{
		request.getSession(false).removeAttribute("tempKey");
	}
				//확인
	@ResponseBody
	@RequestMapping("/confirm/checkKey")
	public Map checkKey(String tempKey, HttpServletRequest request)
	{
		int key=Integer.parseInt(tempKey);
		Map resultMap=new HashMap();
		
		boolean result=false;
		String msg="인증번호를 다시 확인해 주세요";
		
		if((int)(request.getSession(false).getAttribute("tempKey"))==key)
		{
			request.getSession(false).removeAttribute("tempKey");
			result=true;
			msg="인증되었습니다.";
		}
		
		resultMap.put("result", result);
		resultMap.put("msg", msg);
		
		return resultMap;
	}
			//회원 저장
	@ResponseBody
	@RequestMapping("/registUser")
	public int registUser(String email, String pw, String name, int userType, HttpServletRequest request)
	{
		Map user=new HashMap();
		user.put("USER_EMAIL", email);
		user.put("USER_NAME", name);
		user.put("USER_LINK_TYPE", userType);
		user.put("USER_PROFILEPHOTO", "/resources/images/common/header/user_Inform.png");
		
		if(userType==1)
		{
			String newPw=pwEncoder.encode(pw);
			user.put("USER_PASSWORD", newPw);
		}
		else if(userType==2||userType==3)
		{
			user.put("UNIQKEY", pw);
		}
		int result=service.insertUser(user);
		
		return result;
	}
	
	
	//회원탈퇴
		//이동
	@RequestMapping("/leave")
	public String goLeave(HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");
		Map user = service.selectUserBasic(userNo);
		
		String email = String.valueOf(user.get("USER_EMAIL"));
		request.setAttribute("email", email);
		
		return "user/outUser";
	}
		//기능
	@ResponseBody
	@RequestMapping("/leave/outUser")
	public Map deleteUser(String outReason, HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");
		
		Map user=service.selectUserBasic(userNo);
		user.put("OUT_REASON", outReason);
		
		
		String msg="";
		String loc="";
		
		//만든 리워드 중 진행중인 리워드 (reward_state in (3,4,5)) 있으면 탈퇴 불가
		
		List<Map> reward = service.selectUserRewardMade(userNo,"default");
		int flag = 0; // 0 : 진행중인 리워드 없음, 1 : 진행중인 리워드 있음
		
		for(Map r : reward)
		{
			int state = Integer.parseInt(String.valueOf(r.get("REWARD_STATE")));
			if(state>2&&state<6)
			{
				flag = 1;
				break;
			}
		}
		
		if(flag == 0)
		{
			int result=service.outUser(user);
			
			if(result>0)
			{
				msg="지금까지 이용해주셔서 감사합니다.";
				loc="/test/logout";
			}
			else
			{
				msg="다시 시도해 주세요.";
				loc="/test/leave";
			}
		}
		else
		{
			msg="진행중인 리워드가 있을 경우 탈퇴하실 수 없습니다.";
			loc = "/test/myprofile";
		}
		
		
		Map temp = new HashMap();
		temp.put("msg", msg);
		temp.put("loc", loc);
		
		return temp;
	}
	
	//로그인 체크
	@ResponseBody
	@RequestMapping("/user/loginCheckAjax") 
	public boolean isLogin(HttpServletRequest request) {
		System.out.println("들어옴?");
		
		return request.getSession().getAttribute("userNo") != null;
	}
	
	
//로그인
		//이동
	@RequestMapping("/login")
	public String goLogin(HttpServletRequest request)
	{
		String loc = "redirect:/mainPage";
		if(request.getSession().getAttribute("userNo") == null)
		{
			loc="user/login";
		}
		return loc;
	}
		//기능
			//일반회원 로그인
	@ResponseBody
	@RequestMapping("/login.do")
	public Map login(String email, String password, HttpServletRequest request)
	{
		Map user = service.selectUser(email);
		
		String msg="";
		
		if(user!=null&&!user.isEmpty())
		{
			int linkType = Integer.parseInt(String.valueOf((user.get("USER_LINK_TYPE"))));
			System.out.println(linkType);
			//홈페이지 회원 로그인
			if(linkType == 1)
			{
				String encodedPassword = (String)user.get("USER_PASSWORD");
				if(pwEncoder.matches(password, encodedPassword))
				{
					msg=null;
					int userNo=Integer.parseInt(String.valueOf(user.get("USER_NO")));
					request.getSession().setAttribute("userNo",userNo);
				}
				else
				{
					msg="비밀번호를 다시 확인해주세요.";
				}
			}
			else if(linkType == 2 || linkType ==3)
			{
				msg = "해당 소셜 로그인 버튼으로 로그인해주세요.";
			}
		}
		else
		{
			user=new HashMap();
			msg="등록되지 않은 이메일입니다.";
		}
		
		user.put("msg", msg);
		
		return user;
	}
	
	//소셜 로그인
	@RequestMapping("/enroll/naverCallback")
	public String goSocialUserPage() {
		return "user/socialCallback";
	}
	@ResponseBody
	@RequestMapping("/socialUserControl.do")
	public Map loginSocial(String email, String pw, HttpServletRequest request)
	{
		Map user = service.selectUser(email);

		String msg="";
		
		if(user!=null&&!user.isEmpty())
		{
			int linkType = Integer.parseInt(String.valueOf((user.get("USER_LINK_TYPE"))));
			//소셜 회원 로그인
			if(linkType==2)
			{
				String uniqKey=(String)user.get("USER_NAVER_UNIQ");
				if(pw.equals(uniqKey))
				{
					msg=null;
					int userNo=Integer.parseInt(String.valueOf(user.get("USER_NO")));
					request.getSession().setAttribute("userNo",userNo);
				}
				else
				{
					msg="사용자 번호가 틀립니다. 문제가 계속되면 관리자에 문의하세요.";
				}
			}
			else if(linkType==3)
			{
				String uniqKey=(String)user.get("USER_KAKAO_UNIQ");
				if(pw.equals(uniqKey))
				{
					msg=null;
					int userNo=Integer.parseInt(String.valueOf(user.get("USER_NO")));
					request.getSession().setAttribute("userNo",userNo);
				}
				else
				{
					msg="사용자 번호가 틀립니다. 문제가 계속되면 관리자에 문의하세요.";
				}
			}
		}
		
		user.put("msg", msg);

		return user;
	}
	
		//로그인 콜백 페이지
	@RequestMapping("/login/naverCallback")
	public String goLoginNaverCallback()
	{
		return "user/loginSocial";
	}
	@RequestMapping("/login/kakaoCallback")
	public String goLoginKakaoCallback()
	{
		return "user/loginSocial";
	}
	
	//ID/PW찾기
		//이동
			//ID찾기
	@RequestMapping("/find/id")
	public String goFindId(HttpServletRequest request)
	{
		request.setAttribute("title", "ID찾기");
		return "user/findIdPw";
	}
			//PW찾기(기본)
	@RequestMapping("/find/pw")
	public String goFindPw(HttpServletRequest request)
	{
		request.setAttribute("title", "PW찾기");
		return "user/findIdPw";
	}
		//기능
			//ID찾기
	@ResponseBody
	@RequestMapping("/find/id.do")
	public String findId(String email)
	{
		String msg="";
		
		Map user = service.findId(email);
		
		if(user.get("USER_NO")!=null)
		{
			switch(Integer.parseInt(String.valueOf(user.get("USER_LINK_TYPE"))))
			{
				case 2 : msg="네이버 연계 회원입니다. 네이버 소셜 로그인을 이용해 주세요.";break;
				case 3 : msg="카카오 연계 회원입니다. 카카오 소셜 로그인을 이용해 주세요."; break;
				default : msg="홈페이지에 가입된 회원입니다."; break;
			}
		}
		else
		{
			msg="등록되지 않은 이메일입니다.";
		}
		
		return msg;
	}
			//PW찾기
				//PW 링크 보내기
	@ResponseBody
	@RequestMapping("/find/pw.do")
	public String findPw(String email, HttpServletRequest request)
	{
		String msg="";
		Map user=service.findId(email);
		
		//만약 홈페이지 회원일 경우
		if(Integer.parseInt(String.valueOf(user.get("USER_LINK_TYPE")))==1)
		{
			String tempKey=stringUtil.getRandomPassword(15);
			user.put("TEMP_KEY", tempKey);
			
			int result=service.requestFindPw(user);
			
			if(result>0)
			{
				try {
						URLCodec codec=new URLCodec();
						tempKey = codec.encode(tempKey);
					
					}
				catch(Exception e)
					{
						e.printStackTrace();
					}
				service.sendEmailLink(email, tempKey);
				msg="이메일을 확인해 주세요.";
			}
			else
			{
				msg="다시 시도해 주세요.";
			}
		}
		//홈페이지 회원이 아닌 경우
		else
		{
			msg="해당 소셜 버튼으로 로그인해 주세요.";
		}
		
		return msg;
	}
				//PW 링크 확인하고 PW 변경페이지로
	@RequestMapping(value="/resetPw/{key}", method=RequestMethod.GET) 
	public String goResetPw(@PathVariable("key") String key, HttpServletRequest request)
	{
		String tempKey="";
		
		//링크확인
		try {
				URLCodec codec=new URLCodec();
				tempKey=codec.decode(key);
			}
		catch(Exception e)
			{
				e.printStackTrace();
			}
		
		Map temp=service.findPwLink(tempKey);
		
		//PW변경 페이지로
		String loc="";
		if(temp.get("USER_NO")!=null)
		{
			Date tempdate = (Date)temp.get("TEMP_DATE");
			Date sysdate = new Date();
			long compare = sysdate.getTime()-tempdate.getTime();
			if(compare<86400000&&compare>=0)
			{
				int tempUserNo=Integer.parseInt(String.valueOf(temp.get("USER_NO")));
				request.getSession(false).setAttribute("tempUserNo", tempUserNo);
				service.deleteFindPwLink(tempKey);
				loc="/myprofile/modify/basic";
			}
			else
			{
				String msg="만료된 링크입니다.";
				loc="/main";
			}
		}
		else
		{
			loc="/main";
		}
			
		return "redirect:"+loc;
	}
	//로그아웃
	@RequestMapping(value="/logout")
	public ModelAndView logout(HttpServletRequest request)
	{
		int userNo=(int)request.getSession(false).getAttribute("userNo");
	
		request.getSession(false).removeAttribute("userNo");
		
		RedirectView rv=new RedirectView(request.getContextPath()+"/main");
		rv.setExposeModelAttributes(false);
		
		return new ModelAndView(rv);
	}
	//회원정보 수정
		//이동 (+ 회원정보 불러오기)
			//수정메뉴 페이지로
	@RequestMapping("/myprofile")
	public String goProfile(HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");
		Map user=service.selectUserBasic(userNo);
		request.setAttribute("user", user);
		
		return "user/editProfile";
	}
			//기본정보 수정페이지로
	@RequestMapping("/myprofile/modify/basic")
	public String goModifyBasic(HttpServletRequest request)
	{
		int userNo=0;
		String title="";
		
		if(request.getSession(false).getAttribute("userNo")!=null)
		{
			userNo=(Integer)request.getSession(false).getAttribute("userNo");
			title="회원정보변경";
		}
		else if(request.getSession(false).getAttribute("tempUserNo")!=null)
		{
			userNo=(Integer)request.getSession(false).getAttribute("tempUserNo");
			title="비밀번호 변경";
		}
		Map user=service.selectUserBasic(userNo);
		user.put("TITLE",title);
		request.setAttribute("user", user);
		
		return "user/editBasic";
	}
			//주소록 수정페이지로
	@RequestMapping("/myprofile/modify/address")
	public String goModifyAddress(HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");
		List<Map> userAddress=service.selectUserAddress(userNo);
		request.setAttribute("userAddress", userAddress);
		
		return "user/editAddress";
	}
			//계좌 확인페이지로
	@RequestMapping("/myprofile/modify/account")
	public String goModifyAccount(HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");
		Map userAccount = service.selectUserAccount(userNo);
		request.setAttribute("userAccount", userAccount);
		
		return "user/editAccount";
	}
		//기능
			//회원정보 업데이트
				//프로필사진 업데이트
	@ResponseBody
	@RequestMapping("/myprofile/modify/profilephoto.do")
	public Map modifyProfilePhoto(@RequestParam MultipartFile selectedPhoto, Model model, HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");

		String msg="";
		String loc="";
		
		Map user=new HashMap();

		int result=0;
		
		if (!selectedPhoto.isEmpty()) 
		{
			
			String rootDir = request.getSession().getServletContext().getRealPath("/");
			String saveDir = "resources/upload/userProfilePhoto";
			String renamedFileName = fileUtil.getRenamedFileName(selectedPhoto);
			fileUtil.saveFile(selectedPhoto, rootDir, saveDir, renamedFileName);
			String saveAllDir = "/" + saveDir + "/" + renamedFileName;
			
			user.put("USER_NO", userNo);
			user.put("USER_PROFILEPHOTO", saveAllDir);
			
			result = service.updateUserPhoto(user);
		}
		else
		{
			user.put("USER_NO", userNo);
			user.put("USER_PROFILEPHOTO", "/resources/images/common/header/user_Inform.png");
			result = service.updateUserPhoto(user);
		}
		
		if(result>0)
		{
			msg="프로필 사진 변경 성공";
		}
		else
		{
			msg="다시 시도해주세요";
		}

		loc="/test/myprofile";
		
		Map temp=new HashMap();
		temp.put("msg", msg);
		temp.put("loc", loc);
		
		return temp;
	}
				//이름 업데이트
	@ResponseBody
	@RequestMapping("/myprofile/modify/userName.do")
	public Map modifyUserName(@RequestParam String editUserName, HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");
		Map user=new HashMap();
		
		user.put("USER_NO", userNo);
		user.put("USER_NAME", editUserName);
		
		String msg="";
		String loc="";
		
		int result=service.updateUserName(user);
		
		if(result>0)
		{
			msg="회원정보가 업데이트 되었습니다.";
		}
		else
		{
			msg="다시 시도해주세요.";
		}

		loc="/test/myprofile";
		
		Map temp = new HashMap();
		temp.put("msg", msg);
		temp.put("loc", loc);
		
		return temp;
	}
	
				//이메일 업데이트  + //비밀번호 업데이트
	@ResponseBody
	@RequestMapping("/myprofile/modify/basic.do")
	public Map modifyBasic(String email, String password, String newPassword, HttpServletRequest request)
	{
		String msg=null;
		String loc="";
		int flag=1;
		int result=0;
		Map user=null;
		
		//정상접근(userNo)일 때
		if(request.getSession(false).getAttribute("userNo")!=null)
		{
			int userNo=(Integer)request.getSession(false).getAttribute("userNo");
			user=service.selectUserBasic(userNo);
			
			user.put("USER_NO", userNo);
			
			if(email!=null&&!email.trim().equals(""))
			{
				user.put("USER_EMAIL", email);
			}
			
			if(newPassword!=null&&!newPassword.trim().equals(""))
			{
				String encodedPassword = (String)user.get("USER_PASSWORD");
				if(pwEncoder.matches(password, encodedPassword))
				{
					String newEncodedPassword = pwEncoder.encode(newPassword);
					user.put("USER_PASSWORD", newEncodedPassword);
				}
				else
				{
					flag=0;
				}
			}
		}
		else if(request.getSession(false).getAttribute("tempUserNo")!=null)
		{
			int userNo=(Integer)request.getSession(false).getAttribute("tempUserNo");
			user=service.selectUserBasic(userNo);
			
			String newEncodedPassword = pwEncoder.encode(newPassword);
			user.put("USER_PASSWORD", newEncodedPassword);
		}
		
		if(flag==1)
		{
			result=service.updateUserBasic(user);
			
		}
		
		if(result>0)
		{
			if(request.getSession(false).getAttribute("userNo")!=null)
			{
				msg="회원정보가 업데이트 되었습니다.";
				loc="/test/myprofile/modify/basic";
			}
			else if(request.getSession(false).getAttribute("tempUserNo")!=null)
			{
				msg="비밀번호 변경이 완료되었습니다. 로그인해주세요.";
				request.getSession(false).removeAttribute("tempUserNo");
				loc="/test/main";
			}
		}
		else
		{
			msg="비밀번호가 일치하지 않습니다";
		}
		
		Map temp=new HashMap();
		
		temp.put("msg", msg);
		temp.put("loc", loc);
		
		return temp;
	}
				//주소록 업데이트(수정)
	@RequestMapping("/myprofile/modify/address.do")
	public String modifyAddress(int addressNo, String addressName, int addressZipNo,
			String addressWhole, String addressDetail, String addressPhone, String addressReceiverName)
	{
		Map userAddress = new HashMap();
		userAddress.put("ADDRESS_NO", addressNo);
		userAddress.put("ADDRESS_NAME", addressName);
		userAddress.put("ADDRESS_ZIP_NO", addressZipNo);
		userAddress.put("ADRESS_WHOLE", addressWhole);
		userAddress.put("ADDRESS_DETAIL", addressDetail);
		userAddress.put("ADDRESS_PHONE", addressPhone);
		userAddress.put("ADDRESS_RECEIVER_NAME", addressReceiverName);
		
		service.updateUserAddress(userAddress);
		
		return "redirect:/myprofile/modify/address";
	}
				//주소록 추가
	@RequestMapping("/myprofile/modify/address/add")
	public String addMyAddress(HttpServletRequest request, String addressName,
			int addressZipNo, String addressWhole, String addressDetail, String addressPhone,
			String addressReceiverName)
	{
		int userNo = (Integer)request.getSession(false).getAttribute("userNo");
		Map userAddress = new HashMap();
		userAddress.put("USER_NO",userNo);
		userAddress.put("ADDRESS_NAME", addressName);
		userAddress.put("ADDRESS_ZIP_NO", addressZipNo);
		userAddress.put("ADRESS_WHOLE", addressWhole);
		userAddress.put("ADDRESS_DETAIL", addressDetail);
		userAddress.put("ADDRESS_PHONE", addressPhone);
		userAddress.put("ADDRESS_RECEIVER_NAME", addressReceiverName);
		
		service.insertUserAddress(userAddress);
		
		return "redirect:/myprofile/modify/address";
	}
				//주소록 삭제
	@ResponseBody
	@RequestMapping("/myprofile/modify/address/delete")
	public Map deleteMyAddress(int addressNo)
	{
		String msg="";
		String loc="";
		
		int result = service.deleteUserAddress(addressNo);
		if(result>0)
		{
			msg="삭제되었습니다.";
		}
		else
		{
			msg="삭제실패!";
		}
		loc="/test/myprofile/modify/address";
		Map temp=new HashMap();
		temp.put("msg", msg);
		temp.put("loc", loc);
		
		return temp;
	}
	
	//자기 리워드 리스트 보기
		//후원한 reward 가져오기 (기본)
	@RequestMapping("/userPage")
	public String myRewardPage(String order, HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");
		Map user=service.selectUserBasic(userNo);
		
		List<Map> myList = service.selectUserRewardSupport(userNo, order);
		request.setAttribute("myList", myList);
		request.setAttribute("userName", user.get("USER_NAME"));
		request.setAttribute("title", "참여한 리워드");
		
		return "user/mypage";
	}
		//진행하는 reward 가져오기
	@RequestMapping("/userPage/made")
	public String myMadeRewardPage(String order, HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");
		Map user=service.selectUserBasic(userNo);
		
		List<Map> myList = service.selectUserRewardMade(userNo, order);
		request.setAttribute("myList", myList);
		request.setAttribute("userName", user.get("USER_NAME"));
		request.setAttribute("title", "만든 리워드");
		
		return "user/mypage";
	}
		//좋아한 reward 가져오기
	@RequestMapping("/userPage/like")
	public String myLikeRewardPage(String order, HttpServletRequest request)
	{
		int userNo=(Integer)request.getSession(false).getAttribute("userNo");
		Map user=service.selectUserBasic(userNo);
		
		List<Map> myList = service.selectUserRewardLike(userNo, order);
		request.setAttribute("myList", myList);
		request.setAttribute("userName", user.get("USER_NAME"));
		request.setAttribute("title", "좋아한 리워드");
		
		return "user/mypage";
	}
	
		

	
	//특정 유저의 리워드 리스트 보기
		//그 유저가 후원한 reword 가져오기 (기본)
	@RequestMapping("/userPage/{userNo}")
	public String userRewardPage(@PathVariable("userNo") int userNo, String order, HttpServletRequest request)
	{
		List<Map> myList = service.selectUserRewardSupport(userNo, order);
		request.setAttribute("myList", myList);
		request.setAttribute("title", "참여한 리워드");
		
		return "user/mypage";
	}
		//그 유저가 진행하는 reword 가져오기
	@RequestMapping("/userPage/made/{userNo}")
	public String userMadeRewardPage(@PathVariable("userNo") int userNo, String order, HttpServletRequest request)
	{
		List<Map> myList = service.selectUserRewardMade(userNo, order);
		request.setAttribute("myList", myList);
		request.setAttribute("title", "만든 리워드");
		
		return "user/mypage";
	}
		//그 유저가 좋아하는 reword 가져오기
	@RequestMapping("/userPage/like/{userNo}")
	public String userLikeRewardPage(@PathVariable("userNo") int userNo, String order, HttpServletRequest request)
	{
		List<Map> myList = service.selectUserRewardLike(userNo, order);
		request.setAttribute("myList", myList);
		request.setAttribute("title", "좋아한 리워드");
		
		return "user/mypage";
	}
}
