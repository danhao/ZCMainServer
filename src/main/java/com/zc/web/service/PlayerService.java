package com.zc.web.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import com.zc.web.cache.PlayerCache;
import com.zc.web.core.Constant;
import com.zc.web.core.IDGenerator;
import com.zc.web.dao.MoneyHistoryDao;
import com.zc.web.dao.PlayerDao;
import com.zc.web.data.model.Debt;
import com.zc.web.data.model.Debt.Bidder;
import com.zc.web.data.model.File;
import com.zc.web.data.model.MoneyHistory;
import com.zc.web.data.model.Player;
import com.zc.web.data.model.Player.Situation;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.ErrorMsgProto.ErrorMsg;
import com.zc.web.message.common.FileMsgProto.FileMsg;
import com.zc.web.message.player.PlayerMsgProto.PlayerMsg;
import com.zc.web.message.player.UpdateReqProto.UpdateReq;
import com.zc.web.server.MainServer;
import com.zc.web.task.SendMailThread;
import com.zc.web.task.SendSmsThread;
import com.zc.web.util.LogUtil;
import com.zc.web.util.MD5;
import com.zc.web.util.MessageUtil;
import com.zc.web.util.StringUtil;
import com.zc.web.util.TimeUtil;

public class PlayerService {

	private static Logger logger = Logger.getLogger(PlayerService.class);

	private static PlayerDao playerDao = new PlayerDao();
	private static MoneyHistoryDao moneyHistoryDao = new MoneyHistoryDao();

	private static final int SIZE = 10;
	
	/**
	 * 通过id从DB加载玩家基本数据
	 * 
	 * @param id
	 * @return
	 */
	public static Player loadPlayerById(long id) {
		return playerDao.getPlayer(id);
	}

	public static List<Player> listPlayerForValidating(long id, int type){
		return playerDao.listPlayerForValidate(0, SIZE, id, type);
	}

	public static List<Player> listCoPlayers(int role){
		return playerDao.listCoPlayers(SIZE, role);
	}

	/**
	 * 保存玩家数据到DB中
	 * 
	 * @param player
	 */
	public static void savePlayer(Player player) {
		playerDao.save(player);
	}

	/**
	 * 通过openId进行登录
	 * 
	 * @param openId
	 * @return
	 * @throws SmallException
	 */
	public static Player login(String email, String mobile, String passwd) throws SmallException {
		Player player = null;
		if(email != null && !email.equals(""))
			player = playerDao.getPlayerByEmail(email, passwd);
		else if(mobile != null && !mobile.equals(""))
			player = playerDao.getPlayerByMobile(mobile, passwd);
		
		//从DB加载不到玩家数据
		if (player == null) {
			return null;
		}
		
		PlayerCache.INSTANCE.getPlayerSet().put(player.getId(), player);
		LogUtil.logLogin(player.getId(), player.getName());
		
		return player;
	}

	/**
	 * 通过玩家id登录
	 * 
	 * @param playerId
	 * @return
	 */
	public static Player login(long playerId) {

		Player player = PlayerCache.INSTANCE.getPlayer(playerId);

		if (player != null) {
			// 记录登录日志
			LogUtil.logLogin(player.getId(), player.getName());
		}
		return player;
	}

	/**
	 * 创建玩家
	 * 
	 * @param openId
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public static Player createPlayer(PlayerMsg msg) throws Exception {
		validatePlayer(msg.getEmail(), msg.getMobile());
		
		int now = TimeUtil.now();
		Player newPlayer = new Player();
		
		PropertyUtils.copyProperties(newPlayer, msg);
		
		newPlayer.setId(IDGenerator.INSTANCE.nextId());
		newPlayer.setPasswd(MD5.encode(msg.getPasswd()));
		newPlayer.setCreateTime(now);
		newPlayer.setLoginTime(now);
		newPlayer.setSid(UUID.randomUUID().toString());
		savePlayer(newPlayer);

		// 加入到在线列表中
		PlayerCache.INSTANCE.getPlayerSet().put(newPlayer.getId(), newPlayer);

		// 记录注册日志
		LogUtil.logCreatePlayer(newPlayer.getId(), newPlayer.getName());
		
		// 发送验证
		if(newPlayer.getEmail() != null && !newPlayer.getEmail().isEmpty())
			validateEmail(newPlayer);
		
		return newPlayer;
	}

	/**
	 * 添加游戏币
	 * 
	 * @param player
	 * @param money
	 */
	public static void addMoney(Player player, int addMoney, int type, int platform, String... extraMsg) {
		addMoney(player, addMoney, type, platform, 0, extraMsg);
	}
	public static void addMoney(Player player, int addMoney, int type, int platform, long debtId, String... extraMsg) {
		if (addMoney <= 0) {
			return;
		}
		int money = player.getMoney();
		if ((Integer.MAX_VALUE - money) < addMoney) {
			logger.error("money exceed max value. playerId=" + player.getId());
			return;
		}

		player.setMoney(money + addMoney);
		if (player.getMoney() < 0) {
			player.setMoney(0);
		}

		// 保存DB
		savePlayer(player);
		
		LogUtil.logAddMoney(player.getId(), addMoney, money, player.getMoney(), extraMsg);
		
		createMoneyHistory(player, addMoney, type, platform, Constant.MONEY_STATE_SUCC, debtId, (extraMsg != null && extraMsg.length > 0)?extraMsg[0]:"");
	}
	
