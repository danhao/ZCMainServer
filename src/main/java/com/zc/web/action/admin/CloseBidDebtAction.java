package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.cache.PlayerCache;
import com.zc.web.core.Constant;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Player;
import com.zc.web.service.ApplyService;
import com.zc.web.service.DebtService;
import com.zc.web.service.PlayerService;
import com.zc.web.task.SendMailThread;
import com.zc.web.task.SendSmsThread;

public class CloseBidDebtAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Throwable{
		long id = Long.parseLong(ps[0]);
		
		// 修改状态
		Debt debt = DebtService.getDebtById(id);
		
		if(!(debt.getState() == Constant.STATE_DEALED && debt.getType() == Constant.TYPE_BID)){
			throw new Exception();
		}
		
//		debt.setState(Constant.STATE_CLOSED);
		DebtService.updateState(debt, Constant.STATE_CLOSED);
		DebtService.saveDebt(debt);
		
		// 更新申请
		ApplyService.updateDebtEndApply(id, 1);
		
		// 增加信用
		Player winner = PlayerCache.INSTANCE.getPlayer(debt.getWinnerId());
		PlayerService.addRating(winner, Constant.RATING_CLOSE_DEAL, "close deal: " + debt.getId());
		
		// 返款
		Integer bondMoney = winner.getFrozenMoney().get(debt.getId());
		winner.getFrozenMoney().remove(debt.getId());
		PlayerService.addMoney(winner, bondMoney, Constant.MONEY_TYPE_BOND_RETURN, Constant.MONEY_PLATFORM_DEFAULT, debt.getId());
		PlayerService.addSituation(winner, Constant.SITUATION_BOND_RETURN, String.valueOf(id), String.valueOf(bondMoney));

		// 提醒
		String content = "恭喜完成债务（编号" + debt.getId() + "），保证金" + (bondMoney / 100f) + "元已退回，请登录<a href='http://www.ddzhai.cn'>点点债</a>确认！";
		SendSmsThread.inst.addSyncInfo(winner.getMobile(), content);
		SendMailThread.inst.addSyncInfo(winner.getEmail(), "结单提醒", content);

		// 批量更新统计
		PlayerService.updateDeputyPathByDebt(debt);
	}
}
