package com.zc.web.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.zc.web.data.model.MoneyHistory;
import com.zc.web.exception.DBException;

public class MoneyHistoryDao extends BaseDao<MoneyHistory> {
	
	private static final Logger log = Logger.getLogger(MoneyHistoryDao.class);
	/**
	 * @param id
	 * @return
	 */
	public MoneyHistory getMoneyHistory(final long id) {
		final Datastore ds = getDatastore();

		MoneyHistory moneyHistory = null;
		try {
			moneyHistory = ds.get(MoneyHistory.class, id);
		} catch (Exception e) {
			log.error("get moneyHistory exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
		return moneyHistory;
	}
	
	public List<MoneyHistory> listMoneyHistory(int offset, int limit, long playerId, int type, int timeFrom, int timeTo) {
		final Datastore ds = getDatastore();

		try {
			Query<MoneyHistory> query = ds.find(MoneyHistory.class);
			query.offset(offset).limit(limit);
			query.field("playerId").equal(playerId);
			if(type > 0)
				query.field("type").equal(type);
			if(timeFrom > 0)
				query.field("time").greaterThanOrEq(timeFrom);
			if(timeTo > 0)
				query.field("time").lessThanOrEq(timeTo);
			
			return query.asList();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
	}
}
