package com.zc.web.action.player;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.ChangePwdReqProto.ChangePwdReq;
import com.zc.web.service.PlayerService;

public class ChangePwdTwoAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		ChangePwdReq req = (ChangePwdReq)getReq(request, ChangePwdReq.newBuilder());
		
		PlayerService.changePwdTwo(req.getEmail(), req.getMobile(), req.getCode());
	}

}
