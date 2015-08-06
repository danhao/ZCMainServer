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

import com.zc.web.action.Action;
import com.zc.web.action.ActionSet;
import com.zc.web.core.PBRequestSession;
import com.zc.web.core.RequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.util.NettyUtil;

/**
 * 例如:命令码|playerId|参数
 * 1、/?cmd=1|1|100
 * 2、/1|1|100
 * @author Administrator
 *
 */
public class AdminServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger log = Logger.getLogger(AdminServerHandler.class);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		DefaultFullHttpRequest request = (DefaultFullHttpRequest)msg;
		String uri = request.getUri();
		try {
			uri = URLDecoder.decode(uri, "utf-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		if ("/favicon.ico".equals(request.getUri())) {
	        return;
	    }
		
		
		String marker = "cmd=";
		if(request.getMethod() == HttpMethod.POST){
			ByteBuf content = request.content();
			uri = URLDecoder.decode(content.toString(CharsetUtil.UTF_8),"utf-8");
		}
		
		try{
			int index = uri.lastIndexOf(marker);
			if(index != -1){
				index += marker.length();
			}else{
				index = 0;
			}
			
			uri = uri.substring(index);
			if(uri.startsWith("/")){
				uri = uri.substring(1);
			}
			if(uri.startsWith("?")){
				uri = uri.substring(1);
			}
			
			String[] array = uri.split("[|]");
			if(array.length >= 1){
				int code = Integer.valueOf(array[0]);
				
				PBMessage reqData = new PBMessage();
				reqData.setCode(code);
				
				if(array.length >= 2){
					long playerId = Long.valueOf(array[1]);
					reqData.setPid(String.valueOf(playerId));
				}
				
				if(array.length >= 3){
					reqData.setReq(array[2]);
				}
				//生成1个请求对象
	    		RequestSession reqSession = new PBRequestSession(reqData, ctx.channel());
	    		Action action = ActionSet.INSTANCE.getAction(code);
	    		if(action == null){
	    			log.info("admin action not found: code=" + reqSession.getActionCode() + ", playerId="+reqSession.getPlayerId());
	    			sendErrorMsg(ctx.channel(), "管理员命令错误。");
	    			return;
	    		}else{
	    			log.info(uri);
	    		}
	    		action.run(reqSession);
	    		return;
			}else{
				sendErrorMsg(ctx.channel(), "参数格式错误。");
			}
		}catch (Exception e) {
			sendErrorMsg(ctx.channel(), e.getMessage());
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ctx.channel().close();
	}
	
	
	/**
	 * 发送错误信息
	 * @param contentType
	 * @param channel
	 * @param errorMsg
	 */
	private void sendErrorMsg(Channel channel, String errorMsg){
		NettyUtil.sendHttpResponse(channel, errorMsg);
	}

}