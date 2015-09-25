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

import com.zc.web.config.GlobalConfig;
import com.zc.web.service.PayService;
import com.zc.web.util.MD5;
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
			
			// 接收Server返回的支付结果
			String version=((Attribute)decoder.getBodyHttpData("version")).getValue();
			String tranCode=((Attribute)decoder.getBodyHttpData("tranCode")).getValue();
			String merOrderNum=((Attribute)decoder.getBodyHttpData("merOrderNum")).getValue();
			String merchantID=((Attribute)decoder.getBodyHttpData("merchantID")).getValue();
			String tranAmt=((Attribute)decoder.getBodyHttpData("tranAmt")).getValue();
			String feeAmt=((Attribute)decoder.getBodyHttpData("feeAmt")).getValue();
			String tranDateTime=((Attribute)decoder.getBodyHttpData("tranDateTime")).getValue();
			String frontMerUrl=((Attribute)decoder.getBodyHttpData("frontMerUrl")).getValue();
			String backgroundMerUrl=((Attribute)decoder.getBodyHttpData("backgroundMerUrl")).getValue();
			String orderId=((Attribute)decoder.getBodyHttpData("orderId")).getValue();
			String gopayOutOrderId=((Attribute)decoder.getBodyHttpData("gopayOutOrderId")).getValue();
			String respCode=((Attribute)decoder.getBodyHttpData("respCode")).getValue();
			String tranIP=((Attribute)decoder.getBodyHttpData("tranIP")).getValue();
			String merRemark1=((Attribute)decoder.getBodyHttpData("merRemark1")).getValue();
			String signValueFromGopay=((Attribute)decoder.getBodyHttpData("signValue")).getValue();
		
			String plain = "version=[" + version + "]tranCode=[" + tranCode
					+ "]merchantID=[" + merchantID + "]merOrderNum=["
					+ merOrderNum + "]tranAmt=[" + tranAmt + "]feeAmt=["
					+ feeAmt + "]tranDateTime=[" + tranDateTime
					+ "]frontMerUrl=[" + frontMerUrl + "]backgroundMerUrl=["
					+ backgroundMerUrl + "]orderId=[" + orderId
					+ "]gopayOutOrderId=[" + gopayOutOrderId + "]tranIP=["
					+ tranIP + "]respCode=[" + respCode
					+ "]gopayServerTime=[]VerficationCode=[" + GlobalConfig.ALLINPAY.key
					+ "]";
			log.info(plain);
			String signValue = MD5.encode(plain);
			log.info(signValue + ":" + signValueFromGopay);
			
			if(signValue.equals(signValueFromGopay)){
				// 商户订单处理
				int amt = Integer.parseInt(tranAmt) * 100;
				boolean ret = PayService.rechargePlayerOrder(Long.parseLong(merRemark1), Long.parseLong(merOrderNum), amt, merOrderNum);
				
				if(ret){
					log.info("succ|" + merRemark1 + "|" + merOrderNum + "|" + amt);
					NettyUtil.sendHttpResponse(ctx.channel(), "RespCode=0000|JumpURL=http://www.ddzhai.cn/#!/member");
					return;
				}else
					log.info("fail|" + merRemark1 + "|" + merOrderNum + "|" + amt);
			}

			NettyUtil.sendHttpResponse(ctx.channel(), "RespCode=9999|JumpURL=");
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

	// 通联的支付
