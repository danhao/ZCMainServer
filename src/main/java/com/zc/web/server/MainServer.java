package com.zc.web.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.zc.web.action.ActionSet;
import com.zc.web.config.ConfigHelper;
import com.zc.web.config.GlobalConfig;
import com.zc.web.config.GlobalConfig.ZoneConfig;
import com.zc.web.config.SqlMapperConfig;
import com.zc.web.core.IDGenerator;
import com.zc.web.dao.BaseDao;
import com.zc.web.service.DebtService;
import com.zc.web.task.CountDebtsTask;
import com.zc.web.task.SendMailThread;
import com.zc.web.task.SendSmsThread;
import com.zc.web.task.UploadThread;

public class MainServer {
	
	private static final Logger log = Logger.getLogger(MainServer.class);
	public static ZoneConfig ZONE = null;
	public static int SERVER_STATUS = 1; //server运行状态 1:正在运行中  2:正在停服中
	
	private static void initNetty() throws Exception{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        //初始化HTTP服务器
        ServerBootstrap httpBootstrap = new ServerBootstrap();
        httpBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new HttpServerInitializer());
        httpBootstrap.bind(new InetSocketAddress(ZONE.host, ZONE.httpPort)).sync().channel();
        log.info("http server started at host="+ZONE.host+" port=" + ZONE.httpPort + ".");
        
        //初始化管理接口服务器(HTTP服务器)
        ServerBootstrap adminBootstrap = new ServerBootstrap();
        adminBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new AdminServerInitializer());
        adminBootstrap.bind(new InetSocketAddress(ZONE.host, ZONE.adminPort)).sync().channel();
        log.info("admin server started at host="+ZONE.host+" port=" + ZONE.adminPort + ".");

        //初始化支付接口服务器(HTTP服务器)
        ServerBootstrap payBootstrap = new ServerBootstrap();
        payBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new PayServerInitializer());
        payBootstrap.bind(new InetSocketAddress(ZONE.host, ZONE.payPort)).sync().channel();
        log.info("pay server started at host="+ZONE.host+" port=" + ZONE.payPort + ".");

        //初始化外呼鉴权服务器(HTTP服务器)
        ServerBootstrap callBootstrap = new ServerBootstrap();
        callBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new CallServerInitializer());
        callBootstrap.bind(new InetSocketAddress(ZONE.host, ZONE.callPort)).sync().channel();
        log.info("call server started at host="+ZONE.host+" port=" + ZONE.callPort + ".");
	}
	
	private static void initThread() throws Exception{
		// 统计数量线程
		new CountDebtsTask().start();
		
		// 邮件线程
		SendMailThread.init();
		
		// 短信线程
		SendSmsThread.init();
		
		// 协议生成
		UploadThread.init();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args == null || args.length < 1){
			System.out.println("please set zone id.");
			return;
		}
		long startTime = System.currentTimeMillis();
		int zoneId = Integer.valueOf(args[0]);
		
		try {
			GlobalConfig.init();
			ZONE = GlobalConfig.getZoneConfig(zoneId);
			
			//初始化资源库
			SqlMapperConfig.initResSessionFactory();
			ConfigHelper.init();
			
			// 加载主键生成器
            IDGenerator.INSTANCE.init(zoneId);
            
			//初始化所有的操作
			ActionSet.INSTANCE.init();
			
			//初始化mongodb
			BaseDao.initMongo();
			
			//预加载债务
			DebtService.init();
			
			//初始化网络
			initNetty();
			
			//初始化线程
			initThread();
			
			log.info("start main server succ:zoneId="+zoneId + " useTime="+(System.currentTimeMillis() - startTime));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}