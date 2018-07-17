package com.evtape.schedule.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;

public class PoiUtil {

    private final static Logger logger = LoggerFactory.getLogger(PoiUtil.class);

    /**
     * <pre>
     *    指定获取某列所有数据
     * </pre>
     *
     * @return
     */
    public static List<Map<String, String>> readExcelSpecifyColNum(InputStream is, Integer readFromRowNum, Integer specifyColNum) {

        return readExcelSpecifyColNum(is, null, readFromRowNum, specifyColNum);
    }

    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, Integer sheetIndex, Integer readFromRowNum,
                                                             Integer readFromColNum) {
        return readExcelContent(excelFile, sheetIndex, null, readFromRowNum, readFromColNum);
    }

    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, String sheetName, Integer readFromRowNum, Integer readFromColNum) {
        return readExcelContent(excelFile, null, sheetName, readFromRowNum, readFromColNum);
    }

    /**
     * <pre>
     * 读取Excel数据内容
     * 约定格式要求：第一行为标题行，之后为数据行
     * 返回结构为Map结构的List集合：每行的key=第一行的标题，value=单元格值，统一为字符串，根据需要自行转换数据类型
     * </pre>
     *
     * @param excelFile
     * @param sheetName
     * @return Map 包含单元格数据内容的Map对象
     */
    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, String sheetName) {
        return readExcelContent(excelFile, sheetName, 0, 0);
    }

    /**
     * <pre>
     * 读取Excel数据内容
     * 约定格式要求：第readFromRowNum行为标题行，之后为数据行
     * 返回结构为Map结构的List集合：每行的key=第一行的标题，value=单元格值，统一为字符串，根据需要自行转换数据类型
     * </pre>
     *
     * @return Map 包含单元格数据内容的Map对象
     */
    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, String sheetName, Integer readFromRowNum) {
        return readExcelContent(excelFile, sheetName, readFromRowNum, 0);
    }

    /**
     * <pre>
     * 读取Excel数据内容
     * 约定格式要求：第readFromRowNum行为标题行，之后为数据行
     * 返回结构为Map结构的List集合：每行的key=第一行的标题，value=单元格值，统一为字符串，根据需要自行转换数据类型
     * </pre>
     *
     * @return Map 包含单元格数据内容的Map对象
     */
    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, Integer sheetIndex, Integer readFromRowNum) {
        return readExcelContent(excelFile, sheetIndex, null, readFromRowNum, 0);
    }

    /**
     * <pre>
     * 读取Excel数据内容
     * 约定格式要求：第(titleStartRowNum+1)行为标题行，之后为数据行
     * 返回结构为Map结构的List集合：每行的key=第(titleStartRowNum+1)行的标题，
     * value=单元格值，统一为字符串，根据需要自行转换数据类型
     * </pre>
     *
     * @param excelFile      表格名称
     * @param sheetName      读取工作标签项名称
     * @param readFromRowNum 以readFromRowNum作为标题开始读取
     * @param readFromColNum 从readFromColNum列开始读取
     * @return Map                       包含单元格数据内容的Map对象
     */
    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, Integer sheetIndex, String sheetName, Integer readFromRowNum,
                                                             Integer readFromColNum) {
        List<Map<String, String>> rows = Lists.newArrayList();
        if (excelFile.isEmpty()) {
            return rows;
        }
        InputStream is = null;
        String excelName = excelFile.getOriginalFilename();
        try {
            //读取Excel文件
            is = excelFile.getInputStream(); //this.getClass().getResourceAsStream(excelName);
            if (excelName.toLowerCase().endsWith(".xlsx")) {

                Workbook wb = new XSSFWorkbook(is);
                Sheet sheet = null;
                if (StringUtils.isNotBlank(sheetName)) {
                    logger.debug("Excel: {}, Sheet: {}", excelName, sheetName);
                    sheet = wb.getSheet(sheetName);
                } else {

                    if (null == sheetIndex) {
                        sheetIndex = 0;
                    }
                    sheet = wb.getSheetAt(sheetIndex);
                    sheetName = sheet.getSheetName();
                }
                int colNum = readFromColNum;
                Row row0 = sheet.getRow(0);
                // 标题总列数
                List<String> titleList = Lists.newArrayList();
                colNum=getTitleList(row0,titleList,readFromColNum);
                logger.debug("Excel: {}, Sheet: {}, Column Num: {}", excelName, sheetName, colNum);
                String[] titles = titleList.toArray(new String[titleList.size()]);

                // 正文内容应该从第readFromRowNum + 1行开始,第readFromRowNum行为表头的标题
                int rowNum = readFromRowNum + 1;
                while (rowNum > readFromRowNum) {
                    Row row = sheet.getRow(rowNum++);
                    if (row == null) {
                        break;
                    }
                    Map<String, String> rowMap = Maps.newHashMap();
                    rowMap.put("sheetName", sheetName);

                    Cell firstCell = row.getCell(readFromColNum);
                    //假如第colNum列并且为空则终止行项数据处理
                    if (firstCell == null) {
                        logger.info("End as first cell is Null at row: {}", rowNum);
                        break;
                    }

                    int j = readFromColNum;
                    int titleCnt = 0;
                    getColContent(rowMap,j,colNum,titleCnt,row,titles);

                    if (rowNum > readFromRowNum) {
                        rows.add(rowMap);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        logger.debug("Row Map Data: {}", rows);
        return rows;
    }

    private static void getColContent(Map<String, String> rowMap, int j, int colNum, int titleCnt, Row row,String[] titles) {
        while (j < colNum) {
            Cell cell = row.getCell(j);
            if (cell != null) {
                String cellValue = getCellFormatValue(cell);
                if (StringUtils.isNotBlank(cellValue)) {
                    rowMap.put(titles[titleCnt], cellValue);
                }
            }
            titleCnt++;
            j++;
        }
    }


    /**
     * <pre>
     * 读取Excel数据内容
     * 约定格式要求：第(titleStartRowNum+1)行为标题行，之后为数据行
     * 返回结构为Map结构的List集合：每行的key=第(titleStartRowNum+1)行的标题，
     * value=单元格值，统一为字符串，根据需要自行转换数据类型
     * </pre>
     *
     * @param excelFile      表格名称
     * @param readFromRowNum 以readFromRowNum作为标题开始读取
     * @return Map                       包含单元格数据内容的Map对象
     */
    public static List<List<String>> readExcelListContent(MultipartFile excelFile, Integer sheetIndex, Integer readFromRowNum) {
        List<List<String>> rows = Lists.newArrayList();
        if (excelFile.isEmpty()) {
            return rows;
        }
        InputStream is = null;
        String excelName = excelFile.getOriginalFilename();
        try {
            //读取Excel文件
            is = excelFile.getInputStream(); //this.getClass().getResourceAsStream(excelName);
            if (excelName.toLowerCase().endsWith(".xlsx")) {

                Workbook wb = new XSSFWorkbook(is);
                Sheet sheet = null;
                if (null == sheetIndex) {
                    sheetIndex = 0;
                }
                sheet = wb.getSheetAt(sheetIndex);
                int colNum = 0;
                // 标题总列数

                // 正文内容应该从第readFromRowNum + 1行开始,第readFromRowNum行为表头的标题
                int rowNum = readFromRowNum + 1;
                while (rowNum > readFromRowNum) {
                    Row row = sheet.getRow(rowNum++);
                    if (row == null) {
                        break;
                    }
                    List<String> rowList = new ArrayList<>();

                    Cell firstCell = row.getCell(0);
                    //假如第colNum列并且为空则终止行项数据处理
                    if (firstCell == null) {
                        logger.info("End as first cell is Null at row: {}", rowNum);
                        break;
                    }

                    int j = 0;
                    while (j < colNum) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            String cellValue = getCellFormatValue(cell);
                            if (StringUtils.isNotBlank(cellValue)) {
                                rowList.add(cellValue);
                            }
                        }
                        j++;
                    }
                    if (rowNum > readFromRowNum) {
                        rows.add(rowList);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        logger.debug("Row Map Data: {}", rows);
        return rows;
    }

    /**
     * 读取Excel数据内容
     * 约定格式要求：第一行为标题行，之后为数据行
     * 返回结构为Map结构的List集合：每行的key=第一行的标题，value=单元格值，统一为字符串，根据需要自行转换数据类型
     *
     * @return Map 包含单元格数据内容的Map对象
     */
    public static List<Map<String, String>> readExcelContent(InputStream is, String excelName, String sheetName) {
        List<Map<String, String>> rows = Lists.newArrayList();
        try {
            Workbook wb = new XSSFWorkbook(is);
            logger.debug("Excel: {}, Sheet: {}", excelName, sheetName);
            Sheet sheet = wb.getSheet(sheetName);
            Row row0 = sheet.getRow(0);
            // 标题总列数
            List<String> titleList = Lists.newArrayList();
            int colNum = getTitleList(row0,titleList,0);
            logger.debug("Excel: {}, Sheet: {}, Column Num: {}", excelName, sheetName, colNum);
            String[] titles = titleList.toArray(new String[titleList.size()]);

            // 正文内容应该从第二行开始,第一行为表头的标题
            int rowNum = 1;
            while (rowNum > 0) {
                Row row = sheet.getRow(rowNum++);
                if (row == null) {
                    break;
                }
                Map<String, String> rowMap = Maps.newHashMap();
                rowMap.put("sheetName", sheetName);

                Cell firstCell = row.getCell(0);
                //假如第一列并且为空则终止行项数据处理
                if (firstCell == null) {
                    logger.info("End as firt cell is Null at row: {}", rowNum);
                    break;
                }

                int j = 0;
                while (j < colNum) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        String cellValue = getCellFormatValue(cell);
                        rowMap.put(titles[j], cellValue);
                    }
                    j++;
                }
                if (rowNum > 0) {
                    rows.add(rowMap);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        logger.debug("Row Map Data: {}", rows);
        return rows;
    }

    /**
     * 根据HSSFCell类型设置数据
     *
     * @param cell
     * @return
     */
    private static String getCellFormatValue(Cell cell) {
        String cellvalue = null;
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case Cell.CELL_TYPE_NUMERIC:
                case Cell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式

                        //方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                        //cellvalue = cell.getDateCellValue().toLocaleString();

                        //方法2：这样子的data格式是不带带时分秒的：2011-10-12
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        cellvalue = sdf.format(date);

                    }
                    // 如果是纯数字
                    else {
                        // 取得当前Cell的数值
                        DecimalFormat df = new DecimalFormat("#.####");
                        cellvalue = df.format(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case Cell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
            }
        }
        if (cellvalue == null) {
            logger.warn("NULL cell value [{}, {}]", cell.getRowIndex(), cell.getColumnIndex());
        } else {
            cellvalue = cellvalue.trim();
        }
        return cellvalue;
    }

    /**
     * <pre>
     *   读取Excel指定列内容
     * </pre>
     *
     * @param readFromRowNum 以readFromRowNum作为标题开始读取
     * @param specifyColNum  从readFromColNum列开始读取
     * @return Map                       包含单元格数据内容的Map对象
     */
    public static List<Map<String, String>> readExcelSpecifyColNum(InputStream is, Integer sheetIndex, Integer readFromRowNum, Integer specifyColNum) {
        List<Map<String, String>> rows = Lists.newArrayList();
        String sheetName = "defaultSheet";
        try {
            //读取Excel文件
            Workbook wb = new HSSFWorkbook(is);
            Sheet sheet = null;
            if (null == sheetIndex) {
                sheetIndex = 0;
            }

            if (null == readFromRowNum) {
                readFromRowNum = 0;
            }
            sheet = wb.getSheetAt(sheetIndex);
            int colNum = 0;
            Row row0 = sheet.getRow(0);
            // 标题总列数
            List<String> titleList = Lists.newArrayList();
            while (colNum <= specifyColNum) {

                Cell cell = row0.getCell(colNum);
                if (cell == null) {
                    break;
                }
                String title = getCellFormatValue(cell);
                if (StringUtils.isBlank(title)) {
                    break;
                }
                titleList.add(title);
                logger.debug(" - Title : {} = {}", colNum, title);
                colNum++;
            }
            logger.debug("Sheet: {}, Column Num: {}", sheetIndex, colNum);
            String[] titles = titleList.toArray(new String[titleList.size()]);

            // 正文内容应该从第readFromRowNum + 1行开始,第readFromRowNum行为表头的标题
            int rowNum = readFromRowNum + 1;
            while (rowNum > readFromRowNum) {
                Row row = sheet.getRow(rowNum++);
                if (row == null) {
                    break;
                }
                Map<String, String> rowMap = Maps.newHashMap();
                rowMap.put("sheetName", sheetName);

                Cell firstCell = row.getCell(specifyColNum);
                //假如第colNum列并且为空则终止行项数据处理
                if (firstCell == null) {
                    logger.info("End as first cell is Null at row: {}", rowNum);
                    break;
                }

                int j = 0;
                int titleCnt = 0;
                getColContent(rowMap,j,colNum,titleCnt,row,titles);
                if (rowNum > readFromRowNum) {
                    rows.add(rowMap);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        logger.debug("Row Map Data: {}", rows);
        return rows;
    }

    /**
     * 将excel表格转换为List<List<List<String>>>
     *
     * @param excelFile
     * @param ignoreLine
     * @return
     */
    public static List<List<List<String>>> readExcelToList(MultipartFile excelFile, int ignoreLine) {
        List<List<List<String>>> allData = new ArrayList<>();
        try {
            Workbook workbook = createWorkbook(excelFile.getInputStream());
            //获得了Workbook对象之后，就可以通过它得到Sheet（工作表）对象了
            int sheetNumber = workbook.getNumberOfSheets();
            //读取每个工作表
            if (sheetNumber > 0) {
                //对每个工作表进行循环，读取每个工作表
                for (int i = 0; i < sheetNumber; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (null == sheet) {
                        continue;
                    }
                    List<List<String>> sheetList = new ArrayList<>();
                    //遍历每一行，跳过忽略行数
                    int r = 0;
                    Iterator<Row> rows = sheet.iterator();
                    while (rows.hasNext()) {
                        r++;
                        Row row = rows.next();
                        List<String> rowList = new ArrayList<>();
                        if (null == row || r <= ignoreLine) {
                            continue;
                        }
                        //得到当前行的所有单元格
                        if (null != row) {
                            Iterator<Cell> cells = row.iterator();
                            //对每个单元格进行循环
                            while (cells.hasNext()) {
                                Cell cell = cells.next();
                                //读取当前单元格的值
                                try {
                                    String cellStr = cell.getStringCellValue();
                                    if (cellStr == null) {
                                        cellStr = "";
                                    }
                                    rowList.add(cellStr);
                                } catch (Exception e) {
                                    rowList.add("");
                                }
                            }
                        }
                        sheetList.add(rowList);
                    }
                    allData.add(sheetList);
                }
            }
        } catch (Exception e) {
            logger.error("读取excel文件失败" + e.getMessage(), e);
        }
        return allData;
    }

    /**
     * 根据xls和xlsx不同返回不同的workbook
     *
     * @param inp
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    public static Workbook createWorkbook(InputStream inp) throws IOException, InvalidFormatException {
        if (!inp.markSupported()) {
            inp = new PushbackInputStream(inp, 8);
        }
        if (POIFSFileSystem.hasPOIFSHeader(inp)) {
            return new HSSFWorkbook(inp);
        }
        if (POIXMLDocument.hasOOXMLHeader(inp)) {
            return new XSSFWorkbook(OPCPackage.open(inp));
        }
        throw new IllegalArgumentException("无法解析的excel版本");
    }


    public static void getCellStyle(HSSFWorkbook wb, HSSFCellStyle style, HSSFCell cell, Map<String, Integer> colorMap, String color) {
        if (color != null && !"".equals(color)) {
            int index = colorMap.get(color);
            //转为RGB码
            int r = Integer.parseInt((color.substring(0, 2)), 16);   //转为16进制
            int g = Integer.parseInt((color.substring(2, 4)), 16);
            int b = Integer.parseInt((color.substring(4, 6)), 16);
            //自定义cell颜色
            HSSFPalette palette = wb.getCustomPalette();
            //这里的9是索引
            palette.setColorAtIndex((short) index, (byte) r, (byte) g, (byte) b);

            HSSFCellStyle currentStyle = getDefaultHssfCellStyle(wb);
            currentStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            currentStyle.setFillForegroundColor((short) index);

            cell.setCellStyle(currentStyle);
        } else {
            cell.setCellStyle(style);
        }
    }


    /**
     * 获得默认的单元格style 居中有边框
     *
     * @param wb
     * @return
     */
    public static HSSFCellStyle getDefaultHssfCellStyle(HSSFWorkbook wb) {
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);//下边框
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        return style;
    }


    public static List<String> readTitle(MultipartFile file, int sheetIndex) {
        List<String> titleList = Lists.newArrayList();
        try {
            //读取Excel文件
            InputStream is = file.getInputStream();
            Workbook wb = new HSSFWorkbook(is);
            Sheet sheet = null;

            sheet = wb.getSheetAt(sheetIndex);

            Row row0 = sheet.getRow(0);
            // 标题总列数
            getTitleList(row0,titleList,0);
        }catch (Exception e){
            logger.error("error: ",e);
        }
        return titleList;
    }

    private static int getTitleList(Row row0, List<String> titleList,int col) {
        int colNum = col;
        while (true) {
            Cell cell = row0.getCell(colNum);
            if (cell == null) {
                break;
            }
            String title = getCellFormatValue(cell);
            if (StringUtils.isBlank(title)) {
                break;
            }
            titleList.add(title);
            colNum++;
            logger.debug(" - Title : {} = {}", colNum, title);
        }
        return colNum;
    }
}
