package com.zc.web.action.player;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.ChangePwdReqProto.ChangePwdReq;
import com.zc.web.service.PlayerService;

public class ChangePwdOneAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		ChangePwdReq req = (ChangePwdReq)getReq(request, ChangePwdReq.newBuilder());
		
		PlayerService.changePwdOne(req.getEmail(), req.getMobile());
	}

}
