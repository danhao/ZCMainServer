package com.zc.web.action.player;

import java.util.Map.Entry;

import com.zc.web.action.PBBaseAction;
import com.zc.web.config.ConfigHelper;
import com.zc.web.config.model.ConfigVip;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.SingleMsgProto.SingleMsg;

public class DebtCountAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		Player player = reqSession.getPlayer();
		
		ConfigVip vip = ConfigHelper.getConfigVip(player.getVip());
		int limit = (vip == null ? 100 : vip.getBidLimit());
		int count = 0;
		for(Entry<Long, Boolean> entry : player.getBidDebts().entrySet()){
			if(!entry.getValue())
				count ++;
		}
		
		SingleMsg.Builder rsp = SingleMsg.newBuilder();
		rsp.setParam(String.valueOf(limit - count));
		
		response.setRsp(rsp.build());
	}

}
