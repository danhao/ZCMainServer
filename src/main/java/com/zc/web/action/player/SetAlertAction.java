package com.zc.web.action.player;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.AlertMsgProto.AlertMsg;

public class SetAlertAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		
		
		AlertMsg req = (AlertMsg)getReq(request, AlertMsg.newBuilder());
		
		
	}

}
