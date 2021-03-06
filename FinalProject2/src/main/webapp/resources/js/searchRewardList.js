/**
 * 
 */
	var global_reward_category_item_list_page = 1;
	var global_isLoding=false;
	
	$(document).ready(function(){
	
		$(window).scrollTop(0);
		
		//스크롤 페이징
		$(window).scroll(function() {

		    if ($(window).scrollTop() == $(document).height() - $(window).height()+117) {
		   	 $.ajax({
		   		url:getContextPath()+'/searchRewardListAjax',
		   		dataType:'json',
		   		data:{'cPage':global_reward_category_item_list_page,"rewardState":$('#reward_state_filter').val(),"listFilter":$('#reward_watch_filter').val(),"main_header_searchbar":$('#searchInform').val()},
		   		success:function(data){
		   			if(data.length>0&&!(global_isLoding)){
		   				global_isLoding=true;
		   				global_reward_category_item_list_page++;
		   				$('.search_reward_content').append($('<img/>',{
		   					id:'reward_list_loading',
		   					src:getContextPath()+'/resources/images/common/loading.gif'
		   				}));
		   				setTimeout(function(){
							$('#reward_list_loading').remove();
							for(var i=0;i<data.length;i++){
								$('.search_reward_content').append($('<div/>',{
					   				id:data[i].REWARD_NO,
					   				class:'search_reward_content_item',
					   				style:'margin-right:15px',
					   				onclick:"clickReward(this);"
					   			}));
								$('.search_reward_content_item:nth-child(3n)').css("margin-right","-10px");
								
					   			$('#'+data[i].REWARD_NO+'').append($('<img/>',{
					   				src:getContextPath()+data[i].REWARD_REPRESENT_IMAGE
					   			}));
					   			$('#'+data[i].REWARD_NO+'').append($('<div/>',{
					   				id: data[i].REWARD_NO+'_inform1',
					   				class:'search_reward_content_item_inform1'
					   			}));
					   			$('#'+data[i].REWARD_NO+'_inform1').append($('<h3/>',{
					   				class:'search_reward_item_punding_title',
					   				text:data[i].REWARD_SHORT_NAME
					   			}));
					   			$('#'+data[i].REWARD_NO+'_inform1').append($('<h5/>',{
					   				class:'search_reward_item_category_name',
					   				text:data[i].REWARD_CATEGORY_NAME,
					   				style:'margin-right:5px'
					   			}));
					   			$('#'+data[i].REWARD_NO+'_inform1').append($('<h5/>',{
					   				text:'|',
					   				style:'margin-right:5px'
					   			}));
					   			$('#'+data[i].REWARD_NO+'_inform1').append($('<h5/>',{
					   				class:'search_reward_item_company_name',
					   				text:data[i].REWARD_MC_NAME
					   			}));
					   			
					   			if(data[i].REWARD_STATE!=4){
						   			$('#'+data[i].REWARD_NO+'').append($('<div/>',{
						   				id:data[i].REWARD_NO+'_persentBar_background',
						   				class:'search_reward_persentBar_background'
						   			}));
						   			$('#'+data[i].REWARD_NO+'_persentBar_background').append($('<div/>',{
						   				id:data[i].REWARD_NO+'_persentBar',
						   				class:'search_reward_persentBar'
						   			}));
						   			
						   			//퍼센트바 채우기
						   			$('#'+data[i].REWARD_NO+'_persentBar').css("width",(3*data[i].REWARD_ACHIEVEMENT_PERSENT+'px')); 
					   		
						   			$('#'+data[i].REWARD_NO+'').append($('<div/>',{
						   				id:data[i].REWARD_NO+'_inform2',
						   				class:'search_reward_content_item_inform2'
						   			}));
					   			
						   			$('#'+data[i].REWARD_NO+'_inform2').append($('<h3/>',{
						   				class:'search_reward_item_punding_achievement_quotient',
						   				text:data[i].REWARD_ACHIEVEMENT_PERSENT+'%'
						   			}));
						   			
						   			$('#'+data[i].REWARD_NO+'_inform2').append($('<h4/>',{
						   				class:'search_reward_item_punding_sum',
						   				text:data[i].REWARD_PRESENT_COLLECTION.toLocaleString()+'원'
						   			}));
					   			
						   			$('#'+data[i].REWARD_NO+'_inform2').append($('<h4/>',{
						   				class:'search_reward_item_punding_remain_date',
						   				text:data[i].REWARD_REMAIN_DATE+'일 남음'
						   			}));
					   			}
				   			}							
			   				global_isLoding=false;
						},1000);
		   			}
					
		   		}
		   	 });
		   	 
		    }
		});
		
		
		$('#reward_state_filter').val(5);
		$('#reward_watch_filter').val(1);
		
		
	});
