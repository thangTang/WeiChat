package com;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sun.image.codec.jpeg.*;

public class PicServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
//		super.doGet(req, resp);
		String imagePath= req.getParameter("imagePath");
		OutputStream output = resp.getOutputStream();
		resp.setContentType("image/jpeg;charset=GB2312");
//		imagePath = getServletContext().getRealPath(imagePath);
		InputStream imageIn = new FileInputStream(new File(imagePath));         
		  //得到输入的编码器，将文件流进行jpg格式编码     
		  JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(imageIn);         //得到编码后的图片对象           
		BufferedImage image = decoder.decodeAsBufferedImage();   //得到输出的编码器   
		 
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);        
		 
		    encoder.encode(image);//对图片进行输出编码       
		     imageIn.close();//关闭文件流    
		     output.close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
//		super.doPost(req, resp);
		String imagePath= req.getParameter("imagePath");
		OutputStream output = resp.getOutputStream();
		resp.setContentType("image/jpeg;charset=GB2312");
		imagePath = getServletContext().getRealPath(imagePath);
		InputStream imageIn = new FileInputStream(new File(imagePath));         
		  //得到输入的编码器，将文件流进行jpg格式编码     
		  JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(imageIn);         //得到编码后的图片对象           
		BufferedImage image = decoder.decodeAsBufferedImage();   //得到输出的编码器   
		 
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);        
		 
		    encoder.encode(image);//对图片进行输出编码       
		     imageIn.close();//关闭文件流    
		     output.close();
	}

}
