package com.zc.web.action.player;

import org.apache.commons.beanutils.PropertyUtils;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.File;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.FileMsgProto.FileMsg;
import com.zc.web.service.PlayerService;
import com.zc.web.util.TimeUtil;

public class UploadAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		FileMsg req = (FileMsg)getReq(request, FileMsg.newBuilder());

		File file = new File();
		PropertyUtils.copyProperties(file, req);
		file.setCreateTime(TimeUtil.now());
		reqSession.getPlayer().getFiles().add(file);
		
		PlayerService.savePlayer(reqSession.getPlayer());
		response.setRsp(reqSession.getPlayer().build());
	}

}
