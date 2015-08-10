package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.DebtService;

public class GetDebtAction extends BaseAdminAction {
	
	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Exception{
		result.put("data", DebtService.getDebtById(Long.parseLong(ps[0])));
	}
}
