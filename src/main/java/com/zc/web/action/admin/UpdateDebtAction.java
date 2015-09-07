package com.zc.web.action.admin;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.core.Constant;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Player;
import com.zc.web.service.DebtService;
import com.zc.web.service.PlayerService;
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
			
			if(name.equals("state")){
				PlayerService.updateCreditorPath(debt.getOwnerId(), debt.getState(), Integer.parseInt(value));
				
				if(value.equals(String.valueOf(Constant.STATE_PUBLISH))){
					debt.setPublishTime(TimeUtil.now());
					DebtService.updateLatest(debt);
				}
			}
		}
		
		
		DebtService.saveDebt(debt);
		
		result.put("data", debt);
	}
}
