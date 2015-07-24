package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.core.Constant;
import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;
import com.zc.web.util.TimeUtil;

public class ValidateIdAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) {
		int state = Integer.parseInt(ps[0]);
		if(state == 0){
			player.setStatus(state);
		}else if (state < 0){
			player.setStatus(player.getStatus() & -state);
		}else{
			player.setStatus(player.getStatus() | state);
			player.setValidateTime(TimeUtil.now());
			
			if(state == Constant.USER_ID_VALIDATED)
				player.setIdValidating(0);
			else if(state == Constant.USER_CO_VALIDATED)
				player.setCoValidating(0);
		}
		
		PlayerService.savePlayer(player);
		
		if(player.getFileNoneCrime() != null && !player.getFileNoneCrime().getId().isEmpty())
			PlayerService.addRating(player, Constant.RATING_NONECRIME, "none crime rating");
		if(player.getFileCredit() != null && !player.getFileCredit().getId().isEmpty())
			PlayerService.addRating(player, Constant.RATING_CREDIT, "credit rating");
		
		result.put("data", player);
	}
}
