package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.DebtService;

/**
 * 自动处理过期单
 *
 */
public class AutoOldDebtAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) throws Exception{
		StringBuffer sb = new StringBuffer();
		for(String s : ps){
			try{
				DebtService.getDebtById(Long.parseLong(s));
			}catch(Exception e){
				sb.append(s);
				sb.append(",");
			}
		}
		
		result.put("failIds", sb.toString());
	}

}
