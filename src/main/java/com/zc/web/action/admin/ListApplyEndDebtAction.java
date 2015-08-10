package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.ApplyService;

public class ListApplyEndDebtAction extends BaseAdminAction {
	private static final int SIZE = 10;
	
	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) {
		
		int page = Integer.parseInt(ps[0]);
		
		result.put("data", ApplyService.listDebtEndApply((page - 1) * SIZE, SIZE, 0, 0));
		result.put("count", ApplyService.getDao().getCount(0, 0));
	}
}
