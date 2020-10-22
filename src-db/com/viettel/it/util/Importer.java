package com.viettel.it.util;

import com.google.gson.GsonBuilder;
import com.viettel.controller.AppException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.primefaces.event.FileUploadEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public abstract class Importer<T extends Serializable> {
	private static Logger logger = LogManager.getLogger(Importer.class);

	private Class<T> domainClass ;
	private Map<Integer,String> indexMapFieldClass = getIndexMapFieldClass();
	private String dateFormat = "MM/dd/yyyy";
	
	/**
	 * 
	 * @return Map<Integer,String>
	 * <br>Start column = 0
	 * @author huynx6
	 * 
	 */
	protected abstract Map<Integer,String> getIndexMapFieldClass();
	protected abstract String getDateFormat();
	private Map<Integer,String> mapHeader = getMapHeader();

	/*20180724_hoangnd_fix import param to mop_start*/
	private Integer rowHeaderNumber;
	private boolean isReplace = true;
	private boolean isReplaceSpace = true;
    /*20180724_hoangnd_fix import param to mop_end*/

	@SuppressWarnings("unchecked")
	public Importer() {
		super();
		java.lang.reflect.Type genericSuperclass = getClass().getGenericSuperclass();
		if(genericSuperclass!=null && genericSuperclass instanceof ParameterizedType){
			this.domainClass = (Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
//			if(domainClass.getName().equals("java.io.Serializable")){
			if(domainClass instanceof Serializable){
				this.domainClass = (Class<T>) BasicDynaClass.class;
			}
		}else{
			this.domainClass = (Class<T>) BasicDynaClass.class;
		}
		if (getDateFormat()!=null)
			dateFormat = getDateFormat();
	}
	
	public List<T> getDatas(FileUploadEvent event, Integer sheetNumber, String sline){
		try {
			return getDatas(event.getFile().getInputstream(), sheetNumber, sline, null);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	public List<T> getDatas(FileUploadEvent event, String sheetName, String sline){
		try {
			return getDatas(event.getFile().getInputstream(), null, sline, sheetName);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	public List<T> getDatas(InputStream inputStream, Integer sheetNumber,String sline){
		return getDatas(inputStream, sheetNumber, sline, null);
	}
	public List<T> getDatas(InputStream inputStream,String sline, String sheetName){
		return getDatas(inputStream, null, sline, sheetName);
	}
	
	private List<T> getDatas(InputStream inputStream, Integer sheetNumber,String sline, String sheetName) {

		Workbook workbook = null;
		try {
			//Get the workbook instance for XLS/xlsx file 
			workbook = null;
			try {
				workbook = WorkbookFactory.create(inputStream);
//				if (workbook==null)
//					throw new NullPointerException();
			} catch (InvalidFormatException e2) {
				logger.error(e2.getMessage(), e2);
				throw new AppException("File import phải là Excel 97-2012 (xls, xlsx)!");
			}
			return getDatas(workbook, sheetNumber, sline, sheetName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<>();
		} finally {
			if (workbook != null)
				try {
					workbook.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}
	public List<T> getDatas(Workbook workbook , Integer sheetNumber, String sline){
		return getDatas(workbook, sheetNumber, sline, null);
	}
	public List<T> getDatas(Workbook workbook , String sheetName, String sline){
		return getDatas(workbook, null, sline, sheetName);
	}
	@SuppressWarnings("unchecked")
	private List<T> getDatas(Workbook workbook , Integer sheetNumber, String sline, String sheetName){
		try {
			if (workbook==null)
				throw new NullPointerException();
			
			String[] mulLine = sline.split(",",-1);
			 
			List<T> list = new LinkedList<T>();
			Sheet sheet = null;
			if (sheetNumber!=null)
				sheet = workbook.getSheetAt(sheetNumber);
			else if (sheetName!=null)
				sheet = workbook.getSheet(sheetName);
			List<Field> fields = new ArrayList<>();
			try {
				Class<?> cls = domainClass;
				fields = Arrays.asList(cls.getDeclaredFields());
			} catch (SecurityException e) {
				logger.error(e.getMessage(), e);
			}
			int line=1;
			String sLine;
			boolean isFindHeader = false;
			/*20180724_hoangnd_fix import param to mop_start*/
			if (sheet != null) {
				for (Iterator<Row> rowIterator = sheet.iterator(); rowIterator
						.hasNext();) {
					Row row = (Row) rowIterator.next();
					boolean cont = false;
					sLine="";
					if (rowHeaderNumber != null) {
						if (line == rowHeaderNumber) {
							if (indexMapFieldClass != null) {
								indexMapFieldClass.clear();
							} else {
								indexMapFieldClass = new HashMap<>();
							}
							for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
								if(row.getCell(i) != null) {
									if (isReplace) {
										//20170817_hienhv4_fix import param to mop_start
										if (isReplaceSpace) {
											indexMapFieldClass.put(i, row.getCell(i).getStringCellValue().trim().replace(" ", "_").replace(".", "_").toLowerCase());
										} else {
											indexMapFieldClass.put(i, row.getCell(i).getStringCellValue().trim().replace(".", "_").toLowerCase());
										}
										//20170817_hienhv4_fix import param to mop_end
									} else {
										//20170817_hienhv4_fix import param to mop_start
										if (isReplaceSpace) {
											indexMapFieldClass.put(i, row.getCell(i).getStringCellValue().trim().replace(" ", "__").replace(".", "___").toLowerCase());
										} else {
											indexMapFieldClass.put(i, row.getCell(i).getStringCellValue().trim().replace(".", "___").toLowerCase());
										}
										//20170817_hienhv4_fix import param to mop_emd
									}
								}
							}
							System.out.println("indexMapFieldClass: " + indexMapFieldClass);
						}
					}
					try {
						if (Pattern.compile("\""+line+"\"").matcher(new GsonBuilder().create().toJson(mulLine)).find())
							cont = true;
						else {
							for (String _sline : mulLine) {
								String[] strip = _sline.split("-",-1);
								if (strip.length ==1) {
									if(line == Integer.parseInt(strip[0])){
										cont=true;
										break;
									}
								}else if (strip.length ==2) {
									if(strip[1].length()==0){
										if(line >= Integer.parseInt(strip[0])){
											cont=true;
											break;
										}
									}else if(line >= Integer.parseInt(strip[0]) && line <= Integer.parseInt(strip[1]) ){
										cont=true;
										break;
									}

								}

							}
						}
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
					line++;
					if(!cont)
						continue;
					if(mapHeader!=null){

						if(!isFindHeader){
							boolean isCont =false;
							for(int i=row.getFirstCellNum();i<row.getLastCellNum();i++){
								if(row.getCell(i) != null && row.getCell(i).getCellType()== Cell.CELL_TYPE_STRING && mapHeader.get(1).equalsIgnoreCase(row.getCell(i).getStringCellValue())){
									isFindHeader=true;
									isCont = true;
									if(i>0){
										Map<Integer, String>_indexMapFieldClass = new HashMap<Integer, String>();
										for (Iterator<Integer> iterator = indexMapFieldClass.keySet().iterator(); iterator.hasNext();) {
											Integer col = (Integer) iterator.next();
											_indexMapFieldClass.put(i+col, indexMapFieldClass.get(col));
										}
										indexMapFieldClass.clear();
										indexMapFieldClass.putAll(_indexMapFieldClass);
									}
									break;
								}
							}
							if(isCont)
								continue;
						}
						if(!isFindHeader)
							continue;


					}
					{//Read data
						Class<?> obj = domainClass;

						Object objInstance = obj.newInstance();
						if (objInstance instanceof BasicDynaClass) {
							List<DynaProperty> dynaProperties = new ArrayList<DynaProperty>();
							for (Integer idx : indexMapFieldClass.keySet()) {
								dynaProperties.add(new DynaProperty(indexMapFieldClass.get(idx), String.class));
							}
							objInstance = new BasicDynaClass(sheetName, null, dynaProperties.toArray(new DynaProperty[dynaProperties.size()]) );
							objInstance = ((BasicDynaClass) objInstance).newInstance();
						}
						for (Iterator<Integer> columnIndex = indexMapFieldClass.keySet().iterator(); columnIndex
								.hasNext();) {
							Integer nColumn = columnIndex.next();
							Cell cell ;
							if( row.getCell(nColumn) == null ){
								cell = row.createCell(nColumn);
							} else {
								cell = row.getCell(nColumn);
							}
							Object cellValue = null;
							switch (cell.getCellType()) {
								case Cell.CELL_TYPE_BLANK:
									cellValue = new String();
									break;
								case Cell.CELL_TYPE_BOOLEAN:
									cell.setCellType(Cell.CELL_TYPE_STRING);
									cellValue = cell.getStringCellValue();
									break;
								case Cell.CELL_TYPE_FORMULA:
                                    try {
                                        cellValue = cell.getRichStringCellValue().getString();
                                    } catch (Exception e) {
                                        logger.debug(e.getMessage(), e);
                                        cellValue = String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0+$", "");
                                    }
                                    break;
								case Cell.CELL_TYPE_NUMERIC:
									cell.setCellType(Cell.CELL_TYPE_STRING);
									cellValue = cell.getStringCellValue();
									break;
								case Cell.CELL_TYPE_STRING:
								default:
									cellValue = cell.getStringCellValue();
									break;
							}
//							if (cellValue == null)
//								continue;
							sLine += cellValue.toString();
							if (fields.toString().contains(domainClass.getName() + "." + indexMapFieldClass.get(nColumn) + ",") || fields.toString().contains(domainClass.getName() + "." + indexMapFieldClass.get(nColumn) + "]")) {
								try {
									//Field field = fields.get(index);
									for (Field field : fields) {
										if(field.getName().equalsIgnoreCase(indexMapFieldClass.get(nColumn))){
											if ("java.lang.Long".contains(field.getType().getName())) {
												PropertyUtils.setSimpleProperty(objInstance, field.getName(), Long.parseLong(cellValue.toString()));
											} else if ("java.lang.Integer".contains(field.getType().getName()))  {
												PropertyUtils.setSimpleProperty(objInstance, field.getName(), Integer.parseInt(cellValue.toString()));
											} else if ("java.lang.String".contains(field.getType().getName()))  {
												PropertyUtils.setSimpleProperty(objInstance, field.getName(), cellValue);
											} else if ("Date".contains(field.getType().getName()))  {
												DateFormat formatter = new SimpleDateFormat(dateFormat);
												PropertyUtils.setSimpleProperty(objInstance, field.getName(),formatter.parse(cellValue.toString()) );
											} else if ("java.lang.Object".contains(field.getType().getName()))  {
												PropertyUtils.setSimpleProperty(objInstance, field.getName(), cellValue);
											}
											break;
										}
									}

								} catch (IllegalArgumentException e) {
									logger.error(e.getMessage(), e);
								}
							}else {
								if (objInstance instanceof BasicDynaBean) {
									PropertyUtils.setSimpleProperty(objInstance, Util.normalizeParamCode(indexMapFieldClass.get(nColumn)), cellValue);
								}
							}
						}
						if(sLine.trim().length()==0)
							break;
						list.add((T) objInstance);
					}
				}
			}
			/*20180724_hoangnd_fix import param to mop_end*/
			return list;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.err.println(e);
			return null;
		}
		
	}

	//20190416_tudn_start import rule config
	public Map<Integer,T> getDatasLineExcel(Workbook workbook , Integer sheetNumber, String sline, String sheetName){
		try {
			if (workbook==null)
				throw new NullPointerException();

			String[] mulLine = sline.split(",",-1);

			Map<Integer, T> list = new LinkedHashMap<>();
			Sheet sheet = null;
			if (sheetNumber!=null)
				sheet = workbook.getSheetAt(sheetNumber);
			else if (sheetName!=null)
				sheet = workbook.getSheet(sheetName);
			List<Field> fields = new ArrayList<>();
			try {
				Class<?> cls = domainClass;
				fields = Arrays.asList(cls.getDeclaredFields());
			} catch (SecurityException e) {
				logger.error(e.getMessage(), e);
			}
			int line=1;
			String sLine;
			boolean isFindHeader = false;
			/*20180724_hoangnd_fix import param to mop_start*/
			if (sheet != null) {
				for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
					Row row = sheet.getRow(rowIndex);
					boolean cont = false;
					sLine="";
					if (rowHeaderNumber != null) {
						if (line == rowHeaderNumber) {
							if (indexMapFieldClass != null) {
								indexMapFieldClass.clear();
							} else {
								indexMapFieldClass = new HashMap<>();
							}
							for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
								if(row.getCell(i) != null) {
									if (isReplace) {
										//20170817_hienhv4_fix import param to mop_start
										if (isReplaceSpace) {
											indexMapFieldClass.put(i, row.getCell(i).getStringCellValue().trim().replace(" ", "_").replace(".", "_").toLowerCase());
										} else {
											indexMapFieldClass.put(i, row.getCell(i).getStringCellValue().trim().replace(".", "_").toLowerCase());
										}
										//20170817_hienhv4_fix import param to mop_end
									} else {
										//20170817_hienhv4_fix import param to mop_start
										if (isReplaceSpace) {
											indexMapFieldClass.put(i, row.getCell(i).getStringCellValue().trim().replace(" ", "__").replace(".", "___").toLowerCase());
										} else {
											indexMapFieldClass.put(i, row.getCell(i).getStringCellValue().trim().replace(".", "___").toLowerCase());
										}
										//20170817_hienhv4_fix import param to mop_emd
									}
								}
							}
							System.out.println("indexMapFieldClass: " + indexMapFieldClass);
						}
					}
					try {
						if (Pattern.compile("\""+line+"\"").matcher(new GsonBuilder().create().toJson(mulLine)).find())
							cont = true;
						else {
							for (String _sline : mulLine) {
								String[] strip = _sline.split("-",-1);
								if (strip.length ==1) {
									if(line == Integer.parseInt(strip[0])){
										cont=true;
										break;
									}
								}else if (strip.length ==2) {
									if(strip[1].length()==0){
										if(line >= Integer.parseInt(strip[0])){
											cont=true;
											break;
										}
									}else if(line >= Integer.parseInt(strip[0]) && line <= Integer.parseInt(strip[1]) ){
										cont=true;
										break;
									}

								}

							}
						}
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
					line++;
					if(!cont)
						continue;
					if(mapHeader!=null){

						if(!isFindHeader){
							boolean isCont =false;
							for(int i=row.getFirstCellNum();i<row.getLastCellNum();i++){
								if(row.getCell(i) != null && row.getCell(i).getCellType()== Cell.CELL_TYPE_STRING && mapHeader.get(1).equalsIgnoreCase(row.getCell(i).getStringCellValue())){
									isFindHeader=true;
									isCont = true;
									if(i>0){
										Map<Integer, String>_indexMapFieldClass = new HashMap<Integer, String>();
										for (Iterator<Integer> iterator = indexMapFieldClass.keySet().iterator(); iterator.hasNext();) {
											Integer col = (Integer) iterator.next();
											_indexMapFieldClass.put(i+col, indexMapFieldClass.get(col));
										}
										indexMapFieldClass.clear();
										indexMapFieldClass.putAll(_indexMapFieldClass);
									}
									break;
								}
							}
							if(isCont)
								continue;
						}
						if(!isFindHeader)
							continue;


					}
					{//Read data
						Class<?> obj = domainClass;

						Object objInstance = obj.newInstance();
						if (objInstance instanceof BasicDynaClass) {
							List<DynaProperty> dynaProperties = new ArrayList<DynaProperty>();
							for (Integer idx : indexMapFieldClass.keySet()) {
								dynaProperties.add(new DynaProperty(indexMapFieldClass.get(idx), String.class));
							}
							objInstance = new BasicDynaClass(sheetName, null, dynaProperties.toArray(new DynaProperty[dynaProperties.size()]) );
							objInstance = ((BasicDynaClass) objInstance).newInstance();
						}
						for (Iterator<Integer> columnIndex = indexMapFieldClass.keySet().iterator(); columnIndex
								.hasNext();) {
							Integer nColumn = columnIndex.next();
							Cell cell ;
							if( row.getCell(nColumn) == null ){
								cell = row.createCell(nColumn);
							} else {
								cell = row.getCell(nColumn);
							}
							Object cellValue = null;
							switch (cell.getCellType()) {
								case Cell.CELL_TYPE_BLANK:
									cellValue = new String();
									break;
								case Cell.CELL_TYPE_BOOLEAN:
									cell.setCellType(Cell.CELL_TYPE_STRING);
									cellValue = cell.getStringCellValue();
									break;
								case Cell.CELL_TYPE_FORMULA:
									try {
										cellValue = cell.getRichStringCellValue().getString();
									} catch (Exception e) {
										logger.debug(e.getMessage(), e);
										cellValue = String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0+$", "");
									}
									break;
								case Cell.CELL_TYPE_NUMERIC:
									cell.setCellType(Cell.CELL_TYPE_STRING);
									cellValue = cell.getStringCellValue();
									break;
								case Cell.CELL_TYPE_STRING:
								default:
									cellValue = cell.getStringCellValue();
									break;
							}

							sLine += cellValue.toString();
							if (fields.toString().contains(domainClass.getName() + "." + indexMapFieldClass.get(nColumn) + ",") || fields.toString().contains(domainClass.getName() + "." + indexMapFieldClass.get(nColumn) + "]")) {
								try {
									//Field field = fields.get(index);
									for (Field field : fields) {
										if(field.getName().equalsIgnoreCase(indexMapFieldClass.get(nColumn))){
											if ("java.lang.Long".contains(field.getType().getName())) {
												PropertyUtils.setSimpleProperty(objInstance, field.getName(), Long.parseLong(cellValue.toString()));
											} else if ("java.lang.Integer".contains(field.getType().getName()))  {
												PropertyUtils.setSimpleProperty(objInstance, field.getName(), Integer.parseInt(cellValue.toString()));
											} else if ("java.lang.String".contains(field.getType().getName()))  {
												PropertyUtils.setSimpleProperty(objInstance, field.getName(), cellValue);
											} else if ("Date".contains(field.getType().getName()))  {
												DateFormat formatter = new SimpleDateFormat(dateFormat);
												PropertyUtils.setSimpleProperty(objInstance, field.getName(),formatter.parse(cellValue.toString()) );
											} else if ("java.lang.Object".contains(field.getType().getName()))  {
												PropertyUtils.setSimpleProperty(objInstance, field.getName(), cellValue);
											}
											break;
										}
									}

								} catch (IllegalArgumentException e) {
									logger.error(e.getMessage(), e);
								}
							}else {
								if (objInstance instanceof BasicDynaBean) {
									PropertyUtils.setSimpleProperty(objInstance, Util.normalizeParamCode(indexMapFieldClass.get(nColumn)), cellValue);
								}
							}
						}
						if(sLine.trim().length()==0 && rowIndex<=sheet.getLastRowNum())
							continue;
//						list.add((T) objInstance);
						list.put(rowIndex,(T) objInstance);
					}
				}
			}
			/*20180724_hoangnd_fix import param to mop_end*/
			return list;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.err.println(e);
			return null;
		}

	}
	//20190416_tudn_end import rule config

	public static String normalizeParamCode(String paramCode){
		if(paramCode!=null){
			return paramCode.replace("(", "").replace(")", "").replace("[", "").replace("]", "");
		}
		return null;
	}

	public void setIndexMapFieldClass(Map<Integer,String> indexMapFieldClass) {
		this.indexMapFieldClass = indexMapFieldClass;
	}
	public Map<Integer,String> getMapHeader() {
		return mapHeader;
	}
	public void setMapHeader(Map<Integer,String> mapHeader) {
		this.mapHeader = mapHeader;
	}
	public void setDomainClass(Class<T> domainClass) {
		this.domainClass = domainClass;
	}

	/*20180724_hoangnd_fix import param to mop_start*/
	public Integer getRowHeaderNumber() {
		return rowHeaderNumber;
	}

	public void setRowHeaderNumber(Integer rowHeaderNumber) {
		this.rowHeaderNumber = rowHeaderNumber;
	}

	public boolean isIsReplace() {
		return isReplace;
	}

	public void setIsReplace(boolean isReplace) {
		this.isReplace = isReplace;
	}

	public boolean isIsReplaceSpace() {
		return isReplaceSpace;
	}

	public void setIsReplaceSpace(boolean isReplaceSpace) {
		this.isReplaceSpace = isReplaceSpace;
	}
	/*20180724_hoangnd_fix import param to mop_end*/
}
