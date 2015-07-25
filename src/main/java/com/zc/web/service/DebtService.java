package com.zc.web.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.zc.web.cache.PlayerCache;
import com.zc.web.core.Constant;
import com.zc.web.core.IDGenerator;
import com.zc.web.dao.DebtDao;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Debt.Bidder;
import com.zc.web.data.model.Debt.Message;
import com.zc.web.data.model.File;
import com.zc.web.data.model.Player;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.common.FileMsgProto.FileMsg;
import com.zc.web.message.debt.BidReqProto.BidReq;
import com.zc.web.message.debt.DebtMsgProto.DebtMsg;
import com.zc.web.message.debt.ListDebtsReqProto.ListDebtsReq;
import com.zc.web.message.debt.MessageMsgProto.MessageMsg;
import com.zc.web.task.UploadThread;
import com.zc.web.util.TimeUtil;

public class DebtService {

	private static Logger logger = Logger.getLogger(DebtService.class);

	private static DebtDao debtDao = new DebtDao();
	// 最新的债务
	private static List<Debt> latestDebts = new ArrayList<Debt>();
	
	// 最新债务的数量
	private static final int SIZE = 10;
	
	// 总的数量，定时刷新
	public static long TOTAL_DEBTS = 0;
	
	public static void init(){
		List<Debt> list = debtDao.listDebts(SIZE, 0, "-publishTime", Constant.STATE_PUBLISH, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null);
		if(list != null)
			latestDebts.addAll(list);
	}
	
	public static void getCount(){
		TOTAL_DEBTS = debtDao.getCount();
	}
	
	/**
	 * 通过id从DB加载基本数据
	 * 
	 * @param id
	 * @return
	 */
	public static Debt getDebtById(long id) {
		Debt debt = debtDao.getDebt(id);
		
		// 检查状态
		if(debt.getState() == Constant.STATE_PUBLISH && isDebtExpired(debt)){
			switch(debt.getType()){
			case Constant.TYPE_BID:
				debt.setState(Constant.STATE_DEALED);
				debt.setWinnerId(debt.getBidId());
				for(Bidder b : debt.getBidders()){
					if(b.getId() == debt.getBidId()){
						debt.setWinnerName(b.getName());
						break;
					}
				}
				
				// 返还保证金
				bondReturn(debt, debt.getBidId());

				break;
			case Constant.TYPE_DEPUTY:
				if(debt.getState() != Constant.STATE_DEALED){
					debt.setState(Constant.STATE_CLOSED);
					
					// 返还保证金
					bondReturn(debt, 0);
				}
				break;
			}
			
			saveDebt(debt);
		}
		
		return debt;
	}

	/**
	 * 保存数据到DB中
	 * 
	 * @param debt
	 */
	public static void saveDebt(Debt debt) {
		debtDao.save(debt);
	}
	
	/**
	 * 生成新的
	 * 
	 * @param msg
	 * @param player
	 * @return
	 * @throws Exception
	 */
	public static Debt createDebt(DebtMsg msg, Player player, boolean admin) throws Exception{
		Debt debt = new Debt();
		PropertyUtils.copyProperties(debt, msg);
		
		for(FileMsg fileMsg : msg.getFilesList()){
			File file = new File();
			PropertyUtils.copyProperties(file, fileMsg);
			debt.getFiles().add(file);
		}
		
		debt.setId(IDGenerator.INSTANCE.nextId());
		if(admin){
			debt.setState(Constant.STATE_PUBLISH);
			debt.setPublishTime(TimeUtil.now());
			debt.setIsCorp(1);	// 企业单
		}
		debt.setCreateTime(TimeUtil.now());
		debt.setOwnerId(player.getId());
		debt.setOwnerName(player.getName());
		if(debt.getCreditorName().isEmpty())
			debt.setCreditorName(player.getName());
		
		File file = new File();
		PropertyUtils.copyProperties(file, msg.getCreditorIdFile());
		debt.setCreditorFileId(file);
		
		saveDebt(debt);
		
		PlayerService.savePlayer(player);
		
		synchronized(latestDebts){
			latestDebts.add(debt);
			if(latestDebts.size() > SIZE){
				latestDebts.remove(0);
			}
		}
		
		// 动态
		PlayerService.addSituation(player, Constant.SITUATION_CREATE_DEBT, (debt.getType() == Constant.TYPE_BID ? "债权转让":"债务代理") + " 代理期限：" + debt.getDuration() + "天");
		
		return debt;
	}
	
	public static List<Debt> getLatestDebts(){
		return latestDebts;
	}
	
	/**
	 * 债务查询
	 * 
	 * @param req
	 * @return
	 */
	public static List<Debt> searchDebts(ListDebtsReq req, String order){
		int page = req.getPage();
		if(page <= 0)
			page = 1;
		long ownerId = (req.getOwnerId() == null || req.getOwnerId().isEmpty()) ? 0 :Long.parseLong(req.getOwnerId());
		long deputyId = (req.getDeputyId() == null || req.getDeputyId().isEmpty()) ? 0 :Long.parseLong(req.getDeputyId());
		long id = (req.getId() == null || req.getId().isEmpty()) ? 0 :Long.parseLong(req.getId());
		List<Long> ids = null;
		if(id > 0){
			ids = new ArrayList<Long>();
			ids.add(id);
		}
		return debtDao.listDebts(SIZE, (page - 1) * SIZE, order, req.getState(), req.getType(), req.getLocation(),
				req.getPublishDays(), req.getMoneyLow(), req.getMoneyUp(),
				req.getExpireLow(), req.getExpireUp(), ownerId, deputyId,
				req.getCreateTimeFrom(), req.getCreateTimeTo(), ids, req.getKeyword());
	}
	
