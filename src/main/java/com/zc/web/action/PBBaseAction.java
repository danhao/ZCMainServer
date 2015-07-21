package com.zc.web.action;

import io.netty.channel.Channel;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessage.Builder;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.core.PBRequestSession;
import com.zc.web.core.RequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.message.ErrorMessage;
import com.zc.web.message.PBMessage;
import com.zc.web.util.MessageUtil;
import com.zc.web.util.NettyUtil;
import com.zc.web.util.TimeUtil;

public abstract class PBBaseAction extends BaseAction {
	private static final Logger logger = Logger.getLogger(PBBaseAction.class);
	/**
	 * 返回数据给客户端
	 * @param reqSession
	 */
	public void send2Client(RequestSession reqSession){
		try{
			PBRequestSession session = (PBRequestSession)reqSession;
			Channel channel = session.getChannel();
			
			NettyUtil.sendHttpResponse(channel, session.getResponse().buildRsp());
		} finally{
			//记录流水日志
			logFlow(reqSession);
		}
	}
	
	/**
	 * 发送错误信息给客户端
	 * @param reqSession
	 * @param errMsg
	 */
	protected void sendErrorMsg(RequestSession reqSession, ErrorMessage errMsg){
		PBMessage response = ((PBRequestSession)reqSession).getResponse();
		response.setError(errMsg.getErrorCode());
		reqSession.setResult("fail");
		NettyUtil.sendHttpResponse(reqSession.getChannel(), response.buildRsp());
	}
	
	/**
	 * 发送错误信息
	 * @param reqSession
	 * @param errMsg
	 */
	protected void sendErrorMsg(RequestSession reqSession, int errorCode, Object... args){
		ErrorMessage errMsg = MessageUtil.buildErrorMsg(errorCode, args);
		sendErrorMsg(reqSession, errMsg);
	}
	
	@Override
	public void done(RequestSession reqSession) throws Exception {
		PBRequestSession session = (PBRequestSession)reqSession; 
		done(session, session.getRequest(), session.getResponse());
	}
	
	protected void access(RequestSession reqSession) {
		Player player = reqSession.getPlayer();
		player.setAccessTime(TimeUtil.now());
	}
	public abstract void done(PBRequestSession reqSession, PBMessage request, PBMessage response) throws Exception;
	
	protected Message getReq(PBMessage request, Builder builder) throws Exception{
		JsonFormat.merge(request.getReq(), builder);
		return builder.build();
	}
}
