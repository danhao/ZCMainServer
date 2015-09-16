package com.zc.web.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.zc.web.cache.PlayerCache;
import com.zc.web.core.model.CallAuthen;
import com.zc.web.core.model.CallEstablish;
import com.zc.web.core.model.CallHangup;
import com.zc.web.data.model.Player;
import com.zc.web.service.CallService;
import com.zc.web.util.NettyUtil;

/**
 * 语音电话验证请求服务器
 * @author smaller
 *
 */
public class CallServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger log = Logger.getLogger(CallServerHandler.class);
	
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
		
		if(req.getMethod() == HttpMethod.POST){
			ByteBuf content = req.content();
			String data = URLDecoder.decode(content.toString(CharsetUtil.UTF_8),"utf-8");
			log.info("data:" + data);
			if(data.trim().isEmpty()){
				NettyUtil.sendHttpResponse(ctx.channel(), "no data!");
				return;
			}
			Document doc = DocumentHelper.parseText(data);
			Element root = doc.getRootElement();
			String action = root.elementTextTrim("action");
			String body = "";
			if (action.equals("CallAuth")) {
				// 解析呼叫鉴权
				body = parseCallAuth(root);
			} else if (action.equals("CallEstablish")) {
				// 解析摘机请求
				body = parseCallEstablish(root);
			} else if (action.equals("Hangup")) {
				// 解析挂断请求
				body = parseHangup(root);
			} else if (action.equals("AccountLookup")) {
				// 解析挂断请求
				body = parseAccountLookup(root);
			} 

			NettyUtil.sendHttpResponse(ctx.channel(), body);
		}else{
			log.error(req.getMethod() + " not supported!");
			NettyUtil.sendHttpResponse(ctx.channel(), req.getMethod() + " not supported!");
			return;
		}
		
	}
	
	/**
	 * 解析呼叫鉴权
	 * @param e Element
	 * @return result
	 */
	private String parseCallAuth(Element e) {
		log
				.info("--- parseCallAuth   start ---");
		
		CallAuthen call = new CallAuthen();
		call.setType(e.elementTextTrim("type"));
		call.setOrderId(e.elementTextTrim("orderid"));
		call.setSubId(e.elementTextTrim("subid"));
		call.setCaller(e.elementTextTrim("caller"));
		call.setCalled(e.elementTextTrim("called"));
		call.setCallSid(e.elementTextTrim("callSid"));
		log.info(" --- parseCallAuth --- :"+call.toString());
		//请在此处增加逻辑判断代码
		
		//返回的数据,如果需要控制呼叫时长需要增加sessiontime
		String result = "<?xml version='1.0' encoding='UTF-8' ?><Response><statuscode>0000</statuscode><statusmsg>Success</statusmsg><record>1</record></Response>";
		
		return result;
	}
	/**
	 * 解析摘机请求
	 * @param e Element
	 * @return result
	 */
	private String parseCallEstablish(Element e) {
		log
				.info("--- parseCallEstablish   start   ");
				
		CallEstablish call = new CallEstablish();
		call.setType(e.elementTextTrim("type"));
		call.setOrderId(e.elementTextTrim("orderid"));
		call.setSubId(e.elementTextTrim("subid"));
		call.setCaller(e.elementTextTrim("caller"));
		call.setCalled(e.elementTextTrim("called"));
		call.setCallSid(e.elementTextTrim("callSid"));
		log.info(" --- CallEstablish --- : " + call.toString());
		//请在此处增加逻辑判断代码
		
		//返回的数据,如果需要控制呼叫时长需要增加sessiontime
		String result = "<?xml version='1.0' encoding='UTF-8' ?><Response><statuscode>0000</statuscode><statusmsg>Success</statusmsg><billdata>ok</billdata></Response>";
		
		
		log
				.info("--- parseCallEstablish   end ---");

		return result;
	}
	
	/**
	 * 解析挂断请求
	 * @param e Element
	 * @return result
	 */
	private String parseHangup(Element e) {
		log
				.info("---parseHangup   start---");
		// 封装 CallHangup
		CallHangup call = new CallHangup();
		call.setType(e.elementTextTrim("type"));
		call.setOrderId(e.elementTextTrim("orderid"));
		call.setSubId(e.elementTextTrim("subid"));
		call.setCaller(e.elementTextTrim("caller"));
		call.setCalled(e.elementTextTrim("called"));
		call.setByeType(e.elementTextTrim("byeType"));
		call.setStarttime(e.elementTextTrim("starttime"));
		call.setEndtime(e.elementTextTrim("endtime"));
		call.setBilldata(e.elementTextTrim("billdata"));
		call.setCallSid(e.elementTextTrim("callSid"));
		call.setRecordurl(e.elementTextTrim("recordurl"));
		
		log.info(" --- CallHangup --- : " + call.toString());
		//请在此处增加逻辑判断代码
		
		//返回的数据
		String result = "<?xml version='1.0' encoding='UTF-8'?><Response><statuscode>0000</statuscode><statusmsg>Success</statusmsg><totalfee>0.120000</totalfee></Response>";
		
		return result;
	}
	
	private String parseAccountLookup(Element e){
		String id = e.elementTextTrim("id");
		
		Player player = PlayerCache.INSTANCE.getPlayer(Long.parseLong(id));
		if(player.getVoipId() == null){
			CallService.createSubAccount(player);
		}
		
		if(player.getVoipId() != null){
			return "<?xml version='1.0' encoding='UTF-8'?><Response><dname>" + id + "</dname><voipid>" + player.getVoipId() + "</voipid><voippwd>" + player.getVoipPwd() + "</voippwd><hash>" + id + "</hash></Response>";
		}
		
		return "<?xml version='1.0' encoding='UTF-8'?><Response></Response>";
	}

}
