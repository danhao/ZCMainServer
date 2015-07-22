package com.zc.web.action.admin;

import java.util.List;
import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Player;
import com.zc.web.message.debt.ListDebtsReqProto.ListDebtsReq;
import com.zc.web.service.DebtService;

public class QueryDebtAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) {
		ListDebtsReq.Builder req = ListDebtsReq.newBuilder();
		req.setState(-1);
		if(ps != null){
			req.setPage(Integer.parseInt(ps[0]));
			req.setCreateTimeFrom(Integer.parseInt(ps[1]));
			req.setCreateTimeTo(Integer.parseInt(ps[2]));
			req.setState(Integer.parseInt(ps[3]));
			if(ps.length > 4)
				req.setId(ps[4]);
		}
		
		List<Debt> list = DebtService.searchDebts(req.build(), "-createTime");
		
		result.put("data", list);
		result.put("count", DebtService.TOTAL_DEBTS);
	}
}
