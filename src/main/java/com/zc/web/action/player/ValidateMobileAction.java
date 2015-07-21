package com.zc.web.action.player;

import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.ValidateMobileReqProto.ValidateMobileReq;
import com.zc.web.service.PlayerService;

public class ValidateMobileAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		ValidateMobileReq.Builder builder = ValidateMobileReq.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		ValidateMobileReq req = builder.build();
		
		PlayerService.processValidateMobile(reqSession.getPlayer(), req.getCode());
	}

}
