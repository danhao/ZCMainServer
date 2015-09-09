package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Debt.Repayment;
import com.zc.web.data.model.DebtRepay;
import com.zc.web.data.model.Player;
import com.zc.web.service.DebtService;
import com.zc.web.util.TimeUtil;

public class AddRepaymentAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Throwable{
		long id = Long.parseLong(ps[0]);
		
		Debt debt = DebtService.getDebtById(id);
		
		int money = Integer.parseInt(ps[1]);
		int time = TimeUtil.now();
		
		Repayment pay = new Repayment();
		pay.setMoney(money);
		if(ps.length > 2)
			pay.setMemo(ps[2]);
		pay.setTime(time);
		
		debt.getRepayments().add(0, pay);
		DebtService.getDebtDao().updateRepayments(id, debt.getRepayments());
		
		// 添加查询记录
		DebtRepay repay = new DebtRepay();
		repay.setDebtId(debt.getId());
		repay.setOwnerId(debt.getOwnerId());
		repay.setDeputyId(debt.getWinnerId());
		repay.setDebtorName(debt.getDebtorName());
		repay.setReceiveTime(debt.getReceiveTime());
		repay.setState(debt.getState());
		repay.setMoney(debt.getMoney());
		repay.setRepayMoney(money);
		repay.setTime(time);
		if(ps.length > 2)
			repay.setMemo(ps[2]);
		DebtService.getRepayDao().save(repay);
		
		result.put("data", debt);
	}
}
