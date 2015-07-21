package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.PayService;

public class ListCashAction extends BaseAdminAction {
	private static final int SIZE = 10;
	
	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) {
		
		int page = Integer.parseInt(ps[0]);
		long id = ps[1].isEmpty() ? 0 : Long.parseLong(ps[1]);
		long playerId = ps[2].isEmpty() ? 0 : Long.parseLong(ps[2]);
		
		result.put("data", PayService.listPlayerCash((page - 1) * SIZE, SIZE, id, playerId, ps[3]));
		result.put("count", PayService.getCashDao().getCount(id, playerId, ps[3]));
	}
}
