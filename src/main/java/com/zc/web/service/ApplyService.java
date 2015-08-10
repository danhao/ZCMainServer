package com.zc.web.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.zc.web.dao.DebtEndApplyDao;
import com.zc.web.data.model.DebtEndApply;
import com.zc.web.util.TimeUtil;

/***
 * 申请相关操作
 * 
 * @author small
 *
 */
public class ApplyService {

	private static final Logger log = Logger.getLogger(ApplyService.class);

	private static DebtEndApplyDao dao = new DebtEndApplyDao();

	public static void saveDebtEndApply(DebtEndApply playerApply) {
		dao.save(playerApply);
	}
	
	public static void createDebtEndApply(long playerId, long debtId){
		DebtEndApply dea = getDebtEndApply(debtId);
		if(dea == null){
			dea = new DebtEndApply();
		}

		dea.setId(debtId);
		dea.setPlayerId(playerId);
		dea.setCreateAt(TimeUtil.now());
		dea.setStatus(0);

		saveDebtEndApply(dea);
	}
	
	public static void updateDebtEndApply(long debtId, int status){
		DebtEndApply dea = getDebtEndApply(debtId);
		if(dea == null){
			return;
		}

		dea.setStatus(status);

		saveDebtEndApply(dea);
	}	
	
	public static DebtEndApply getDebtEndApply(long id){
		return dao.getDebtEndApply(id);
	}

	public static List<DebtEndApply> listDebtEndApply(int offset, int limit, long debtId, int status){
		return dao.listDebtEndApply(offset, limit, debtId, status);
	}
	
	public static DebtEndApplyDao getDao(){
		return dao;
	}
}
