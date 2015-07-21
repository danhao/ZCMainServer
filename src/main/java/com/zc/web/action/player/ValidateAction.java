package com.zc.web.action.player;

import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.ValidateReqProto.ValidateReq;
import com.zc.web.service.PlayerService;

public class ValidateAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		ValidateReq.Builder builder = ValidateReq.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		ValidateReq req = builder.build();
		
		PlayerService.validatePlayer(req.getEmail(), req.getMobile());
	}

}
