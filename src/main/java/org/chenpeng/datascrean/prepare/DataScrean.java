package org.chenpeng.datascrean.prepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.TreeMap;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;

/**
 * 
 * @author ChenPeng
 *
 */
public class DataScrean {

	private TreeMap<String, String> tm = new TreeMap<String, String>();

	/**
	 * 
	 * @param path
	 * @throws IOException
	 */
	public TreeMap<String, String> dataScrean(File path) throws IOException {
		System.out.println("------------------------------------------------");
		System.out.println("Start！");
		System.out.println();
		// 先判断一下输入的路径是否是一个有效的路径
		File fileDir = path;
		if (!fileDir.isDirectory()) {
			throw new RuntimeException("It is not an effective path!");
		}
		// 开始遍历文件夹
		// 获取输入路径下的文件夹列表
		File[] dirs = fileDir.listFiles();
		// 判断如果获取的文件夹列表有效则继续执行
		if (dirs == null) {
			throw new RuntimeException("No documents!");
		}
		// 上面那步未执行说明路径是有效的，下面开始遍历
		for (File dir : dirs) {
			// 逐个判断每个文件是否为文件夹，如果不是文件夹就不进行处理！是文件夹才进入并遍历下面的文件
			if (!dir.isDirectory()) {
				continue;
			} else {
				// 已经执行到该行说明这个dir是一个文件夹，随后对这个文件夹进行遍历查询
				File[] files = dir.listFiles();
				// 开始for循环遍历
				for (File file : files) {
					// 先对文件做一个类型判断，如果不是.csv的文件我们就不进行处理，事实上也是如此，本程序暂不支持处理.xlsx文件，需后期升级完成
					if (!file.isFile() || !".CSV".equals(file.getName()
							.substring(file.getName().length() - 4, file.getName().length()).toUpperCase())) {
						// 严格来讲，应在这里加入对.xlsx文件的处理，暂时没有项目这样反馈过数据，因此暂时不处理
						continue;
					} else {
						// 开始读取.csv文件
						String encode = getEncode(file);
						System.out.println(encode + " : " + file.getName());
						tm.put(file.getName(), encode);
						BufferedReader br = new BufferedReader(
								new InputStreamReader(new FileInputStream(file), encode));
						String line = null;
						line = br.readLine();
						br.close();
						/*
						 * 下面是对文件进行逻辑判断和处理，对文件的第一行进行按‘，’分列，
						 * 1、如果判断仅有一列，说明这不是一个正规的csv文件，一般来讲应该是使用Tab符号作为分隔符了。 这样的情况我们需要将分隔符Tab替换成‘，’，
						 * 2、如果判断少一列，我们需要按照少一或三列的处理方法处理文件，目前发现的情况都是少LCR_ID
						 * 3、如果判断多一列，我们需要按照多一列的处理方法处理文件，目前发现的情况都是多了前面的编号
						 */
						if (line != null) {
							Object[] obj = line.split(",");
							if (obj == null || obj.length == 1) {
								obj = line.split("	");
								// 用tab符号进行分段后如果返回为空或者返回长度为1，说明分隔符并不是tab则报出错误提示！终止程序运行。
								if (obj == null || obj.length == 1) {
									System.out.println(file.getName());
									throw new RuntimeException("	Unknown separator!Have not handled!");
								} else {
									System.out.println("Only one column!");
									System.out.println(file.getName());
									// 已判定此文件只有1列，且使用tab分割，因此需要进行字符转换。
									File file2 = symbolConvert(file);
									tm.put(file2.getName(), tm.get(file.getName()));
									file.renameTo(new File(file.getParentFile().getParentFile().getAbsoluteFile()
											+ System.getProperty("file.separator") + file.getName()));
									// 进行多列少列的判断和处理
									fileHandle(file2, obj);
								}
							} else {
								fileHandle(file, obj);
							}
						} else {
							continue;
						}
					}
				}
			}
		}
		System.out.println();
		System.out.println("Over！");
		System.out.println("------------------------------------------------");
		return tm;
	}

	/**
	 * 用于处理文件列数不符合条件的整体逻辑判断
	 * 
	 * @param file
	 * @param obj
	 * @throws IOException
	 */
	private void fileHandle(File file, Object[] obj) throws IOException {
		// 判断如果是BHcapacity文件我们应该怎样处理
		if (file.getName().startsWith("BHcapacity")) {
			if (obj.length == 42) {// 正常情况下，数据本应该是42列

			} else if (obj.length == 43) {// 当发现数据是43列的时候应该是第一列多了编号（例如河南！）
				if (obj[1].toString().contains("SDATETIME")
						&& obj[obj.length - 3].toString().contains("AGG8_USED_PDCCH")) {
					System.out.println("One more column:");
					System.out.println(file.getName());
					File file2 = reduceColumn1(file);
					tm.put(file2.getName(), tm.get(file.getName()));
					file.renameTo(new File(file.getParentFile().getParentFile().getAbsoluteFile()
							+ System.getProperty("file.separator") + file.getName()));
				} else if (obj[obj.length - 1].toString().contains("OMC标识")) {
					System.out.println("One more column:");
					System.out.println(file.getName());
					File file2 = reduceColumn2(file);
					tm.put(file2.getName(), tm.get(file.getName()));
					file.renameTo(new File(file.getParentFile().getParentFile().getAbsoluteFile()
							+ System.getProperty("file.separator") + file.getName()));
				}
			} else if (obj.length == 39) {//39列应该是少了lcr_id和PDCP_SDU_VOL_UL_ALLDAY_MB、PDCP_SDU_VOL_DL_ALLDAY_MB
				System.out.println("Lack of three columns:");
				System.out.println(file.getName());
				File file2 = addColumn(file);
				tm.put(file2.getName(), tm.get(file.getName()));
				file.renameTo(new File(file.getParentFile().getParentFile().getAbsoluteFile()
						+ System.getProperty("file.separator") + file.getName()));
			}
			// 如果是Daily2g2d文件应该怎样处理
		} else if (file.getName().startsWith("Daily2g2d")) {
			if (obj.length == 42) {

			} else if (obj.length == 54) {

				if (obj[1].toString().contains("SDATETIME")
						&& obj[obj.length - 1].toString().contains("VoLTE_erls_2")) {
					System.out.println("One more column:");
					System.out.println(file.getName());
					File file2 = reduceColumn1(file);
					tm.put(file2.getName(), tm.get(file.getName()));
					file.renameTo(new File(file.getParentFile().getParentFile().getAbsoluteFile()
							+ System.getProperty("file.separator") + file.getName()));
				} else if (obj[obj.length - 1].toString().contains("OMC标识")) {
					System.out.println("One more column:");
					System.out.println(file.getName());
					File file2 = reduceColumn2(file);
					tm.put(file2.getName(), tm.get(file.getName()));
					file.renameTo(new File(file.getParentFile().getParentFile().getAbsoluteFile()
							+ System.getProperty("file.separator") + file.getName()));
				}
			} else if (obj.length == 52) {
				System.out.println("Lack of one columns:");
				System.out.println(file.getName());
				File file2 = addColumn(file);
				tm.put(file2.getName(), tm.get(file.getName()));
				file.renameTo(new File(file.getParentFile().getParentFile().getAbsoluteFile()
						+ System.getProperty("file.separator") + file.getName()));
			}
		} else {

		}
	}

