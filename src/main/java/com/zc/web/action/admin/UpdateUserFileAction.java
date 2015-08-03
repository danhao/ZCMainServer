package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.core.Constant;
import com.zc.web.data.model.File;
import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;

public class UpdateUserFileAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) {
		for(String s : ps){
			if(!s.isEmpty()){
				for(File file : player.getFiles()){
					if(file.getId().equals(s)){
						file.setState(Constant.FILE_STATE_DONE);
					}
				}
			}
		}
		
		PlayerService.savePlayer(player);
		result.put("data", player);
	}
}
