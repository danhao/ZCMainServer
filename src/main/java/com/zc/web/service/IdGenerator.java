//package com.zc.web.service;
//
//import com.zc.web.dao.IdDao;
//
///**
// * ID生成器
// * @author Administrator
// *
// */
//public class IdGenerator {
//	
//	private static IdDao idDao = new IdDao();
//	
//	/**
//	 * 根据表名生成id
//	 * @param table
//	 * @return
//	 */
//	public synchronized static int generateId(String table){
//		int newMaxId = idDao.findAndModify(table);
//		return newMaxId;
//	}
//}
