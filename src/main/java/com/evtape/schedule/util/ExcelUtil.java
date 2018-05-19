package com.evtape.schedule.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelUtil {

	private final static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

	public static Object createExcel(String name, HashMap<String, String> headinfo, List<Object> datalist) {

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFCellStyle style = initStyle(workbook);
		HSSFSheet sheet;
		HSSFRow row;
		HSSFCell cell;
		sheet = workbook.createSheet(name);
		row = sheet.createRow(0);
		// 自动列宽
		sheet.autoSizeColumn(0);
		List<String> key = new ArrayList<String>();
		List<String> head = new ArrayList<String>();
		for (String string : headinfo.keySet()) {
			key.add(string);
			head.add(headinfo.get(string));
		}
		for (int i = 0; i < head.size(); i++) {
			cell = row.createCell(i);
			cell.setCellValue(head.get(i));
			cell.setCellStyle(style);
		}
		// datalist
		for (Object o : datalist) {
			
			
		}
		
		
		return null;
	}

	public static HSSFCellStyle initStyle(HSSFWorkbook workbook) {
		HSSFCellStyle subjectStyle = workbook.createCellStyle();
		// 设置四边框
		subjectStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		subjectStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		subjectStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		subjectStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

		subjectStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		subjectStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		subjectStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		// subjectStyle.setFillForegroundColor(HSSFColor.YELLOW.index);

		HSSFFont font = workbook.createFont();
		font.setFontName("黑体");
		subjectStyle.setFont(font);
		return subjectStyle;
	}

	public static Object createOneSheet(String name, Map<String, String> tablehead, List<Object> datalist) {

		return null;
	}

}
