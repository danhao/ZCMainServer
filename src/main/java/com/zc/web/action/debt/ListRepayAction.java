package com.zc.web.action.debt;

import java.util.List;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.DebtRepay;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.ListRepayReqProto.ListRepayReq;
import com.zc.web.message.debt.ListRepayRspProto.ListRepayRsp;
import com.zc.web.message.debt.ListRepayRspProto.ListRepayRsp.RepayMsg;
import com.zc.web.service.DebtService;
import com.zc.web.util.PropUtil;

/**
 * 还款查询
 * 
 * @author dan
 *
 */
public class ListRepayAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		ListRepayReq req = (ListRepayReq)getReq(request, ListRepayReq.newBuilder());

		List<DebtRepay> list = DebtService.listRepay((req.getPage() - 1) * DebtService.SIZE, DebtService.SIZE, 
				req.getDebtId(), req.getOwnerId(), req.getDeputyId(), 
				req.getTimeFrom(), req.getTimeTo());
		
		ListRepayRsp.Builder rsp = ListRepayRsp.newBuilder();
		for(DebtRepay repay : list){
			RepayMsg.Builder debtRepay = RepayMsg.newBuilder();
			PropUtil.copyProperties(debtRepay, repay, RepayMsg.Builder.getDescriptor());
			rsp.addRepay(debtRepay.build());
		}
		
		response.setRsp(rsp.build());

	}

}