	/**
	 * 减少列操作，处理那种最后一列多出来名叫‘OMC标识’的情况
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private File reduceColumn2(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		File file2 = createFile(file, "More");
		file2.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(file2));
		String line = null;

		while ((line = br.readLine()) != null) {
			String[] lineContext = line.split(",");
			String newLine = lineContext[0];
			for (int j = 1; j < lineContext.length - 1; j++) {
				newLine = newLine + "," + lineContext[j];
			}
			bw.write(newLine);
			bw.newLine();

		}
		br.close();
		bw.close();
		return file2;

	}

	/**
	 * 减少列操作，处理那种第一列是行号的情况。
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private File reduceColumn1(File file) throws IOException {// 处理那种第一列是多出来的情况，一般是多出了行号
		BufferedReader br = new BufferedReader(new FileReader(file));
		File file2 = createFile(file, "More");
		file2.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(file2));
		String line = null;

		while ((line = br.readLine()) != null) {
			String[] lineContext = line.split(",");
			String newLine = lineContext[1];
			for (int j = 2; j < lineContext.length; j++) {
				newLine = newLine + "," + lineContext[j];
			}
			bw.write(newLine);
			bw.newLine();

		}
		br.close();
		bw.close();
		return file2;
	}

	/**
	 * 添加列操作
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private File addColumn(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		File file2 = createFile(file, "Less");
		file2.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(file2));
		String line = null;
		String lineB = br.readLine();
		String[] lineContextB = lineB.split(",");
		String newLineB = lineContextB[0];

		for (int i = 1; i < lineContextB.length; i++) {
			if (i == 3) {
				newLineB = newLineB + "," + lineContextB[i] + "," + "LCR_ID";
			} else {
				newLineB = newLineB + "," + lineContextB[i];
			}
		}
		if (file.getName().startsWith("BHcapacity")) {
			newLineB = newLineB + "," + "PDCP_SDU_VOL_UL_ALLDAY_MB" + "," + "PDCP_SDU_VOL_DL_ALLDAY_MB";
		}
		bw.write(newLineB);
		while ((line = br.readLine()) != null) {
			String[] lineContext = line.split(",");
			String newLine = lineContext[0];
			for (int j = 1; j < lineContext.length; j++) {
				if (j == 3) {
					newLine = newLine + "," + lineContext[j] + "," + lineContext[j];
				} else {
					newLine = newLine + "," + lineContext[j];
				}
			}
			newLine = newLine + "," + ",";
			bw.newLine();
			bw.write(newLine);
		}
		br.close();
		bw.close();
		return file2;
	}

	/**
	 * 常见的一种情况：将数据文件中字段分隔符由“ ”改成“,”
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private File symbolConvert(File file) throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), tm.get(file.getName())));
		File file2 = createFile(file, "Only_one");
		file2.createNewFile();
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file2), tm.get(file.getName())));
		String line = null;
		String lineB = br.readLine();
		String newLineB = lineB.replaceAll("	", ",");
		bw.write(newLineB);
		while ((line = br.readLine()) != null) {
			String newLine = line.replaceAll("	", ",");
			bw.newLine();
			bw.write(newLine);
		}
		br.close();
		bw.close();
		return file2;
	}

	/**
	 * 所有方法中，负责接收一个原File文件，根据原文件名生成一个新File文件
	 * 
	 * @param file
	 * @param msg
	 *            用以描述创建的这个新文件是做了什么操作
	 * @return
	 */
	private File createFile(File file, String msg) {
		String name = file.getName().replaceAll(".csv", "");
		String newName = file.getAbsolutePath().replace(name, name + "-update-" + msg);
		File file2 = new File(newName);
		return file2;
	}

	/**
	 * 使用网络cpdetector插件检测文件编码格式。
	 * 
	 * @param f
	 * @return 返回编码格式的String名称
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String getEncode(File f) throws MalformedURLException, IOException {

		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
		detector.add(UnicodeDetector.getInstance());
		detector.add(JChardetFacade.getInstance());
		detector.add(ASCIIDetector.getInstance());
		Charset charset = detector.detectCodepage(f.toURI().toURL());
		return charset.toString();
	}
}
