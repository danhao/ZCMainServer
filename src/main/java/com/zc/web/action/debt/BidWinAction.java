package com.zc.web.action.debt;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.BidWinReqProto.BidWinReq;
import com.zc.web.service.DebtService;
import com.zc.web.util.JsonFormat;

/**
 * Win the bid
 * 
 * @author dan
 *
 */
public class BidWinAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		BidWinReq.Builder builder = BidWinReq.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		BidWinReq req = builder.build();
		
		response.setRsp(DebtService.bidWin(reqSession.getPlayerId(), Long.parseLong(req.getDebtId()), Long.parseLong(req.getPlayerId())).build());
	}

}
