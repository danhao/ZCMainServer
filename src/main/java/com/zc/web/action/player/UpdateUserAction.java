package com.zc.web.action.player;

import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.UpdateReqProto.UpdateReq;
import com.zc.web.service.PlayerService;

public class UpdateUserAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		UpdateReq.Builder builder = UpdateReq.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		
		PlayerService.updatePlayer(reqSession.getPlayer(), builder.build());
	}

}
