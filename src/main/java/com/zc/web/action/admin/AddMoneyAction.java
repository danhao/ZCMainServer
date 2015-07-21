package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;

/**
 * 增加游戏币
 * @author smaller
 *
 */
public class AddMoneyAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Exception{
		int addMoney = Integer.valueOf(ps[0]);
		int type = 0;
		int platform = 0;
		
		if(ps.length > 1)
			type = Integer.parseInt(ps[1]);
		if(ps.length > 2)
			platform = Integer.parseInt(ps[2]);
		
		if(addMoney > 0)
			PlayerService.addMoney(player, addMoney, type, platform, "admin");
		else
			PlayerService.consumeMoney(player, addMoney, type, platform, "admin");
	}

}
