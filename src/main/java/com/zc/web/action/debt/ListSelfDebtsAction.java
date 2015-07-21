package com.zc.web.action.debt;

import java.util.List;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.SingleMsgProto.SingleMsg;
import com.zc.web.message.debt.ListDebtsRspProto.ListDebtsRsp;
import com.zc.web.message.debt.ListDebtsRspProto.ListDebtsRsp.SimpleDebtMsg;
import com.zc.web.service.DebtService;
import com.zc.web.util.PropUtil;

public class ListSelfDebtsAction extends PBBaseAction {

	private static final int TYPE_SELF = 1;
	private static final int TYPE_BID = 2;
	private static final int TYPE_WIN = 3;
	
	private static final int SIZE = 50;
	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		SingleMsg req = (SingleMsg)getReq(request, SingleMsg.newBuilder());

		List<Debt> list = null;
		switch(Integer.parseInt(req.getParam())){
//		case TYPE_SELF:
//			list = reqSession.getPlayer().getIssueDebts();
//			break;
		case TYPE_BID:
			list = DebtService.getDebtDao().listDebts(SIZE, 0, "-publishTime", -1, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, reqSession.getPlayer().getBidDebts(), null);
			break;
		case TYPE_WIN:
			list = DebtService.getDebtDao().listDebts(SIZE, 0, "-publishTime", -1, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, reqSession.getPlayer().getWinDebts(), null);
			break;
		}
		
		ListDebtsRsp.Builder rsp = ListDebtsRsp.newBuilder();
		for(Debt debt : list){
			SimpleDebtMsg.Builder simpleDebt = SimpleDebtMsg.newBuilder();
			PropUtil.copyProperties(simpleDebt, debt, SimpleDebtMsg.Builder.getDescriptor());
			rsp.addDebt(simpleDebt.build());
		}
		
		response.setRsp(rsp.build());
	}

}
