/**
 * 
 */
package cn.pdd.util.file;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * 文件处理工具类;
 * @author paddingdun
 *
 * 2018年7月3日
 * @since 1.0
 * @version 1.0
 */
public class FileHelper {
	
	/**
	 * FileHelper 日志变量;
	 */
	private final static Logger logger = Logger.getLogger(FileHelper.class);

	/**
	 * 删除文件或者目录;
	 * @param file
	 */
	public static void delete(File file){
		if(file != null){
			String fp = null;
			try{
				fp = file.getAbsolutePath();
				
				FileUtils.forceDelete(file);
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("FileHelper.delete [%s] success!", fp));
				}
			}catch(Exception e){
				e.printStackTrace();
				if (logger.isEnabledFor(Level.WARN)) {
					logger.warn(String.format("FileHelper.delete [%s] error!", fp));
				}
			}
		}else{
			if (logger.isEnabledFor(Level.WARN)) {
				logger.warn("FileHelper.delete file is null!");
			}
		}
	}
	
	public static void delete(String fp){
		if(StringUtils.isNotBlank(fp)){
			File file = new File(fp.trim());
			delete(file);
		}else{
			if (logger.isEnabledFor(Level.WARN)) {
				logger.warn("FileHelper.delete file is null!");
			}
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
