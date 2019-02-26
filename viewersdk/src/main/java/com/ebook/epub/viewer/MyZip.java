package com.ebook.epub.viewer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;



public class MyZip {

    
    private ZipFile mZip;
    
    public static String mUnzipDir="";
    public static boolean mIsUnzip=false;

    public static interface ZipDone
    {
        public abstract void OnZipDone(boolean flag);
    }
    
    public MyZip(String s) throws ZipException, IOException {
        File file = new File(s);
        ZipFile zipfile = new ZipFile(file);
        mZip = zipfile;
    }

    public MyZip(ZipFile zipfile) {
        mZip = zipfile;
    }

    private static void ZipFiles(String dir, String fileName, ZipOutputStream zipoutputstream, boolean flag)
        throws Exception
    {
        String path = String.valueOf(dir);
        String fullPath = (new StringBuilder(path)).append(fileName).toString();
        File file = new File(fullPath);
        
        if(file.isFile())
        {
            if(flag)
                fileName= createNumFilename(fileName);
            ZipEntry zipentry = new ZipEntry(fileName);
            FileInputStream fileinputstream = new FileInputStream(file);
            zipoutputstream.putNextEntry(zipentry);
            byte abyte0[] = new byte[4096];
            do
            {
                int i = fileinputstream.read(abyte0);
                if(i == -1)
                {
                    zipoutputstream.closeEntry();
                    return;
                }
                zipoutputstream.write(abyte0, 0, i);
            } while(true);
        }
        
        String as[] = file.list();
        if(as.length <= 0)
        {
            String s4 = String.valueOf(fileName);
            StringBuilder stringbuilder = new StringBuilder(s4);
            String s5 = File.separator;
            String s6 = stringbuilder.append(s5).toString();
            ZipEntry zipentry1 = new ZipEntry(s6);
            zipoutputstream.putNextEntry(zipentry1);
            zipoutputstream.closeEntry();
        }
        
        int j = 0;
        do
        {
            int k = as.length;
            if(j >= k)
                return;
            
            String s7 = String.valueOf(fileName);
            StringBuilder stringbuilder1 = new StringBuilder(s7);
            String s8 = File.separator;
            StringBuilder stringbuilder2 = stringbuilder1.append(s8);
            String s9 = as[j];
            String s10 = stringbuilder2.append(s9).toString();
            ZipFiles(dir, s10, zipoutputstream, flag);
            j++;
        } while(true);
    }

    public static void ZipFolder(String s, String s1)
        throws Exception
    {
        ZipFolder(s, s1, false);
    }

    public static void ZipFolder(String s, String s1, boolean flag)
        throws Exception
    {
        if(flag)
            initNumFilenames(s);
        
        FileOutputStream fileoutputstream = new FileOutputStream(s1);
        ZipOutputStream zipoutputstream = new ZipOutputStream(fileoutputstream);
        File file = new File(s);
        String s2 = String.valueOf(file.getParent());
        StringBuilder stringbuilder = new StringBuilder(s2);
        String s3 = File.separator;
        String s4 = stringbuilder.append(s3).toString();
        String s5 = file.getName();
        ZipFiles(s4, s5, zipoutputstream, flag);
        
        if(flag)
            saveNumFilenames(zipoutputstream);
        
        zipoutputstream.finish();
        zipoutputstream.close();
    }

    private static String createNumFilename(String s)
    {
        numFilenames.add(s);
        
        String s1 = String.valueOf(baseFolder);
        StringBuilder stringbuilder = (new StringBuilder(s1)).append("/");
        int i = numFilenames.size();
        return stringbuilder.append(i).append(".tag").toString();
    }
    
    private static void initNumFilenames(String s)
    {
        numFilenames = new ArrayList<String>();
        baseFolder = BookHelper.getFilename(s);
    }
    
