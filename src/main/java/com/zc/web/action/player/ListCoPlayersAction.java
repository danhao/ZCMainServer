package com.zc.web.action.player;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.ListCoPlayersRspProto.ListCoPlayersRsp;
import com.zc.web.service.PlayerService;

public class ListCoPlayersAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		ListCoPlayersRsp.Builder rsp = ListCoPlayersRsp.newBuilder();
		for(Player p : PlayerService.listCoPlayers(Constant.USER_ROLE_DEPUTOR)){
			rsp.addPlayer(p.build());
		}
		
		response.setRsp(rsp.build());
	}

}
