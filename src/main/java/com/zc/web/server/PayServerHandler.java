package com.zc.web.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.zc.web.core.Constant;
import com.zc.web.service.PayService;
import com.zc.web.util.NettyUtil;

/**
 *
 */
public class PayServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger log = Logger.getLogger(PayServerHandler.class);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		DefaultFullHttpRequest request = (DefaultFullHttpRequest)msg;
		
		String clientIP = request.headers().get("X-Forwarded-For");
		if (clientIP == null) {
			InetSocketAddress insocket = (InetSocketAddress) ctx.channel()
					.remoteAddress();
			clientIP = insocket.getAddress().getHostAddress();
		}
		
		// TODO 限制IP
		log.info("pay:" + clientIP);
		
		if ("/favicon.ico".equals(request.getUri())) {
	        return;
	    }
		
		try{
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
			
			//接收Server返回的支付结果
			String merchantId=((Attribute)decoder.getBodyHttpData("merchantId")).getValue();
			String version=((Attribute)decoder.getBodyHttpData("version")).getValue();
			String language=((Attribute)decoder.getBodyHttpData("language")).getValue();
			String signType=((Attribute)decoder.getBodyHttpData("signType")).getValue();
			String payType=((Attribute)decoder.getBodyHttpData("payType")).getValue();
			String issuerId=((Attribute)decoder.getBodyHttpData("issuerId")).getValue();
			String paymentOrderId=((Attribute)decoder.getBodyHttpData("paymentOrderId")).getValue();
			String orderNo=((Attribute)decoder.getBodyHttpData("orderNo")).getValue();
			String orderDatetime=((Attribute)decoder.getBodyHttpData("orderDatetime")).getValue();
			String orderAmount=((Attribute)decoder.getBodyHttpData("orderAmount")).getValue();
			String payDatetime=((Attribute)decoder.getBodyHttpData("payDatetime")).getValue();
			String payAmount=((Attribute)decoder.getBodyHttpData("payAmount")).getValue();
			String ext1=((Attribute)decoder.getBodyHttpData("ext1")).getValue();
			String ext2=((Attribute)decoder.getBodyHttpData("ext2")).getValue();
			String payResult=((Attribute)decoder.getBodyHttpData("payResult")).getValue();
			String errorCode=((Attribute)decoder.getBodyHttpData("errorCode")).getValue();
			String returnDatetime=((Attribute)decoder.getBodyHttpData("returnDatetime")).getValue();
			String signMsg=((Attribute)decoder.getBodyHttpData("signMsg")).getValue();
		
			//验签是商户为了验证接收到的报文数据确实是支付网关发送的。
			//构造订单结果对象，验证签名。
			com.allinpay.ets.client.PaymentResult paymentResult = new com.allinpay.ets.client.PaymentResult();
			paymentResult.setMerchantId(merchantId);
			paymentResult.setVersion(version);
			paymentResult.setLanguage(language);
			paymentResult.setSignType(signType);
			paymentResult.setPayType(payType);
			paymentResult.setIssuerId(issuerId);
			paymentResult.setPaymentOrderId(paymentOrderId);
			paymentResult.setOrderNo(orderNo);
			paymentResult.setOrderDatetime(orderDatetime);
			paymentResult.setOrderAmount(orderAmount);
			paymentResult.setPayDatetime(payDatetime);
			paymentResult.setPayAmount(payAmount);
			paymentResult.setExt1(ext1);
			paymentResult.setExt2(ext2);
			paymentResult.setPayResult(payResult);
			paymentResult.setErrorCode(errorCode);
			paymentResult.setReturnDatetime(returnDatetime);
			//signMsg为服务器端返回的签名值。
			paymentResult.setSignMsg(signMsg); 
			//signType为"1"时，必须设置证书路径。
			if(signType.equals("1"))
				paymentResult.setCertPath(Constant.CERT_PATH); 
			
			//验证签名：返回true代表验签成功；否则验签失败。
			boolean verifyResult = paymentResult.verify();
			
			//验签成功，还需要判断订单状态，为"1"表示支付成功。
			boolean paySuccess = verifyResult && payResult.equals("1");

			if(paySuccess){
				// 商户订单处理
				boolean ret = PayService.rechargePlayerOrder(Long.parseLong(ext1), Long.parseLong(orderNo), Integer.parseInt(orderAmount), orderNo);
				
				if(ret){
					log.info("succ|" + ext1 + "|" + orderNo + "|" + orderAmount);
					NettyUtil.sendHttpResponse(ctx.channel(), "商户订单处理完成");
				}else
					log.info("fail|" + ext1 + "|" + orderNo + "|" + orderAmount);
			}

			NettyUtil.sendHttpResponse(ctx.channel(), "Server接收处理完成");
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