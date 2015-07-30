package com.zc.web.action.player;

import com.zc.web.action.PBBaseAction;
import com.zc.web.cache.PlayerCache;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.SingleMsgProto.SingleMsg;
import com.zc.web.message.player.SimplePlayerMsgProto.SimplePlayerMsg;
import com.zc.web.service.PlayerService;
import com.zc.web.util.FileUtil;
import com.zc.web.util.PropUtil;

/**
 * 查看其他人
 * 
 * @author dan
 *
 */
public class GetOtherAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		SingleMsg req = (SingleMsg)getReq(request, SingleMsg.newBuilder());

		Player player = PlayerCache.INSTANCE.getPlayer(Long.parseLong(req.getParam()));
		
		SimplePlayerMsg.Builder sp = SimplePlayerMsg.newBuilder();
		PropUtil.copyProperties(sp, player, SimplePlayerMsg.Builder.getDescriptor());
		if(player.getHead() != null && !player.getHead().isEmpty())
			sp.setHead(FileUtil.genDownloadUrl(player.getHead()));
		
		response.setRsp(sp.build());
	}

}
