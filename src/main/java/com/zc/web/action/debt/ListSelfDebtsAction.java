package com.zc.web.action.debt;

import java.util.List;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.ListDebtsReqProto.ListDebtsReq;
import com.zc.web.message.debt.ListDebtsRspProto.ListDebtsRsp;
import com.zc.web.message.debt.ListDebtsRspProto.ListDebtsRsp.SimpleDebtMsg;
import com.zc.web.service.DebtService;
import com.zc.web.util.PropUtil;

public class ListSelfDebtsAction extends PBBaseAction {

	private static final int TYPE_BID = 2;
	private static final int TYPE_WIN = 3;
	
	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		ListDebtsReq req = (ListDebtsReq)getReq(request, ListDebtsReq.newBuilder());

		List<Debt> list = null;
		switch(req.getQueryType()){
		case TYPE_BID:
			list = DebtService.getDebtDao().listDebts(DebtService.SIZE, (req.getPage() - 1) * DebtService.SIZE, "-publishTime", req.getState(), req.getType(), null, 0, 0, 0, 0, 0, 0, 0, 0, 0, reqSession.getPlayer().getBidDebts().keySet(), null);
			break;
		case TYPE_WIN:
			list = DebtService.getDebtDao().listDebts(DebtService.SIZE, (req.getPage() - 1) * DebtService.SIZE, "-publishTime", req.getState(), req.getType(), null, 0, 0, 0, 0, 0, 0, 0, 0, 0, reqSession.getPlayer().getWinDebts(), null, req.getDebtorName(), req.getDebtorId(), req.getProperty(), req.getHandFrom(), req.getHandTo(), req.getNewestMessage());
			break;
		}
		
		ListDebtsRsp.Builder rsp = ListDebtsRsp.newBuilder();
		for(Debt debt : list){
			SimpleDebtMsg.Builder simpleDebt = SimpleDebtMsg.newBuilder();
			PropUtil.copyProperties(simpleDebt, debt, SimpleDebtMsg.Builder.getDescriptor());
			simpleDebt.setBidCount(debt.getBondBidders().size());
			rsp.addDebt(simpleDebt.build());
		}
		
		response.setRsp(rsp.build());
	}

}
