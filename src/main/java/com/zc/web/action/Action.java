package com.zc.web.action;

import com.zc.web.core.RequestSession;

public interface Action {
	
	public final int FLOW_GOLD = 1;//人民币
	public final int FLOW_MONEY = 2;//游戏币
	public final int FLOW_EXP = 3;//经验
	
	
	public void run(RequestSession reqSession) throws Exception;
	
	/**
	 * 发送数据给客户端
	 * @param reqSession
	 */
	public void send2Client(RequestSession reqSession);
}
