package com.viettel.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RepairCorruptZip {
	private static Logger logger = LogManager.getLogger(RepairCorruptZip.class);

	public File extractZipFiles(File fileName) {
		Path tmpDir = null;
		ZipInputStream ziStream = null;
		InputStream inputStream = null;
		try {
			tmpDir = Files.createTempDirectory("zip-");

			logger.info(fileName + "\t" + tmpDir);

			byte[] by = new byte[2048];
			ZipEntry zipEntry;
			inputStream = new FileInputStream(fileName);
			ziStream = new ZipInputStream(inputStream);

			zipEntry = ziStream.getNextEntry();
			while (zipEntry != null) {
				String entry = tmpDir + File.separator + zipEntry.getName();
				entry = entry.replace('/', File.separatorChar).replace('\\', File.separatorChar);
				int n;
				FileOutputStream fOTStream;
				File newFile = new File(entry);
				if (zipEntry.isDirectory()) {
					if (!newFile.mkdirs()) {
						break;
					}
					zipEntry = ziStream.getNextEntry();
					continue;
				}
				fOTStream = new FileOutputStream(entry);
				while ((n = ziStream.read(by, 0, 2048)) > -1) {
					fOTStream.write(by, 0, n);
				}
				fOTStream.close();
				ziStream.closeEntry();
				zipEntry = ziStream.getNextEntry();
			}
			System.out.println("DONE!!!!!");
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage() + fileName);
		} finally {
			if (ziStream != null)
				try {
					ziStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}

			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
		}

		return tmpDir == null ? null : tmpDir.toFile();
	}

	public static File recompressZip(File dir, File outFile) throws ZipException {
		ZipFile zipFile = new ZipFile(dir.getPath() + ".zip");
		ZipParameters parameters = new ZipParameters();

		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
		parameters.setIncludeRootFolder(false);

		if (!zipFile.getFile().exists())
			zipFile.createZipFileFromFolder(dir, parameters, false, 0);
		else
			zipFile.addFolder(dir, parameters);

		return zipFile.getFile();
	}

	public static void repairZip(File fileName) {
		if (fileName == null)
			return;
		logger.info("==> Begin repair " + fileName.getPath());
		File outDir = null;
		File fixedFile = null;
		try {
			RepairCorruptZip extractCorruptZip = new RepairCorruptZip();
			outDir = extractCorruptZip.extractZipFiles(fileName);
			fixedFile = recompressZip(outDir, fileName);

			Files.move(fixedFile.toPath(), fileName.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (ZipException ex) {
			if (ex.getMessage().contains("no files to add") && fileName.exists()) {
				try {
					FileUtils.forceDelete(fileName);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			logger.error(ex.getMessage() + fileName, ex);
		} catch (Exception ex) {
			logger.error(ex.getMessage() + fileName, ex);
		} finally {
			if (outDir != null && outDir.exists()) {
				try {
					FileUtils.deleteQuietly(outDir);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

			if (fixedFile != null && fixedFile.exists()) {
				try {
					FileUtils.forceDelete(fixedFile);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		logger.info("==> End repair " + fileName.getPath());
	}
}