    private static void saveNumFilenames(ZipOutputStream zipoutputstream)
    {
        if(numFilenames.size() == 0)
            return;
        
        ZipEntry zipentry;
        StringBuilder stringbuilder;
        Iterator<String> iterator;
        String s = String.valueOf(baseFolder);
        String s1 = (new StringBuilder(s)).append("/").append("_names.list").toString();
        zipentry = new ZipEntry(s1);
        stringbuilder = new StringBuilder();
        iterator = numFilenames.iterator();

        InputStream inputstream = BookHelper.String2InputStream(stringbuilder.toString());
        byte abyte[] = new byte[4096];
        
        if( !iterator.hasNext() ) {
            numFilenames = null;
            try {
                zipoutputstream.putNextEntry(zipentry);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        

        while( iterator.hasNext() ) {
            
            try {
                
                int i = inputstream.read(abyte);
                if( i== -1 ) {
                    zipoutputstream.closeEntry();
                    return;
                }
                
                String s2 = String.valueOf( (String)iterator.next() );
                String s3 = (new StringBuilder(s2)).append("\n").toString();
                
                StringBuilder stringbuilder1 = stringbuilder.append(s3);
    
                zipoutputstream.write(abyte, 0, i);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
    
//    public static String unZipFile(String dir, String fileName, boolean flag, ZipDone zipdone)
//    {
//        boolean flag1 = flag;
//        ZipDone zipdone1 = zipdone;
//        boolean flag2 = false;
//        boolean flag3 = false;
//        return unZipFile(dir, fileName, flag1, zipdone1, false, flag2, flag3);
//    }
    
    /**
     * 도서의 압축을 해제 하는 메소드 
     * 
     * @param fullName String : 압축파일 전체 경로
     * @param outDir String : 따로 압축을 풀 경로
     * @param useName boolean : 기존 파일명 사용여부
     * @param zipdone ZipDone : Zip 완료 리스너
     * @return String : 결과 메세지
     */
    public static String unZipFile(String fullName, String outDir, boolean useName, ZipDone zipdone) {

        File srcFile = new File(fullName);
        if (!srcFile.isFile()) {
            return "invalid file";
        }
        
        String filePath;
        if (useName) {
            String cache = String.valueOf(outDir);
            StringBuilder stringbuilder = (new StringBuilder(cache));   //.append("/");
            String fileName = BookHelper.getOnlyFilename(fullName);
            filePath = stringbuilder.append(fileName).toString();
        } else {
            filePath = outDir;
        }
        
        File destDir = new File(filePath);
        if (!destDir.exists() || !destDir.isDirectory()) {
            destDir.mkdirs();
        } 
        else {
            // already exist
            return null;
        }

        BufferedOutputStream dest = null;
        BufferedInputStream is = null;
        ZipEntry entry;

        try {
            ZipFile zipfile = new ZipFile(srcFile);
            Enumeration<?> entries = zipfile.entries();

            while (true) {

                if (!entries.hasMoreElements()) {
                    if (zipdone != null) {
                        zipdone.OnZipDone(true);
                    }
                    break;
                }

                entry = (ZipEntry) entries.nextElement();

                is = new BufferedInputStream(zipfile.getInputStream(entry), 8192);
                
                int count;
                byte data[] = new byte[8192];
                String fileName = entry.getName();
                File outFile = new File(destDir, fileName);
                if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }
                // 파일,디렉터리 판단
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                dest = new BufferedOutputStream(new FileOutputStream(outFile),
                        8192);
                while ((count = is.read(data, 0, 8192)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.close();
                is.close();

            }

            if (dest != null)
                dest.close();
            if (is != null)
                is.close();
            
            mIsUnzip = true;
            mUnzipDir = filePath;
            
        } catch (ZipException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return null;

    }
    
//    public ArrayList<FileInfo> getFileInfoOfZip()
//    {
//        if( mZip == null )
//            return null;
//        
//        ArrayList<FileInfo> arraylist = new ArrayList<FileInfo>();
//        Enumeration<?> entries = mZip.entries();
//
//        while( entries.hasMoreElements() ) {
//            
//            ZipEntry zipentry = (ZipEntry)entries.nextElement();
//            String name = zipentry.getName();
//            long size = zipentry.getSize();
//            long compSize = zipentry.getCompressedSize();
//            long time = zipentry.getTime();
//            boolean dir = zipentry.isDirectory();
//            String comment = zipentry.getComment();
//            
//            FileInfo fileInfo = new FileInfo(name, size, compSize, time, dir, comment);
//            arraylist.add(fileInfo);
//        }
//        
//        return arraylist;
//        
//    }
    
    public InputStream getFileStreamFromZip(String filename)
    {
        if( mZip == null ) 
            return null;

        InputStream inputstream=null;
        
        try {
            Enumeration<?> entries = mZip.entries();
            
            while( entries.hasMoreElements() ) {
                
                ZipEntry entry = (ZipEntry)entries.nextElement();
                if( !entry.getName().toLowerCase().equals(filename) ) {
                    continue;
                }
                
                inputstream = mZip.getInputStream(entry);
                    
                break;
                
            }

            
        } catch (IOException e) {
            inputstream = null;
            e.printStackTrace();
        }
        

        return inputstream;
    }

    
    public static final String ZIPPED_FILENAMES_FILE = "_names.list";
    private static String baseFolder;
    private static ArrayList<String> numFilenames;
    
}
