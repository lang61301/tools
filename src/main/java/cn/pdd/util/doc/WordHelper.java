/**
 * 
 */
package cn.pdd.util.doc;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

/**
 * word工具类;
 * 
 * @author paddingdun
 *
 *         2018年7月3日
 * @since 1.0
 * @version 1.0
 */
@SuppressWarnings({ "unused" })
public class WordHelper {
	public static void op(String fn) throws Exception {
		String name = FilenameUtils.getBaseName(fn);

		HSSFWorkbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet();
		int idx = 0;
		Row r0 = sheet.createRow(idx++);
		r0.createCell(0).setCellValue("序号");
		r0.createCell(1).setCellValue("问题描述");
		r0.createCell(2).setCellValue("处理");
		//
		XWPFDocument document = new XWPFDocument(new FileInputStream(fn));
		// 获取所有表格
		List<XWPFTable> tables = document.getTables();
		int ti = 0;
		for (XWPFTable table : tables) {
			System.out.println("table:::::::::::::" + (ti));
			// 获取表格的行
			List<XWPFTableRow> rows = table.getRows();
			Row r = sheet.createRow(idx++);

			int ri = 0;
			for (XWPFTableRow row : rows) {
				System.out.println("table:::::::::::::" + (ti) + ":row:" + (ri));
				// 获取表格的每个单元格
				List<XWPFTableCell> tableCells = row.getTableCells();
				for (XWPFTableCell cell : tableCells) {
					// 获取单元格的内容
					String text = cell.getText();

					if (ri == 2) {
						r.createCell(0).setCellValue("" + (ti+1));
						r.createCell(1).setCellValue(StringUtils.trimToEmpty(text));
					}
					System.out.println("[" + text + "]");
				}
				ri++;
			}
			ti++;
		}

		wb.write(new File("d:\\home\\Desktop\\" + name + ".xls"));
		wb.close();
		document.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		op("d:\\home\\Desktop\\问题文档（2018年8月24日）.docx");
	}

}
