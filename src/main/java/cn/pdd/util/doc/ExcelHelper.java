/**
 * 
 */
package cn.pdd.util.doc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cn.pdd.util.date.DateHelper;
import cn.pdd.util.file.FileHelper;
import cn.pdd.util.io.IOHelper;

/**
 * excel 导入,导出工具类;
 * @author paddingdun
 *
 * 2018年7月3日
 * @since 1.0
 * @version 1.0
 */
@SuppressWarnings({"unused"})
public class ExcelHelper {
	
	/**
	 * ExcelHelper 日志变量;
	 */
	private final static Logger logger = Logger.getLogger(ExcelHelper.class);
	
	private static boolean isExcel2003(String filePath){
		return filePath.matches("^.+\\.(?i)(xls)$");
	}

	private static boolean isExcel2007(String filePath){
		return filePath.matches("^.+\\.(?i)(xlsx)$");
	}

	/**
	 * 
	 * @param filePath
	 * @param sheetnum
	 * @param startRow
	 * @return
	 */
	public static List<String[]> importExcel(String filePath, int sheetnum, int startRow)throws Exception{
		List<String[]> result = new ArrayList<String[]>();
		
		InputStream inputStream	= null;
		Workbook 	wb			= null;
		try {
			inputStream = new FileInputStream(filePath);
			//2003;
			if(isExcel2003(filePath)){
				wb = new HSSFWorkbook(inputStream);
			//2007;
			}else if(isExcel2007(filePath)){
				wb = new XSSFWorkbook(inputStream);
			}
			//获取公式计算器;
			FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator(); 
			
			//获取指定的sheet;
			Sheet sheet = wb.getSheetAt(sheetnum);
			//行数;
			int rowNum = sheet.getPhysicalNumberOfRows();
			//取前5行中的最大列数;
			int max = 5;
			int colNum = 0;
			for (int i = 0; i < rowNum && i < max; i++) {
				Row r = sheet.getRow(i);
				if(r != null){
					colNum = Math.max(r.getPhysicalNumberOfCells(), colNum);
				}
			}
			//循环遍历excel表格;
			for (int i = startRow; i < rowNum; i++) {
				String[] obj = new String[colNum];
				for (int j = 0; j < colNum; j++) {
					//获取单元格值;
					obj[j] = getCellValue(evaluator, sheet, i, j);
				}
				result.add(obj);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			IOHelper.close(inputStream);
		}
		return result;
	}
	
	/**
	 * 获取指定的行列的cell;当行列在合并区域内时,返回该合并去区域的第一个cell;
	 * @param sheet
	 * @param row
	 * @param column
	 * @return
	 */
	public static Cell getCell(Sheet sheet, int row, int column){
		int sheetMergeCount = sheet.getNumMergedRegions();  
		for(int i = 0 ; i < sheetMergeCount ; i++){
			CellRangeAddress ca = sheet.getMergedRegion(i);  
			int firstColumn = ca.getFirstColumn();  
			int lastColumn = ca.getLastColumn();  
			int firstRow = ca.getFirstRow();
			int lastRow = ca.getLastRow();  
			if(row >= firstRow && row <= lastRow){   
				if(column >= firstColumn && column <= lastColumn){ 
					Row fRow = sheet.getRow(firstRow); 
					Cell fCell = fRow.getCell(firstColumn);
					return fCell;
				}
			}
		}
		Cell cell = sheet.getRow(row).getCell(column);
		return cell;
	}
	
	/**
	 * 获取包含合并区域内的cell的值;
	 * @param evaluator, 没有可以为null;
	 * @param sheet
	 * @param row
	 * @param column
	 * @return
	 */
	public static String getCellValue(FormulaEvaluator evaluator, Sheet sheet, int row, int column){
		Cell cell = getCell(sheet, row, column);
		if(cell == null)return "";
		return getCellValue(cell, evaluator) ;     
	}
	
	/**
	 * 将给定的值设置给该cell;
	 * @param sheet
	 * @param row
	 * @param column
	 * @param value
	 */
	public static void setCellValue(Sheet sheet, int row, int column, String value){     
		Cell cell = getCell(sheet, row, column);
		cell.setCellValue(StringUtils.trimToEmpty(value));
	}
    
    /**
     * 获取指定cell的值;
     * @param cell
     * @param evaluator
     * @return
     */
    public static String getCellValue(Cell cell, FormulaEvaluator evaluator){
    	int cellType = cell.getCellType();

    	//当cell表明是公式时,单独处理;
    	if(Cell.CELL_TYPE_FORMULA == cellType 
    			&& evaluator != null){
    		CellValue cv = evaluator.evaluate(cell);
    		return getCellValue(cv);
    	}else{
    		return getCellValue(cell);
    	}
    }

	/**
	 * 获取值;
	 * @param obj
	 * @return
	 */
	public static String getCellValue(Object obj){

		String cellValue = null;

		CellValue	cv	 = null;
		Cell 		cell = null;

		if(obj instanceof CellValue ){
			cv = (CellValue)obj;
		}else if(obj instanceof Cell){
			cell = (Cell)obj;
		}else{
			return cellValue;
		}
		int cellType = (cv != null)? cv.getCellType() : cell.getCellType();
		switch(cellType) {  
		//文本  
		case Cell.CELL_TYPE_STRING: 
			cellValue = (cv != null)? cv.getStringValue() : cell.getStringCellValue();  
			break;  
			//布尔型 
		case Cell.CELL_TYPE_BOOLEAN:  
			cellValue = (cv != null)? String.valueOf(cv.getBooleanValue()) : String.valueOf(cell.getBooleanCellValue());  
			break;  
			//空白  
		case Cell.CELL_TYPE_BLANK: 
			cellValue = (cv != null)? cv.getStringValue() : cell.getStringCellValue();  
			break;  
			//公式  
		case Cell.CELL_TYPE_FORMULA: 
			cellValue = cell.getCellFormula() ;   
			break; 
			//错误 
		case Cell.CELL_TYPE_ERROR:  
			cellValue = "error";  
			break;
			//数字、日期  
		case Cell.CELL_TYPE_NUMERIC: 
			//日期型
			if(cell != null && HSSFDateUtil.isCellDateFormatted(cell)) {  
				double value = cell.getNumericCellValue();
				Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);  
				cellValue = DateHelper.format(date,DateHelper.DATE_FMT_1);                
			} 
			//数字型
			else {  
				cellValue = (cv != null)? String.valueOf(cv.getNumberValue()):  String.valueOf(cell.getNumericCellValue());
			}  
			break;  
		default:  
			cellValue = null;  
		}
		return cellValue;  
	}
	
