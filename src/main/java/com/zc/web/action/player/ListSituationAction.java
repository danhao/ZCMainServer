package com.zc.web.action.player;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Player.Situation;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.ListSituationRspProto.ListSituationRsp;
import com.zc.web.message.player.ListSituationRspProto.ListSituationRsp.SituationMsg;
import com.zc.web.util.PropUtil;

public class ListSituationAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		ListSituationRsp.Builder rsp = ListSituationRsp.newBuilder();
		
		for(Situation situation : reqSession.getPlayer().getSituations()){
			SituationMsg.Builder mh = SituationMsg.newBuilder();
			PropUtil.copyProperties(mh, situation, SituationMsg.Builder.getDescriptor());
			rsp.addSituation(0, mh.build());
		}
		
		response.setRsp(rsp.build());

	}

}
