package com.zc.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.zc.web.cache.PlayerCache;
import com.zc.web.core.Constant;
import com.zc.web.core.IDGenerator;
import com.zc.web.dao.DebtDao;
import com.zc.web.dao.DebtRepayDao;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Debt.Bidder;
import com.zc.web.data.model.Debt.Contact;
import com.zc.web.data.model.Debt.Message;
import com.zc.web.data.model.Debt.Repayment;
import com.zc.web.data.model.DebtRepay;
import com.zc.web.data.model.File;
import com.zc.web.data.model.Player;
import com.zc.web.data.model.Stat;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.common.ContactMsgProto.ContactMsg;
import com.zc.web.message.common.FileMsgProto.FileMsg;
import com.zc.web.message.debt.DebtMsgProto.DebtMsg;
import com.zc.web.message.debt.ListDebtsReqProto.ListDebtsReq;
import com.zc.web.message.debt.MessageMsgProto.MessageMsg;
import com.zc.web.task.SendMailThread;
import com.zc.web.task.SendSmsThread;
import com.zc.web.task.UploadThread;
import com.zc.web.util.TimeUtil;

public class DebtService {

	private static Logger logger = Logger.getLogger(DebtService.class);

	private static DebtDao debtDao = new DebtDao();
	private static DebtRepayDao repayDao = new DebtRepayDao();
	
	// 最新的债务
	private static List<Debt> latestDebts = new ArrayList<Debt>();
	
	// 最新债务的数量
	public static final int SIZE = 12;
	public static final int SIZE_MAIN = 8;
	
	// 总的数量，定时刷新
	public static long TOTAL_DEBTS = 0;
	
