package org.chenpeng.datascrean.prepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.TreeMap;

public class SymbolConvert {
	private TreeMap<String,String> tm;
	public SymbolConvert(TreeMap<String,String> tm) {
		this.tm=tm;
	}
	public File symbolConvert(File dir) throws IOException {
		System.out.println("开始处理文件列项中带逗号的数据！");
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
						if (!projectData.isDirectory()&&".CSV".equals(projectData.getName().substring(projectData.getName().length()-4, projectData.getName().length()).toUpperCase())) {
							System.out.println("处理：" + projectData.getAbsolutePath());
							File projectDataHandled = new File(projectDirHandled.getAbsolutePath()
									+ System.getProperty("file.separator") + projectData.getName());
							BufferedReader br = new BufferedReader(
									new InputStreamReader(new FileInputStream(projectData),tm.get(projectData.getName())));
							BufferedWriter bw = new BufferedWriter(
									new OutputStreamWriter(new FileOutputStream(projectDataHandled),"utf-8"));
							String line = null;

							while ((line = br.readLine()) != null) {
								String lineN = null;
								while(!line.equals(lineN)) {
									lineN=line;
									line = line.replace("(\".*),(.*\")", "$1;$2");
								}
								
								bw.write(line.toString());
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
