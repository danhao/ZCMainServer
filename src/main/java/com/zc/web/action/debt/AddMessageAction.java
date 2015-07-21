package com.zc.web.action.debt;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.MessageMsgProto.MessageMsg;
import com.zc.web.service.DebtService;
import com.zc.web.util.JsonFormat;

public class AddMessageAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		MessageMsg.Builder builder = MessageMsg.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		MessageMsg req = builder.build();

		DebtService.addBidMessage(reqSession.getPlayer(), req);
	}

}
