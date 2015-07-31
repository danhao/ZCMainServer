package com.zc.web.dao;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.logging.MorphiaLoggerFactory;
import com.google.code.morphia.logging.slf4j.SLF4JLogrImplFactory;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.zc.web.config.GlobalConfig.MongoConfig;
import com.zc.web.data.model.BaseModel;
import com.zc.web.exception.DBException;
import com.zc.web.server.MainServer;

public abstract class BaseDao<T extends BaseModel> {
	
	private static final Logger log = Logger.getLogger(BaseDao.class);
	private static Mongo mongo = null;
	private static Morphia morphia = null;
	
	private static Mongo globalMongo = null;
	private static Morphia globalMorphia = null;
	
	/**
	 * 初始化Mongo实例
	 * 
	 * @throws Exception
	 */
	public static void initMongo() throws Exception {
		MongoConfig config = MainServer.ZONE.mongoConfig;
		mongo = createMongo(config);
		if( mongo == null ) {
			throw new Exception("init mongo db exception, dbName="+config.dbName);
		}
		
		MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory.class);
		morphia = new Morphia();
	}
	
	/**
	 * 创建Mongo实例
	 * 
	 * @param host
	 * @param port
	 * @param dbName
	 * @return
	 */
	private static Mongo createMongo(MongoConfig config) {
		try {
			MongoOptions options = new MongoOptions();
			options.autoConnectRetry = true;
			
			Mongo mongo = null;
			if(config.slaveHost != null){
				List<ServerAddress> replset = new ArrayList<ServerAddress>();
				replset.add(new ServerAddress(config.masterHost, config.masterPort));
				replset.add(new ServerAddress(config.slaveHost, config.slavePort));
				mongo = new Mongo(replset, options);
				mongo.slaveOk();
				DB db = mongo.getDB("admin");
				db.authenticate(config.userName, config.password.toCharArray());
			}else{
				mongo = new Mongo(new DBAddress(config.masterHost, config.masterPort, config.dbName), options);
			}
			log.debug("new mongo:{" + mongo.hashCode() + "}|{" + mongo.debugString() + "}");
			return mongo;
		} catch (UnknownHostException ex) {
			log.error("UnknownHostException:" + ex.getMessage());
		} catch (MongoException ex) {
			log.error("Mongo Exception:", ex);
		}
		return null;
	}
	
	/**
	 * 获取数据源
	 * @return
	 */
	public Datastore getDatastore() {
		//TODO datasource不能缓存?
		return morphia.createDatastore(mongo, MainServer.ZONE.mongoConfig.dbName);
	}

	/**
	 * 保存
	 * 
	 * @param model
	 */
	public void save(T model) {
		final Datastore ds = getDatastore();
		try {
			ds.save(model);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new DBException(e);
		}
	}
	
	/**
	 * 更新
	 * 
	 * @param model
	 * @param query
	 * @param ops
	 */
	public void update(Query<T> query, UpdateOperations<T> ops) {
		final Datastore ds = getDatastore();
		try {
			ds.update(query, ops);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}	
	
	/**
	 * 删除
	 * 
	 * @param object
	 */
	public void delete(Query<T> query) {
		final Datastore ds = getDatastore();
		try {
			ds.delete(query);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}	
}
