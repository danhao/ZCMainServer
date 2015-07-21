package com.zc.web.action.player;

import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.PlayerMsgProto.PlayerMsg;
import com.zc.web.service.PlayerService;

public class CreateUserAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		PlayerMsg.Builder builder = PlayerMsg.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		
		response.setRsp(PlayerService.createPlayer(builder.build()).build());
	}

}