	/**
	 * 修改信用评分
	 * 
	 * @param player
	 * @param num
	 */
	public static void addRating(Player player, int num, String... extraMsg){
		if (num == 0) {
			return;
		}
		int rating = player.getRating();
		if ((Integer.MAX_VALUE - rating) < num) {
			logger.error("rating exceed max value. playerId=" + player.getId());
			return;
		}

		player.setRating(rating + num);
		if (player.getRating() < 0) {
			player.setRating(0);
		}

		// 保存DB
		savePlayer(player);
		
		LogUtil.logAddRating(player.getId(), num, rating, player.getRating(), extraMsg);
	}

	/**
	 * 检查游戏币够不够
	 * 
	 * @param player
	 * @param money
	 * @return
	 */
	public static ErrorMsg checkMoney(Player player, int money) {
		if (player.getMoney() < money) {
			return MessageUtil.buildPBErrorMsg(ErrorCode.ERR_NO_MONEY_VALUE);
		}
		return null;
	}


	/**
	 * 消耗游戏币
	 * 
	 * @param player
	 * @param money
	 */
	public static void consumeMoney(Player player, int money, int type, int platform, String... extLog) throws Exception{
		consumeMoney(player, money, type, platform, 0, extLog);
	}
	public static void consumeMoney(Player player, int money, int type, int platform, long debtId, String... extLog) throws Exception{
		if (player.getMoney() < money) {
			logger.error("cost_money|error|" + player.getId() + "|"
					+ player.getMoney() + "|" + money);
			throw new SmallException(ErrorCode.ERR_NO_MONEY);
		}
		if (money == 0) {
			return;
		}

		int oldMoney = player.getMoney();
		player.setMoney(player.getMoney() - money);

		// 保存到DB
		savePlayer(player);

		// 记录消耗
		LogUtil.logReduceMoney(player.getId(), money, oldMoney,
				player.getMoney(), extLog);
		
		createMoneyHistory(player, money, type, platform, Constant.MONEY_STATE_SUCC, debtId, extLog != null && extLog.length > 0?extLog[0]:"");
	}
	
	/**
	 * 验证
	 * 
	 * @param email
	 * @param mobile
	 * @throws Exception
	 */
	public static void validatePlayer(String email, String mobile) throws Exception {
		if(email != null && !email.isEmpty()){
			if(playerDao.getPlayerByEmail(email) != null)
				throw new SmallException(ErrorCode.ERR_EMAIL_EXIST_VALUE);
		}else if(mobile != null && !mobile.isEmpty()){
			if(playerDao.getPlayerByMobile(mobile) != null)
				throw new SmallException(ErrorCode.ERR_MOBILE_EXIST_VALUE);
		}
	}
	
	/**
	 * 验证email
	 * 
	 * @param player
	 * @throws Exception
	 */
	public static void validateEmail(Player player) throws Exception{
		if(player.getEmail() == null)
			return;
		
		player.setEmailValidateCode(Hex.encodeHexString(MD5.encode(player.getEmail()).getBytes()));
		savePlayer(player);
		
        ///邮件的内容
		String url = "http://" + MainServer.ZONE.hostOut + ":" + MainServer.ZONE.httpPort + "/req?" + 
				player.getId() + "_" + player.getEmailValidateCode();
		
        StringBuffer sb=new StringBuffer("点击下面链接激活账号！");
        sb.append("<a href=\"");
        sb.append(url);
        sb.append("\">马上激活</a><br>");
        sb.append("<a href=\"");
        sb.append(url);
        sb.append("\">");
        sb.append(url);
        sb.append("</a><br>");

        //发送邮件
        SendMailThread.inst.addSyncInfo(player.getEmail(), "点点债平台账号激活", sb.toString());
	}
	
