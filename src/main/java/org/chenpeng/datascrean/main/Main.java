package org.chenpeng.datascrean.main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.TreeMap;

import org.chenpeng.datascrean.create.CreateCtlV4SSH;
import org.chenpeng.datascrean.prepare.DataScrean;
import org.chenpeng.datascrean.prepare.SymbolConvert;

public class Main {
	/**
	 * @author ChenPeng
	 * @param args
	 * 
	 *            本程序主要完成其他模块的整合： 1、对数据的预处理 2、将现有数据生成导入脚本 3、执行导入
	 * 
	 *            如有精力还可以对导入结果进行研究！
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException, URISyntaxException {
		
		if (args.length > 1) {
			throw new RuntimeException("Just input the path, no need to enter other parameters!");
		}

		// 先判断一下输入的路径是否是一个有效的路径
		File fileDir = new File(args[0]);
		if (!fileDir.isDirectory()) {
			throw new RuntimeException("Not an effective path!");
		}

		// 对数据进行预处理
		DataScrean ds = new DataScrean();
		//用一个TreeMap接收文件，编码格式的数据。
		TreeMap<String,String> tm = ds.dataScrean(fileDir);
		
		SymbolConvert sc = new SymbolConvert(tm);

		// 根据处理完的数据生成数据库导入文件,并接收新生成的两个批处理bat文件
		CreateCtlV4SSH cc = new CreateCtlV4SSH();
		cc.createCtl(sc.symbolConvert(fileDir));
		// 执行bat文件
		// Docmd dc = new Docmd();

	}
	
}
