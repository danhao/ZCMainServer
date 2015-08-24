package com.zc.web.action.pay;

import com.allinpay.ets.client.SecurityUtil;
import com.zc.web.action.PBBaseAction;
import com.zc.web.config.GlobalConfig;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.PlayerOrder;
import com.zc.web.message.PBMessage;
import com.zc.web.message.pay.CreateOrderReqProto.CreateOrderReq;
import com.zc.web.message.pay.CreateOrderRspProto.CreateOrderRsp;
import com.zc.web.server.MainServer;
import com.zc.web.service.PayService;

public class CreateOrderAction extends PBBaseAction {
	
	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {

		CreateOrderReq req = (CreateOrderReq) getReq(request, CreateOrderReq.newBuilder());

		String version = req.getVersion();
		String language = req.getLanguage();
		String inputCharset = req.getInputCharset();
		String merchantId = GlobalConfig.ALLINPAY.merchantId;
		String pickupUrl = req.getPickupUrl();
		String receiveUrl = "http://" + MainServer.ZONE.hostOut + ":" + MainServer.ZONE.payPort;
		String payType = req.getPayType();
		String signType = req.getSignType();
		String orderAmount = req.getOrderAmount();
		String orderCurrency = req.getOrderCurrency();
		String orderExpireDatetime = req.getOrderExpireDatetime();
		String payerTelephone = req.getPayerTelephone();
		String payerEmail = req.getPayerEmail();
		String payerName = req.getPayerName();
		String payerIDCard = req.getPayerIDCard();
		String pid = req.getPid();
		String productName = req.getProductName();
		String productId = req.getProductId();
		String productNum = req.getProductNum();
		String productPrice = req.getProductPrice();
		String productDesc = req.getProductDesc();
		String ext1 = String.valueOf(reqSession.getPlayerId());
		String ext2 = req.getExt2();
		String extTL = req.getExtTL();// 通联商户拓展业务字段，在v2.2.0版本之后才使用到的，用于开通分账等业务
		String issuerId = req.getIssuerId();
		String pan = req.getPan();
		String tradeNature = req.getTradeNature();

		PlayerOrder order = PayService.createPlayerOrder(reqSession.getPlayer(), Integer.parseInt(orderAmount));
		String orderNo = String.valueOf(order.getId());
		String orderDatetime = order.getCreateAt();

		// 若直连telpshx渠道，payerTelephone、payerName、payerIDCard、pan四个字段不可为空
		// 其中payerIDCard、pan需使用公钥加密（PKCS1格式）后进行Base64编码
		if (null != payerIDCard && !"".equals(payerIDCard)) {
			payerIDCard = SecurityUtil.encryptByPublicKey(
					Constant.CERT_PATH, payerIDCard);
		}
		if (null != pan && !"".equals(pan)) {
			pan = SecurityUtil.encryptByPublicKey(Constant.CERT_PATH,
						pan);
		}

		// 构造订单请求对象，生成signMsg。
		com.allinpay.ets.client.RequestOrder requestOrder = new com.allinpay.ets.client.RequestOrder();
		if (null != inputCharset && !"".equals(inputCharset)) {
			requestOrder.setInputCharset(Integer.parseInt(inputCharset));
		}
		requestOrder.setPickupUrl(pickupUrl);
		requestOrder.setReceiveUrl(receiveUrl);
		requestOrder.setVersion(version);
		if (null != language && !"".equals(language)) {
			requestOrder.setLanguage(Integer.parseInt(language));
		}
		requestOrder.setSignType(Integer.parseInt(signType));
		requestOrder.setPayType(Integer.parseInt(payType));
		requestOrder.setIssuerId(issuerId);
		requestOrder.setMerchantId(merchantId);
		requestOrder.setPayerName(payerName);
		requestOrder.setPayerEmail(payerEmail);
		requestOrder.setPayerTelephone(payerTelephone);
		requestOrder.setPayerIDCard(payerIDCard);
		requestOrder.setPid(pid);
		requestOrder.setOrderNo(orderNo);
		requestOrder.setOrderAmount(Long.parseLong(orderAmount));
		requestOrder.setOrderCurrency(orderCurrency);
		requestOrder.setOrderDatetime(orderDatetime);
		requestOrder.setOrderExpireDatetime(orderExpireDatetime);
		requestOrder.setProductName(productName);
		if (null != productPrice && !"".equals(productPrice)) {
			requestOrder.setProductPrice(Long.parseLong(productPrice));
		}
		if (null != productNum && !"".equals(productNum)) {
			requestOrder.setProductNum(Integer.parseInt(productNum));
		}
		requestOrder.setProductId(productId);
		requestOrder.setProductDesc(productDesc);
		requestOrder.setExt1(ext1);
		requestOrder.setExt2(ext2);
		requestOrder.setExtTL(extTL);// 通联商户拓展业务字段，在v2.2.0版本之后才使用到的，用于开通分账等业务
		requestOrder.setPan(pan);
		requestOrder.setTradeNature(tradeNature);
		requestOrder.setKey(GlobalConfig.ALLINPAY.key); // key为MD5密钥，密钥是在通联支付网关会员服务网站上设置。

		String strSignMsg = requestOrder.doSign(); // 签名，设为signMsg字段值。
		
		CreateOrderRsp.Builder ret = CreateOrderRsp.newBuilder();
		ret.setSignMsg(strSignMsg);
		ret.setOrderNo(orderNo);
		ret.setOrderDatetime(orderDatetime);
		ret.setMerchantId(merchantId);
		ret.setReceiveUrl(receiveUrl);		
		ret.setExt1(ext1);
		response.setRsp(ret.build());
	}

}
