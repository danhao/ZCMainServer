package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Debt.Repayment;
import com.zc.web.data.model.Player;
import com.zc.web.service.DebtService;
import com.zc.web.util.TimeUtil;

public class AddRepaymentAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Throwable{
		long id = Long.parseLong(ps[0]);
		
		Debt debt = DebtService.getDebtById(id);
		
		Repayment pay = new Repayment();
		pay.setMoney(Integer.parseInt(ps[1]));
		if(ps.length > 2)
			pay.setMemo(ps[2]);
		pay.setTime(TimeUtil.now());
		
		debt.getRepayments().add(0, pay);
		DebtService.getDebtDao().addRepayment(id, pay);
		
		result.put("data", debt);
	}
}
