package com.viettel.util;

import com.mchange.v2.collection.MapEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetResultUtil {
	private static Logger logger = LogManager.getLogger(GetResultUtil.class);
	public List<String> subLogToList(String log) {
		List<String> list = new ArrayList<>();
		try {
			if ("".equals(log.trim()))
				return new ArrayList<>();
			log = log.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
			list = Arrays.asList(log.split("\n"));
			int size = list.size();
			int i = 0;
			while (i < size && "".equals(list.get(i).trim())) {
				i++;
			}
			list = list.subList(i, size);
			size = list.size();

			int j = size - 1;
			while (j > 0 && "".equals(list.get(j).trim())) {
				j--;
			}
			list = list.subList(0, j + 1);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	public List<String> arrayToList(String[] array) {
		List<String> list = new ArrayList<>();
		try {
			list = Arrays.asList(array);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	public List<String> subRowToList(String row) {
		List<String> list = new ArrayList<>();
		try {
			row = row.trim().replaceAll("\t", " ").replaceAll("\r\n", " ").replaceAll("\r", " ")
					.replaceAll("\n", " ").replaceAll(" +", " ");
			String[] array = row.split(" ");
			list = Arrays.asList(array);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	public int getfrequency(String log, String kytu) {
		int frequency = 0; // t?n s? xu?t hi?n c?a k� t?
		try {
			frequency = StringUtils.countOccurrencesOf(log, kytu);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return frequency;
	}

	public List<String> getlistLog(List<String> arrLog, String beginChild, String endChild) {
		List<String> listChild = new ArrayList<>();
		String childLog = "";
		boolean begin = false;
		for (String str : arrLog) {
			if (str.contains(beginChild)) // n?u g?p d�ng ch?a k� t? b?t ??u log
											// con
			{
				begin = true; // thi?t d?t vi?c c?ng log
				childLog += str; // c?ng log
				continue;

			}
			if (begin && !str.contains(beginChild) && !str.contains(endChild)) //
			{
				childLog += str; // c?ng log
				continue;
			}

			if (str.contains(endChild)) {

				if (begin && !"".equals(childLog.trim())) {
					childLog += str;
					listChild.add(childLog);
				}
				childLog = "";
				begin = false;
				continue;
			}
		}

		if (begin && !"".equals(childLog.trim())) {
			listChild.add(childLog);
		}

		return listChild;
	}

	public List<String> findFloat(String log) {
		List<String> listFloats = new ArrayList<>();
		Pattern p = Pattern.compile("\\d+\\.{0,1}\\d*");
		Matcher m = p.matcher(log);
		while (m.find()) {
			System.out.println(m.group());
			listFloats.add(m.group());
		}
		return listFloats;
	}

	public boolean checkFloatType(String text) {

		try {

			text = text.trim();
			String reg1 = "^\\d+\\D*$";
			Pattern pattern1 = Pattern.compile(reg1);
			Matcher matcher1 = pattern1.matcher(text);

			String reg2 = "^\\d+\\s+.*$";
			Pattern pattern2 = Pattern.compile(reg2);
			Matcher matcher2 = pattern2.matcher(text);

			String reg3 = "^\\d+\\.\\d+\\D*$";
			Pattern pattern3 = Pattern.compile(reg3);
			Matcher matcher3 = pattern3.matcher(text);

			String reg4 = "^\\d+\\.\\d+\\s+.*$";
			Pattern pattern4 = Pattern.compile(reg4);
			Matcher matcher4 = pattern4.matcher(text);

			if (!matcher1.matches() && !matcher2.matches() && !matcher3.matches() && !matcher4.matches())
				return false;
			if (text.contains(" "))
				text = text.substring(0, text.indexOf(" "));

			text = text.replaceAll("[^\\d\\.]", "");
			if (text.endsWith("."))
				return false;
//			Float.parseFloat(text);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	public boolean checkLongType(String text) {

		try {

			text = text.trim();
			String reg1 = "^\\d+\\D*$";
			Pattern pattern1 = Pattern.compile(reg1);
			Matcher matcher1 = pattern1.matcher(text);

			String reg2 = "^\\d+\\s+.*$";
			Pattern pattern2 = Pattern.compile(reg2);
			Matcher matcher2 = pattern2.matcher(text);

			String reg3 = "^\\d+\\.\\d+\\D*$";
			Pattern pattern3 = Pattern.compile(reg3);
			Matcher matcher3 = pattern3.matcher(text);

			String reg4 = "^\\d+\\.\\d+\\s+.*$";
			Pattern pattern4 = Pattern.compile(reg4);
			Matcher matcher4 = pattern4.matcher(text);

			if (!matcher1.matches() && !matcher2.matches() && !matcher3.matches() && !matcher4.matches())
				return false;
			if (text.contains(" "))
				text = text.substring(0, text.indexOf(" "));

			text = text.replaceAll("[^\\d\\.]", "");
			if (text.endsWith("."))
				return false;
			Long.parseLong(text);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

	}

	public float floatTryParse(String log) {

		try {
			log = log.trim();
			int p = log.indexOf(" ");
			if (p != -1)
				log = log.substring(0, p);
			String str = log.trim().replaceAll("[^\\d\\.]", "");
			return Float.parseFloat(str);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return -Float.MAX_VALUE;
		}

	}

	public Long LongTryParse(String log) {

		try {
			log = log.trim();
			int p = log.indexOf(" ");
			if (p != -1)
				log = log.substring(0, p);
			String str = log.trim().replaceAll("[^\\d\\.]", "");
			return Long.parseLong(str);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}

	}

	public int integerTryParse(String log) {
		try {
			return Integer.parseInt(log.trim());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return -Integer.MAX_VALUE;
		}
	}

	public boolean checkContainAllListString(String row, List<String> findList) {

		try {

			row = row.trim().toUpperCase().replaceAll("\r", "");
			for (String findString : findList) {
				if (findString != null && !"".equals(findString.trim())) {
					if (!row.contains(findString.trim().toUpperCase())) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	/*
	 * public List<MapObject> creatGetObjects(String txt) { List<MapObject>
	 * mapObjects=new ArrayList<>(); try {
	 *
	 * String[] dataList=txt.split(Constant.SPLIST_CHAR); for(String
	 * data:dataList) { if(data==null || data.trim().equals("")) { continue; }
	 * data=data.trim(); MapObject object=new MapObject();
	 *
	 * String[] param=data.trim().split(Constant.VALUE_QUANTI_SPLIST_CHAR);
	 * if(param.length==0) { continue; } if(param.length>2) // n?u n� ghi l�o ,
	 * koong ?�ng ??nh d?ng ki?u nh? : 1(-)2(-)3 {
	 * object.setKey(data.trim().toUpperCase());
	 * object.setValue(String.valueOf(1)); mapObjects.add(object); continue;
	 * }else {
	 *
	 * object.setKey(param[0].trim().toUpperCase()); if(param.length==2) {
	 * object.setValue(param[1].trim().toUpperCase()); }else {
	 * if(param.length==1) { object.setValue(Constant.NONE); } }
	 * mapObjects.add(object); } } } catch (Exception e) { logger.error(e.getMessage(), e);
	 * } return mapObjects; }
	 */

	public int getFromIndexOfData(String row, int fromIndex) {

		if (fromIndex < 0) {
			return 0;
		}
		if (row.length() < fromIndex + 1) {
			return fromIndex; // n?u v? tr� b?t ??u l?y data l?n h?n c? d? d�i
								// c?a d�ng k� t?
		}
		try {

			int newFromIndex = fromIndex;

			while (newFromIndex - 1 >= 0 && row.charAt(newFromIndex - 1) != ' ') {
				newFromIndex--;
			}
			return newFromIndex;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return fromIndex;
	}

	public int getToIndexOfData(String row, int toIndex) {
		if (toIndex < 0) {
			return 0;
		}
		if (row.length() < toIndex + 1) {
			return row.length() - 1; // n?u v? tr� b?t ??u l?y data l?n h?n c?
										// d? d�i c?a d�ng k� t?
		}
		try {

			int newToIndex = toIndex;
			while (newToIndex + 1 < row.length() && row.charAt(newToIndex + 1) != ' ') {
				newToIndex++;
			}
			return newToIndex;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return toIndex;
	}

	public List<String> getExactlyColumn(String log, int totalColum, // Kieur
																		// h�ng
																		// c?t
																		// ch�nh
																		// x�c
			int getInColumn) // n?u l?y ? c?t x t?c l� l?y ? c?t c� th? t? ( x-1
								// )
	{

		List<String> list = new ArrayList<>();
		if (totalColum < 1 || getInColumn < 1 || totalColum < getInColumn || log == null) {
			return new ArrayList<>(); // c�c ?i?u ki?n t�m ki?m kh�ng th?a m�n
		}

		List<String> allRow = this.subLogToList(log);
		for (String str : allRow) {
			List<String> rowList = this.subRowToList(str);
			if (rowList.size() != totalColum) {
				continue;
			}
			list.add(rowList.get(getInColumn - 1));
		}
		return list;
	}

	public List<MapEntry> getRowContainSpecialString(String log, // ki?u d�ng
																	// c� ch?a
																	// c�c k� t?
																	// ??c bi?t
			List<String> findList, Integer totalColum, Integer getInColumn) {
		List<MapEntry> list = new ArrayList<>();

		if (totalColum < 1 || getInColumn < 1 || totalColum < getInColumn || log == null) {
			return new ArrayList<>(); // c�c ?i?u ki?n t�m ki?m kh�ng th?a m�n
		}

		List<String> allRow = this.subLogToList(log);
		try {

			for (String row : allRow) {

				boolean check = this.checkContainAllListString(row, findList);

				if (check) {

					List<String> rowList = this.subRowToList(row);
					if (rowList.size() == totalColum) {
						MapEntry mapObject = new MapEntry(row, (rowList.get(getInColumn - 1)));
/*						mapObject.setKey(row);
						mapObject.setValue((rowList.get(getInColumn - 1)));*/
						list.add(mapObject);
					}
				}

			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	public List<String> getColumnHaveName(String log, String columnName, int type) { // ki?u
																						// l?y
																						// c?t
																						// c�
																						// t�n
		columnName = columnName.trim().toUpperCase();
		List<String> list = new ArrayList<>();
		if (log != null && !"".equals(log.trim())) {
			List<String> allRow = subLogToList(log);
			int beginRow = 0;
			String title = null;

			for (int i = 0; i < allRow.size(); i++) {
				if (allRow.get(i).toUpperCase().contains(columnName)) {
					beginRow = i + 1;
					title = allRow.get(i).toUpperCase();
					break;
				}
			}
			if (title == null) {
				return new ArrayList<>();
			}

			List<String> listColumn = subRowToList(title);
			if (listColumn.size() > 1) // n?u c� nhi?u h?n m?t c?t
			{

				for (int i = beginRow; i < allRow.size(); i++) {

					int beginIndex = title.indexOf(columnName);
					int endIndex = beginIndex + columnName.length() - 1;

					String row = allRow.get(i);
					if (row == null || "".equals(row.trim())) {
						continue;
					}
					if (type == Constant.DATA_END_WITH_COLUMN_NAME_TYPE
							|| type == Constant.DATA_CONTAIN_COLUMN_NAME_TYPE) {
						beginIndex = this.getFromIndexOfData(row, beginIndex);
					}

					if (type == Constant.DATA_BEGIN_WITH_COLUMN_NAME_TYPE
							|| type == Constant.DATA_CONTAIN_COLUMN_NAME_TYPE) // b?t
																				// ??u
																				// t?
																				// c?t
																				// ti�u
																				// ??
																				// ho?c
																				// c?
																				// ti�
																				// ??
																				// n?m
																				// tr?n
																				// trong
																				// c?t
																				// data
					{
						endIndex = this.getToIndexOfData(row, endIndex);
					}

					String data = "";
					try {
						data = row.substring(beginIndex, endIndex + 1);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}

					if (data != null && !"".equals(data.trim())) {
						list.add(data);
					}

				}

			} else // n?u ch? c� duy nh?t m?t c?t
			{
				for (int i = beginRow; i < allRow.size(); i++) {
					String row = allRow.get(i);
					if (row != null && !"".equals(row.trim())) {
						list.add(row);
					}
				}
			}

		}
		return list;

	}

	public String getMathResult(String expression) {

		ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
		Object result = null;
		try {
			System.out.println("Bieu thuc tim ?uoc: " + expression);
			result = engine.eval(expression);

		} catch (ScriptException e) {
			logger.error(e.getMessage(), e);
		}
		if (result == null) {
			return null;
		}
		return result.toString();

	}

	public List<MapEntry> getResultOfLog(String defaultLog, String log, String strParameter) // ki?u
																								// v?
																								// tr�
																								// trong
																								// log
	{
		List<MapEntry> mapObjects = new ArrayList<>();
		String[] arrParameter = strParameter.trim().split(Constant.SPLIST_CHAR);

		List<String> defaultList = subRowToList(defaultLog);
		List<String> list = subRowToList(log);
		for (String parameter : arrParameter) {
			if (parameter == null || "".equals(parameter.trim())) {
				continue;
			}
			String value = "";
			int index = defaultList.indexOf(parameter);
			if (index != -1) {
				try {
					value = list.get(index);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			MapEntry mapObject = new MapEntry(parameter, value);
//			mapObject.setKey(parameter);
//			mapObject.setValue(value);
			mapObjects.add(mapObject);

		}

		return mapObjects;
	}

	public List<String> getLineByPattern(String log, String pattern) {

		List<String> listResult = new ArrayList<>();
		List<String> allLine = this.subLogToList(log);
		Pattern p = Pattern.compile(pattern);
		Matcher matcher;

		for (String line : allLine) {
			matcher = p.matcher(line);
			boolean check = matcher.matches();
			if (check) {
				listResult.add(line);
			}
		}
		return listResult;

	}

	/*
	 * public static void main(String[] args) { try {
	 * 
	 * String result=""; BufferedReader br = new BufferedReader(new
	 * FileReader("D://voffice_check.txt")); try { StringBuilder sb = new
	 * StringBuilder(); String line = br.readLine();
	 * 
	 * while (line != null) { sb.append(line);
	 * sb.append(System.lineSeparator()); line = br.readLine(); } result =
	 * sb.toString(); } finally { br.close(); }
	 * 
	 * GetResultUtil resultUtil=new GetResultUtil(); List<String>
	 * arr=resultUtil.subLogToList(result);
	 * System.err.println("*****************"); for(String str:arr) {
	 * System.err.println(str); }
	 * 
	 * } catch (Exception e) { logger.error(e.getMessage(), e); } }
	 */

	public static void main(String[] args) {
		/*GetResultUtil util = new GetResultUtil();

		String abc = "  1235.0%";
		System.out.println(util.checkFloatType(abc));*/

		try {
			System.out.println(PasswordEncoderQltn.encrypt("quanns2"));
		} catch (GeneralSecurityException e) {
			logger.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
