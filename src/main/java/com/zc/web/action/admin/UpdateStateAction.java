package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;

/**
 * 更新状态
 * @author smaller
 *
 */
public class UpdateStateAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Exception{
		PlayerService.updateCreditorPath(player.getId(), Integer.parseInt(ps[0]), Integer.parseInt(ps[1]));
	}

}
