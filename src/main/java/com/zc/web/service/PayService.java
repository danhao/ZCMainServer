package com.zc.web.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.zc.web.cache.PlayerCache;
import com.zc.web.core.Constant;
import com.zc.web.core.IDGenerator;
import com.zc.web.dao.PlayerCashDao;
import com.zc.web.dao.PlayerOrderDao;
import com.zc.web.data.model.Player;
import com.zc.web.data.model.PlayerCash;
import com.zc.web.data.model.PlayerOrder;
import com.zc.web.message.pay.PlayerCashMsgProto.PlayerCashMsg;
import com.zc.web.util.TimeUtil;

/***
 * 支付相关操作
 * 
 * @author small
 *
 */
public class PayService {

	private static final Logger log = Logger.getLogger(PayService.class);

	public static String ORDER_STATUS_0 = "0";// 未完成状态
	public static String ORDER_STATUS_1 = "1";// 已经完成状态
	public static String ORDER_STATUS_2 = "2";// 失败状态

	private static PlayerOrderDao orderDao = new PlayerOrderDao();
	private static PlayerCashDao cashDao = new PlayerCashDao();

	public static PlayerOrder getPlayerOrder(long orderId) {
		return orderDao.getPlayerOrder(orderId);
	}

	public static PlayerOrder getOrderByOutOrderNo(String outOrderNo) {
		return orderDao.getPlayerOrderByOutOrderNo(outOrderNo);
	}

	public static PlayerOrder createPlayerOrder(Player player, int amount) {
		PlayerOrder order = new PlayerOrder();
		order.setId(IDGenerator.INSTANCE.nextId());
		order.setPlayerId(player.getId());
		order.setAmount(amount);
		order.setRealityAmount(0);
		order.setCreateAt(TimeUtil.dateToString(new Date(), "yyyyMMddHHmmss"));
		order.setStatus(ORDER_STATUS_0);
		if (order != null) {
			savePlayerOrder(order);
		}
		return order;
	}

	public static void savePlayerOrder(PlayerOrder playerOrder) {
		orderDao.save(playerOrder);
	}
	
	public static PlayerCash createPlayerCash(Player player, PlayerCashMsg msg) throws Exception {
		PlayerCash cash = new PlayerCash();
		
		PropertyUtils.copyProperties(cash, msg);
		
		cash.setId(IDGenerator.INSTANCE.nextId());
		cash.setPlayerId(player.getId());
		cash.setCreateAt(TimeUtil.dateToString(new Date(), "yyyyMMddHHmmss"));
		cash.setStatus(ORDER_STATUS_0);
		if (cash != null) {
			savePlayerCash(cash);
		}
		return cash;
	}

	public static void savePlayerCash(PlayerCash playerCash) {
		cashDao.save(playerCash);
	}
	
	public static PlayerCash getPlayerCash(long id){
		return cashDao.getPlayerCash(id);
	}

	/***
	 * 充值操作
	 * 
	 * @param player
	 * @param playerOrder
	 * @param realityAmount
	 */
	public static boolean rechargePlayerOrder(long playerId, long orderId,
			int realityAmount, String outOrderNo) {
		PlayerOrder playerOrder = getPlayerOrder(orderId);
		playerOrder.setRealityAmount(realityAmount);
		if (playerOrder.getStatus().equals(ORDER_STATUS_0)) {
			Player player = PlayerCache.INSTANCE.getPlayer(playerId);
			if(player == null)
				return false;
			
			PlayerService
					.addMoney(player, realityAmount,
							Constant.MONEY_TYPE_CHARGE,
							Constant.MONEY_PLATFORM_DEFAULT);
		}

		playerOrder.setOutOrderNo(outOrderNo);
		playerOrder.setStatus(ORDER_STATUS_1);
		playerOrder.setFinishAt(TimeUtil.dateToString(new Date(),
				"yyyyMMddHHmmss"));
		savePlayerOrder(playerOrder);
		
		return true;
	}
	
	public static List<PlayerCash> listPlayerCash(int offset, int limit, long id, long playerId, String status){
		return cashDao.listPlayerCash(offset, limit, id, playerId, status);
	}

	public static PlayerCashDao getCashDao() {
		return cashDao;
	}
}
