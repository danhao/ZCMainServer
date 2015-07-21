package com.zc.web.action.player;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Player.MoneyHistory;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.ListMoneyHistoryReqProto.ListMoneyHistoryReq;
import com.zc.web.message.player.ListMoneyHistoryRspProto.ListMoneyHistoryRsp;
import com.zc.web.message.player.ListMoneyHistoryRspProto.ListMoneyHistoryRsp.MoneyHistoryMsg;
import com.zc.web.util.PropUtil;

public class ListMoneyHistoryAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		ListMoneyHistoryReq req = (ListMoneyHistoryReq)getReq(request, ListMoneyHistoryReq.newBuilder());
		
		ListMoneyHistoryRsp.Builder rsp = ListMoneyHistoryRsp.newBuilder();
		
		for(MoneyHistory history : reqSession.getPlayer().getHistories()){
			if(history.getTime() < req.getTimeFrom() || history.getTime() > req.getTimeTo())
				continue;
			
			if(req.getType() > 0 && history.getType() != req.getType())
				continue;
			
			MoneyHistoryMsg.Builder mh = MoneyHistoryMsg.newBuilder();
			PropUtil.copyProperties(mh, history, MoneyHistoryMsg.Builder.getDescriptor());
			rsp.addHistory(mh.build());
		}
		
		response.setRsp(rsp.build());

	}

}
