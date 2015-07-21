package com.zc.web.action.debt;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Player;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.PBMessage;
import com.zc.web.message.debt.BidReqProto.BidReq;
import com.zc.web.service.DebtService;
import com.zc.web.service.PlayerService;
import com.zc.web.util.JsonFormat;

/**
 * Bid
 * 
 * @author dan
 *
 */
public class BidAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		// 验证权限
		PlayerService.isValidate(reqSession.getPlayer());
		
		BidReq.Builder builder = BidReq.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		BidReq req = builder.build();
		
		Debt debt = DebtService.getDebtById(Long.parseLong(req.getId()));
		if(debt == null || debt.getState() != Constant.STATE_PUBLISH){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}
		
		Player player = reqSession.getPlayer();
		
		if(debt.getOwnerId() == player.getId()){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}
		
		if(DebtService.isDebtExpired(debt)){
			throw new SmallException(ErrorCode.ERR_DEBT_EXPIRED);
		}
		
		if(!debt.getBondBidders().contains(player.getId())){
			// 保证金
			int bond = debt.getMoney() * Constant.BOND / 100;	
			
			player.getFrozenMoney().put(debt.getId(), bond);
			PlayerService.consumeMoney(player, bond, Constant.MONEY_TYPE_BOND_PAY, Constant.MONEY_PLATFORM_DEFAULT);
		}
		
		// 类型
		if(debt.getType() == Constant.TYPE_BID){
			if(req.getMoney() <= 0)
				throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
			
			// 金额判断
			if(req.getMoney() < debt.getBidMoney() + debt.getBidIncrease())
				throw new SmallException(ErrorCode.ERR_DEBT_BID_LOW);
			
		}else if(debt.getType() == Constant.TYPE_DEPUTY && req.getRate() <= 0){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}
		
		DebtService.bid(player, debt, req);
	}

}
