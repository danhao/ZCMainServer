package com.zc.web.action.pay;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.zc.web.action.PBBaseAction;
import com.zc.web.config.GlobalConfig;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.PlayerOrder;
import com.zc.web.message.PBMessage;
import com.zc.web.message.pay.CreateOrderReqProto.CreateOrderReq;
import com.zc.web.message.pay.CreateOrderRspProto.CreateOrderRsp;
import com.zc.web.server.MainServer;
import com.zc.web.service.PayService;
import com.zc.web.util.MD5;

public class CreateOrderAction extends PBBaseAction {
	private static final Logger logger = Logger.getLogger(CreateOrderAction.class);
	
	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {

		CreateOrderReq req = (CreateOrderReq) getReq(request, CreateOrderReq.newBuilder());

		String version = req.getVersion();
		String merchantID = GlobalConfig.ALLINPAY.merchantId;
		String backgroundMerUrl = "http://" + MainServer.ZONE.hostOut + ":" + MainServer.ZONE.payPort;
		String tranCode = req.getTranCode();
		String tranAmt = req.getTranAmt();
		String feeAmt = req.getFeeAmt();
		String frontMerUrl = req.getFrontMerUrl();
		String tranDateTime = req.getTranDateTime();
		String gopayServerTime = getGopayServerTime();
		
		PlayerOrder order = PayService.createPlayerOrder(reqSession.getPlayer(), Integer.parseInt(tranAmt));
		String merOrderNum = String.valueOf(order.getId());

		// 构造订单请求对象，生成signMsg。
		String plain = "version=[" + version + "]tranCode=[" + tranCode + "]merchantID=[" + merchantID + "]merOrderNum=[" + merOrderNum + "]tranAmt=[" + tranAmt + "]feeAmt=[" + feeAmt+ "]tranDateTime=[" + tranDateTime + "]frontMerUrl=[" + frontMerUrl + "]backgroundMerUrl=[" + backgroundMerUrl + "]orderId=[]gopayOutOrderId=[]tranIP=[" + request.getClientIp() + "]respCode=[]gopayServerTime=[" + gopayServerTime + "]VerficationCode=[" + GlobalConfig.ALLINPAY.key + "]";
		logger.info(plain);
		String signValue = MD5.encode(plain);
		logger.info("signValue:" + signValue);
		
		CreateOrderRsp.Builder ret = CreateOrderRsp.newBuilder();
		ret.setBackgroundMerUrl(backgroundMerUrl);
		ret.setMerchantId(merchantID);
		ret.setServerTime(gopayServerTime);
		ret.setSignValue(signValue);
		ret.setMerRemark1(String.valueOf(reqSession.getPlayerId()));
		ret.setMerOrderNum(merOrderNum);
		ret.setTransIp(request.getClientIp());
		
		response.setRsp(ret.build());
	}

	/**
	 * 获取国付宝服务器时间 用于时间戳
	 * @return 格式YYYYMMDDHHMMSS
	 */
	private static String getGopayServerTime() {
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 10000); 
		GetMethod getMethod = new GetMethod("https://gateway.gopay.com.cn/time.do");
		getMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"GBK");  
		// 执行getMethod
		int statusCode = 0;
		try {
			statusCode = httpClient.executeMethod(getMethod);			
			if (statusCode == HttpStatus.SC_OK){
				String respString = (new String(getMethod.getResponseBody(),"GBK")).trim();
				return respString;
			}			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}
		return null;
	}
}
