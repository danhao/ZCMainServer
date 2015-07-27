package com.zc.web.action.player;

import com.zc.web.action.PBBaseAction;
import com.zc.web.config.ConfigHelper;
import com.zc.web.config.model.ConfigVip;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.SingleMsgProto.SingleMsg;
import com.zc.web.service.PlayerService;

public class BuyVipAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		SingleMsg req = (SingleMsg)getReq(request, SingleMsg.newBuilder());
		
		ConfigVip config = ConfigHelper.getConfigVip(Integer.parseInt(req.getParam()));
		if(config == null){
			throw new SmallException(ErrorCode.ERR_SYSTEM);
		}
		
		Player player = reqSession.getPlayer();
		int money = 0;
		if((player.getStatus() | Constant.USER_ID_VALIDATED) == Constant.USER_ID_VALIDATED)
			money = config.getCost();
		else
			money = config.getCostCo();
		PlayerService.consumeMoney(player, money, Constant.MONEY_TYPE_BUY_VIP, Constant.MONEY_PLATFORM_DEFAULT);
		
		player.setVip(config.getLevel());
		PlayerService.savePlayer(player);
	}

}
