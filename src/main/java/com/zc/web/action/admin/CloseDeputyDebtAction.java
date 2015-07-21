package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.cache.PlayerCache;
import com.zc.web.core.Constant;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Player;
import com.zc.web.service.DebtService;
import com.zc.web.service.PlayerService;

public class CloseDeputyDebtAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Throwable{
		long id = Long.parseLong(ps[0]);
		
		// 修改状态
		Debt debt = DebtService.getDebtById(id);
		debt.setState(Constant.STATE_CLOSED);
		DebtService.saveDebt(debt);
		
		// 增加信用
		Player winner = PlayerCache.INSTANCE.getPlayer(debt.getWinnerId());
		PlayerService.addRating(winner, Constant.RATING_CLOSE_DEAL, "close deal: " + debt.getId());
		
		// 返款
		Integer money = winner.getFrozenMoney().get(debt.getId());
		int winnerMoney = debt.getMoney() * debt.getRate() / 100;
		int ownerMoney = debt.getMoney() - winnerMoney;
		if(money != null)
			winnerMoney += money;
		PlayerService.addMoney(winner, winnerMoney, Constant.MONEY_TYPE_CLOSE, Constant.MONEY_PLATFORM_DEFAULT);
		PlayerService.addMoney(PlayerCache.INSTANCE.getPlayer(debt.getOwnerId()), ownerMoney, Constant.MONEY_TYPE_CLOSE, Constant.MONEY_PLATFORM_DEFAULT);
	}
}
