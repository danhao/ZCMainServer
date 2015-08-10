package com.zc.web.action.debt;

import java.util.List;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.DebtEndApply;
import com.zc.web.data.model.Player;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.SingleMsgProto.SingleMsg;
import com.zc.web.service.ApplyService;
import com.zc.web.service.DebtService;

public class ApplyEndDebtAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		SingleMsg req = (SingleMsg)getReq(request, SingleMsg.newBuilder());
		
		Player player = reqSession.getPlayer();
		long debtId = Long.parseLong(req.getParam());
		
		Debt debt = DebtService.getDebtById(debtId);
//		if(debt == null || debt.getState() != Constant.STATE_DEALED)
//			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);

//		if(debt.getWinnerId() != player.getId()){
//			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
//		}
		
		if(debt.getRepayments().size() == 0)
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		

		ApplyService.createPlayerApply(player.getId(), debtId);
	}

}
