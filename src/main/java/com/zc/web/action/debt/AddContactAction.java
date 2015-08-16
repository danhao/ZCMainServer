package com.zc.web.action.debt;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.ContactMsgProto.ContactMsg;
import com.zc.web.service.DebtService;

public class AddContactAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		ContactMsg req = (ContactMsg)getReq(request, ContactMsg.newBuilder());

		DebtService.addContact(reqSession.getPlayer(), req);
	}

}
