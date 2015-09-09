package com.zc.web.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.zc.web.data.model.DebtRepay;
import com.zc.web.exception.DBException;

public class DebtRepayDao extends BaseDao<DebtRepay> {
	
	private static final Logger log = Logger.getLogger(DebtRepayDao.class);
	
	public List<DebtRepay> listDebtRepay(int offset, int limit, long debtId,  
			long ownerId, long deputyId, int timeFrom, int timeTo, String debtorName) {
		final Datastore ds = getDatastore();

		try {
			Query<DebtRepay> query = ds.find(DebtRepay.class);
			query.offset(offset).limit(limit).order("-time");
			if(debtId > 0)
				query.field("debtId").equal(debtId);
			if(ownerId > 0 && deputyId > 0){
				query.or(
						query.criteria("ownerId").equal(ownerId),
						query.criteria("deputyId").equal(deputyId)
						);
			}else{
				if(ownerId > 0)
					query.field("ownerId").equal(ownerId);
				if(deputyId > 0)
					query.field("deputyId").equal(deputyId);
			}
			if(timeFrom > 0)
				query.field("time").greaterThanOrEq(timeFrom);
			if(timeTo > 0)
				query.field("time").lessThanOrEq(timeTo);
			if(debtorName != null && !debtorName.isEmpty())
				query.field("debtorName").containsIgnoreCase(debtorName);
			
			return query.asList();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
	}
}
