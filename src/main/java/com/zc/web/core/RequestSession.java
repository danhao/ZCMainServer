package com.zc.web.core;

import io.netty.channel.Channel;

import java.util.Map;

import lombok.Data;

import com.zc.web.data.model.Player;

/**
 * 请求对象
 * @author Administrator
 */
@Data
public class RequestSession {
	protected Channel channel = null;//连接
	protected Map<Integer, Long> logData = null; //Flow日志数据
	protected long recvTime; //请求接收时间
	protected Player player;
	protected String result = "succ"; //命令执行结果标示
	
	protected Map<String, Object> adminResult = null;//管理接口结果 
	
	/**
	 * 获取操作码
	 * @return
	 */
	public int getActionCode(){
		return 0;
	}
	
	/**
	 * 获取玩家id
	 * @return
	 */
	public long getPlayerId(){
		return 0;
	}
}
