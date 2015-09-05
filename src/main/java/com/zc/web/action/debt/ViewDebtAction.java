package com.zc.web.action.debt;

import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.action.PBBaseAction;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.DebtEndApply;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.SingleMsgProto.SingleMsg;
import com.zc.web.service.ApplyService;
import com.zc.web.service.DebtService;
import com.zc.web.service.PlayerService;

public class ViewDebtAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {

		SingleMsg.Builder builder = SingleMsg.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		SingleMsg req = builder.build();
		
		long id = Long.parseLong(req.getParam());
		Debt debt = DebtService.getDebtById(id);
		if(debt != null){
			if(reqSession.getPlayerId() != debt.getOwnerId() &&
					reqSession.getPlayerId() != debt.getWinnerId()){
				// 验证权限
				PlayerService.isValidate(reqSession.getPlayer());

				response.setRsp(debt.build(true, false, 0));
			}else{
				if(reqSession.getPlayerId() == debt.getWinnerId()){
					int canEnd = 0;
					if(debt.getType() == Constant.TYPE_BID){
						canEnd = 1;
					}else if(debt.getRepayments().size() > 0){
						DebtEndApply apply = ApplyService.getDebtEndApply(id);
						if(apply == null || apply.getStatus() == 1)
							canEnd = 1;
					}
					response.setRsp(debt.build(false, true, canEnd));
				}else{
					response.setRsp(debt.build(false, false, 0));
				}
			}
		}
	}

}
