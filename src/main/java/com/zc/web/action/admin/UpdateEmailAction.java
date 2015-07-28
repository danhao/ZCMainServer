package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;

public class UpdateEmailAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) {
		player.setEmail(ps[0]);
		PlayerService.savePlayer(player);
	}
}
