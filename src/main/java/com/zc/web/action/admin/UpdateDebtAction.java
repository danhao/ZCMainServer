package com.zc.web.action.admin;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Player;
import com.zc.web.service.DebtService;
import com.zc.web.util.TimeUtil;

public class UpdateDebtAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Throwable{
		long id = Long.parseLong(ps[0]);
		
		Debt debt = DebtService.getDebtById(id);
		for(int i = 1; i < ps.length; i ++){
			String name = ps[i];
			String value = ps[++i];

			BeanUtils.setProperty(debt, name, value);
			
			if(name.equals("state") && value.equals("1"))
				debt.setPublishTime(TimeUtil.now());
		}
		
		
		DebtService.saveDebt(debt);
		
		result.put("data", debt);
	}
}