	/**
	 * 验证处理
	 * 
	 * @param id
	 * @param code
	 * @return
	 */
	public static String processValidateEmail(long id, String code){
		Player player = PlayerCache.INSTANCE.getPlayer(id);
		String ret = null;
		if(player != null && player.getEmailValidateCode().equals(code)){
			player.setStatus(player.getStatus() | Constant.USER_EMAIL_VALIDATED);
			player.setEmailValidateCode(null);
			savePlayer(player);
			ret = "邮箱已验证！";
		}else
			ret = "验证失败!";
		
		return "<html><meta charset='utf-8'><body>" + ret + "</body></html>";
	}
	
	/**
	 * 更新用户信息
	 * 
	 * @param player
	 * @param req
	 * @throws Exception
	 */
	public static void updatePlayer(Player player, UpdateReq req) throws Exception {
		// 改密码
		if(req.getOldPasswd() != null && req.getNewPasswd() != null &&
				!req.getOldPasswd().isEmpty() && !req.getNewPasswd().isEmpty()){
			String passwd = MD5.encode(req.getOldPasswd());
			if(!player.getPasswd().equals(passwd)){
				throw new SmallException(ErrorCode.ERR_PASSWD_NOT_SAME);
			}
			
			player.setPasswd(MD5.encode(req.getNewPasswd()));
		}
		
		// 改email
		if(!req.getEmail().isEmpty()){
			if(playerDao.getPlayerByEmail(req.getEmail()) != null){
				throw new SmallException(ErrorCode.ERR_EMAIL_EXIST);
			}
			
			player.setEmail(req.getEmail());
		}
		
		// 改手机号
		if(!req.getMobile().isEmpty()){
			if(playerDao.getPlayerByMobile(req.getMobile()) != null){
				throw new SmallException(ErrorCode.ERR_MOBILE_EXIST);
			}
			
			player.setMobile(req.getMobile());
		}
		
		if(req.hasGender()) player.setGender(req.getGender());
		if(req.hasUserId() && !req.getUserId().isEmpty()) player.setUserId(req.getUserId());
		if(req.hasUserName() && !req.getUserName().isEmpty()) player.setUserName(req.getUserName());
		if(req.hasCompanyName() && !req.getCompanyName().isEmpty()) player.setCompanyName(req.getCompanyName());
		if(req.hasArtificialPerson() && !req.getArtificialPerson().isEmpty()) player.setArtificialPerson(req.getArtificialPerson());
		if(req.hasAddress()) player.setAddress(req.getAddress());
		if(req.hasCompanyAddress()) player.setCompanyAddress(req.getCompanyAddress());
		if(req.hasIdFile()) player.setFileId(createFile(req.getIdFile()));
		if(req.hasOrganizationCodeFile()) player.setFileOrganizationCode(createFile(req.getOrganizationCodeFile()));
		if(req.hasBusinessLicence()) player.setBusinessLicence(req.getBusinessLicence());
		if(req.hasBusinessLicenceFile()) player.setFileBusinessLicence(createFile(req.getBusinessLicenceFile()));
		if(req.hasTaxNumber()) player.setTaxNumber(req.getTaxNumber());
		if(req.hasTaxNumberFile()) player.setFileTaxNumber(createFile(req.getTaxNumberFile()));
		if(req.hasAccountPermit()) player.setAccountPermit(req.getAccountPermit());
		if(req.hasAccountPermitFile()) player.setFileAccountPermit(createFile(req.getAccountPermitFile()));
		if(req.hasCreditFile()) player.setFileCredit(createFile(req.getCreditFile()));
		if(req.hasNoneCrimeFile()) player.setFileNoneCrime(createFile(req.getNoneCrimeFile()));
		if(req.hasRegisteredCapital()) player.setRegisteredCapital(req.getRegisteredCapital());
		if(req.hasReqisteredType()) player.setReqisteredType(req.getReqisteredType());
		if(req.hasFoundTime()) player.setFoundTime(req.getFoundTime());
		if(req.hasBusinessScope()) player.setBusinessScope(req.getBusinessScope());
		if(req.hasBusinessAddress()) player.setBusinessAddress(req.getBusinessAddress());
		if(req.hasIdValidating()) player.setIdValidating(req.getIdValidating());
		if(req.hasCoValidating()) player.setCoValidating(req.getCoValidating());
		if(req.hasHead()) player.setHead(req.getHead());
		if(req.hasFiveInOne()) player.setFiveInOne(req.getFiveInOne());
		if(req.hasDescript()) player.setDescript(req.getDescript());
				
		
		savePlayer(player);
	}	
	
