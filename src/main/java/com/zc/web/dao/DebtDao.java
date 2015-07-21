package com.zc.web.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.zc.web.core.Constant;
import com.zc.web.data.model.Debt;
import com.zc.web.exception.DBException;
import com.zc.web.util.TimeUtil;

/**
 * 债务数据操作
 * @author Administrator
 *
 */
public class DebtDao extends BaseDao<Debt> {
	
	private static final Logger log = Logger.getLogger(DebtDao.class);
	
	/**
	 * 根据id获取基本数据
	 * @param id
	 * @return
	 */
	public Debt getDebt(final long id) {
		final Datastore ds = getDatastore();

		Debt pdebt = null;
		try {
			pdebt = ds.get(Debt.class, id);
		} catch (Exception e) {
			log.error("get debt exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
		return pdebt;
	}
	
	public long getCount(){
		final Datastore ds = getDatastore();

		try {
//			return ds.getCount(Debt.class);
			Query<Debt> query = ds.find(Debt.class);
			query.field("id").greaterThan(0);
			return ds.getCount(query);
		} catch (Exception e) {
			log.error("get debt exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
	}
	
	public List<Debt> listDebts(int limit, int offset, String order, int state, 
			int type, String location, int publishDays, int moneyLow, int moneyUp,
			int expireLow, int expireUp, long ownerId, long deputyId,
			int createTimeFrom, int createTimeTo, List<Long> ids, String keyword){
		final Datastore ds = getDatastore();

		try {
			Query<Debt> query = ds.find(Debt.class);
			query.offset(offset).limit(limit).order(order);
			if(state >= 0)
				query.field("state").equal(state);
			if(type > 0)
				query.field("type").equal(type);
			if(location != null && !location.isEmpty())
				query.field("debtorLocation").containsIgnoreCase(location);
			if(publishDays > 0)
				query.field("publishTime").greaterThanOrEq(TimeUtil.now() - publishDays * Constant.ONE_DAY);
			query.field("money").greaterThanOrEq(moneyLow);
			if(moneyUp > 0)
				query.field("money").lessThanOrEq(moneyUp);
			if(expireUp > 0)
				query.field("expireDays").greaterThanOrEq(expireLow).field("expireDays").lessThanOrEq(expireUp);
			if(ownerId > 0)
				query.field("ownerId").equal(ownerId);
			if(deputyId > 0)
				query.field("winnerId").equal(deputyId);
			if(createTimeFrom > 0)
				query.field("createTime").greaterThanOrEq(createTimeFrom);
			if(createTimeTo > 0)
				query.field("createTime").lessThanOrEq(createTimeTo);
			if(ids != null && ids.size() > 0)
				query.field("id").in(ids);
			if(keyword != null && !keyword.isEmpty()){
				query.or(
						query.criteria("debtorName").containsIgnoreCase(keyword),
						query.criteria("reason").containsIgnoreCase(keyword),
						query.criteria("debtorLocation").containsIgnoreCase(keyword),
						query.criteria("ownerName").containsIgnoreCase(keyword)
						);

			}
			return query.asList();
		} catch (Exception e) {
			log.error("list debt exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
	}
}
