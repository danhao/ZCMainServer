package com.zc.web.task;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zc.web.util.SendEmail;

/**
 *
 */
public class SendMailThread extends Thread {
	public static SendMailThread inst = new SendMailThread();
	private static final Logger log = LoggerFactory.getLogger(SendMailThread.class);
	private LinkedBlockingQueue<MailInfo> operaQueue = new LinkedBlockingQueue<MailInfo>();

	public static void init(){
		if(inst == null){
			inst = new SendMailThread();
		}
		inst.start();
	}
	
	@Data
	class MailInfo {
		private String toEmail;
		private String title;
		private String content;
	}
	
	/**
	 * 外部调用
	 * @throws InterruptedException 
	 */
	public void addSyncInfo(String toEmail, String title, String content) throws InterruptedException {
		MailInfo info = new MailInfo();
		info.setToEmail(toEmail);
		info.setTitle(title);
		info.setContent(content);
		operaQueue.put(info);
		if(operaQueue.size()>100){
			log.warn("SyncMail queue warning! size="+operaQueue.size());
		}
	}
	
	private void exec(MailInfo info) {
		SendEmail.send(info.getToEmail(), info.getTitle(), info.getContent());
	}

	public void run() {
		while (true) {
			try{
				MailInfo op = operaQueue.poll(500, TimeUnit.MILLISECONDS);
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
