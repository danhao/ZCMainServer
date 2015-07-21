package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.util.FileUtil;

public class GetDownloadUrlAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player player, Map<String, Object> result) {
		result.put("url", FileUtil.genDownloadUrl(ps[0]));
	}
}
