package com.zc.web.task;

import java.util.Timer;
import java.util.TimerTask;

import com.zc.web.service.DebtService;

/**
 * 日志定时器
 */
public class CountDebtsTask {

	private Timer timer = new Timer();

	public void start() {
		timer.schedule(new TimerTask() {
			public void run() {
				DebtService.getCount();
			}
		}, 1 * 10 * 1000, 10 * 60 * 1000);
	}

}
