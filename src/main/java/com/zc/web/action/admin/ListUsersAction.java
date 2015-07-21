package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;

public class ListUsersAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result)
			throws Throwable {
		
		long id = ps[0].isEmpty() ? 0L : Long.parseLong(ps[0]);
		int type = Integer.parseInt(ps[1]);
		
		result.put("data", PlayerService.listPlayerForValidating(id, type));
	}

}
