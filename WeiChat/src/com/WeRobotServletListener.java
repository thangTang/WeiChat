package com;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WeRobotServletListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		//测试使用
//		MsgManager.getMsgManager().testArticle();
		MsgManager.getMsgManager();
	}

}
