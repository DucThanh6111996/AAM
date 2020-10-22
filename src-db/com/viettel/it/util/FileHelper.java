package com.viettel.it.util;

import org.mozilla.universalchardet.UniversalDetector;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHelper {
	
	 static final Logger _log = LoggerFactory.getLogger(FileHelper.class);

	private static final int BUFFER_SIZE = 4096;
	/**
	 * Su dung cho primefaces.
	 */
	public static String uploadFile(String folderStore, UploadedFile fileUpload, String fileName) {
		if (fileUpload == null)
			return "FALSE";

		OutputStream outputStream;
		try {
			outputStream = getOutputStream(folderStore, fileName);
			outputStream.write(fileUpload.getContents());
			outputStream.close();
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
			_log.info(e.getMessage());
			return "FALSE";
		}

		return "SUCCESS";
	}

	/**
	 * Get output stream
	 */
	public static OutputStream getOutputStream(String folderStore, String fileName) {
		OutputStream outputStream = null;
		try {
			File fileToCreate = new File(folderStore);
			if (!fileToCreate.exists()) {
				_log.info("run mkdir: " + folderStore);
				fileToCreate.mkdirs();
			}

			fileToCreate = new File(folderStore, fileName);
			outputStream = new FileOutputStream(fileToCreate);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}

		return outputStream;
	}

	/**
	 * Remove file
	 */
	public static Boolean removeFile(String folderStore) {
		try {
			File fileToRemove = new File(folderStore);
			if (fileToRemove.exists()) {
				_log.info("run remove: " + folderStore);
				if (fileToRemove.delete()) {
					_log.info("remove file success");
				}
			}
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
		return false;
	}

	public static String unzip(ZipInputStream zipIn, String... destDirectories) throws IOException {
		String fileName = "";
		for(String dir : destDirectories){
			File destDir = new File(dir);
			if (!destDir.exists()) {
				destDir.mkdir();
			}
		}
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String destDirectory = destDirectories[2];
			if(entry.getName().endsWith(".mp3") || entry.getName().endsWith(".wma"))
				destDirectory = destDirectories[0];
			else if(entry.getName().endsWith(".png") || entry.getName().endsWith(".jpg") || entry.getName().endsWith(".gif"))
				destDirectory = destDirectories[1];
			else if(entry.getName().endsWith(".mp4") || entry.getName().endsWith(".mpg") || entry.getName().endsWith(".mov"))
				destDirectory = destDirectories[2];
			String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				if (entry.getName().endsWith(".htm") || entry.getName().endsWith(".html")) {
					fileName = entry.getName();
				}
				extractFile(zipIn, filePath);
				if (entry.getName().endsWith(".htm") || entry.getName().endsWith(".html")) {
					optimizeFile(filePath);
				}
			} else {
				// if the entry is a directory, make the directory
				 File dir = new File(filePath);
				 dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
		return fileName;
	}
	public static void optimizeFile(String filePath) {
		// TODO Auto-generated method stub

		BufferedWriter bw = null;
		BufferedReader br = null;
		
		try {
			String data="";
			String encoding = getEncoding(filePath);
			if(encoding!=null)
				br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),encoding));
			else
				br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String line;
			boolean isContTag =false;
			while ((line = br.readLine()) != null) {
				if(isContTag){
					line = line.replaceAll("width:\\d{1,4}pt", "");
					isContTag = false;
				}
				if (line.trim().startsWith("<table")){
					line = line.replaceAll("width=\\d{1,4}", "width=100%");
					isContTag = true;
				}
				data += line+"\n";
			}
			br.close();
			if(encoding!=null)
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),encoding));
			else
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
			bw.write(data);
			bw.flush();
			bw.close();
		} catch (Exception e1) {
			_log.error(e1.getMessage(), e1);
		} finally {

		}
	}
	public static String getEncoding(String fileName){
		String encoding =null;
		try {
			byte[] buf = new byte[4096];
			FileInputStream fis = new FileInputStream(fileName);

			// (1)
			UniversalDetector detector = new UniversalDetector(null);

			// (2)
			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
			  detector.handleData(buf, 0, nread);
			}
			// (3)
			detector.dataEnd();

			// (4)
			encoding = detector.getDetectedCharset();
			if (encoding != null) {
			  System.out.println("Detected encoding = " + encoding);
			 
			} else {
			  System.out.println("No encoding detected.");
			}

			// (5)
			detector.reset();
			fis.close();
		} catch (FileNotFoundException e) {
			_log.error(e.getMessage(), e);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
		 return encoding;

	}
	
	public static void main(String[] args) {
		String filePath = "D:/Work/workspace2/.metadata/.plugins/org.eclipse.wst.server.core/tmp6/wtpwebapps/ksclm/resources/staff-info/KV3/AGG_thuynn1.htm";
		//FileHelper.getEncoding(filePath);
		FileHelper.optimizeFile(filePath);
	}

	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read ;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}
}
