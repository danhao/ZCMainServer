package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.task.SendMailThread;
import com.zc.web.task.SendSmsThread;

/**
 *  发送提醒：1：email；2：短信；3：push
 * @author smaller
 *
 */
public class SendNotificationAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Exception{
		int type = Integer.parseInt(ps[0]);
		String title = ps[1];
		String content = ps[2];
		
		switch(type){
		case 1:
		case 2:
			SendSmsThread.inst.addSyncInfo(player.getMobile(), content);
			SendMailThread.inst.addSyncInfo(player.getEmail(), title, content);
			break;
		case 3:
			break;
		}
	}

}
