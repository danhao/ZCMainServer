package com.zc.web.task;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zc.web.util.SendSms;

/**
 *
 */
public class SendSmsThread extends Thread {
	public static SendSmsThread inst = new SendSmsThread();
	private static final Logger log = LoggerFactory.getLogger(SendSmsThread.class);
	private LinkedBlockingQueue<SmsInfo> operaQueue = new LinkedBlockingQueue<SmsInfo>();

	public static void init(){
		if(inst == null){
			inst = new SendSmsThread();
		}
		inst.start();
	}
	
	@Data
	class SmsInfo {
		private String mobile;
		private String content;
	}
	
	/**
	 * 外部调用
	 * @throws InterruptedException 
	 */
	public void addSyncInfo(String mobile, String content) throws InterruptedException {
		if(mobile == null || mobile.isEmpty())
			return;
		
		SmsInfo info = new SmsInfo();
		info.setMobile(mobile);
		info.setContent(content);
		operaQueue.put(info);
		if(operaQueue.size()>100){
			log.warn("SyncMail queue warning! size="+operaQueue.size());
		}
	}
	
	private void exec(SmsInfo info) {
		try{
			SendSms.send(info.getContent(), info.getMobile());
		}catch(Exception e){
			log.error("error:", e);
		}
	}

	public void run() {
		while (true) {
			try{
				SmsInfo op = operaQueue.poll(500, TimeUnit.MILLISECONDS);
				while (op != null){
					exec(op);
					op = operaQueue.poll();
				}
			} catch (Exception e) {
				log.error("ERROR:",e);
			}
		}
	}
}
