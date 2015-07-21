package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;

public class AddRatingAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result)
			throws Throwable {
		PlayerService.addRating(player, Integer.parseInt(ps[0]), "admin");
	}

}
