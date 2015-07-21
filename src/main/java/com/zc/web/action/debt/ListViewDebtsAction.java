package com.zc.web.action.debt;

import java.util.List;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.ListDebtsRspProto.ListDebtsRsp;
import com.zc.web.message.debt.ListDebtsRspProto.ListDebtsRsp.SimpleDebtMsg;
import com.zc.web.service.DebtService;
import com.zc.web.util.PropUtil;

public class ListViewDebtsAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		ListDebtsRsp.Builder rsp = ListDebtsRsp.newBuilder();
		List<Debt> list = DebtService.getLatestDebts();
		
		for(Debt debt : list){
			SimpleDebtMsg.Builder simpleDebt = SimpleDebtMsg.newBuilder();
			PropUtil.copyProperties(simpleDebt, debt, SimpleDebtMsg.Builder.getDescriptor());
			rsp.addDebt(simpleDebt.build());
		}
		
		response.setRsp(rsp.build());
	}

}
