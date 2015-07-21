package com.zc.web.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.zc.web.data.model.PlayerOrder;
import com.zc.web.exception.DBException;

public class PlayerOrderDao extends BaseDao<PlayerOrder> {
	
	private static final Logger log = Logger.getLogger(PlayerOrderDao.class);
	/**
	 * 根据订单号
	 * @param id
	 * @return
	 */
	public PlayerOrder getPlayerOrder(final long id) {
		final Datastore ds = getDatastore();

		PlayerOrder playerOrder = null;
		try {
			playerOrder = ds.get(PlayerOrder.class, id);
		} catch (Exception e) {
			log.error("get playerOrder exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
		return playerOrder;
	}
	
	/**
	 * 根据AppStore交易ID查找订单信息
	 * @param transactionId
	 * @return
	 */
	public PlayerOrder getPlayerOrderByOutOrderNo(String outOrderNo) {
		final Datastore ds = getDatastore();

		PlayerOrder playerOrder = null;
		try {
			Query<PlayerOrder> query = ds.find(PlayerOrder.class);
			query.field("outOrderNo").equal(outOrderNo);
			List<PlayerOrder> list = query.asList();
			if( list.size()>0 ){
				return list.get(0);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
		return playerOrder;
	}
	

}
