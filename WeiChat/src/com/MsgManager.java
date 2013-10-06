package com;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.bean.MenuBean;
import com.bean.MessageBean;

public class MsgManager {
	private static MsgManager manager;
	
	private String url = "jdbc:mysql://42.96.142.222:9999/we_robot";
	private String user = "root";
	private String pwd = "123456";
	
	private Map<Integer, MenuBean> menuMap = new HashMap<Integer, MenuBean>();
	private Map<Integer, ArrayList<MessageBean>> messageMap = new HashMap<Integer, ArrayList<MessageBean>>();
	private static final String RESPONSE_TXT = "<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%s</CreateTime><MsgType><![CDATA[%s]]></MsgType><Content><![CDATA[%s]]></Content><FuncFlag>0</FuncFlag></xml>";
	private static final String RESPONSE_ARTICLES = "<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%s</CreateTime><MsgType><![CDATA[%s]]></MsgType><ArticleCount>%s</ArticleCount><Articles>%s</Articles></xml>";
	
	public static MsgManager getMsgManager(){
		if(null == manager)
			manager = new MsgManager();
		return manager;
	}
	
	private MsgManager(){
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url,user, pwd);
			Statement stmt = conn.createStatement();
		    ResultSet menuRs = stmt.executeQuery("select * from werobot_menu");
		    while(menuRs.next()){
		    	MenuBean menuBean = new MenuBean();
		    	menuBean.setMenuId(menuRs.getInt("menu_id"));
		    	menuBean.setMenuName(menuRs.getString("menu_name"));
		    	menuBean.setMessageType(menuRs.getInt("message_type"));
		    	menuBean.setParentMenuId(menuRs.getInt("parent_menu_id"));
		    	menuMap.put(menuBean.getMenuId(), menuBean);
		    }
		    
		    ResultSet messageRs = stmt.executeQuery("select * from werobot_message");
		    while(messageRs.next()){
		    	MessageBean messageBean = new MessageBean();
		    	messageBean.setMessageId(messageRs.getInt("message_id"));
		    	messageBean.setMessageTitle(messageRs.getString("message_title"));
		    	int menuId = messageRs.getInt("menu_id");
				messageBean.setMenuId(menuId);
				messageBean.setDescription(messageRs.getString("description"));
				messageBean.setPicPath(messageRs.getString("pic_path"));
				if(messageMap.containsKey(menuId)){
					List<MessageBean> list = messageMap.get(menuId);
					list.add(messageBean);
				}else{
					ArrayList<MessageBean> list = new ArrayList<MessageBean>();
					list.add(messageBean);
					messageMap.put(menuId, list);
				}
		    }
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				if(null != conn)
					conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public String getReturnInfo(InputStream in) throws IOException{
		String responceStr = "";
		Document doc = null;
		SAXReader reader = new SAXReader();
		try {
			doc = reader.read(in);
            Element root = doc.getRootElement();
            String toUserName = root.element("ToUserName").getTextTrim();
            String fromUserName = root.element("FromUserName").getTextTrim();
            String content = root.element("Content").getTextTrim();
			// root.element("MsgType")可以获取消息类型
			// 这里只回复简单文本信息
            String responceContent = "欢迎光临味之美零食工厂～～";
            Iterator<Entry<Integer, MenuBean>> it = menuMap.entrySet().iterator();
            while(it.hasNext()){
            	Entry<Integer, MenuBean> entry = it.next();
            	int menuId = entry.getKey();
            	MenuBean menuBean = entry.getValue();
            	responceContent += "回复" + menuId + "了解" + menuBean.getMenuName() + ";";
            }
            responceStr = String.format(RESPONSE_TXT, new Object[]{fromUserName,toUserName,System.currentTimeMillis(),"text",responceContent});
//            out.printf(RESPONSE_TXT, fromUserName,toUserName,System.currentTimeMillis(),"text","您发送的内容是:"+content);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		in.close();
		in = null;
		return responceStr;
	}
}
