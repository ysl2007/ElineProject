package edu.bit.eline.recognise.feature;
import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.imageanalysis.sift.*;

import javax.imageio.ImageIO;

import java.awt.image.RasterFormatException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class FeatureExtract {

	/**
	 * @param args
	 */
    public void format(String sourcefilepath, String destfilepath){
   	 try {
   		
                BufferedReader inFile = new BufferedReader(new FileReader(sourcefilepath));
   		        BufferedWriter outFile = new BufferedWriter(new FileWriter(destfilepath));
   		         String line;
   		         String rec="";
   		        while ((line=inFile.readLine())!=null)
   		        {
   		        	if(line.charAt(0)=='-'){
	   		        	 Record rc=new Record(rec.substring(0, rec.length()-1));
	   		        	 outFile.write(rc.getRepresentation()+"\n");
					     System.out.println(rc.getRepresentation());
	   		        	 rec="";    		        		
   		        	}	        		  
   		        		
   		        	else
   		        		 rec=rec+line+"@";
   		        }   		       
   		        inFile.close();
			    outFile.close();
		 }catch(IOException e) {
                e.printStackTrace();
		 }	
    }
	public String extractfeatures( AnnotationAnalysis aa){
		  
		 String rect=aa.getRect();
		 String lefttop=rect.substring(0,rect.indexOf(";"));
		 String rightlow=rect.substring(rect.indexOf(";")+1,rect.length());
		 String features="";
		 int lefttopx=Integer.parseInt(lefttop.substring(0, lefttop.indexOf(",")));
		 int lefttopy=Integer.parseInt(lefttop.substring(lefttop.indexOf(",")+1,lefttop.length()));
		 int rightlowx=Integer.parseInt(rightlow.substring(0, rightlow.indexOf(",")));
		 int rightlowy=Integer.parseInt(rightlow.substring(rightlow.indexOf(",")+1,rightlow.length()));
		 AutoColorCorrelogram ac=new AutoColorCorrelogram();
		 CEDD cd=new CEDD();
		 ColorLayout cl=new ColorLayout();		
		 EdgeHistogram ed=new  EdgeHistogram();
		 ScalableColor sc=new ScalableColor();
		 FCTH ft=new FCTH();
		 Tamura tm=new Tamura();
		 
		
		 
		 if(lefttopx==rightlowx||lefttopy==rightlowy){
			 return null;
		 }
		 
		 try {
			File imgfile=new File(aa.getfilepath()+"/"+aa.getfileName());			
			BufferedImage bi=ImageIO.read(imgfile);
			BufferedImage subimg= null;
		    String [] arrs;
			subimg=bi.getSubimage(lefttopx, lefttopy, rightlowx-lefttopx, rightlowy-lefttopy);
			ImageIO.write(subimg,"JPEG",new File("E:/电网项目/train/classes/"+aa.getobjType()+"/"+aa.getfileName()+".sub.jpg"));
		 	 ac.extract(subimg);
		 	 features=features+"AutoColorCorrelogram: "+ac.getStringRepresentation()+";\n";
		 	 arrs=ac.getStringRepresentation().split(" "); 
		 	 System.out.println("AutoColorCorrelogram:"+arrs.length);
		 	 cd.extract(subimg);
		 	 features=features+"CEDD: "+cd.getStringRepresentation()+";\n";
		 	 arrs=cd.getStringRepresentation().split(" "); 
		 	 System.out.println("CEDD:"+arrs.length);
		 	 cl.extract(subimg);
		 	 features=features+"ColorLayout: "+cl.getStringRepresentation()+";\n";
		     arrs=cl.getStringRepresentation().split(" "); 
		 	 System.out.println("ColorLayout:"+arrs.length);
		 	 ed.extract(subimg);
		 	 features=features+"EdgeHistogram: "+ed.getStringRepresentation()+";\n";
		 	 arrs=ed.getStringRepresentation().split(" "); 
		 	 System.out.println("EdgeHistogram:"+arrs.length);
		 	 sc.extract(subimg);
		 	 features=features+"ScalableColor: "+sc.getStringRepresentation()+";\n";
		 	 arrs=sc.getStringRepresentation().split(" "); 
		 	 System.out.println("ScalableColor: "+arrs.length);
		 	 ft.extract(subimg);
		 	 features=features+"FCTH: "+ft.getStringRepresentation()+";\n";
		 	 arrs=ft.getStringRepresentation().split(" "); 
		 	 System.out.println("FCTH:  "+arrs.length);
		 	 tm.extract(subimg);
		 	 features=features+"Tamura: "+tm.getStringRepresentation()+";";
		 	 arrs=tm.getStringRepresentation().split(" "); 
		 	 System.out.println("Tamura:  "+arrs.length);
		 	
		 	 
			 return features;
		}catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();   
             return null;
        }catch(RasterFormatException e){
        	 e.printStackTrace(); 
        	 return null;
		}catch(NullPointerException e)
		 {
			 e.printStackTrace(); 
        	 return null;
		 }
		
	}
	public String[] getFileList(String foldpath)
	{  
		File rootDir= new File(foldpath);
		String[] subDirs = rootDir.list();
		List<String> list=new ArrayList<String>();
		for (int i = 0; i < subDirs.length; ++i){
			String subdir = new String(rootDir.getAbsolutePath() + "\\" + subDirs[i] + "\\");
			File dir = new File(subdir);
			
			File [] imgs = dir.listFiles(new FilenameFilter(){
				public boolean accept(File dir, String name){
					return name.toLowerCase().endsWith("txt");
				}
			});
			for(int j=0;j<imgs.length;j++)
			{
			 list.add(imgs[j].getAbsolutePath());
			}
		}
		
		String strings[]=new String[list.size()];
		for(int i=0,j=list.size();i<j;i++){
		  strings[i]=list.get(i);
		}
		return strings;
	}
	public static void main(String[] args) {
		
		FeatureExtract fe=new FeatureExtract();
	// 	fe.format("E:/电网项目/train/1.features", "E:/电网项目/train/111.features.format");
	    String [] filelist=fe.getFileList("E:/电网项目/train/2");
		 try {
			    BufferedWriter outFile = new BufferedWriter(new FileWriter("E:/电网项目/train/111112.features"));
			    
			    for(int i=0;i<filelist.length;i++){
			    	 AnnotationAnalysis aa=new AnnotationAnalysis(filelist[i]);
			    	 String features=fe.extractfeatures(aa);
			    	 if(features!=null){
					     String featureinf="fileName: "+aa.getfilepath()+"\\"+aa.getfileName()+"\n";
					     featureinf=featureinf+"LineName: "+aa.getlineName()+"\n";
					     featureinf=featureinf+"illum: "+aa.getillum()+"\n";
					     featureinf=featureinf+"bgType: "+aa.getbgType()+"\n";
					     featureinf=featureinf+"objType: "+aa.getobjType()+"\n";
					     featureinf=featureinf+features+"\n-----------------------------------------------------------------\n";
					     outFile.write(featureinf);
					     System.out.println(featureinf);
			         }
			    }
			    
			    outFile.close();
		 }catch(IOException e) {
             e.printStackTrace();
		 } 
		 /*
		 filelist=fe.getFileList("E:/电网项目/train/2");
		 try {
			    BufferedWriter outFile = new BufferedWriter(new FileWriter("E:/电网项目/train/2.features"));
			    
			    for(int i=0;i<filelist.length;i++){
			    	 AnnotationAnalysis aa=new AnnotationAnalysis(filelist[i]);
			    	 String features=fe.extractfeatures(aa.getfilepath()+"\\"+aa.getfileName(),aa.getRect());
			    	 if(features!=null){
					     String featureinf="fileName: "+aa.getfilepath()+"\\"+aa.getfileName()+"\n";
					     featureinf=featureinf+"LineName: "+aa.getlineName()+"\n";
					     featureinf=featureinf+"illum: "+aa.getillum()+"\n";
					     featureinf=featureinf+"bgType: "+aa.getbgType()+"\n";
					     featureinf=featureinf+"objType: "+aa.getobjType()+"\n";
					     featureinf=featureinf+features+"\n-----------------------------------------------------------------\n";
					     outFile.write(featureinf);
					     System.out.println(featureinf);
			    	 }
			    }
			    
			    outFile.close();
		 }catch(IOException e) {
               e.printStackTrace();
		 }	 
		 */
		 
     }
 
	}
	
	

