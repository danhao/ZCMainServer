package com.zc.web.action;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.zc.web.cache.PlayerCache;
import com.zc.web.core.PBRequestSession;
import com.zc.web.core.RequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorMessage;
import com.zc.web.message.PBMessage;
import com.zc.web.util.NettyUtil;

/**
 * 管理接口操作基类
 * @author Administrator
 *
 */
public abstract class BaseAdminAction extends BaseAction{
	protected static final String TAG_RESULT = "result";
	protected static final String TAG_DESC = "description";
	
//	protected int[] sysAdminCodes = new int[]{1000003, 1000006, 1000007, 1000008, 1000010, 1000011, 1000012, 1000013}; //系统的管理接口,不需要传入playerId
	
	@Override
	public void run(RequestSession reqSession) throws Exception {
//		if(isSystemAdminCode(reqSession.getActionCode())){
//			done(reqSession);
//		}else{
		
		if(reqSession.getPlayerId() == 0){
			done(reqSession);
		}else{
			doInit(reqSession);
			Player player = reqSession.getPlayer();
			synchronized (player) {
				done(reqSession);
			}
		}
		send2Client(reqSession);
	}
	
	protected abstract void done(String[] ps, Player player, Map<String, Object> result) throws Throwable;

	
	protected Player getPlayer(long playerId){
		Player player = PlayerCache.INSTANCE.getPlayer(playerId);
		return player;
	}
	
	@Override
	protected void doInit(RequestSession reqSession) throws SmallException {
		Player player = getPlayer(reqSession.getPlayerId());
		if(player == null){
			throw new SmallException("player not exists. playerId=" + reqSession.getPlayerId());
		}
		reqSession.setPlayer(player);
		//设置日志信息
		initLogData(reqSession);
	}
	
	@Override
	public void send2Client(RequestSession reqSession) {
		try{
			Map<String, Object> adminResult = reqSession.getAdminResult();
			String json = JSON.toJSONString(adminResult);
			NettyUtil.sendHttpResponse(reqSession.getChannel(), json);
		}finally{
			//记录流水日志
			logFlow(reqSession);
		}
	}

	@Override
	public void done(RequestSession reqSession) throws Exception {
		PBRequestSession session = (PBRequestSession)reqSession;
		Map<String, Object> result = new HashMap<String, Object>();
		PBMessage request = session.getRequest();
		String[] ps = null;
		if(request.getReq() != null){
			ps = request.getReq().split("[_]");
		}
		
		//具体操作
		try{
			result.put(TAG_RESULT, true);
			done(ps, reqSession.getPlayer(), result);
		}catch (Throwable e) {
			result.put(TAG_RESULT, false);
			result.put(TAG_DESC, e.getMessage());
		}
		
		//设置返回客户端数据
		reqSession.setAdminResult(result);
	}
	
	/**
	 * 是否为系统的管理口,此类管理接口不用传入playerId
	 * @param code
	 * @return
	 */
//	private boolean isSystemAdminCode(int code){
//		for(int actCode : sysAdminCodes){
//			if(code == actCode){
//				return true;
//			}
//		}
//		return false;
//	}
	
	@Override
	protected void sendErrorMsg(RequestSession reqSession, int errorCode, Object... args) {
		
	}

	@Override
	protected void sendErrorMsg(RequestSession reqSession, ErrorMessage errMsg) {
		
	}
}