	/**
	 * 投标
	 * 
	 * @param player
	 * @param debt
	 * @param req
	 * @throws Exception
	 */
	public static void bid(Player player, Debt debt, BidReq req) throws Exception{
		if(!debt.getBondBidders().contains(player.getId())){
			// 标示交保证金
			debt.getBondBidders().add(player.getId());
		}
		
		Bidder bidder = new Bidder();
		bidder.setId(player.getId());
		bidder.setName(player.getName());
		bidder.setCreateTime(TimeUtil.now());
		
		if(req.getMoney() > 0){
			// 投标
			bidder.setMoney(req.getMoney());
			if(req.getMoney() > debt.getBidMoney()){
				debt.setBidMoney(req.getMoney());
				debt.setBidId(player.getId());
			}
			debt.getBidders().add(bidder);
			DebtService.saveDebt(debt);
			
			player.getBidDebts().put(debt.getId(), false);
		}else{
			// 代理
			boolean hasBid = false;
			for(Bidder b : debt.getBidders()){
				if(b.getId() == player.getId()){
					hasBid = true;
					break;
				}
			}
			
			if(!hasBid){
				bidder.setRate(req.getRate());
				debt.getBidders().add(bidder);
				DebtService.saveDebt(debt);
				
				player.getBidDebts().put(debt.getId(), false);
			}
		}
		
		PlayerService.savePlayer(player);
	}
	
	/**
	 * 中标
	 * 
	 * @param player
	 * @param debtId
	 * @param winnerId
	 * @return
	 * @throws Exception
	 */
	public static Debt bidWin(long playerId, long debtId, long winnerId) throws Exception{
		Debt debt = getDebtById(debtId);
		
		if(debt.getType() != Constant.TYPE_DEPUTY ||
				debt.getState() != Constant.STATE_PUBLISH ||
				(playerId > 0 && debt.getOwnerId() != playerId) ||
				!debt.getBondBidders().contains(winnerId)){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}

		Player winner = PlayerCache.INSTANCE.getPlayer(winnerId);
		debt.setWinnerId(winnerId);
		debt.setWinnerName(winner.getName());
		debt.setState(Constant.STATE_DEALED);
		for(Bidder b : debt.getBidders()){
			if(b.getId() == winnerId){
				debt.setRate(b.getRate());
				break;
			}
		}

		saveDebt(debt);
		
		winner.getWinDebts().add(debt.getId());
		winner.getBidDebts().remove(debt.getId());
		PlayerService.savePlayer(winner);
		
		// 返还保证金
		bondReturn(debt, winnerId);
		
		// 生成协议
		UploadThread.inst.addSyncInfo(debt);
		
		return debt;
	}
	
	/**
	 * 是否到期
	 * 
	 * @param debt
	 * @return
	 */
	public static boolean isDebtExpired(Debt debt){
		if(TimeUtil.now() > debt.getPublishTime() + debt.getExpireDays() * Constant.ONE_DAY){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 更新催收动态
	 * 
	 * @param msg
	 * @throws Exception
	 */
	public static void addBidMessage(Player player, MessageMsg msg) throws Exception{
		Debt debt = getDebtById(Long.parseLong(msg.getId()));
		if(debt == null)
			return;
		
		if(player.getId() != debt.getWinnerId())
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		
		Message message = new Message();
		PropertyUtils.copyProperties(message, msg);
		
		debt.getMessages().add(message);
		if(debt.getMessages().size() > 20)
			debt.getMessages().remove(0);
		saveDebt(debt);
	}
	
	public static DebtDao getDebtDao() {
		return debtDao;
	}
	
	
	/**
	 * 未中标返回保证金
	 * 
	 * @param debtId
	 */
	private static void bondReturn(Debt debt, long playerId){
		// 返还未中标用户保证金
		int bond = debt.getMoney() * Constant.BOND / 100;	

		for(long id : debt.getBondBidders()){
			// 中标用户暂不返回
			if(id == playerId)
				continue;
			
			Player p = PlayerCache.INSTANCE.getPlayer(id);
			
			// 结束
			p.getBidDebts().put(debt.getId(), true);
			
			Integer money = p.getFrozenMoney().get(debt.getId());
			if(money == null){
				logger.info("bondReturn|" + debt.getId() + "|null");
				continue;
			}
			
			if(money != bond){
				logger.info("bondReturn|" + debt.getId() + "|" + money + "|" + bond);
				continue;
			}
			
			p.getFrozenMoney().remove(debt.getId());
			PlayerService.addMoney(p, bond, Constant.MONEY_TYPE_BOND_RETURN, Constant.MONEY_PLATFORM_DEFAULT, "bond return");
		}
	}
}
