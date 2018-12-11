package org.chenpeng.datascrean.prepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbleConvert2 {
	public File symbolConvert(File dir) throws IOException {
		System.out.println("开始处理文件中不带引号的数据！");
		File dirHandled = new File(dir.getAbsolutePath().replaceAll(dir.getName(), dir.getName() + "-Handled"));

		File[] projectDirs = dir.listFiles();

		for (File projectDir : projectDirs) {
			if (projectDir.isDirectory()) {
				File projectDirHandled = new File(
						dirHandled.getAbsolutePath() + System.getProperty("file.separator") + projectDir.getName());

				if (!projectDirHandled.exists()) {
					projectDirHandled.mkdirs();
				}
				if (projectDir.isDirectory()) {

					File[] projectDatas = projectDir.listFiles();

					for (File projectData : projectDatas) {
						if (!projectData.isDirectory()) {
							System.out.println("处理：" + projectData.getAbsolutePath());
							File projectDataHandled = new File(projectDirHandled.getAbsolutePath()
									+ System.getProperty("file.separator") + projectData.getName());
							BufferedReader br = new BufferedReader(
									new InputStreamReader(new FileInputStream(projectData), "GBK"));
							BufferedWriter bw = new BufferedWriter(
									new OutputStreamWriter(new FileOutputStream(projectDataHandled), "GBK"));
							String line = null;

							while ((line = br.readLine()) != null) {
								String regex1 =  "\".*,.*\"";
								Pattern p = Pattern.compile(regex1);
								Matcher m = p.matcher(line);
								while(m.find()) {
									line = line.replace("(\".*),(.*\")", "$1;$2");
								}
								StringBuilder lineHandled = new StringBuilder();
								String[] lines = line.split(",");
								for (int i = 0; i < lines.length; i++) {
									if (lines[i].equals("")) {
										lines[i] = "\"" + lines[i] + "\"";
									} else {
										if (!lines[i].startsWith("\"")) {
											lines[i] = "\"" + lines[i];
										}
										if (!lines[i].endsWith("\"")) {
											lines[i] = lines[i] + "\"";
										}
									}
									if (i == lines.length - 1) {
										lineHandled.append(lines[i]);
									} else {
										lineHandled.append(lines[i] + ",");
									}
								}
								bw.write(lineHandled.toString());
								bw.newLine();
							}
							br.close();
							bw.close();
						}
					}

				}
			} else {
				continue;
			}
		}

		System.out.println("处理完成！");
		return dirHandled;
	}
}
