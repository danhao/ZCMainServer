package com.zc.web.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloopen.rest.sdk.CCPRestSDK;
import com.zc.web.cache.PlayerCache;
import com.zc.web.data.model.Player;

/**
 * 电话服务
 * 
 * @author small
 *
 */
public class CallService {

	private static final Logger log = Logger.getLogger(CallService.class);

	private static CCPRestSDK restAPI = new CCPRestSDK();
	static{
		restAPI.init("sandboxapp.cloopen.com", "8883");// 初始化服务器地址和端口，格式如下，服务器地址不需要写https://
		restAPI.setAccount("8a48b5514fba2f87014fc02ceaf40f83", "a8025d9dd64241d89e94002cce6e4a55");// 初始化主帐号和主帐号TOKEN
		restAPI.setAppId("8a48b5514fba2f87014fc0a3b47710a3");// 初始化应用ID
	}
	
	public static Map<String, Object> querySubAccount(String id){
		HashMap<String, Object> result = restAPI.querySubAccount(id);

		log.info("SDKTestQuerySubAccount result=" + result);
		if("000000".equals(result.get("statusCode"))){
			//正常返回输出data包体信息（map）
			return (HashMap<String, Object>) result.get("data");
		}else{
			//异常返回输出错误码和错误信息
			log.error("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
			
			return null;
		}
	}
	
	public static void createSubAccount(Player player){
		HashMap<String, Object> result = restAPI.createSubAccount(String.valueOf(player.getId()));

		log.info("SDKTestCreateSubAccount result=" + result);
		
		if("000000".equals(result.get("statusCode"))){
			//正常返回输出data包体信息（map）
			Map<String, Object> data = (HashMap<String, Object>) result.get("data");
			player.setVoipId((String)data.get("voipAccount"));
			player.setVoipPwd((String)data.get("voipPwd"));
			
			PlayerService.savePlayer(player);
		}else{
			//异常返回输出错误码和错误信息
			log.info("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
		}		
	}
	
}