//////////////ready 끝




	
	//필터변경 시 리워드 초기화 이벤트
	function changeFilter(targ){
		global_reward_category_item_list_page=1;
		if(targ=='option'){
			if($('#reward_state_filter').val()==4){
				$('#reward_watch_filter').val(1);
			}
		}

		$('.search_reward_content').empty();
		$.ajax({
		   		url:getContextPath()+'/searchRewardListAjax',
		   		dataType:'json',
		   		data:{'cPage':0,"rewardState":$('#reward_state_filter').val(),"listFilter":$('#reward_watch_filter').val(),"main_header_searchbar":$('#searchInform').val()},
		   		success:function(data){
		   			
					for(var i=0;i<data.length;i++){
			   			$('.search_reward_content').append($('<div/>',{
			   				id:data[i].REWARD_NO,
			   				class:'search_reward_content_item',
			   				style:'margin-right:15px',
			   				onclick:"clickReward(this);"
			   			}));
						$('.search_reward_content_item:nth-child(3n)').css("margin-right","-10px");
						
			   			$('#'+data[i].REWARD_NO+'').append($('<img/>',{
			   				src:getContextPath()+data[i].REWARD_REPRESENT_IMAGE
			   			}));
			   			$('#'+data[i].REWARD_NO+'').append($('<div/>',{
			   				id: data[i].REWARD_NO+'_inform1',
			   				class:'search_reward_content_item_inform1'
			   			}));
			   			$('#'+data[i].REWARD_NO+'_inform1').append($('<h3/>',{
			   				class:'search_reward_item_punding_title',
			   				text:data[i].REWARD_SHORT_NAME
			   			}));
			   			$('#'+data[i].REWARD_NO+'_inform1').append($('<h5/>',{
			   				class:'search_reward_item_category_name',
			   				text:data[i].REWARD_CATEGORY_NAME,
			   				style:'margin-right:5px'
			   			}));
			   			$('#'+data[i].REWARD_NO+'_inform1').append($('<h5/>',{
			   				text:'|',
			   				style:'margin-right:5px'
			   			}));
			   			$('#'+data[i].REWARD_NO+'_inform1').append($('<h5/>',{
			   				class:'search_reward_item_company_name',
			   				text:data[i].REWARD_MC_NAME
			   			}));
			   			
			   			if(data[i].REWARD_STATE!=4){
				   			$('#'+data[i].REWARD_NO+'').append($('<div/>',{
				   				id:data[i].REWARD_NO+'_persentBar_background',
				   				class:'search_reward_persentBar_background'
				   			}));
				   			$('#'+data[i].REWARD_NO+'_persentBar_background').append($('<div/>',{
				   				id:data[i].REWARD_NO+'_persentBar',
				   				class:'search_reward_persentBar'
				   			}));
				   			
				   			//퍼센트바 채우기
				   			$('#'+data[i].REWARD_NO+'_persentBar').css("width",(3*data[i].REWARD_ACHIEVEMENT_PERSENT+'px')); 
				   			
				   			$('#'+data[i].REWARD_NO+'').append($('<div/>',{
				   				id:data[i].REWARD_NO+'_inform2',
				   				class:'search_reward_content_item_inform2'
				   			}));
				   			
					   			$('#'+data[i].REWARD_NO+'_inform2').append($('<h3/>',{
					   				class:'search_reward_item_punding_achievement_quotient',
					   				text:data[i].REWARD_ACHIEVEMENT_PERSENT+'%'
					   			}));
					   			
					   			$('#'+data[i].REWARD_NO+'_inform2').append($('<h4/>',{
					   				class:'search_reward_item_punding_sum',
					   				text:data[i].REWARD_PRESENT_COLLECTION.toLocaleString()+'원'
					   			}));
				   			
				   			$('#'+data[i].REWARD_NO+'_inform2').append($('<h4/>',{
				   				class:'search_reward_item_punding_remain_date',
				   				text:data[i].REWARD_REMAIN_DATE+'일 남음'
				   			}));
			   			}
		   			}
		   			
		   			
		   			
		   		}
		   	 
		   	 });
	}
	
	function clickReward(targ){
		location.href=getContextPath()+"/project/reward/"+$(targ).attr("id");
	}