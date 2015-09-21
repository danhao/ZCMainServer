package com.zc.web.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zc.web.action.Action;
import com.zc.web.action.ActionSet;
import com.zc.web.core.PBRequestSession;
import com.zc.web.core.RequestSession;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.PBMessage;
import com.zc.web.service.PlayerService;
import com.zc.web.util.MessageUtil;
import com.zc.web.util.NettyUtil;

/**
 * HTTP请求服务器
 * @author smaller
 *
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger log = Logger.getLogger(HttpServerHandler.class);
	private static final String MARKER = "req?";
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		DefaultFullHttpRequest req = (DefaultFullHttpRequest)msg;
		String uri = req.getUri();
		try {
			uri = URLDecoder.decode(uri, "utf-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		if ("/favicon.ico".equals(req.getUri())) {
	        return;
	    }
		
		String data = "";
		if(req.getMethod() == HttpMethod.POST){
			ByteBuf content = req.content();
			data = URLDecoder.decode(content.toString(CharsetUtil.UTF_8),"utf-8");
		}else if(req.getMethod() == HttpMethod.OPTIONS){
			NettyUtil.sendHttpResponse(ctx.channel(), "");
			return;
		}else if(req.getMethod() == HttpMethod.GET){
			int index = uri.lastIndexOf(MARKER);
			if(index != -1){
				index += MARKER.length();
			}else{
				index = 0;
			}
			String s = uri.substring(index);
			
			// validate by email TODO
			String[] tmp = s.split("_");
			if(tmp.length < 2)
				return;
			
			NettyUtil.sendHttpResponse(ctx.channel(), PlayerService.processValidateEmail(Long.parseLong(tmp[0]), tmp[1]));
			return;
		}else{
			log.error(req.getMethod() + " not supported!");
			return;
		}
		
		long playerId = 0;
		int actCode = 0;
		try{
			JSONObject json = JSON.parseObject(data);
			PBMessage request = new PBMessage(
					json.getIntValue("code"), 
					json.getString("pid"),
					json.getString("sid"),
					json.getString("req"));
		
			if(request.getPid() != null)
				playerId = Long.parseLong(request.getPid());
			
    		actCode = request.getCode();
    		
    		//停服维护中
    		if(MainServer.SERVER_STATUS == 2){
    			sendErrorMessage(ctx.channel(), playerId, actCode, ErrorCode.ERR_SERVER_STOPPING_VALUE);
    			return;
    		}
    		
    		log.info("recieve client request: playerId="+playerId + " actCode="+actCode);
    		//生成1个请求对象
    		RequestSession reqSession = new PBRequestSession(request, ctx.channel());
    		Action action = ActionSet.INSTANCE.getAction(actCode);
    		if(action == null){
    			log.info("action not found: code=" + reqSession.getActionCode() + ", playerId="+reqSession.getPlayerId());
    			sendErrorMessage(ctx.channel(), playerId, actCode, ErrorCode.ERR_UNKNOWN_VALUE);
    			return;
    		}
    		action.run(reqSession);
    		return;
		} catch (Exception e) {
//			if(!(e instanceof SmallException))
				log.error(e.getMessage(), e);
//			else
//				log.error("SmallException: " + ((SmallException)e).getError());
			
			//将错误信息返回客户端
			Channel channel = ctx.channel();
			if(e instanceof SmallException)
				sendErrorMessage(channel, playerId, actCode, ((SmallException)e).getError());
			else
				sendErrorMessage(channel, playerId, actCode, ErrorCode.ERR_SYSTEM_VALUE);
			return;
		}
		
	}
	
	
	private void sendErrorMessage(Channel channel, long playerId, int actCode, int errorCode){
		String jsonMsg = MessageUtil.buildErrorMsg(playerId, actCode, errorCode);
		NettyUtil.sendHttpResponse(channel, jsonMsg);
	}

}
