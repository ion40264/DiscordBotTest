
package jp.highwide.common.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.highwide.common.excel.ExcelEntity.Align;

/**
 * Excelのユーティリティ
 */
public class ExcelUtil {
	private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

	/**
	 * Excelを読み込む。
	 *
	 * @param sheetNo
	 *            読み込むシートの番号（0から始まる）
	 * @param <T>
	 *            ExcelのBeanクラス
	 * @param reader
	 *            ExcelのInputStream
	 * @param beanClass
	 *            ExcelのBeanクラス
	 * @return Excelのリスト
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public static <T> List<T> readExcelToBean(int sheetNo, int startRowNum, InputStream is, Class<T> beanClass)
			throws InstantiationException, IllegalAccessException, IOException, NoSuchFieldException, SecurityException,
			NoSuchMethodException, InvocationTargetException {
		List<T> result = new ArrayList<T>();

		// ワークブックを読み込む
		XSSFWorkbook workbook;
		workbook = new XSSFWorkbook(is);
		try {

			// シートを読み込む
			XSSFSheet sheet = workbook.getSheetAt(sheetNo);

			// BeanのフィールドのExcelColumnアノテーションを読み込む
			TreeMap<Integer, Field> treeMap = new TreeMap<Integer, Field>();
			Field[] fields = beanClass.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
				if (excelColumn != null) {
					treeMap.put(excelColumn.columnIndex(), field);
				}
			}
			ArrayList<Field> fieldList = new ArrayList<Field>(treeMap.values());
			int rowNum = startRowNum;
			while (true) {
				XSSFRow row = sheet.getRow(rowNum);
				rowNum++;
				if (row == null) {
					break;
				}
				T obj = beanClass.getDeclaredConstructor().newInstance();
				int columnNum = 0;
				for (Field field : fieldList) {
					try {
						XSSFCell cell = row.getCell(columnNum);

						ExcelConvertor excelConvertor = field.getAnnotation(ExcelConvertor.class);
						if (excelConvertor != null) {
							setExcelConvertor(beanClass, obj, field, cell, excelConvertor);
							columnNum++;
							continue;
						}

						if (field.getGenericType().equals(String.class)) {
							String str = "";
							if (!isCellBlank(cell)) {
								switch (cell.getCellType()) {
								case STRING:
									field.set(obj, cell.getRichStringCellValue().toString());
									break;
								case NUMERIC:
									if (DateUtil.isCellDateFormatted(cell)) {
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
										str = sdf.format(cell.getDateCellValue());
									} else {
										str = (long) cell.getNumericCellValue() + "";
									}
									field.set(obj, str);
									break;
								case FORMULA:
									field.set(obj, cell.getCellFormula());
									break;
								default:
									break;
								}

							} else {
								str = null;
								field.set(obj, str);
							}
						} else if (field.getGenericType().equals(Integer.class)
								|| field.getGenericType().equals(int.class)) {
							if (!isCellBlank(cell)) {
								field.set(obj, (int) cell.getNumericCellValue());
							} else {
								field.set(obj, null);
							}
						} else if (field.getGenericType().equals(Long.class)
								|| field.getGenericType().equals(long.class)) {
							if (!isCellBlank(cell)) {
								field.set(obj, (long) cell.getNumericCellValue());
							} else {
								field.set(obj, null);
							}
						} else if (field.getGenericType().equals(Boolean.class)
								|| field.getGenericType().equals(boolean.class)) {
							if (!isCellBlank(cell)) {
								switch (cell.getCellType()) {
								case BOOLEAN:
									field.set(obj, Boolean.valueOf(cell.getBooleanCellValue()));
									break;
								case STRING:
									field.set(obj, Boolean.valueOf(cell.getRichStringCellValue().toString()));
									break;
								default:
									break;
								}
							} else {
								field.set(obj, false);
							}
						} else if (field.getGenericType().equals(Double.class)
								|| field.getGenericType().equals(double.class)) {
							if (!isCellBlank(cell)) {
								field.set(obj, cell.getNumericCellValue());
							} else {
								field.set(obj, 0.0);
							}
						} else if (field.getGenericType().equals(Float.class)
								|| field.getGenericType().equals(float.class)) {
							if (!isCellBlank(cell)) {
								field.set(obj, Float.valueOf((float) cell.getNumericCellValue()));
							} else {
								field.set(obj, 0.0F);
							}
						} else if (field.getGenericType().equals(Date.class)) {
							if (!isCellBlank(cell)) {
								switch (cell.getCellType()) {
								case STRING:
									String str = cell.getRichStringCellValue().toString();
									Date date = null;
									SimpleDateFormat sdf;
									try {
										sdf = new SimpleDateFormat("H:m:s");
										date = sdf.parse(str);
									} catch (java.text.ParseException e) {
									}
									if (date == null) {
										try {
											sdf = new SimpleDateFormat("yyyy/MM/dd H:m:s");
											date = sdf.parse(str);
										} catch (java.text.ParseException e) {
										}
									}
									if (date == null) {
										try {
											sdf = new SimpleDateFormat("yyyy/MM/dd");
											date = sdf.parse(str);
										} catch (java.text.ParseException e) {
										}
									}
									if (date == null) {
										try {
											sdf = new SimpleDateFormat("yyyy-MM-dd");
											date = sdf.parse(str);
										} catch (java.text.ParseException e) {
										}
									}
									if (date == null) {
										try {
											sdf = new SimpleDateFormat("yyyy-MM-dd H:m:s");
											date = sdf.parse(str);
										} catch (java.text.ParseException e) {
										}
									}
									if (date == null) {
										throw new Exception("日付の型が不正です");
									}
									field.set(obj, date);
									break;
								default:
									field.set(obj, cell.getDateCellValue());
									break;
								}
							} else {
								field.set(obj, null);
							}
						} else if (field.getGenericType().equals(Time.class)) {
							if (!isCellBlank(cell)) {
								switch (cell.getCellType()) {
								case STRING:
									String str = cell.getRichStringCellValue().toString();
									field.set(obj, Time.valueOf(str));
								default:
									Date d = cell.getDateCellValue();
									Time t = new Time(d.getTime());
									field.set(obj, t);
								}
							} else {
								field.set(obj, null);
							}
						} else {
							throw new RuntimeException("Beanの形式が不正です。型=" + field.getGenericType());
						}
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(
								"不正な値が入力されています。行番号=" + rowNum + " 列番号=" + convertColumnNum(columnNum),
								e);
					} catch (IllegalStateException e) {
						throw new RuntimeException(
								"不正な値が入力されています。行番号=" + rowNum + " 列番号=" + convertColumnNum(columnNum),
								e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(
								"不正な値が入力されています。行番号=" + rowNum + " 列番号=" + convertColumnNum(columnNum),
								e);
					} catch (Exception e) {
						throw new RuntimeException(
								"不正な値が入力されています。行番号=" + rowNum + " 列番号=" + convertColumnNum(columnNum),
								e);
					}
					columnNum++;
				}
				result.add(obj);
			}
		} finally {
			workbook.close();
		}
		return result;
	}

	private static <T> void setExcelConvertor(Class<T> beanClass, T obj, Field field, XSSFCell cell,
			ExcelConvertor excelConvertor) throws IllegalAccessException, InvocationTargetException {
		String convToObjMethod = null;
		convToObjMethod = excelConvertor.convToObjMethod();
		if (convToObjMethod != null) {
			Method[] methods = beanClass.getMethods();
			Method targetMethod = null;
			for (Method method : methods) {
				if (method.getName().equals(convToObjMethod)) {
					targetMethod = method;
					break;
				}
			}
			Class<?> parameterType = targetMethod.getParameterTypes()[0];
			Object invokeValue = null;
			if (isCellBlank(cell)) {
				return;
			} else if (parameterType.equals(String.class)) {
				invokeValue = targetMethod.invoke(obj, cell.getRichStringCellValue().toString());
			} else if (parameterType.equals(Boolean.class) || parameterType.equals(boolean.class)) {
				invokeValue = targetMethod.invoke(obj, cell.getBooleanCellValue());
			} else if (parameterType.equals(Integer.class) || parameterType.equals(int.class)) {
				invokeValue = targetMethod.invoke(obj, Integer.valueOf((int) cell.getNumericCellValue()));
			} else if (parameterType.equals(Long.class) || parameterType.equals(long.class)) {
				invokeValue = targetMethod.invoke(obj, Long.valueOf((long) cell.getNumericCellValue()));
			} else if (parameterType.equals(Float.class) || parameterType.equals(float.class)) {
				invokeValue = targetMethod.invoke(obj, Float.valueOf((float) cell.getNumericCellValue()));
			} else if (parameterType.equals(Double.class) || parameterType.equals(double.class)) {
				invokeValue = targetMethod.invoke(obj, cell.getNumericCellValue());
			} else if (parameterType.equals(Date.class)) {
				invokeValue = targetMethod.invoke(obj, cell.getDateCellValue());
			}
			field.set(obj, invokeValue);
		}
	}

	public static <T> void writeBeanToExcel(List<T> recordList, Class<T> beanClass, OutputStream os)
			throws IOException {
		XSSFWorkbook writeBeanToExcel = writeBeanToExcel(recordList, beanClass, null, 0, null);
		writeBeanToExcel.write(os);
	}

	public static <T> XSSFWorkbook writeBeanToExcel(List<T> recordList, Class<T> beanClass) {
		return writeBeanToExcel(recordList, beanClass, null, 0, null);
	}

	/**
	 * ExcelのXSSFWorkbookを作成する。
	 *
	 * @param <T>
	 *            ExcelのBeanクラス
	 * @param recordList
	 *            レコードリスト
	 * @param beanClass
	 *            ExcelのBeanクラス
	 * @param template
	 *            エクセルのテンプレート。不要の場合はnull
	 * @param startRowNum
	 *            何行目から書き込むか。0が初期値
	 * @return ExcelのXSSFWorkbook
	 */
	public static <T> XSSFWorkbook writeBeanToExcel(List<T> recordList, Class<T> beanClass, InputStream template,
			int startRowNum, String sheetName) {
		XSSFWorkbook workbook;
		try {
			if (template != null) {
				workbook = new XSSFWorkbook(template);
			} else {
				workbook = new XSSFWorkbook();
			}

			XSSFCellStyle dateStyle = workbook.createCellStyle();
			XSSFCellStyle headerStyle = null;
			XSSFCellStyle bodyStyle = null;

			TreeMap<Integer, String> headerMap = new TreeMap<Integer, String>();
			TreeMap<Integer, Field> fieldMap = new TreeMap<Integer, Field>();
			ExcelEntity excelEntity = beanClass.getAnnotation(ExcelEntity.class);
			boolean isHeader = true;
			if (excelEntity != null) {
				// ヘッダありorなし
				isHeader = excelEntity.header();
				// ヘッダのAlignスタイル
				if (excelEntity.headerStyle() == Align.ALIGN_CENTER) {
					XSSFCellStyle centerStyle = workbook.createCellStyle();
					centerStyle.setAlignment(HorizontalAlignment.CENTER);
					Font font = workbook.createFont();
					font.setFontName("ＭＳ Ｐゴシック");
					centerStyle.setFont(font);
					headerStyle = centerStyle;
				} else if (excelEntity.headerStyle() == Align.ALIGN_LEFT) {
					XSSFCellStyle leftStyle = workbook.createCellStyle();
					leftStyle.setAlignment(HorizontalAlignment.LEFT);
					Font font = workbook.createFont();
					font.setFontName("ＭＳ Ｐゴシック");
					leftStyle.setFont(font);
					headerStyle = leftStyle;
				} else if (excelEntity.headerStyle() == Align.ALIGN_RIGHT) {
					XSSFCellStyle rightStyle = workbook.createCellStyle();
					rightStyle.setAlignment(HorizontalAlignment.RIGHT);
					Font font = workbook.createFont();
					font.setFontName("ＭＳ Ｐゴシック");
					rightStyle.setFont(font);
					headerStyle = rightStyle;
				}

				headerStyle.setBorderTop(BorderStyle.THIN);
				headerStyle.setBorderLeft(BorderStyle.THIN);
				headerStyle.setBorderRight(BorderStyle.THIN);
				headerStyle.setBorderBottom(BorderStyle.DOUBLE);
				headerStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
				headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

				bodyStyle = workbook.createCellStyle();
				bodyStyle.setBorderTop(BorderStyle.THIN);
				bodyStyle.setBorderLeft(BorderStyle.THIN);
				bodyStyle.setBorderRight(BorderStyle.THIN);
				bodyStyle.setBorderBottom(BorderStyle.THIN);
				Font font = workbook.createFont();
				font.setFontName("ＭＳ Ｐゴシック");
				bodyStyle.setFont(font);

				dateStyle.setBorderTop(BorderStyle.THIN);
				dateStyle.setBorderLeft(BorderStyle.THIN);
				dateStyle.setBorderRight(BorderStyle.THIN);
				dateStyle.setBorderBottom(BorderStyle.THIN);
				font = workbook.createFont();
				font.setFontName("ＭＳ Ｐゴシック");
				dateStyle.setFont(font);
			}

			Field[] fields = beanClass.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
				if (excelColumn != null) {
					if (ExcelColumn.NON.equals(excelColumn.columnName())) {
						headerMap.put(excelColumn.columnIndex(), field.getName());
					} else {
						headerMap.put(excelColumn.columnIndex(), excelColumn.columnName());
					}
					fieldMap.put(excelColumn.columnIndex(), field);
				}
			}

			if (template == null) {
				workbook.createSheet();
			}
			XSSFSheet sheet = workbook.getSheetAt(0);
			if (sheetName != null) {
				workbook.setSheetName(0, sheetName);
			}

			// ヘッダ作成
			int rowNum = startRowNum;
			int columnNum = 0;
			if (isHeader) {
				XSSFRow row = sheet.getRow(rowNum);
				if (row == null) {
					row = sheet.createRow(rowNum);
				}
				rowNum++;
				for (String head : headerMap.values()) {
					XSSFCell cell = row.getCell(columnNum);
					if (cell == null) {
						cell = row.createCell(columnNum);
					}
					columnNum++;
					cell.setCellValue(new XSSFRichTextString(head));
					if (headerStyle != null) {
						cell.setCellStyle(headerStyle);
					}
				}
			}

			// ボディ作成
			int maxColumnSize = 0;
			for (T record : recordList) {
				XSSFRow row = sheet.getRow(rowNum);
				if (row == null) {
					row = sheet.createRow(rowNum);
				}
				rowNum++;

				columnNum = 0;

				if (maxColumnSize < headerMap.size()) {
					maxColumnSize = headerMap.size();
				}

				for (Field cellField : fieldMap.values()) {
					Object cellVal = cellField.get(record);
					XSSFCell cell = row.getCell(columnNum);
					if (cell == null) {
						cell = row.createCell(columnNum);
					}
					columnNum++;
					// 値
					if (cellVal == null) {
						cell.setCellStyle(bodyStyle);
					} else if (cellVal instanceof Number) {
						ExcelConvertor excelConvertor = cellField.getAnnotation(ExcelConvertor.class);
						if (excelConvertor != null) {
							String convToExcelMethod = null;
							convToExcelMethod = excelConvertor.convToExcelMethod();
							if (convToExcelMethod != null) {
								getExcelConvertor(cellField, convToExcelMethod, record, cell, bodyStyle);
								continue;
							}
						}

						String cellValNum = String.valueOf(cellVal);
						cell.setCellValue(Double.valueOf(cellValNum));
						cell.setCellStyle(bodyStyle);
					} else if (cellVal instanceof Date) {
						ExcelConvertor excelConvertor = cellField.getAnnotation(ExcelConvertor.class);
						if (excelConvertor != null) {
							String convToExcelMethod = null;
							convToExcelMethod = excelConvertor.convToExcelMethod();
							if (convToExcelMethod != null) {
								getExcelConvertor(cellField, convToExcelMethod, record, cell, bodyStyle);
								continue;
							}
						}

						XSSFDataFormat dateFormat = workbook.createDataFormat();
						dateStyle.setDataFormat(dateFormat.getFormat("yyyy/mm/dd hh:mm:ss"));
						Date cellValDate = (Date) cellVal;
						cell.setCellStyle(dateStyle);
						cell.setCellValue(cellValDate);
					} else {
						String val = String.valueOf(cellVal);
						if (val.startsWith("=")) {
							ExcelConvertor excelConvertor = cellField.getAnnotation(ExcelConvertor.class);
							if (excelConvertor != null) {
								String convToExcelMethod = null;
								convToExcelMethod = excelConvertor.convToExcelMethod();
								if (convToExcelMethod != null) {
									getExcelConvertor(cellField, convToExcelMethod, record, cell, bodyStyle);
									continue;
								}
							}

							val = val.substring(1, val.length());
							cell.setCellFormula(val);
						} else {
							ExcelConvertor excelConvertor = cellField.getAnnotation(ExcelConvertor.class);
							if (excelConvertor != null) {
								String convToExcelMethod = null;
								convToExcelMethod = excelConvertor.convToExcelMethod();
								if (convToExcelMethod != null) {
									getExcelConvertor(cellField, convToExcelMethod, record, cell, bodyStyle);
									continue;
								}
							}

							cell.setCellValue(new XSSFRichTextString(val));
						}
						cell.setCellStyle(bodyStyle);
					}
				}
			}

			for (int i = 0; i < maxColumnSize; i++) {
				sheet.autoSizeColumn(i);
			}
			sheet.setForceFormulaRecalculation(true);
		} catch (

		Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Excelの書き込みに失敗しました。", e);
		}

