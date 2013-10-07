package com;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
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
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.bean.MenuBean;
import com.bean.MessageBean;

public class MsgManager {
	private static MsgManager manager;
	
	private String url = "jdbc:mysql://42.96.142.222:9999/we_robot";
	private String user = "root";
	private String pwd = "123456";
	
	private Map<String, MenuBean> menuMap = new HashMap<String, MenuBean>();
	private Map<Integer, ArrayList<MessageBean>> messageMap = new HashMap<Integer, ArrayList<MessageBean>>();
	private static final String RESPONSE_TXT = "<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%s</CreateTime><MsgType><![CDATA[%s]]></MsgType><Content><![CDATA[%s]]></Content><FuncFlag>0</FuncFlag></xml>";
	private static final String RESPONSE_NEWS = "<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%s</CreateTime><MsgType><![CDATA[%s]]></MsgType><ArticleCount>%s</ArticleCount>%s</xml>";
	
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
		    	menuMap.put("" + menuBean.getMenuId(), menuBean);
		    }
		    
		    ResultSet messageRs = stmt.executeQuery("select * from werobot_message");
		    while(messageRs.next()){
		    	MessageBean messageBean = new MessageBean();
		    	messageBean.setMessageId(messageRs.getInt("message_id"));
		    	messageBean.setMessageTitle(messageRs.getString("message_title"));
		    	int menuId = messageRs.getInt("menu_id");
				messageBean.setMenuId(menuId);
				if(null != messageRs.getString("description"))
					messageBean.setDescription(messageRs.getString("description"));
				if(null != messageRs.getString("pic_path"))
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
			System.out.println("content = " + content);
			// root.element("MsgType")可以获取消息类型
			String responceContent = "";
			if(!menuMap.containsKey(content)){
				responceContent = "欢迎光临味之美零食工厂～～";
				Iterator<Entry<String, MenuBean>> it = menuMap.entrySet().iterator();
				while(it.hasNext()){
					Entry<String, MenuBean> entry = it.next();
					int menuId = Integer.parseInt(entry.getKey());
					MenuBean menuBean = entry.getValue();
					responceContent += "回复" + menuId + "了解" + menuBean.getMenuName() + ";";
				}
				responceStr = String.format(RESPONSE_TXT, new Object[]{fromUserName,toUserName,System.currentTimeMillis(),"text",responceContent});
				//            out.printf(RESPONSE_TXT, fromUserName,toUserName,System.currentTimeMillis(),"text","您发送的内容是:"+content);
			}else{
				MenuBean menuBean = menuMap.get(content);
				//图文信息
				if(1 == menuBean.getMessageType()){
					System.out.println("msgType = 1");
					ArrayList<MessageBean> msgList = messageMap.get(Integer.parseInt(content));
					int articleCount = msgList.size();
					System.out.println("articleCount = " + articleCount);
					String articlesStr = writeArticles(msgList);
					responceStr = String.format(RESPONSE_NEWS, new Object[]{fromUserName,toUserName,System.currentTimeMillis(),"news",articleCount,articlesStr});
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		in.close();
		in = null;
		return responceStr;
	}
	
	private String writeArticles(ArrayList<MessageBean> msgList){
		String articleStr = "";
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Articles");
		for(MessageBean msgBean : msgList){
			Element item = root.addElement("item");
			String title = "![CDATA[%s]]";
			String description = "![CDATA[%s]]";
			String picUrl = "![CDATA[%s]]";
			
			title = String.format(title, new Object[]{msgBean.getMessageTitle()});
			description = String.format(description, new Object[]{msgBean.getDescription()});
			picUrl = String.format(picUrl, new Object[]{"http://42.96.142.222/pic?imagePath=/usr/local/weChatPics/" + msgBean.getPicPath()});
			
//			item.addElement("Title").addText(title);
//			item.addElement("Description").addText(description);
//			item.addElement("PicUrl").addText(picUrl);
//			item.addElement("Url").addText("![CDATA[]]");
			item.addElement("Title").addText(msgBean.getMessageTitle());
			item.addElement("Description").addText(msgBean.getDescription());
			item.addElement("PicUrl").addText("http://42.96.142.222/pic?imagePath=/usr/local/weChatPics/" + msgBean.getPicPath());
			item.addElement("Url").addText("");
		}
		
		StringWriter out = new StringWriter();
		try {
			document.write(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		articleStr = out.toString();
		//<?xml version="1.0" encoding="UTF-8"?>先这样把这声明搞了
		articleStr = articleStr.split("\n")[1];
		System.out.println("articleStr = " + articleStr);
		return articleStr;
	}
	
	public void testArticle(){
		writeArticles(messageMap.get(1));
	}
}
