package com.zc.web.action.debt;

import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.action.PBBaseAction;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.DebtMsgProto.DebtMsg;
import com.zc.web.service.DebtService;

public class CreateDebtAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {

		// 验证权限
//		if((reqSession.getPlayer().getStatus() & Constant.USER_MOBILE_VALIDATED) != Constant.USER_MOBILE_VALIDATED &&
//				(reqSession.getPlayer().getStatus() & Constant.USER_EMAIL_VALIDATED) != Constant.USER_EMAIL_VALIDATED){
//			throw new SmallException(ErrorCode.ERR_AUTHORIZED_FAILED);
//		}
		
		DebtMsg.Builder builder = DebtMsg.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		
		String from = reqSession.getChannel().remoteAddress().toString();
		
		Debt debt = DebtService.createDebt(builder.build(), reqSession.getPlayer(), from.indexOf("127.0.0.1") >= 0);
		if(debt != null)
			response.setRsp(debt.build());
	}

}
