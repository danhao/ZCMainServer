package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.cache.PlayerCache;
import com.zc.web.core.Constant;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Debt.Repayment;
import com.zc.web.data.model.Player;
import com.zc.web.service.DebtService;
import com.zc.web.service.PlayerService;
import com.zc.web.task.SendMailThread;
import com.zc.web.task.SendSmsThread;

public class CloseDeputyDebtAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Throwable{
		long id = Long.parseLong(ps[0]);
		
		// 修改状态
		Debt debt = DebtService.getDebtById(id);
		
		if(!(debt.getState() == Constant.STATE_DEALED && debt.getType() == Constant.TYPE_DEPUTY)){
			throw new Exception();
		}
		
		debt.setState(Constant.STATE_CLOSED);
		DebtService.saveDebt(debt);
		
		// 增加信用
		Player winner = PlayerCache.INSTANCE.getPlayer(debt.getWinnerId());
		PlayerService.addRating(winner, Constant.RATING_CLOSE_DEAL, "close deal: " + debt.getId());
		
		// 收到的回款
		int money = 0;
		for(Repayment pay : debt.getRepayments()){
			money += pay.getMoney();
		}
		
		// 返款
		Integer bondMoney = winner.getFrozenMoney().get(debt.getId());
		int winnerMoney = money * debt.getRate() / 100;
		int serviceFee = winnerMoney * Constant.SERVICE_FEE / 100;
		winnerMoney -= serviceFee;
		int ownerMoney = money - winnerMoney;
		if(bondMoney == null)
			bondMoney = 0;
		PlayerService.addMoney(winner, winnerMoney + bondMoney, Constant.MONEY_TYPE_CLOSE, Constant.MONEY_PLATFORM_DEFAULT);
		PlayerService.addSituation(winner, Constant.SITUATION_DEBT_END, String.valueOf(id), String.valueOf(winnerMoney), String.valueOf(serviceFee));
		PlayerService.addSituation(winner, Constant.SITUATION_BOND_RETURN, String.valueOf(id), String.valueOf(bondMoney));

		Player owner = PlayerCache.INSTANCE.getPlayer(debt.getOwnerId());
		PlayerService.addMoney(owner, ownerMoney, Constant.MONEY_TYPE_CLOSE, Constant.MONEY_PLATFORM_DEFAULT);
		PlayerService.addSituation(winner, Constant.SITUATION_DEBT_END, String.valueOf(id), String.valueOf(ownerMoney));
		
		// 提醒
		String content = "恭喜完成债务（编号" + debt.getId() + "），佣金（扣除平台服务费）" + (winnerMoney / 100f) + "元已到账，请登录<a href='http://www.ddzhai.cn'>点点债</a>确认！";
		SendSmsThread.inst.addSyncInfo(winner.getMobile(), content);
		SendMailThread.inst.addSyncInfo(winner.getEmail(), "结单提醒", content);
		
		content = "您的债务（编号" + debt.getId() + "）已成功结束，回款（扣除佣金）" + (ownerMoney / 100f) + "元已到账，请登录<a href='http://www.ddzhai.cn'>点点债</a>确认！";
		SendSmsThread.inst.addSyncInfo(owner.getMobile(), content);
		SendMailThread.inst.addSyncInfo(owner.getEmail(), "结单提醒", content);
		

	}
}