		return workbook;
	}

	private static void getExcelConvertor(Field cellField, String convToExcelMethod, Object obj, XSSFCell cell,
			XSSFCellStyle bodyStyle)
			throws IllegalAccessException, InvocationTargetException, IllegalArgumentException, InstantiationException {

		Method[] methods = obj.getClass().getDeclaredMethods();
		Method targetMethod = null;
		for (Method method : methods) {
			if (method.getName().equals(convToExcelMethod)) {
				targetMethod = method;
				break;
			}
		}
		Object invokeValue = targetMethod.invoke(obj, cellField.get(obj));

		if (invokeValue instanceof Integer) {
			cell.setCellValue((Integer) invokeValue);
		} else if (invokeValue instanceof Double) {
			cell.setCellValue((Double) invokeValue);
		} else if (invokeValue instanceof Float) {
			cell.setCellValue((Float) invokeValue);
		} else if (invokeValue instanceof Long) {
			cell.setCellValue((Long) invokeValue);
		} else if (invokeValue instanceof String) {
			cell.setCellValue((String) invokeValue);
		} else if (invokeValue instanceof Boolean) {
			cell.setCellValue((Boolean) invokeValue);
		} else if (invokeValue instanceof Date) {
			cell.setCellValue((Date) invokeValue);
		}
		cell.setCellStyle(bodyStyle);
	}

	private static boolean isCellBlank(XSSFCell cell) {
		if (cell == null) {
			return true;
		}
		switch (cell.getCellType()) {
		case STRING:
			if ("".equals(cell.getRichStringCellValue().toString())) {
				return true;
			}
			break;
		case FORMULA:
			if ("".equals(cell.getRichStringCellValue().toString())) {
				return true;
			}
			break;
		case BLANK:
			return true;
		case NUMERIC:
			if (cell.getNumericCellValue() == 0.0) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}

	public static enum Difference {
		NONE, CHANGE, NEW, DELTE
	}

	public static class RecordPair {
		public Difference difference;
		public List<ColumnVal> record;
	}

	private static class PkeyPair<T> {
		public List<Object> pkeyList;
		public T val;

		@Override
		public String toString() {
			return "pkey=" + pkeyList + " val=" + val;
		}
	}

	public static <T> List<RecordPair> getDifferenceRecordList(List<T> newList, List<T> oldList, Class<T> beanClass)
			throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException {

		ArrayList<RecordPair> result = new ArrayList<>();

		// フィールドをカラム順に
		TreeMap<Integer, String> treeMap = new TreeMap<>();
		Field[] fields = beanClass.getDeclaredFields();
		for (Field field : fields) {
			ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
			if (excelColumn != null) {
				treeMap.put(excelColumn.columnIndex(), field.getName());
			}
		}
		ArrayList<String> fieldNameList = new ArrayList<>(treeMap.values());

		// Pkeyのフィールドを取得
		List<Field> pkeyFieldList = new ArrayList<Field>();
		for (Field field : fields) {
			ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
			if (excelColumn.pkey() == true) {
				pkeyFieldList.add(field);
			}
		}

		// 全てのPKEYを取得する
		LinkedHashSet<List<Object>> allPkeyList = new LinkedHashSet<>();
		List<T> oldList2 = new ArrayList<>();
		oldList2.addAll(oldList);
		for (T newT : newList) {
			List<Object> newPkeyValList = new ArrayList<>();
			for (Field pkeyField : pkeyFieldList) {
				newPkeyValList.add(pkeyField.get(newT));
			}
			for (T oldT : oldList2) {
				List<Object> oldPkeyValList = new ArrayList<>();
				for (Field pkeyField : pkeyFieldList) {
					oldPkeyValList.add(pkeyField.get(oldT));
				}
				oldList2.remove(oldT);
				if (!newPkeyValList.equals(oldPkeyValList)) {
					allPkeyList.add(oldPkeyValList);
				} else {
					break;
				}
			}
			allPkeyList.add(newPkeyValList);
		}

		ArrayList<PkeyPair<T>> newPkeyList = new ArrayList<>();
		ArrayList<PkeyPair<T>> oldPkeyList = new ArrayList<>();
		int i = 0;
		int j = 0;
		PkeyPair<T> newPair;
		PkeyPair<T> oldPair;
		for (List<Object> o : allPkeyList) {
			T newListVal = null;
			T oldListVal = null;
			List<Object> newPkeyVal = new ArrayList<>();
			List<Object> oldPkeyVal = new ArrayList<>();
			try {
				newListVal = newList.get(i);
				for (Field pkeyField : pkeyFieldList) {
					newPkeyVal.add(pkeyField.get(newListVal));
				}
			} catch (Exception e) {
			}
			try {
				oldListVal = oldList.get(j);
				for (Field pkeyField : pkeyFieldList) {
					oldPkeyVal.add(pkeyField.get(oldListVal));
				}
			} catch (Exception e) {
			}

			if (o.equals(newPkeyVal) && o.equals(oldPkeyVal)) {
				newPair = new PkeyPair<>();
				newPair.pkeyList = newPkeyVal;
				newPair.val = newListVal;
				oldPair = new PkeyPair<>();
				oldPair.pkeyList = oldPkeyVal;
				oldPair.val = oldListVal;

				newPkeyList.add(newPair);
				oldPkeyList.add(oldPair);
				i++;
				j++;
			} else if (o.equals(newPkeyVal) && !o.equals(oldPkeyVal)) {
				newPair = new PkeyPair<>();
				newPair.pkeyList = newPkeyVal;
				newPair.val = newListVal;
				oldPair = new PkeyPair<>();
				oldPair.pkeyList = null;
				oldPair.val = null;

				newPkeyList.add(newPair);
				oldPkeyList.add(oldPair);
				i++;
			} else if (!o.equals(newPkeyVal) && o.equals(oldPkeyVal)) {
				newPair = new PkeyPair<>();
				newPair.pkeyList = null;
				newPair.val = null;
				oldPair = new PkeyPair<>();
				oldPair.pkeyList = oldPkeyVal;
				oldPair.val = oldListVal;

				newPkeyList.add(newPair);
				oldPkeyList.add(oldPair);
				j++;
			} else {
				System.err.println("newPkeyVal=" + newPkeyVal + " oldPkeyVal=" + oldPkeyVal);
			}
		}

		for (i = 0; i < allPkeyList.size(); i++) {
			List<ColumnVal> record = new ArrayList<>();

			PkeyPair<T> newPkeyPair = newPkeyList.get(i);
			PkeyPair<T> oldPkeyPair = oldPkeyList.get(i);

			Object newPkeyVal = newPkeyPair.pkeyList;
			Object oldPkeyVal = oldPkeyPair.pkeyList;
			T newListVal = newPkeyPair.val;
			T oldListVal = oldPkeyPair.val;
			if (newPkeyVal != null && oldPkeyVal != null) {
				// 同じ(カラムは違う可能性あり)
				List<ColumnVal> columnList = new ArrayList<>();
				boolean isChange = false;
				for (String fieldName : fieldNameList) {
					ColumnVal columnVal = new ColumnVal();
					Field declaredField = beanClass.getDeclaredField(fieldName);
					Object newColumnVal = declaredField.get(newListVal);
					Object oldColumnVal = declaredField.get(oldListVal);
					if (!newColumnVal.equals(oldColumnVal)) {
						columnVal.difference = Difference.CHANGE;
						isChange = true;
					} else {
						columnVal.difference = Difference.NONE;
					}
					columnVal.field = declaredField;
					columnVal.newObject = newColumnVal;
					columnVal.oldObject = oldColumnVal;
					columnList.add(columnVal);
				}
				record.addAll(columnList);
				RecordPair pair = new RecordPair();
				if (isChange) {
					pair.difference = Difference.CHANGE;
					pair.record = record;
					result.add(pair);
				} else {
					pair.difference = Difference.NONE;
					pair.record = record;
					result.add(pair);
				}
			} else if (newPkeyVal == null && oldPkeyVal != null) {
				// 削除
				List<ColumnVal> columnList = new ArrayList<>();
				for (String fieldName : fieldNameList) {
					ColumnVal columnVal = new ColumnVal();
					Field declaredField = beanClass.getDeclaredField(fieldName);
					Object oldColumnVal = declaredField.get(oldListVal);
					columnVal.difference = Difference.DELTE;
					columnVal.field = declaredField;
					columnVal.newObject = null;
					columnVal.oldObject = oldColumnVal;
					columnList.add(columnVal);
				}
				record.addAll(columnList);
				RecordPair pair = new RecordPair();
				pair.difference = Difference.DELTE;
				pair.record = record;
				result.add(pair);
			} else if (newPkeyVal != null && oldPkeyVal == null) {
				// 挿入
				List<ColumnVal> columnList = new ArrayList<>();
				for (String fieldName : fieldNameList) {
					ColumnVal columnVal = new ColumnVal();
					Field declaredField = beanClass.getDeclaredField(fieldName);
					Object newColumnVal = declaredField.get(newListVal);
					columnVal.difference = Difference.NEW;
					columnVal.field = declaredField;
					columnVal.newObject = newColumnVal;
					columnVal.oldObject = null;
					columnList.add(columnVal);
				}
				record.addAll(columnList);
				RecordPair pair = new RecordPair();
				pair.difference = Difference.NEW;
				pair.record = record;
				result.add(pair);
			}
		}

		return result;
	}

	private static final String[] COLUMN_NAME = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
			"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH",
			"AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AW", "AX", "AY", "AZ",
			"BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BM", "BN", "BO", "BP", "BQ", "BR",
			"BS", "BT", "BU", "BV", "BW", "BX", "BY", "BZ", "CA", "CB", "CC", "CD", "CE", "CF", "CG", "CH", "CI", "CJ",
			"CK", "CL", "CM", "CN", "CO", "CP", "CQ", "CR", "CS", "CT", "CU", "CV", "CW", "CX", "CY", "CZ" };

	private static String convertColumnNum(int num) {
		String result;
		try {
			result = COLUMN_NAME[num];
		} catch (Exception e) {
			result = num + 1 + "";
		}

		return result;
	}
}