	/**
	 * 发送激活码
	 * 
	 * @param player
	 * @throws Exception
	 */
	public static void validateMobile(Player player) throws Exception{
		if(player.getMobile() == null)
			return;
		
		int code = (int)((Math.random()*9+1)*100000);
		
		player.setMobileValidateCode(code);
		savePlayer(player);
		
		SendSmsThread.inst.addSyncInfo(player.getMobile(), "您的验证码是：" + code + "。请不要把验证码泄露给其他人。");
	}
	
	/**
	 * 验证激活码
	 * 
	 * @param player
	 * @param code
	 * @throws Exception
	 */
	public static void processValidateMobile(Player player, int code) throws Exception{
		if(player.getMobileValidateCode() == code){
			player.setStatus(player.getStatus() | Constant.USER_MOBILE_VALIDATED);
			player.setMobileValidateCode(0);
			savePlayer(player);
			return;
		}
		
		throw new SmallException(ErrorCode.ERR_MOBILE_FAILED);
	}
	
	/**
	 * 重置第一步
	 * 
	 * @param player
	 * @param email
	 * @param mobile
	 * @throws Exception
	 */
	public static void changePwdOne(String email, String mobile) throws Exception{
		if(email.isEmpty() && mobile.isEmpty())
			return;

		int code = (int)((Math.random()*9+1)*100000);
		
		Player player = null;
		if(!email.isEmpty()){
			player = playerDao.getPlayerByEmail(email);
			if(player == null)
				throw new SmallException(ErrorCode.ERR_NO_PLAYER);
			
			player = PlayerCache.INSTANCE.getPlayer(player.getId());
			player.setEmailValidateCode(String.valueOf(code));
			
	        ///邮件的内容
	        StringBuffer sb=new StringBuffer("您正在重置密码，请在验证码输入框中输入<br>");
	        sb.append(code);
	        sb.append("<br>以完成操作");
	
	        //发送邮件
	        SendMailThread.inst.addSyncInfo(player.getEmail(), "点点债会员邮箱验证", sb.toString());
		}else{
			player = playerDao.getPlayerByMobile(mobile);
			if(player == null)
				throw new SmallException(ErrorCode.ERR_NO_PLAYER);
			
			player = PlayerCache.INSTANCE.getPlayer(player.getId());
			player.setMobileValidateCode(code);
			
			SendSmsThread.inst.addSyncInfo(player.getMobile(), "您的验证码是：" + code + "。请不要把验证码泄露给其他人。");
		}
		
		savePlayer(player);
	}
	
	/**
	 * 重置第二步
	 * 
	 * @param player
	 * @param email
	 * @param mobile
	 * @param code
	 * @throws Exception
	 */
	public static Player changePwdTwo(String email, String mobile, int code) throws Exception{
		if(email.isEmpty() && mobile.isEmpty())
			return null;
		
		Player player = null;
		if(!email.isEmpty()){
			player = playerDao.getPlayerByEmail(email);
			if(player == null)
				throw new SmallException(ErrorCode.ERR_NO_PLAYER);
			
			player = PlayerCache.INSTANCE.getPlayer(player.getId());
			if(code != Integer.parseInt(player.getEmailValidateCode())){
				throw new SmallException(ErrorCode.ERR_CODE_FAILED);
			}
		}else{
			player = playerDao.getPlayerByMobile(mobile);
			if(player == null)
				throw new SmallException(ErrorCode.ERR_NO_PLAYER);
			
			player = PlayerCache.INSTANCE.getPlayer(player.getId());

			if(code != player.getMobileValidateCode()){
				throw new SmallException(ErrorCode.ERR_CODE_FAILED);
			}
		}
		
		savePlayer(player);
		
		return player;
	}
	
	/**
	 * 重置密码
	 * 
	 * @param player
	 * @param passwd
	 * @throws Exception
	 */
	public static void changePwdThree(Player player, String passwd) throws Exception{
		if(passwd == null || passwd.isEmpty())
			return;
		
		player.setEmailValidateCode("");
		player.setMobileValidateCode(0);
		player.setPasswd(MD5.encode(passwd));
		savePlayer(player);
	}
	
	/**
	 * 合法用户
	 * 
	 * @param player
	 * @throws SmallException
	 */
	public static void isValidate(Player player) throws SmallException{
		if(!((player.getStatus() & Constant.USER_ID_VALIDATED) == Constant.USER_ID_VALIDATED ||
				(player.getStatus() & Constant.USER_CO_VALIDATED) == Constant.USER_CO_VALIDATED)){
			throw new SmallException(ErrorCode.ERR_AUTHORIZED_FAILED);
		}
	}
	
