package com.zc.web.action.player;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;

/**
 * 获取用户信息，自动登录
 * 
 * @author dan
 *
 */
public class GetUserAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		response.setRsp(reqSession.getPlayer().build());
	}

}
