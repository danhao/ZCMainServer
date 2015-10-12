package com.zc.web.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.zc.web.core.Constant;
import com.zc.web.data.model.Player;
import com.zc.web.exception.DBException;
import com.zc.web.util.MD5;

/**
 * 玩家数据操作
 * @author Administrator
 *
 */
public class PlayerDao extends BaseDao<Player> {
	
	private static final Logger log = Logger.getLogger(PlayerDao.class);
	
	/**
	 * 根据玩家id获取玩家基本数据
	 * @param id
	 * @return
	 */
	public Player getPlayer(final long id) {
		final Datastore ds = getDatastore();

		Player player = null;
		try {
			player = ds.get(Player.class, id);
		} catch (Exception e) {
			log.error("get player exception "+e.getMessage(), e);
			throw new DBException(e);
		} 
		return player;
	}
	
	public Player getPlayerByEmail(String email) {
		final Datastore ds = getDatastore();

		Player player = null;
		try {
			Query<Player> query = ds.find(Player.class);
			query.field("email").equal(email);
			List<Player> list = query.asList();
			if( list.size()>0 ){
				return list.get(0);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
		return player;
	}
	
	public Player getPlayerByEmail(String email, String passwd) {
		final Datastore ds = getDatastore();

		Player player = null;
		try {
			Query<Player> query = ds.find(Player.class);
			query.field("email").equal(email).field("passwd").equal(MD5.encode(passwd));
			List<Player> list = query.asList();
			if( list.size()>0 ){
				return list.get(0);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
		return player;
	}
	
	public Player getPlayerByMobile(String mobile) {
		final Datastore ds = getDatastore();

		Player player = null;
		try {
			Query<Player> query = ds.find(Player.class);
			query.field("mobile").equal(mobile);
			List<Player> list = query.asList();
			if( list.size()>0 ){
				return list.get(0);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
		return player;
	}
	
	public Player getPlayerByMobile(String mobile, String passwd) {
		final Datastore ds = getDatastore();

		Player player = null;
		try {
			Query<Player> query = ds.find(Player.class);
			query.field("mobile").equal(mobile).field("passwd").equal(MD5.encode(passwd));
			List<Player> list = query.asList();
			if( list.size()>0 ){
				return list.get(0);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
		return player;
	}
	
	public Player getPlayerByVoip(String voip) {
		final Datastore ds = getDatastore();

		Player player = null;
		try {
			Query<Player> query = ds.find(Player.class);
			query.field("voipId").equal(voip);
			List<Player> list = query.asList();
			if( list.size()>0 ){
				return list.get(0);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
		return player;
	}
	
	public List<Player> listPlayerForValidate(int offset, int limit, long id, int type) {
		final Datastore ds = getDatastore();

		try {
			Query<Player> query = ds.find(Player.class);
			query.offset(offset).limit(limit);
			if(id > 0)
				query.field("id").equal(id);
			else{
				if(type == 0)
					query.field("idValidating").equal(1);
				else if(type == 1)
					query.field("coValidating").equal(1);
			}
			
			return query.asList();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
	}
	
	public List<Player> listCoPlayers(int limit, int role) {
		final Datastore ds = getDatastore();

		try {
			Query<Player> query = ds.find(Player.class);
			query.limit(limit).order("-createTime");
			query.field("status").greaterThanOrEq(Constant.USER_CO_VALIDATED);
			query.field("role").equal(role);
			
			return query.asList();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DBException(e);
		} 
	}
	
}
