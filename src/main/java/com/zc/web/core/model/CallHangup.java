package com.zc.web.core.model;
/**
 * <p>
 * Title: CallHangup
 * </p>
 * <p>
 * Description: 鉴权响应实体
 * </p>
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * <p>
 * Company: hisunsray
 * </p>
 * <p>
 * Date: 2013-07-09
 * </p>
 * 
 * @version 1.0
 */
public class CallHangup {

	private String type;
	private String orderId;
	private String subId;
	private String caller;
	private String called;
	private String byeType;
	private String starttime;
	private String endtime;
	private String billdata;
	private String callSid;
	private String recordurl;
	
	public String getRecordurl() {
		return recordurl;
	}

	public void setRecordurl(String recordurl) {
		this.recordurl = recordurl;
	}

	public String getCallSid() {
		return callSid;
	}

	public void setCallSid(String callSid) {
		this.callSid = callSid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getSubId() {
		return subId;
	}

	public void setSubId(String subId) {
		this.subId = subId;
	}

	public String getCaller() {
		return caller;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public String getCalled() {
		return called;
	}

	public void setCalled(String called) {
		this.called = called;
	}

	public String getByeType() {
		return byeType;
	}

	public void setByeType(String byeType) {
		this.byeType = byeType;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getBilldata() {
		return billdata;
	}

	public void setBilldata(String billdata) {
		this.billdata = billdata;
	}
	@Override
	public String toString(){
		return "CallHangup [type = "+type+", orderId = "+type+", subId = "+subId+", caller = "+caller+", called = "+called+", byeType = "+byeType+", starttime = "+starttime+", endtime = "+endtime+", billdata = "+billdata+", callSid = "+callSid+", recordurl = "+recordurl+"]";
	}
}
