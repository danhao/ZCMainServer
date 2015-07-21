package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.server.MainServer;

/**
 * 停服操作
 * @author smaller
 *
 */
public class StopServerAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) {
		MainServer.SERVER_STATUS = 2;
		System.exit(0);
	}

}
