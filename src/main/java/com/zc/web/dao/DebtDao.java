package com.zc.web.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.zc.web.core.Constant;
import com.zc.web.data.model.BaseModel;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Debt.Contact;
import com.zc.web.data.model.Debt.Message;
import com.zc.web.data.model.Debt.Repayment;
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
			int createTimeFrom, int createTimeTo, Collection<Long> ids, String keyword){
		return listDebts(limit, offset, order, state, 
				type, location, publishDays, moneyLow, moneyUp,
				expireLow, expireUp, ownerId, deputyId,
				createTimeFrom, createTimeTo, ids, keyword,
				null, null, 0, 0, 0, 0);
	}
	public List<Debt> listDebts(int limit, int offset, String order, int state, 
			int type, String location, int publishDays, int moneyLow, int moneyUp,
			int expireLow, int expireUp, long ownerId, long deputyId,
			int createTimeFrom, int createTimeTo, Collection<Long> ids, String keyword,
			String debtorName, String debtorId, int property, int handFrom, int handTo, int newestMessage){
		
		if(ids != null && ids.size() == 0){
			return new ArrayList<Debt>();
		}
		
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
			if(ids != null)
				query.field("id").in(ids);
			if(debtorName != null && !debtorName.isEmpty())
				query.field("debtorName").equal(debtorName);
			if(debtorId != null && !debtorId.isEmpty())
				query.field("debtorId").equal(debtorId);
			if(property > 0)
				query.field("property").equal(property);
			if(handTo > 0)
				query.field("debtExpireTime").greaterThanOrEq(TimeUtil.now() - handTo * Constant.ONE_DAY);
			if(handFrom > 0)
				query.field("debtExpireTime").lessThanOrEq(TimeUtil.now() - handFrom * Constant.ONE_DAY);
			if(newestMessage > 0)
				query.field("newestMessage").equal(newestMessage);
			
			if(keyword != null && !keyword.isEmpty()){
				query.or(
						query.criteria("debtorName").containsIgnoreCase(keyword),
						query.criteria("reason").containsIgnoreCase(keyword),
						query.criteria("debtorLocation").containsIgnoreCase(keyword)
						);

			}
			return query.queryNonPrimary().asList();
		} catch (Exception e) {
			log.error("list debt exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
	}
	
	public List<Debt> listDebts(long winnerId, int state, int receiveTimeFrom, int receiveTimeTo, int type){
		
		final Datastore ds = getDatastore();

		try {
			Query<Debt> query = ds.find(Debt.class);
			if(winnerId > 0){
				if(type == 1)
					query.field("ownerId").equal(winnerId);
				else
					query.field("winnerId").equal(winnerId);
			}
			if(receiveTimeFrom > 0)
				query.field("receiveTime").greaterThanOrEq(receiveTimeFrom);
			if(receiveTimeTo > 0)
				query.field("receiveTime").lessThanOrEq(receiveTimeTo);
			if(state > 0)
				query.field("state").equal(state);
			
			return query.queryNonPrimary().asList();
		} catch (Exception e) {
			log.error("list debt exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
	}	
	
	public void updateContacts(long id, List<Contact> contacts) {
		try {
			final Datastore ds = getDatastore();
			
			Query<Debt> query = ds.find(Debt.class).field(BaseModel.ID_KEY).equal(id);
			UpdateOperations<Debt> ops = ds.createUpdateOperations(Debt.class).disableValidation();
			
			ops.set("contacts", contacts);
			update(query, ops);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void updateMessages(long id, List<Message> messages) {
		try {
			final Datastore ds = getDatastore();
			
			Query<Debt> query = ds.find(Debt.class).field(BaseModel.ID_KEY).equal(id);
			UpdateOperations<Debt> ops = ds.createUpdateOperations(Debt.class).disableValidation();
			
			ops.set("messages", messages);
			update(query, ops);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void updateRepayments(long id, List<Repayment> pays) {
		try {
			final Datastore ds = getDatastore();
			
			Query<Debt> query = ds.find(Debt.class).field(BaseModel.ID_KEY).equal(id);
			UpdateOperations<Debt> ops = ds.createUpdateOperations(Debt.class).disableValidation();
			
			ops.set("repayments", pays);
			update(query, ops);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
}
