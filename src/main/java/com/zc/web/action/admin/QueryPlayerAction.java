package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;

public class QueryPlayerAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player p, Map<String, Object> result) {
		Player player = PlayerService.getPlayer(ps[0]);
		result.put("data", player);
	}
}
