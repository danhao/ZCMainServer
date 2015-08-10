package com.zc.web.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.zc.web.data.model.DebtEndApply;
import com.zc.web.exception.DBException;

public class DebtEndApplyDao extends BaseDao<DebtEndApply> {
	
	private static final Logger log = Logger.getLogger(DebtEndApplyDao.class);
	/**
	 * @param id
	 * @return
	 */
	public DebtEndApply getDebtEndApply(final long id) {
		final Datastore ds = getDatastore();

		DebtEndApply debtEndApply = null;
		try {
			debtEndApply = ds.get(DebtEndApply.class, id);
		} catch (Exception e) {
			log.error("get debtEndApply exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
		return debtEndApply;
	}
	
	public List<DebtEndApply> listDebtEndApply(int offset, int limit, long id, int status) {
		final Datastore ds = getDatastore();

		try {
			Query<DebtEndApply> query = ds.find(DebtEndApply.class);
			query.offset(offset).limit(limit);
			if(id > 0)
				query.field("id").equal(id);
			if(status >= 0)
				query.field("status").equal(status);
			
			return query.asList();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
	}

	public long getCount(long id, int status){
		final Datastore ds = getDatastore();

		try {
			Query<DebtEndApply> query = ds.find(DebtEndApply.class);
			if(id > 0)
				query.field("id").equal(id);
			if(status >= 0)
				query.field("status").equal(status);
			
			return ds.getCount(query);
		} catch (Exception e) {
			log.error("get apply exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
	}
}
