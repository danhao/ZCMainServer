package com.zc.web.action.debt;

import com.zc.web.action.PBBaseAction;
import com.zc.web.core.Constant;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Debt.Message;
import com.zc.web.data.model.Player;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.PBMessage;
import com.zc.web.message.common.SingleMsgProto.SingleMsg;
import com.zc.web.service.DebtService;
import com.zc.web.service.PlayerService;
import com.zc.web.util.TimeUtil;

/**
 * 退单
 * 
 * @author dan
 *
 */
public class ReturnDebtAction extends PBBaseAction {

	@Override
	public void done(PBRequestSession reqSession, PBMessage request,
			PBMessage response) throws Exception {
		
		// 验证权限
		Player player = reqSession.getPlayer();
		
		SingleMsg req = (SingleMsg)getReq(request, SingleMsg.newBuilder());
		
		Debt debt = DebtService.getDebtById(Long.parseLong(req.getParam()));
		if(debt == null || debt.getState() != Constant.STATE_DEALED){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}
		
		if(debt.getOwnerId() != player.getId()){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}
		
		if(debt.getRepayments().size() > 0){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}
		
		if((TimeUtil.now() - debt.getPublishTime()) / Constant.ONE_DAY < 30)
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);

		
		// 更新单
		debt.setState(Constant.STATE_PUBLISH);
		debt.setPublishTime(TimeUtil.now());
		debt.getBidders().clear();
		debt.getBondBidders().clear();
		debt.setWinnerId(0);
		debt.setWinnerName(null);
		debt.setWinnerHead(null);
		
		// 动态
		Message message = new Message();
		message.setType(Constant.MESSAGE_TYPE_RETURN);
		message.setTime(TimeUtil.now());
		message.setMemo("退单处理。");
		debt.getMessages().add(message);
		if(debt.getMessages().size() > Constant.MAX_MESSAGE)
			debt.getMessages().remove(0);
		
		// 退还保证金
		Integer money = player.getFrozenMoney().get(debt.getId());
		if(money != null){
			player.getFrozenMoney().remove(debt.getId());
			PlayerService.addMoney(player, money, Constant.MONEY_TYPE_BOND_RETURN, Constant.MONEY_PLATFORM_DEFAULT, "bond return");
			PlayerService.addSituation(player, Constant.SITUATION_BOND_RETURN, String.valueOf(debt.getId()), String.valueOf(money.intValue()));
		}
		
		DebtService.saveDebt(debt);
	}

}
