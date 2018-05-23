package com.evtape.schedule.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExcelUtil {

	private final static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

	/**
	 * 报表导出，生成一个sheet页
	 * 
	 * @param name,sheet名
	 * @param headinfo,key是字段名,value是对应的中文表头
	 * @param datalist,接收一切类型的bean
	 * @return
	 */
	public static Object createExcel(String name, LinkedHashMap<String, String> headinfo, List<Object> datalist) {

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFCellStyle style = initStyle(workbook);
		HSSFSheet sheet;
		HSSFRow row;
		HSSFCell cell;
		sheet = workbook.createSheet(name);
		row = sheet.createRow(0);
		// 自动列宽
		sheet.autoSizeColumn(0);
		// key 字段名
		// Value 字段对应的表头名
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
		ObjectMapper objectMapper = new ObjectMapper();
		// datalist
		int a = 1;
		for (Object o : datalist) {
			try {
				String string = objectMapper.writeValueAsString(o);
				JSONObject object = JSONObject.parseObject(string);
				row = sheet.createRow(a);
				for (int i = 0; i < key.size(); i++) {
					cell = row.createCell(i);
					cell.setCellValue(object.get(key.get(i)).toString());
				}
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			a++;
		}
		// try {
		// String path = "D:/asdfasdfasdfasdfasdfasdf/a.xls";
		// OutputStream outputStream = new FileOutputStream(path);
		// workbook.write(outputStream);
		// outputStream.flush();
		// outputStream.close();
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		return workbook;
	}

	public static List<Object> readExcel(MultipartFile excelFile, LinkedHashMap<String, String> headinfo, Class<?> c) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Object object = objectMapper.readValue("", c);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// public static void main(String[] args) {
	// class a {
	// a(String name, String age) { this.age = age; this.name = name; }
	// private String name;
	// private String age;
	// public String getName() { return name; }
	// public void setName(String name) { this.name = name; }
	// public String getAge() { return age; }
	// public void setAge(String age) { this.age = age; }
	// }
	// LinkedHashMap<String, String> headinfo = new LinkedHashMap<String,
	// String>();
	// headinfo.put("age", "年龄");
	// headinfo.put("name", "姓名");
	// List<Object> datalist = new ArrayList<Object>();
	// datalist.add(new a("小王", "1"));
	// datalist.add(new a("小刘", "2"));
	// datalist.add(new a("小张", "3"));
	// datalist.add(new a("小李", "4"));
	// createExcel("123", headinfo, datalist);
	// }
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
		subjectStyle.setFillForegroundColor(HSSFColor.WHITE.index);

		HSSFFont font = workbook.createFont();
		font.setFontName("黑体");
		subjectStyle.setFont(font);
		return subjectStyle;
	}
}
