package com.zc.web.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.zc.web.data.model.PlayerCash;
import com.zc.web.exception.DBException;

public class PlayerCashDao extends BaseDao<PlayerCash> {
	
	private static final Logger log = Logger.getLogger(PlayerCashDao.class);
	/**
	 * @param id
	 * @return
	 */
	public PlayerCash getPlayerCash(final long id) {
		final Datastore ds = getDatastore();

		PlayerCash playerCash = null;
		try {
			playerCash = ds.get(PlayerCash.class, id);
		} catch (Exception e) {
			log.error("get playerCash exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
		return playerCash;
	}
	
	public List<PlayerCash> listPlayerCash(int offset, int limit, long id, long playerId, String status) {
		final Datastore ds = getDatastore();

		try {
			Query<PlayerCash> query = ds.find(PlayerCash.class);
			query.offset(offset).limit(limit);
			if(id > 0)
				query.field("id").equal(id);
			if(playerId > 0)
				query.field("playerId").equal(playerId);
			query.field("status").equal(status);
			
			return query.asList();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
	}

	public long getCount(long id, long playerId, String status){
		final Datastore ds = getDatastore();

		try {
			Query<PlayerCash> query = ds.find(PlayerCash.class);
			if(id > 0)
				query.field("id").equal(id);
			if(playerId > 0)
				query.field("playerId").equal(playerId);
			query.field("status").equal(status);
			
			return ds.getCount(query);
		} catch (Exception e) {
			log.error("get debt exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
	}
}