	/**
	 * 增加动态
	 * 
	 * @param player
	 * @param type
	 * @param content
	 */
	public static void addSituation(Player player, int type, String... data){
		Situation situation = new Situation();
		situation.setId(IDGenerator.INSTANCE.nextId());
		situation.setType(type);
		situation.setTime(TimeUtil.now());
		situation.setData(Arrays.asList(data));

		player.getSituations().add(situation);
		if(player.getSituations().size() > 5)
			player.getSituations().remove(0);
		
		savePlayer(player);
	}
	
	/**
	 * 是否能够接企业单
	 * 
	 * @param player
	 * @return
	 */
	public static boolean checkUserForCorp(Player player){
		if(player.getVip() > 0 || 
				(
				((player.getStatus() & Constant.USER_ID_VALIDATED) == Constant.USER_ID_VALIDATED ||
				(player.getStatus() & Constant.USER_CO_VALIDATED) == Constant.USER_CO_VALIDATED) 
				&&
				player.getFileNoneCrime() != null && !player.getFileNoneCrime().getId().isEmpty() 
						&& player.getFileCredit() != null && !player.getFileCredit().getId().isEmpty()))
			return true;
		
		return false;
	}
	
	/**
	 * 获取用户
	 * 
	 * @param value
	 * @return
	 */
	public static Player getPlayer(String value){
		if(StringUtil.isNumeric(value)){
			return playerDao.getPlayerByMobile(value);
		}else
			return playerDao.getPlayerByEmail(value);
	}
	
	/**
	 * 资金历史
	 * 
	 * @param player
	 * @param money
	 * @param type
	 * @param platform
	 * @param state
	 * @param descript
	 */
	private static void createMoneyHistory(Player player, int money, int type, int platform, 
			int state, long debtId, String descript){
		MoneyHistory history = new MoneyHistory();
		history.setId(IDGenerator.INSTANCE.nextId());
		history.setPlayerId(player.getId());
		history.setMoney(money);
		history.setType(type);
		history.setPlatform(platform);
		history.setTime(TimeUtil.now());
		history.setState(state);
		history.setBalance(player.getMoney());
		if(debtId > 0)
			history.setDebtId(String.valueOf(debtId));
		history.setDescript(descript);
		
		moneyHistoryDao.save(history);
	}
	
	private static File createFile(FileMsg fileMsg) throws Exception{
		File file = new File();
		PropertyUtils.copyProperties(file, fileMsg);
		
		return file;
	}

	public static MoneyHistoryDao getMoneyHistoryDao() {
		return moneyHistoryDao;
	}
	
	/**
	 * 更新发布人统计
	 * 
	 * @param playerId
	 * @param oldState
	 * @param newState
	 */
	public static void updateCreditorPath(long playerId, int oldState, int newState){
		Player player = PlayerCache.INSTANCE.getPlayer(playerId);
		
		if(oldState == newState){
			player.getPathCreditor()[newState] ++;
		}else{
			if(oldState > -1 && player.getPathCreditor()[oldState] > 0)
				player.getPathCreditor()[oldState] --;
			
			if(newState > -1)
				player.getPathCreditor()[newState] ++;
		}
		savePlayer(player);
	}
	
	/**
	 * 更新催债人统计
	 * 
	 * @param playerId
	 * @param oldState
	 * @param newState
	 */
	public static void updateDeputyPath(Player player, int oldState, int newState){
		if(oldState == newState){
			player.getPathDeputy()[newState] ++;
		}else{
			if(oldState > -1 && player.getPathDeputy()[oldState] > 0)
				player.getPathDeputy()[oldState] --;
			
			if(newState > -1)
				player.getPathDeputy()[newState] ++;
		}
		savePlayer(player);
	}
	
	/**
	 * 结束批量更新状态
	 * 
	 * @param debt
	 */
	public static void updateDeputyPathByDebt(Debt debt){
		for(long id : debt.getBondBidders()){
			Player player = PlayerCache.INSTANCE.getPlayer(id);
			if(id == debt.getWinnerId()){
				updateDeputyPath(player, Constant.DEPUTY_STATE_WIN, Constant.DEPUTY_STATE_DONE);
			}else{
				updateDeputyPath(player, Constant.DEPUTY_STATE_LOSE, Constant.DEPUTY_STATE_CLOSE);
			}
		}
	}
	
	public static Player getPlayerByVoip(String voip){
		Player player = playerDao.getPlayerByVoip(voip);
		if(player != null)
			return PlayerCache.INSTANCE.getPlayer(player.getId());
		
		return null;
	}
}
