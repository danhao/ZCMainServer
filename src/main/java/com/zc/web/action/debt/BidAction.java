package com.zc.web.action.debt;

import java.util.Map.Entry;

import com.zc.web.action.PBBaseAction;
import com.zc.web.config.ConfigHelper;
import com.zc.web.config.model.ConfigVip;
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
		Player player = reqSession.getPlayer();
		PlayerService.isValidate(player);
		
		BidReq.Builder builder = BidReq.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		BidReq req = builder.build();
		
		Debt debt = DebtService.getDebtById(Long.parseLong(req.getId()));
		if(debt == null || debt.getState() != Constant.STATE_PUBLISH){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}
		
		if(debt.getOwnerId() == player.getId()){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}
		
		if(debt.getIsCorp() > 0 && !PlayerService.checkUserForCorp(player)){
			throw new SmallException(ErrorCode.ERR_DEBT_NO_CORP);
		}
		
		// 可以同时申请的数量
		ConfigVip vip = ConfigHelper.getConfigVip(player.getVip());
		int limit = (vip == null ? 100 : vip.getBidLimit());
		int count = 0;
		for(Entry<Long, Boolean> entry : player.getBidDebts().entrySet()){
			if(!entry.getValue())
				count ++;
		}
		if(count > limit){
			throw new SmallException(ErrorCode.ERR_DEBT_OVER_LIMIT);
		}

		// 保证金
		int bond = 0;
		
		// 类型
		if(debt.getType() == Constant.TYPE_BID){
			if(req.getMoney() <= 0)
				throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
			
			// 金额判断
			int money = debt.getPrice();
			if(debt.getBidMoney() > 0)
				money = debt.getBidMoney();
			if(req.getMoney() < money + debt.getBidIncrease())
				throw new SmallException(ErrorCode.ERR_DEBT_BID_LOW);
			
			bond = req.getMoney();
			
			Integer frozenMoney = player.getFrozenMoney().get(debt.getId());
			if(frozenMoney == null)
				frozenMoney = 0;
			
			if(frozenMoney > bond)
				throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
			
			player.getFrozenMoney().put(debt.getId(), bond);
			PlayerService.consumeMoney(player, bond - frozenMoney, Constant.MONEY_TYPE_BOND_PAY, Constant.MONEY_PLATFORM_DEFAULT, debt.getId());
		}else if(debt.getType() == Constant.TYPE_DEPUTY){
			if(req.getRate() <= 0)
				throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
			
			bond = debt.getMoney() * Constant.BOND / 100;	
			if(bond > Constant.MAX_BOND)
				bond = Constant.MAX_BOND;
			
			if(!debt.getBondBidders().contains(player.getId())){
				player.getFrozenMoney().put(debt.getId(), bond);
				PlayerService.consumeMoney(player, bond, Constant.MONEY_TYPE_BOND_PAY, Constant.MONEY_PLATFORM_DEFAULT, debt.getId());
			}
		}

		DebtService.bid(player, debt, bond, req.getMoney(), req.getRate());
		
		PlayerService.savePlayer(player);
	}

}
