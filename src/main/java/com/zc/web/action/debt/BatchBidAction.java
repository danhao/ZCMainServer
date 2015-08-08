package com.zc.web.action.debt;

import java.util.HashMap;
import java.util.Map;
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
import com.zc.web.message.debt.BatchBidReqProto.BatchBidReq;
import com.zc.web.service.DebtService;
import com.zc.web.service.PlayerService;

/**
 * Batch bid
 * 
 * @author dan
 *
 */
public class BatchBidAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		// 验证权限
		Player player = reqSession.getPlayer();
		PlayerService.isValidate(player);
		
		BatchBidReq req = (BatchBidReq)getReq(request, BatchBidReq.newBuilder());

		// 可以同时申请的数量
		ConfigVip vip = ConfigHelper.getConfigVip(player.getVip());
		int limit = (vip == null ? 100 : vip.getBidLimit());
		int count = 0;
		for(Entry<Long, Boolean> entry : player.getBidDebts().entrySet()){
			if(!entry.getValue())
				count ++;
		}
		if(count + req.getIdList().size() > limit){
			throw new SmallException(ErrorCode.ERR_DEBT_OVER_LIMIT);
		}

		int totalBond = 0;
		Map<Debt, Integer> map = new HashMap<Debt, Integer>();
		for(String id : req.getIdList()){
			Debt debt = DebtService.getDebtById(Long.parseLong(id));
			if(debt == null || debt.getState() != Constant.STATE_PUBLISH){
				throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
			}
			
			if(debt.getOwnerId() == player.getId()){
				throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
			}
			
			if(DebtService.isDebtExpired(debt)){
				throw new SmallException(ErrorCode.ERR_DEBT_EXPIRED);
			}
			
			if(debt.getIsCorp() > 0 && !PlayerService.checkUserForCorp(player)){
				throw new SmallException(ErrorCode.ERR_DEBT_NO_CORP);
			}
			
			if(debt.getType() != Constant.TYPE_DEPUTY || req.getRate() <= 0){
				throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
			}
			
			// 保证金
			int bond = debt.getMoney() * Constant.BOND / 100;	
			if(bond > Constant.MAX_BOND)
				bond = Constant.MAX_BOND;
			
			totalBond += bond;
			map.put(debt, bond);
		}
		
		// 保证金
		if(totalBond != req.getBond())
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		
		for(Entry<Debt, Integer> entry : map.entrySet()){
			Debt debt = entry.getKey();
			int bond = entry.getValue();
			if(!debt.getBondBidders().contains(player.getId())){
				player.getFrozenMoney().put(debt.getId(), bond);
				PlayerService.consumeMoney(player, bond, Constant.MONEY_TYPE_BOND_PAY, Constant.MONEY_PLATFORM_DEFAULT, debt.getId());
			}
	
			DebtService.bid(player, debt, bond, 0, req.getRate());
		}
		
		PlayerService.savePlayer(player);
	}

}
