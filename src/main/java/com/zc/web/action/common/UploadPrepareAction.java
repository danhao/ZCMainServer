package com.zc.web.action.common;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.UploadMsgProto.UploadMsg;

/**
 * 上传准备
 * 
 * @author dan
 *
 */
public class UploadPrepareAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		UploadMsg.Builder msg = UploadMsg.newBuilder();
		msg.setOssId(Constant.OSSAccessKeyId);
		msg.setPolicy(Constant.POLICY);
		msg.setSignature(Constant.SIGNATURE);
		msg.setUrl("http://" + Constant.BUCKET + "." + Constant.ENDPOINT);
		
		response.setRsp(msg.build());
	}

}