//	@Override
//	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
//			throws Exception {
//		DefaultFullHttpRequest request = (DefaultFullHttpRequest)msg;
//		
//		String clientIP = request.headers().get("X-Forwarded-For");
//		if (clientIP == null) {
//			InetSocketAddress insocket = (InetSocketAddress) ctx.channel()
//					.remoteAddress();
//			clientIP = insocket.getAddress().getHostAddress();
//		}
//		
//		// TODO 限制IP
//		log.info("pay:" + clientIP);
//		
//		if ("/favicon.ico".equals(request.getUri())) {
//	        return;
//	    }
//		
//		try{
//			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
//			
//			// 接收Server返回的支付结果
//			String merchantId=((Attribute)decoder.getBodyHttpData("merchantId")).getValue();
//			String version=((Attribute)decoder.getBodyHttpData("version")).getValue();
//			String language=((Attribute)decoder.getBodyHttpData("language")).getValue();
//			String signType=((Attribute)decoder.getBodyHttpData("signType")).getValue();
//			String payType=((Attribute)decoder.getBodyHttpData("payType")).getValue();
//			String issuerId=((Attribute)decoder.getBodyHttpData("issuerId")).getValue();
//			String paymentOrderId=((Attribute)decoder.getBodyHttpData("paymentOrderId")).getValue();
//			String orderNo=((Attribute)decoder.getBodyHttpData("orderNo")).getValue();
//			String orderDatetime=((Attribute)decoder.getBodyHttpData("orderDatetime")).getValue();
//			String orderAmount=((Attribute)decoder.getBodyHttpData("orderAmount")).getValue();
//			String payDatetime=((Attribute)decoder.getBodyHttpData("payDatetime")).getValue();
//			String payAmount=((Attribute)decoder.getBodyHttpData("payAmount")).getValue();
//			String ext1=((Attribute)decoder.getBodyHttpData("ext1")).getValue();
//			String ext2=((Attribute)decoder.getBodyHttpData("ext2")).getValue();
//			String payResult=((Attribute)decoder.getBodyHttpData("payResult")).getValue();
//			String errorCode=((Attribute)decoder.getBodyHttpData("errorCode")).getValue();
//			String returnDatetime=((Attribute)decoder.getBodyHttpData("returnDatetime")).getValue();
//			String signMsg=((Attribute)decoder.getBodyHttpData("signMsg")).getValue();
//		
//			//验签是商户为了验证接收到的报文数据确实是支付网关发送的。
//			//构造订单结果对象，验证签名。
//			com.allinpay.ets.client.PaymentResult paymentResult = new com.allinpay.ets.client.PaymentResult();
//			paymentResult.setMerchantId(merchantId);
//			paymentResult.setVersion(version);
//			paymentResult.setLanguage(language);
//			paymentResult.setSignType(signType);
//			paymentResult.setPayType(payType);
//			paymentResult.setIssuerId(issuerId);
//			paymentResult.setPaymentOrderId(paymentOrderId);
//			paymentResult.setOrderNo(orderNo);
//			paymentResult.setOrderDatetime(orderDatetime);
//			paymentResult.setOrderAmount(orderAmount);
//			paymentResult.setPayDatetime(payDatetime);
//			paymentResult.setPayAmount(payAmount);
//			paymentResult.setExt1(ext1);
//			paymentResult.setExt2(ext2);
//			paymentResult.setPayResult(payResult);
//			paymentResult.setErrorCode(errorCode);
//			paymentResult.setReturnDatetime(returnDatetime);
//			//signMsg为服务器端返回的签名值。
//			paymentResult.setSignMsg(signMsg); 
//			//signType为"1"时，必须设置证书路径。
//			if(signType.equals("1"))
//				paymentResult.setCertPath(Constant.CERT_PATH); 
//			
//			//验证签名：返回true代表验签成功；否则验签失败。
//			boolean verifyResult = paymentResult.verify();
//			
//			//验签成功，还需要判断订单状态，为"1"表示支付成功。
//			boolean paySuccess = verifyResult && payResult.equals("1");
//
//			if(paySuccess){
//				// 商户订单处理
//				boolean ret = PayService.rechargePlayerOrder(Long.parseLong(ext1), Long.parseLong(orderNo), Integer.parseInt(orderAmount), orderNo);
//				
//				if(ret){
//					log.info("succ|" + ext1 + "|" + orderNo + "|" + orderAmount);
//					NettyUtil.sendHttpResponse(ctx.channel(), "商户订单处理完成");
//				}else
//					log.info("fail|" + ext1 + "|" + orderNo + "|" + orderAmount);
//			}
//
//			NettyUtil.sendHttpResponse(ctx.channel(), "Server接收处理完成");
//		}catch (Exception e) {
//			sendErrorMsg(ctx.channel(), e.getMessage());
//		}
//	}
}