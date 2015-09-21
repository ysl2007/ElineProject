package edu.bit.eline.imgdownload;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IMGDownLoad {
	public IMGDownLoad(){
		
	}
	public  void IMGQuery(String query, int start, String savapath) throws IOException {
	      //  URL url = new URL(
	     //          "https://www.googleapis.com/customsearch/v1?key=AIzaSyBrwg-rMRmK94NErXbpzPH7PSa1STdw6sc&cx=001506688360071074671:7zlg5tc-3_i&q=bank+logo&alt=json&searchType=image&hr=en");
	        
		    String urlstr="https://www.googleapis.com/customsearch/v1?key=AIzaSyBrwg-rMRmK94NErXbpzPH7PSa1STdw6sc&cx=001506688360071074671:7zlg5tc-3_i"
		    		     +"&q="+query+"&alt=json&searchType=image&hr=en"+"&start="+Integer.toString(start);		    	
	      
		    URL url = new URL(urlstr);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Accept", "application/json");
	        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

	         String output;
	         //int imgid=start;
	         String[]imgURLs=new String[10];
	         String[] imgsavepaths=new String[10];
	         int imgnum=0;
	         while ((output = br.readLine()) != null) {
	        	//System.out.println(output);
	        	if(output.contains("\"link\":"))
	        	{
	        	    
	        		 System.out.println(output);
	        		 String imgURL=output.substring(output.indexOf("http://"), output.length()-2);
	        		
	        		 String imgsavepath=savapath+"\\"+Integer.toString(start+imgnum)+".jpg";	        		 
	        		 //downloadIMG(imgURL,imgsavepath);
	        		 imgURLs[imgnum]=imgURL;
	        		 imgsavepaths[imgnum]=imgsavepath;
	        		 imgnum++;
	        	}
	           
	        }

	        conn.disconnect();
	        for(int i=0;i<imgnum;i++)
	         downloadIMG(imgURLs[i],imgsavepaths[i]);
	    }
	private void downloadIMG(String imgURL,String savePath)
	{
		
		try {
			   System.out.println("Image is downloading......");
			   URL url = new URL(imgURL);
			   BufferedInputStream in = new BufferedInputStream(url.openStream());
			   FileOutputStream file = new FileOutputStream(new File(savePath));
			   
				   int t;
				   while ((t = in.read()) != -1) {
				    file.write(t);
				   }
				   file.close();
				   in.close();
			      System.out.println("Download finished!");
			} catch (Exception e) {
			   e.printStackTrace();
			} 
		
		
	}
	public static void main(String[] args){
		IMGDownLoad id=new IMGDownLoad();
		   
		for (int start=100;start<200;start=start+10)
			try
		   {       id.IMGQuery("excavator+red", start, "E:\\电网项目\\config\\classes\\diggerLoader");
		   }catch (Exception e) {
				   e.printStackTrace();
		   } 
		
	}
}
