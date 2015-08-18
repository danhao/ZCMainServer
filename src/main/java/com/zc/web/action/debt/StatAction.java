package com.zc.web.action.debt;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Stat;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.StatReqProto.StatReq;
import com.zc.web.message.debt.StatRspProto.StatRsp;
import com.zc.web.service.DebtService;
import com.zc.web.util.PropUtil;

public class StatAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		StatReq req = (StatReq)getReq(request, StatReq.newBuilder());
		
		Stat stat = DebtService.queryStat(reqSession.getPlayer(), req.getState(), 
				req.getReceiveTimeFrom(), req.getReceiveTimeTo());
		
		StatRsp.Builder rsp = StatRsp.newBuilder();
		PropUtil.copyProperties(rsp, stat, StatRsp.Builder.getDescriptor());
		
		response.setRsp(rsp.build());
	}

}
