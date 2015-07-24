package com.zc.web.action.player;

import org.apache.commons.beanutils.PropertyUtils;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.data.model.Player.Alert;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.AlertMsgProto.AlertMsg;
import com.zc.web.service.PlayerService;

public class SetAlertAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		Player player = reqSession.getPlayer();
		if(player.getVip() <= 0){
			throw new SmallException(ErrorCode.ERR_AUTHORIZED_FAILED);
		}
		
		AlertMsg req = (AlertMsg)getReq(request, AlertMsg.newBuilder());
		if(player.getAlert() == null)
			player.setAlert(new Alert());
		PropertyUtils.copyProperties(player.getAlert(), req);
		
		PlayerService.savePlayer(player);
	}

}
