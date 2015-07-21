package com.zc.web.action.pay;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.message.PBMessage;
import com.zc.web.message.pay.PlayerCashMsgProto.PlayerCashMsg;
import com.zc.web.service.PayService;
import com.zc.web.service.PlayerService;

public class DrawCashAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		PlayerCashMsg req = (PlayerCashMsg)getReq(request, PlayerCashMsg.newBuilder());
		
		Player player = reqSession.getPlayer();
		
		PlayerService.consumeMoney(player, req.getAmount(), Constant.MONEY_TYPE_REDRAW, Constant.MONEY_PLATFORM_DEFAULT);
		
		PayService.createPlayerCash(player, req);
	}

}