	public static void init(){
		List<Debt> list = debtDao.listDebts(SIZE_MAIN, 0, "-publishTime", Constant.STATE_PUBLISH, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null);
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
	public static Debt getDebtById(long id) throws Exception{
		return getDebtById(id, false);
	}
	public static Debt getDebtById(long id, boolean forceExpire) throws Exception{
		Debt debt = debtDao.getDebt(id);
		
		// 检查状态
		if(debt.getState() == Constant.STATE_PUBLISH && (forceExpire || isDebtExpired(debt))){
			switch(debt.getType()){
			case Constant.TYPE_BID:
				if(debt.getBidId() > 0){
					DebtService.updateState(debt, Constant.STATE_DEALED);
					debt.setWinnerId(debt.getBidId());
					for(Bidder b : debt.getBidders()){
						if(b.getId() == debt.getBidId()){
							debt.setWinnerName(b.getName());
							break;
						}
					}
					
					// 返还保证金
					bondReturn(debt, debt.getWinnerId(), Constant.DEPUTY_STATE_BID, Constant.DEPUTY_STATE_LOSE);
					
					// 更新用户
					Player winner = PlayerCache.INSTANCE.getPlayer(debt.getWinnerId());
					winner.getWinDebts().add(debt.getId());
					winner.getBidDebts().remove(debt.getId());
					PlayerService.savePlayer(winner);
					
					// 统计
					PlayerService.updateDeputyPath(winner, Constant.DEPUTY_STATE_BID, Constant.DEPUTY_STATE_WIN);
				}else{
//					debt.setState(Constant.STATE_CLOSED);
					DebtService.updateState(debt, Constant.STATE_CLOSED);
				}
				saveDebt(debt);
				break;
			case Constant.TYPE_DEPUTY:
				if(debt.getState() != Constant.STATE_DEALED){
					if(debt.getBidders().size() == 0){
						// 无人应标
//						debt.setState(Constant.STATE_CLOSED);
						DebtService.updateState(debt, Constant.STATE_CLOSED);
						saveDebt(debt);
						
						// 更新申请
						ApplyService.updateDebtEndApply(id, 1);
					}else{
						// 自动选择
						if(debt.getIsCorp() == 0){
							// 个人单
							Collections.sort(debt.getBidders(),
									new Comparator<Bidder>() {
										@Override
										public int compare(Bidder b1, Bidder b2) {
											return Integer.valueOf(b1.getRate()).compareTo(Integer.valueOf(b2.getRate()));
										}
									});
						}else{
							// 企业单
							Collections.sort(debt.getBidders(),
									new Comparator<Bidder>() {
										@Override
										public int compare(Bidder b1, Bidder b2) {
											if(b1.getRating() > 0 || b2.getRating() > 0)
												return Integer.valueOf(b2.getRating()).compareTo(Integer.valueOf(b1.getRating()));
											
											return Integer.valueOf(b1.getRate()).compareTo(Integer.valueOf(b2.getRate()));
										}
									});
						}
						
						debt = bidWin(debt.getOwnerId(), debt.getId(), debt.getBidders().get(0).getId());
					}
				}
				break;
			}
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
	public static Debt createOrUpdateDebt(DebtMsg msg, Player player, boolean admin) throws Exception{
		Debt debt = null;
		if(msg.getUpdateId() != null && !msg.getUpdateId().isEmpty()){
			long id = Long.parseLong(msg.getUpdateId());
			debt = getDebtById(id);
			
			if(debt.getState() != Constant.STATE_NEW)
				throw new SmallException(ErrorCode.ERR_DEBT_WRONG_STATE);

			PropertyUtils.copyProperties(debt, msg);
			debt.setId(id);
			debt.setOwnerId(player.getId());
			debt.setOwnerName(player.getName());
		}else{
			debt = new Debt();
			PropertyUtils.copyProperties(debt, msg);
			debt.setId(IDGenerator.INSTANCE.nextId());
			debt.setOwnerId(player.getId());
			debt.setOwnerName(player.getName());
			DebtService.updateState(debt, Constant.STATE_NEW);
		}
		
		debt.getFiles().clear();
		for(FileMsg fileMsg : msg.getFilesList()){
			File file = new File();
			PropertyUtils.copyProperties(file, fileMsg);
			debt.getFiles().add(file);
		}
		
		debt.getContacts().clear();
		for(ContactMsg contactMsg : msg.getContactsList()){
			Contact contact = new Contact();
			PropertyUtils.copyProperties(contact, contactMsg);
			debt.getContacts().add(contact);
		}
		
		if(admin){
//			debt.setState(Constant.STATE_PUBLISH);
			DebtService.updateState(debt, Constant.STATE_PUBLISH);
			debt.setIsCorp(1);
			debt.setPublishTime(TimeUtil.now());
			updateLatest(debt);
		}
		
		debt.setCreateTime(TimeUtil.now());
		if(debt.getCreditorName().isEmpty())
			debt.setCreditorName(player.getName());
		
		File file = new File();
		PropertyUtils.copyProperties(file, msg.getCreditorIdFile());
		debt.setCreditorFileId(file);
		
		saveDebt(debt);
		
		PlayerService.savePlayer(player);
		
		// 动态
		if(msg.getUpdateId() != null && !msg.getUpdateId().isEmpty())
			PlayerService.addSituation(player, Constant.SITUATION_UPDATE_DEBT, String.valueOf(debt.getType()), String.valueOf(debt.getId()), String.valueOf(debt.getDuration()), String.valueOf(debt.getMoney()));
		else
			PlayerService.addSituation(player, Constant.SITUATION_CREATE_DEBT, String.valueOf(debt.getType()), String.valueOf(debt.getId()), String.valueOf(debt.getDuration()), String.valueOf(debt.getMoney()));
		
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
		return debtDao.listDebts(SIZE, (page - 1) * SIZE, order, req.hasState() ? req.getState() : -1, req.getType(), req.getLocation(),
				req.getPublishDays(), req.getMoneyLow(), req.getMoneyUp(),
				req.getExpireLow(), req.getExpireUp(), ownerId, deputyId,
				req.getCreateTimeFrom(), req.getCreateTimeTo(), ids, req.getKeyword(),
				req.getDebtorName(), req.getDebtorId(), req.getProperty(), req.getHandFrom(), req.getHandTo(), req.getNewestMessage());
	}
	
	/**
	 * 投标
	 * 
	 * @param player
	 * @param debt
	 * @param req
	 * @throws Exception
	 */
	public static void bid(Player player, Debt debt, int bond, int money, int rate) throws Exception{
		if(!debt.getBondBidders().contains(player.getId())){
			// 标示交保证金
			debt.getBondBidders().add(player.getId());
		}
		
		Bidder bidder = new Bidder();
		bidder.setId(player.getId());
		bidder.setName(player.getName());
		bidder.setCreateTime(TimeUtil.now());
		bidder.setMoney(money);
		bidder.setHead(player.getHead());
		bidder.setRating(player.getRating());
		
		// 统计
		if(!player.getBidDebts().containsKey(debt.getId())){
			PlayerService.updateDeputyPath(player, -1, Constant.DEPUTY_STATE_BID);
		}
		
		if(money > 0){
			// 投标
			bidder.setMoney(money);
			if(money > debt.getBidMoney()){
				debt.setBidMoney(money);
				debt.setBidId(player.getId());
			}
			debt.getBidders().add(bidder);
			saveDebt(debt);
			
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
				bidder.setRate(rate);
				debt.getBidders().add(bidder);
				DebtService.saveDebt(debt);
				
				player.getBidDebts().put(debt.getId(), false);
			}
		}
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
		Debt debt = debtDao.getDebt(debtId);
		
		if(debt.getType() != Constant.TYPE_DEPUTY ||
				debt.getState() != Constant.STATE_PUBLISH ||
				(playerId > 0 && debt.getOwnerId() != playerId) ||
				!debt.getBondBidders().contains(winnerId)){
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		}

		Player winner = PlayerCache.INSTANCE.getPlayer(winnerId);
		debt.setWinnerId(winnerId);
		debt.setWinnerName(winner.getName());
		debt.setWinnerHead(winner.getHead());
//		debt.setState(Constant.STATE_DEALED);
		DebtService.updateState(debt, Constant.STATE_DEALED);
		debt.setReceiveTime(TimeUtil.now());
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
		
		// 统计
		PlayerService.updateDeputyPath(winner, Constant.DEPUTY_STATE_BID, Constant.DEPUTY_STATE_WIN);
		
		// 返还保证金
		bondReturn(debt, winnerId, Constant.DEPUTY_STATE_BID, Constant.DEPUTY_STATE_LOSE);
		
		// 生成协议
		UploadThread.inst.addSyncInfo(debt);
		
		// 提醒
		String content = "恭喜中标债务，编号" + debt.getId() + "，金额" + (debt.getMoney() / 100f) + "元，请登录<a href='http://www.ddzhai.cn'>点点债</a>确认！";
		SendSmsThread.inst.addSyncInfo(winner.getMobile(), content);
		SendMailThread.inst.addSyncInfo(winner.getEmail(), "中标提醒", content);
		
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

		if(debt.getState() != Constant.STATE_DEALED)
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);

		if(player.getId() != debt.getWinnerId())
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		
		Message message = new Message();
		PropertyUtils.copyProperties(message, msg);
		
		for(FileMsg f : msg.getFilesList()){
			File file = new File();
			PropertyUtils.copyProperties(file, f);
			message.getFiles().add(file);
		}
		
		debt.getMessages().add(message);
		if(debt.getMessages().size() > Constant.MAX_MESSAGE)
			debt.getMessages().remove(0);
		
		debt.setNewestMessage(message.getType());
		
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
	public static void bondReturn(Debt debt, long playerId, int oldState, int newState){
		// 返还未中标用户保证金
		int bond = debt.getMoney() * Constant.BOND / 100;	
		if(bond > Constant.MAX_BOND)
			bond = Constant.MAX_BOND;

		for(long id : debt.getBondBidders()){
			// 中标用户暂不返回
			if(id == playerId)
				continue;
			
			Player p = PlayerCache.INSTANCE.getPlayer(id);
			
			// 统计
			PlayerService.updateDeputyPath(p, oldState, newState);

			// 结束
			p.getBidDebts().put(debt.getId(), true);
			
			Integer money = p.getFrozenMoney().get(debt.getId());
			if(money == null){
				logger.info("bondReturn|" + debt.getId() + "|null");
				continue;
			}
			
			if(debt.getType() == Constant.TYPE_DEPUTY){
				if(money != bond){
					logger.info("bondReturn|" + debt.getId() + "|" + money + "|" + bond);
					continue;
				}
			}
			
			p.getFrozenMoney().remove(debt.getId());
			PlayerService.addMoney(p, bond, Constant.MONEY_TYPE_BOND_RETURN, Constant.MONEY_PLATFORM_DEFAULT, debt.getId());
			PlayerService.addSituation(p, Constant.SITUATION_BOND_RETURN, String.valueOf(debt.getId()), String.valueOf(bond));
			
			// 提醒
			try{
				String content = "参与投标的债务（编号" + debt.getId() + "）未中标，保证金已返回，请登录<a href='http://www.ddzhai.cn'>点点债</a>确认！";
				SendSmsThread.inst.addSyncInfo(p.getMobile(), content);
				SendMailThread.inst.addSyncInfo(p.getEmail(), "关闭提醒", content);
			}catch(Exception e){
				
			}
		}
	}
	
	/**
	 * 额外联系方式
	 * 
	 * @param player
	 * @param msg
	 * @throws Exception
	 */
	public static void addContact(Player player, ContactMsg msg) throws Exception{
		long id = Long.parseLong(msg.getId());
		Debt debt = getDebtById(id);
		if(debt == null)
			return;
		
		if(player.getId() != debt.getWinnerId())
			throw new SmallException(ErrorCode.ERR_DEBT_INVALID);
		
		Contact contact = new Contact();
		PropertyUtils.copyProperties(contact, msg);
		
		debt.getContacts().add(contact);
		if(debt.getContacts().size() > Constant.MAX_MESSAGE)
			debt.getContacts().remove(0);
		
		debtDao.updateContacts(id, debt.getContacts());
	}
	
	/**
	 * 统计查询
	 * 
	 * @param player
	 * @param winnerId
	 * @param state
	 * @param receiveTimeFrom
	 * @param receiveTimeTo
	 * @return
	 */
	public static Stat queryStat(Player player, int state, int receiveTimeFrom, int receiveTimeTo, int type){
		String key = player.getId() + "_" + state + "_" + receiveTimeFrom + "_" + receiveTimeTo;
		Stat stat = player.getStatMap().get(key);
		if(stat != null)
			return stat;
		
		
		List<Debt> list = debtDao.listDebts(player.getId(), state, receiveTimeFrom, receiveTimeTo, type);
		
		int money = 0;
		int repayment = 0;
		int done = 0;
		for(Debt debt : list){
			money += debt.getMoney();
			for(Repayment repay : debt.getRepayments()){
				repayment += repay.getMoney();
			}
			
			if(debt.getState() == Constant.STATE_CLOSED && debt.getRepayments().size() > 0)
				done ++;
		}

		stat = new Stat();
		stat.setTotal(list.size());
		stat.setMoney(money);
		stat.setDone(done);
		stat.setRepayment(repayment);
		
		player.getStatMap().put(key, stat);
		
		return stat;
	}
	
	public static void updateLatest(Debt debt){
		synchronized(latestDebts){
			latestDebts.add(0, debt);
			if(latestDebts.size() > SIZE_MAIN){
				latestDebts.remove(latestDebts.size() - 1);
			}
		}
	}
	
	public static void updateState(Debt debt, int state){
		PlayerService.updateCreditorPath(debt.getOwnerId(), debt.getState(), state);
		debt.updateState(state);
	}
	
	public static List<DebtRepay> listRepay(int offset, int limit, String debtId,  
			String ownerId, String deputyId, int timeFrom, int timeTo, String debtorName){
		long debtId_ = 0;
		long ownerId_ = 0;
		long deputyId_ = 0;
		
		if(!debtId.isEmpty())
			debtId_ = Long.parseLong(debtId);
		if(!ownerId.isEmpty())
			ownerId_ = Long.parseLong(ownerId);
		if(!deputyId.isEmpty())
			deputyId_ = Long.parseLong(deputyId);
		
		if(ownerId_ == 0 && deputyId_ == 0)
			return new ArrayList<DebtRepay>();
		
		return repayDao.listDebtRepay(offset, limit, debtId_, ownerId_, deputyId_, timeFrom, timeTo, debtorName);
	}

	public static DebtRepayDao getRepayDao() {
		return repayDao;
	}
}
