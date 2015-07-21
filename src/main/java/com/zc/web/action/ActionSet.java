package com.zc.web.action;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ActionSet {
	private final String ACTION_FILE = "action.properties";
	public static ActionSet INSTANCE = new ActionSet();
	
	private Map<Integer, Action> actionMap = new HashMap<Integer, Action>(); 
	
	private ActionSet(){
		
	}
	
	/**
	 * 初始化所有的操作
	 * @throws Exception
	 */
	public void init() throws Exception{
		Properties properties = new Properties();
		InputStream inputStream = null;
		try{
			inputStream = ActionSet.class.getClassLoader().getResourceAsStream(ACTION_FILE);
			properties.load(inputStream);
			for(Object key : properties.keySet()){
				String keyCode = String.valueOf(key); 
				String clazz = properties.getProperty(keyCode);
				if (clazz == null) {
					throw new Exception("action load exception: key=" + keyCode + "");
				}
				try {
					Action action = (Action) Class.forName(clazz).newInstance();
					actionMap.put(Integer.valueOf(keyCode), action);
				} catch (Exception e) {
					throw new Exception("init class error:key=" + keyCode + ", class=" + clazz);
				}
			}
		}finally{
			if(inputStream != null){
				inputStream.close();
			}
		}
	}
	
	public Action getAction(int keyCode){
		return actionMap.get(keyCode);
	}
}