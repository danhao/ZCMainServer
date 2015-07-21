package com.zc.web.action.debt;

import java.util.List;

import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.ListDebtsReqProto.ListDebtsReq;
import com.zc.web.message.debt.ListDebtsRspProto.ListDebtsRsp;
import com.zc.web.message.debt.ListDebtsRspProto.ListDebtsRsp.SimpleDebtMsg;
import com.zc.web.service.DebtService;
import com.zc.web.util.PropUtil;

public class ListDebtsAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		ListDebtsReq.Builder builder = ListDebtsReq.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		ListDebtsReq req = builder.build();	
		
		ListDebtsRsp.Builder rsp = ListDebtsRsp.newBuilder();
		List<Debt> list = DebtService.searchDebts(req, "-publishTime");
		
		
		for(Debt debt : list){
			SimpleDebtMsg.Builder simpleDebt = SimpleDebtMsg.newBuilder();
			PropUtil.copyProperties(simpleDebt, debt, SimpleDebtMsg.Builder.getDescriptor());
			rsp.addDebt(simpleDebt.build());
		}
		
		response.setRsp(rsp.build());
	}

}
