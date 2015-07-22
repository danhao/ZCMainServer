package com.zc.web.action.player;

import java.util.UUID;

import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.action.PBBaseAction;
import com.zc.web.core.PBRequestSession;
import com.zc.web.data.model.Player;
import com.zc.web.exception.SmallException;
import com.zc.web.message.ErrorCodeProto.ErrorCode;
import com.zc.web.message.ErrorMessage;
import com.zc.web.message.PBMessage;
import com.zc.web.message.player.LoginReqProto.LoginReq;
import com.zc.web.service.PlayerService;
import com.zc.web.util.TimeUtil;

public class LoginAction extends PBBaseAction{

	@Override
	public void done(PBRequestSession reqSession, PBMessage request, PBMessage response) throws Exception {
		LoginReq.Builder builder = LoginReq.newBuilder();
		JsonFormat.merge(request.getReq(), builder);
		LoginReq req = builder.build();
		String email = req.getEmail();
		String mobile = req.getMobile();
		String passwd = req.getPasswd();
		
		Player player = PlayerService.login(email, mobile, passwd);
		if (player == null) {
			throw new SmallException(ErrorCode.ERR_NO_PLAYER_VALUE);
		}else{
			//修改最后登陆时间
			player.setLastLoginTime(player.getLoginTime());
		}
		
		//账号检测
		ErrorMessage errMsg = checkAccount(player);
		if(errMsg != null){
			sendErrorMsg(reqSession, errMsg);
			return;
		}
		
		player.setSid(UUID.randomUUID().toString());
		player.setLoginTime(TimeUtil.now());
		//设置玩家日登录数据
		PlayerService.savePlayer(player);
		reqSession.setPlayer(player);
		//设置日志信息
		initLogData(reqSession);
		
		//调用一下access方法,重置一些必须数据
		access(reqSession);
		
		response.setRsp(player.build());
	}
}
