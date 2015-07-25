package com.zc.web.action.admin;

import java.util.Map;

import com.zc.web.action.BaseAdminAction;
import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;
import com.zc.web.util.FileUtil;

public class QueryPlayerAction extends BaseAdminAction {

	@Override
	protected void done(String[] ps, Player p, Map<String, Object> result) {
		Player player = PlayerService.getPlayer(ps[0]);
		result.put("id", player.getId());
		result.put("name", player.getName());
		result.put("email", player.getEmail());
		result.put("mobile", player.getMobile());
		result.put("money", player.getMoney());
		result.put("type", player.getType());
		result.put("role", player.getRole());
		result.put("createTime", player.getCreateTime());
		result.put("loginTime", player.getLoginTime());
		result.put("lastLoginTime", player.getLastLoginTime());
		result.put("banAccountTime", player.getBanAccountTime());
		result.put("vip", player.getVip());
		result.put("userId", player.getUserId());		// 用户身份证/组织机构代码
		result.put("userName", player.getUserName());	// 用户真实姓名/企业名称
		result.put("artificialPerson", player.getArtificialPerson());	// 法人
		result.put("address", player.getAddress());		// 地址/注册地址
		result.put("idFile", player.getFileId() == null ? "":FileUtil.genDownloadUrl(player.getFileId().getId()));	// 用于认证的身份证文件
		result.put("organizationCodeFile", player.getFileOrganizationCode() == null ? "":FileUtil.genDownloadUrl(player.getFileOrganizationCode().getId()));	// 组织机构代码证文件
		result.put("businessLicence", player.getBusinessLicence());	// 营业执照号
		result.put("businessLicenceFile", player.getFileBusinessLicence() == null ? "":FileUtil.genDownloadUrl(player.getFileBusinessLicence().getId()));	// 营业执照文件
		result.put("taxNumber", player.getTaxNumber());	// 税务登记号
		result.put("taxNumberFile", player.getFileTaxNumber() == null ? "":FileUtil.genDownloadUrl(player.getFileTaxNumber().getId()));	// 税务登记证文件
		result.put("accountPermit", player.getAccountPermit());	// 开户许可核准号
		result.put("accountPermitFile", player.getFileAccountPermit() == null ? "":FileUtil.genDownloadUrl(player.getFileAccountPermit().getId()));	// 开户许可证
		result.put("registeredCapital", player.getRegisteredCapital());	// 注册资金
		result.put("reqisteredType", player.getReqisteredType());		// 注册类型
		result.put("foundTime", player.getFoundTime());	// 成立时间
		result.put("businessScope", player.getBusinessScope());	// 经营范围
		result.put("businessAddress", player.getBusinessAddress());	// 经营地址
	}
}
