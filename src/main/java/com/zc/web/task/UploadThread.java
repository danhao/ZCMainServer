package com.zc.web.task;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zc.web.data.model.Debt;
import com.zc.web.util.FileUtil;
import com.zc.web.util.PdfUtil;
import com.zc.web.util.StringUtil;

/**
 *
 */
public class UploadThread extends Thread {
	public static UploadThread inst = new UploadThread();
	private static final Logger log = LoggerFactory.getLogger(UploadThread.class);
	private LinkedBlockingQueue<Debt> operaQueue = new LinkedBlockingQueue<Debt>();
	
	private static String template = null;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private static final PdfUtil PDF = new PdfUtil();

	public static void init() throws Exception{
		if(inst == null){
			inst = new UploadThread();
		}
		inst.start();
		
		InputStream is = PdfUtil.class.getResourceAsStream("/template1.txt");
	    BufferedReader br = new BufferedReader(new InputStreamReader(is));  
  
	    StringBuffer sb = new StringBuffer();
        for (String line = br.readLine(); line != null; line = br.readLine()) {  
            sb.append(line);
            sb.append("\n");
        }  
        br.close();  

        template = sb.toString();
	}
	
	/**
	 * 外部调用
	 * @throws InterruptedException 
	 */
	public void addSyncInfo(Debt debt) throws InterruptedException {
		operaQueue.put(debt);
		if(operaQueue.size()>100){
			log.warn("SyncMail queue warning! size="+operaQueue.size());
		}
	}
	
	private void exec(Debt debt) throws Exception{
		String content = template
				.replaceFirst("##1", debt.getCreditorName())
				.replaceFirst("##2", debt.getWinnerName())
				.replaceFirst("##4", StringUtil.digitUppercase(debt.getMoney() / 100f))
				.replaceFirst("##5", debt.getMoney() / 100f + "元")
				.replaceFirst("##6", debt.getDebtorName())
				.replaceFirst("##7", debt.getDebtorAddr())
				.replaceFirst("##9", debt.getDebtorPhone())
				.replaceFirst("##10", debt.getRate() + "%")
				.replaceFirst("##11", debt.getCreditorName())
				.replaceFirst("##12", debt.getWinnerName())
				.replaceFirst("##13", sdf.format(Calendar.getInstance().getTime()));
		
		FileUtil.uploadFile("contract/" + debt.getId() + ".pdf", PDF.writePdf("债务委托协议", content, null));
	}

	public void run() {
		while (true) {
			try{
				Debt op = operaQueue.poll(500, TimeUnit.MILLISECONDS);
				while (op != null){
					exec(op);
					op = operaQueue.poll();
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("ERROR:",e);
			}
		}
	}
}
