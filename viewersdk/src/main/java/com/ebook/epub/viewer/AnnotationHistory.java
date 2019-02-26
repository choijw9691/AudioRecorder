package com.ebook.epub.viewer;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

public class AnnotationHistory {
    
    class HistoryData {
        long uniqueID;
        String path;
        String file;
        int state;
        
        HistoryData(long id, String path, String file, int state) {
            uniqueID = id;
            this.path = path;
            this.file = file;
            this.state = state;
        }
        
        HistoryData(HistoryData hd, int state) {
            this.uniqueID = hd.uniqueID;
            this.path = hd.path;
            this.file = hd.file;
            this.state = state;
        }
        
    }
    
    HashMap<String, HistoryData> __histories = new HashMap<String, HistoryData>();
    
    HashMap<String, String> __histories2 = new HashMap<String, String>();
    
    public AnnotationHistory() {
    }
    
    public void clear() {
        __histories2.clear();
    }
    public void read(String fileName) {
        try {
            
            File dataFile = new File( fileName );
            if( !dataFile.exists() ) {
                return;
            }
            
            InputStream input = new FileInputStream( dataFile );
           
            String inputByte = BookHelper.inputStream2String(input);
            input.close();
            
            __histories2.clear();
            
            JSONObject object = new JSONObject(inputByte);
            Iterator<?> it = object.keys();
            while(it.hasNext()) {
                String key = (String)it.next();
                String value = object.getString(key);
                
                __histories2.put(key, value);
            }
               
        } catch( Exception e ) {
            e.printStackTrace();
        }
        
    }
    
    public void write(String fileName) {

        try {
            
            File bookmarkDataFile = new File(fileName);
            if( !bookmarkDataFile.exists()) {
                bookmarkDataFile.createNewFile();
            }
            
            FileOutputStream output = new FileOutputStream(bookmarkDataFile);
    
            JSONObject object = new JSONObject();

            for (HashMap.Entry<String, String> entry : __histories2.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                object.put(key, value);
            }
            
            output.write(object.toString(1).getBytes());
            output.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    

    HistoryData exist(String path, String file ) {
        
        for( String key : __histories.keySet() ) {
            HistoryData hd = __histories.get(key);
            
            String srcPath = path.toLowerCase();
            String dstPath = hd.path.toLowerCase();
            
            String srcFile = file.toLowerCase();
            String dstFile = hd.file.toLowerCase();
            
            if( dstPath.equals(srcPath) && dstFile.equals(srcFile) ) {
                return hd;
            }
        }
        
        return null;
    }

    public void add(long id, String path, String file) {
        
        try {
            HistoryData hd = exist(path, file);
            if(  hd != null ) {
                __histories.remove(""+hd.uniqueID);
                __histories.put(""+hd.uniqueID, new HistoryData(hd, 1));
            }
            else {
                __histories.put(""+id, new HistoryData(id, path, file, 1));
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void remove(long id, String path, String file) {

        try {
            HistoryData hd = exist(path, file);
            if(  hd != null ) {
                __histories.remove(""+hd.uniqueID);
                __histories.put(""+hd.uniqueID, new HistoryData(hd, 0));
            }
            else {
                __histories.put(""+id, new HistoryData(id,path,file,0));
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void add(long id) {
        try {
            
            if( __histories2.containsKey(""+id) ) {
                __histories2.remove(""+id);
            }
            
            __histories2.put(""+id, ""+1);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void remove(long id) {
        try {
            
            if( __histories2.containsKey(""+id) ) {
                __histories2.remove(""+id);
            }
            
            __histories2.put(""+id, ""+0);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void modifyAdd(long id){
    	try {
            
            if( __histories2.containsKey(""+id) ) {
                __histories2.remove(""+id);
            }
            
            __histories2.put(""+id, ""+3);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void modifyRemove(long id, long newId){
    	try {
            
            if( __histories2.containsKey(""+id) ) {
                __histories2.remove(""+id);
            }
            
            __histories2.put(""+id, 2+String.valueOf(newId));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void mergeAdd(long id){
    	try {
            
            if( __histories2.containsKey(""+id) ) {
                __histories2.remove(""+id);
            }
            
            __histories2.put(""+id, ""+5);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void mergeRemove(long id, long newId){
    	try {
            
            if( __histories2.containsKey(""+id) ) {
                __histories2.remove(""+id);
            }
            
            __histories2.put(""+id, 4 + String.valueOf(newId));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void modify(long id) {        
        if( __histories2.containsKey(""+ id) ) {
            __histories2.remove("" + id);
        }
        
        __histories2.put(""+id, ""+2);
    }
    
    public String toString() {

        String ret="";
        
        for (HashMap.Entry<String, String> entry : __histories2.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            ret += key + ":" + value + "\n";
        }            
        
        return ret;
    }
    
    
//  public void read(String fileName) {
//  
//  try {
//      
//      File dataFile = new File( fileName );
//      if( !dataFile.exists() ) {
//          return;
//      }
//      
//      InputStream input = new FileInputStream( dataFile );
//     
//      String inputByte = BookHelper.inputStream2String(input);
//      input.close();
//      
//      
//      __histories.clear();
//      
//      JSONObject object = new JSONObject(inputByte);
//
//      JSONArray array = object.getJSONArray(AnnotationConst.FLK_HISTORY_LIST);
//      for(int i=0; i<array.length(); i++) {
//          JSONObject item = array.getJSONObject(i);
//          
//          long uniqueID = item.getLong("key");
//          int state = item.getInt(""+uniqueID);
//          String path = item.getString("path");
//          String file = item.getString("file");
//          
//          __histories.put(""+uniqueID, new HistoryData(uniqueID, path, file, state) );
//      }
//      
//      
//         
//  } catch( Exception e ) {
//      e.printStackTrace();
//  }
//  
//}
//
//public void write(String fileName) {
//
//  try {
//      
//      File bookmarkDataFile = new File(fileName);
//      if( !bookmarkDataFile.exists()) {
//          bookmarkDataFile.createNewFile();
//      }
//      
//      FileOutputStream output = new FileOutputStream(bookmarkDataFile);
//
//      JSONObject object = new JSONObject();
//
//      JSONArray array = new JSONArray();
//      for (HashMap.Entry<String, HistoryData> entry : __histories.entrySet()) {
//          String key = entry.getKey();
//          HistoryData hd = entry.getValue();
//          
//          JSONObject obj = new JSONObject();
//          obj.put(key, hd.state);
//          obj.put("key", hd.uniqueID);
//          obj.put("path", hd.path);
//          obj.put("file", hd.file);
//          
//          array.put(obj);
//      }            
//      object.put(AnnotationConst.FLK_HISTORY_LIST, array);
//      
//      output.write(object.toString(1).getBytes());
//      output.close();
//  }
//  catch(Exception e) {
//      e.printStackTrace();
//  }
//}
//    
//  public String toString() {
//
//      String ret="";
//      
//      for (HashMap.Entry<String, HistoryData> entry : __histories.entrySet()) {
//          String key = entry.getKey();
//          HistoryData hd = entry.getValue();
//          
//          ret += key + ":" + hd.state + "\n";
//      }            
//      
//      return ret;
//  }
    
}
