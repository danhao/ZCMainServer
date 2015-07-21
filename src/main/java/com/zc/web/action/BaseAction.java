package com.zc.web.action;


import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.zc.web.cache.PlayerCache;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.core.RequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ClientActionProto.ClientAction;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.ErrorMessage;
import com.zc.web.util.MessageUtil;
import com.zc.web.util.TimeUtil;

public abstract class BaseAction implements Action {

	private static final Logger logFlow = Logger.getLogger("FLOW");
//	private static final Logger logger = Logger.getLogger(BaseAction.class);
	
	private static List<Integer> IGNORE_CHECK_ACTIONS = new ArrayList<Integer>();
	static{
		IGNORE_CHECK_ACTIONS.add(ClientAction.ACTION_LOGIN_VALUE);
		IGNORE_CHECK_ACTIONS.add(ClientAction.ACTION_CREATE_USER_VALUE);
		IGNORE_CHECK_ACTIONS.add(ClientAction.ACTION_VALIDATE_VALUE);
		IGNORE_CHECK_ACTIONS.add(ClientAction.ACTION_LIST_VIEW_DEBTS_VALUE);
		IGNORE_CHECK_ACTIONS.add(ClientAction.ACTION_CHANGE_PWD_ONE_VALUE);
		IGNORE_CHECK_ACTIONS.add(ClientAction.ACTION_CHANGE_PWD_TWO_VALUE);
		IGNORE_CHECK_ACTIONS.add(ClientAction.ACTION_CHANGE_PWD_THREE_VALUE);
	}
	
	@Override
	public void run(RequestSession reqSession) throws Exception {
		//登录操作需要额外处理
		if(IGNORE_CHECK_ACTIONS.contains(reqSession.getActionCode())){
			done(reqSession);
			send2Client(reqSession);
		}else{
			//初始化操作
			doInit(reqSession);
			Player player = reqSession.getPlayer();
			
			//检查账号是否封号
			ErrorMessage errMsg = checkAccount(player);
			if(errMsg != null){
				sendErrorMsg(reqSession, errMsg);
				return;
			}
			
			//检查session过期
			if(player.getAccessTime() > 0 && TimeUtil.now() - player.getAccessTime() > Constant.SESSION_EXPIRED){
				// 超时
				player.setSid(null);
			}
			PBRequestSession session = (PBRequestSession)reqSession; 
			if(session.getRequest().getSid() == null || !session.getRequest().getSid().equals(player.getSid())){
				sendErrorMsg(reqSession, MessageUtil.buildErrorMsg(ErrorCode.ERR_PLATFORM_INVALID_VALUE));
				return;
			}			
			
			synchronized (player) {
				//所有提供的命令的都会调用
				access(reqSession);
				done(reqSession);
				//发送数据给客户端
				send2Client(reqSession);
			}
		}
	}

	/**
	 * 玩家访问(所有请求都会调用)
	 * @param reqSession
	 */
	protected void access(RequestSession reqSession) {
		Player player = reqSession.getPlayer();
		player.setAccessTime(TimeUtil.now());
	}

	/**
	 * 初始化操作
	 * @param reqSession
	 * @throws SmallException
	 * @throws NoSuchAlgorithmException 
	 * @throws Exception 
	 */
	protected void doInit(RequestSession reqSession) throws Exception{
		long playerId = reqSession.getPlayerId();
		if (playerId <= 0) {
			throw new SmallException("player id is error");
		}
		
		//非法请求
//		if(!verifySession(reqSession)){
//			throw new SmallException("illegal request");
//		}
		
		Player player = PlayerCache.INSTANCE.getPlayer(playerId);

		if (player == null) {
			throw new SmallException("player not exists. playerId=" + playerId);
		}

		reqSession.setPlayer(player);
		
		//设置日志信息
		initLogData(reqSession);
	}
	
	/**
	 * 返回数据给客户端
	 * @param reqSession
	 */
	public abstract void send2Client(RequestSession reqSession);
	
	/**
	 * 发送错误信息
	 * @param reqSession
	 * @param errMsg
	 */
	protected abstract void sendErrorMsg(RequestSession reqSession, int errorCode, Object... args);
	
	/**
	 * 发送错误信息给客户端
	 * @param reqSession
	 * @param errMsg
	 */
	protected abstract void sendErrorMsg(RequestSession reqSession, ErrorMessage errMsg);
	
	/**
	 * 初始化日志
	 * @param reqSession
	 */
	protected void initLogData(RequestSession reqSession) {
		Map<Integer, Long> logData = reqSession.getLogData();
		Player player = reqSession.getPlayer();
		logData.put(FLOW_MONEY, (long)player.getMoney());
	}
	/**
	 * 记录流水日志
	 * @param reqSession
	 */
	protected void logFlow(RequestSession reqSession){
		Map<Integer, Long> logData = reqSession.getLogData();
		if(logData != null && logData.size() > 0){
			StringBuilder logContent = new StringBuilder();
			Player player = reqSession.getPlayer();
			logContent.append(player.getId()).append("|");//玩家id
			logContent.append(reqSession.getActionCode()).append("|");//玩家操作码
			logContent.append(reqSession.getResult()).append("|");//操作执行结果
			logContent.append(System.currentTimeMillis() - reqSession.getRecvTime()).append("|");//命令执行时间
			logContent.append(player.getMoney() - logData.get(FLOW_MONEY)).append("|");//游戏币变化
			logContent.append(player.getMoney()).append("_");//当前游戏币数量
			logFlow.debug(logContent.toString());
		}
	}
	/**
	 * 子类实现(每个操作的具体逻辑)
	 * @param reqSession
	 */
	public abstract void done(RequestSession reqSession) throws Exception;
	
	/**
	 * 校验请求的合法性(合法则返回true)
	 * @param reqSession
	 * @return
	 * @throws Exception 
	 */
	protected boolean verifySession(RequestSession reqSession) throws Exception {
		return true;
	}
	
	/**
	 * 检查账号
	 * @param player
	 * @return
	 */
	public ErrorMessage checkAccount(Player player){
		int banTime = player.getBanAccountTime();
		if(banTime == 0){
			return null;
		}
		
		//封号检查
		if(banTime > TimeUtil.now()){
			return MessageUtil.buildErrorMsg(ErrorCode.ERR_ACCOUNT_FORBIDDEN_VALUE);
		}
		return null;
	}
	
	
}
