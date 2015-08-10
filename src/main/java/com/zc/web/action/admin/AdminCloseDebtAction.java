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

public class AdminCloseDebtAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Throwable{
		long id = Long.parseLong(ps[0]);
		
		// 修改状态
		Debt debt = DebtService.getDebtById(id);
		
		// 返还保证金
		DebtService.bondReturn(debt, 0L);

		debt.setState(Constant.STATE_CLOSED);
		DebtService.saveDebt(debt);
		
		// 更新申请
		ApplyService.updateDebtEndApply(id, 1);
		
		Player owner = PlayerCache.INSTANCE.getPlayer(debt.getOwnerId());
		PlayerService.addSituation(owner, Constant.SITUATION_DEBT_CLOSE, String.valueOf(id));
		
		// 提醒
		String content = "您的债务（编号" + debt.getId() + "）已关闭，请登录<a href='http://www.ddzhai.cn'>点点债</a>确认！";
		SendSmsThread.inst.addSyncInfo(owner.getMobile(), content);
		SendMailThread.inst.addSyncInfo(owner.getEmail(), "关闭提醒", content);
	}
}
