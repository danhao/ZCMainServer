package com.zc.web.core;

import io.netty.channel.Channel;

import java.util.HashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.zc.web.message.PBMessage;

@EqualsAndHashCode(callSuper=false)
@Data
public class PBRequestSession extends RequestSession {
	private PBMessage request = null;//请求数据
	private PBMessage response = null; //响应数据
	
	public PBRequestSession(PBMessage request, Channel channel){
		this.request = request;
		this.channel = channel;
		
		//新建一个响应数据,并设置头部信息
		this.response = new PBMessage();
		this.response.setCode(request.getCode());
		this.response.setPid(request.getPid());
		
		this.recvTime = System.currentTimeMillis();
		
		this.logData = new HashMap<Integer, Long>();
	}
	
	/**
	 * 获取操作码
	 * @return
	 */
	public int getActionCode(){
		return this.request.getCode();
	}
	
	/**
	 * 获取玩家id
	 * @return
	 */
	public long getPlayerId(){
		return Long.parseLong(this.request.getPid());
	}
}
