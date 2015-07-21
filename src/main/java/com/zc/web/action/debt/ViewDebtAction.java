package com.zc.web.action.debt;

import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.action.PBBaseAction;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Player;
import com.zc.web.exception.SmallException;
import com.zc.web.message.PBMessage;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.common.SingleMsgProto.SingleMsg;
import com.zc.web.service.DebtService;
import com.zc.web.service.PlayerService;

public class ViewDebtAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {

		// 验证权限
		PlayerService.isValidate(reqSession.getPlayer());
		
		SingleMsg.Builder builder = SingleMsg.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		SingleMsg req = builder.build();
		
		long id = Long.parseLong(req.getParam());
		Debt debt = DebtService.getDebtById(id);
		if(debt != null){
			if(reqSession.getPlayerId() != debt.getOwnerId() ||
					reqSession.getPlayerId() != debt.getWinnerId()){
				response.setRsp(debt.build(true));
			}else{
				response.setRsp(debt.build());
			}
		}
	}

}
