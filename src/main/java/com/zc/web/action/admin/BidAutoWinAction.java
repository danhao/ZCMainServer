package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.DebtService;

public class BidAutoWinAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Throwable{
		long debtId = Long.parseLong(ps[0]);
		long winnerId = Long.parseLong(ps[1]);
		
		DebtService.bidWin(0, debtId, winnerId);
	}
}
