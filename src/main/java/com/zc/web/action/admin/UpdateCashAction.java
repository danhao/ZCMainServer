package com.zc.web.action.admin;

import java.util.Date;
import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.data.model.PlayerCash;
import com.zc.web.service.PayService;
import com.zc.web.util.TimeUtil;

public class UpdateCashAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Throwable{
		long id = Long.parseLong(ps[0]);
		
		PlayerCash cash = PayService.getPlayerCash(id);
		cash.setStatus(ps[1]);
		if(ps.length > 2)
			cash.setDescription(ps[2]);
		cash.setFinishAt(TimeUtil.dateToString(new Date(), "yyyyMMddHHmmss"));
		PayService.savePlayerCash(cash);
		
		result.put("data", cash);
	}
}