	/**
	 * 生成excel下载;
	 * @param filePath
	 * @param response
	 * @throws Exception
	 */
	public static void downLoadExcel(String filePath, HttpServletResponse response) throws Exception {
		downLoadExcel(filePath, null, response, null);
	} 
	
	/**
	 * 生成指定的文件名称的excel下载;
	 * @param filePath
	 * @param response
	 * @param fileName
	 * @throws Exception
	 */
	public static void downLoadExcel(String filePath, HttpServletRequest request, HttpServletResponse response, String fileName) throws Exception {
		BufferedInputStream bis  = null;
		OutputStream 		out  = null;
		File 				file = null;
		try{
			file = new File(filePath);
			if(StringUtils.isBlank(fileName)){
		    	fileName = file.getName();
		    }
			String rtn = "filename=\"" + new String(fileName.getBytes("utf-8"),"iso-8859-1")+"\"";
		    if(request == null){
		    }else{
		    	String userAgent = request.getHeader("User-Agent");
		    	if (userAgent != null)  
		    	{  
		    		String new_filename = URLEncoder.encode(fileName, "utf-8");
		    		userAgent = userAgent.toLowerCase();  
		    		// IE浏览器，只能采用URLEncoder编码  
		    		if (userAgent.indexOf("msie") != -1)  
		    		{  
		    			rtn = "filename=\"" + new_filename + "\"";  
		    		}  
		    		// Opera浏览器只能采用filename*  
		    		else if (userAgent.indexOf("opera") != -1)  
		    		{  
		    			rtn = "filename*=UTF-8''" + new_filename;  
		    		}  
		    		// Safari浏览器，只能采用ISO编码的中文输出  
		    		else if (userAgent.indexOf("safari") != -1 )  
		    		{  
		    		}  
		    		// Chrome浏览器，只能采用MimeUtility编码或ISO编码的中文输出  
		    		else if (userAgent.indexOf("applewebkit") != -1 )  
		    		{  
		    			rtn = "filename=\"" + new_filename + "\"";  
		    		}  
		    		// FireFox浏览器，可以使用MimeUtility或filename*或ISO编码的中文输出  
		    		else if (userAgent.indexOf("mozilla") != -1)  
		    		{  
		    			rtn = "filename*=UTF-8''" + new_filename;  
		    		}  
		    	}  
		    }
		    
			bis =  new BufferedInputStream(new FileInputStream(file));  
			byte[] buf = new byte[1024];  
			int len = 0;

			response.reset(); 
			response.setContentType("application/x-msdownload");
			response.setHeader("Content-Disposition", "attachment; " + rtn);  
			out = response.getOutputStream();  
			while ((len = bis.read(buf)) > 0){  
				out.write(buf, 0, len);
			}
			out.flush();  
		}finally{
			IOHelper.close(bis);
			IOHelper.close(out);
			FileHelper.delete(file);
		}
	}  

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
	}

}
