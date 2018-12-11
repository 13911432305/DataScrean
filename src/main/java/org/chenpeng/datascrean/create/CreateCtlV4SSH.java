package org.chenpeng.datascrean.create;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * 
 * @author ChenPeng
 *
 */
public class CreateCtlV4SSH {
	
	/**
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	
	public File[] createCtl(File dir) throws IOException, URISyntaxException {
		System.out.println("------------------------------------------------");
		System.out.println("Start generating import files!");
		System.out.println();

		File[] fileResult = new File[2];
		// 先判断一下输入的路径是否是一个有效的路径
		File fileDir = dir;
		if (!fileDir.isDirectory()) {
			throw new RuntimeException("Not an effective path!");
		}
		// 先拿到class文件所在目录的路径，为后期新建文件夹创造
		String currentPath = System.getProperty("user.dir");
		Calendar cl = Calendar.getInstance();
		String date = "" + cl.get(Calendar.YEAR) + (cl.get(Calendar.MONTH) + 1) + cl.get(Calendar.DAY_OF_MONTH)
				+ cl.get(Calendar.HOUR_OF_DAY) + cl.get(Calendar.MINUTE) + cl.get(Calendar.SECOND);

		String BHCtlDir = currentPath + System.getProperty("file.separator") + "ctlFiles"
				+ System.getProperty("file.separator") + "bhcapacity-" + date;
		new File(BHCtlDir).mkdirs();
		String DaCtlDir = currentPath + System.getProperty("file.separator") + "ctlFiles"
				+ System.getProperty("file.separator") + "daily2g2d-" + date;
		new File(DaCtlDir).mkdirs();

		File rootDir = dir;
		// 通过根路径获取下面的子文件夹，此处默认为跟路径下的文件都是文件夹！
		File[] subDirs = rootDir.listFiles();
		// 遍历根目录下的所有子目录
		ArrayList<String> alAllBH = new ArrayList<String>();
		ArrayList<String> alAllDa = new ArrayList<String>();
		for (int i = 0; i < subDirs.length; i++) {
			String projectName = subDirs[i].getName();
			// 判断该子目录文件是否为一个有效的目录文件夹,因此不用担心会处理到文件夹以外的文件！
			if (!subDirs[i].isDirectory()) {
				continue;
			} else {
				ArrayList<String> alBH = new ArrayList<String>();
				ArrayList<String> alDa = new ArrayList<String>();

				// 此处默认二级目录下都是文件，而不是文件夹！文件夹将不做处理。
				File[] files = subDirs[i].listFiles();
				// 对二级目录下的文件进行遍历
				for (int j = 0; j < files.length; j++) {

					if (files[j].isDirectory()) {
						continue;
					} else {
						if (files[j].getName().startsWith("BH")) {
							alBH.add("INFILE '" + files[j].getAbsolutePath() + "'");
						} else if (files[j].getName().startsWith("Da")) {
							alDa.add("INFILE '" + files[j].getAbsolutePath() + "'");
						} else {

						}
					}
				}
				File BHCtlFile = new File(
						BHCtlDir + System.getProperty("file.separator") + "bhcapacity-" + projectName + ".ctl");
				File DaCtlFile = new File(
						DaCtlDir + System.getProperty("file.separator") + "daily2g2d-" + projectName + ".ctl");

				createCtlFile(currentPath + System.getProperty("file.separator") + "bhcapacity.ctl", projectName,
						BHCtlFile, alBH);
				createCtlFile(currentPath + System.getProperty("file.separator") + "daily2g2d.ctl", projectName,
						DaCtlFile, alDa);

				alAllBH.add(BHCtlFile.getName());
				alAllDa.add(DaCtlFile.getName());
			}
			File file1 = createBatFile(alAllBH, BHCtlDir);
			File file2 = createBatFile(alAllDa, DaCtlDir);

			fileResult[0] = file1;
			fileResult[1] = file2;
		}
		System.out.println();
		System.out.println("Import files have been generated!");
		System.out.println("------------------------------------------------");
		return fileResult;

	}
	/**
	 * 
	 * @param sampleFilePath
	 * @param projectName
	 * @param file
	 * @param al
	 * @param encode
	 * @throws IOException
	 */
	private void createCtlFile(String sampleFilePath, String projectName, File file, ArrayList<String> al) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		BufferedReader br = new BufferedReader(new FileReader(sampleFilePath));
		String line = null;
		if (sampleFilePath.contains("bhcapacity")) {
			while ((line = br.readLine()) != null) {
				if (line.contains("CHARACTERSET ZHS16GBK")) {
					bw.write(line);
					bw.write(System.getProperty("line.separator"));
					for (int i = 0; i < al.size(); i++) {
						bw.write(al.get(i).toString());
						bw.write(System.getProperty("line.separator"));
					}
				} else if (line.contains("PDCP_SDU_VOL_DL_ALLDAY_MB")) {
					bw.write(line);
					bw.write(System.getProperty("line.separator"));
					bw.write(", ProjectName CONSTANT'" + projectName + "'");

					bw.write(System.getProperty("line.separator"));
				} else {
					bw.write(line);
					bw.write(System.getProperty("line.separator"));
				}
			}
		} else if (sampleFilePath.contains("daily2g2d")) {
			while ((line = br.readLine()) != null) {
				if (line.contains("CHARACTERSET ZHS16GBK")) {
					bw.write(line);
					bw.write(System.getProperty("line.separator"));
					for (int i = 0; i < al.size(); i++) {
						bw.write(al.get(i).toString());
						;
						bw.write(System.getProperty("line.separator"));
					}
				} else if (line.contains("OLTE_ERLS_2")) {
					bw.write(line);
					bw.write(System.getProperty("line.separator"));
					bw.write(", ProjectName CONSTANT'" + projectName + "'");
					bw.write(System.getProperty("line.separator"));
				} else {
					bw.write(line);
					bw.write(System.getProperty("line.separator"));
				}
			}
		}
		bw.close();
		br.close();
	}

	private File createBatFile(ArrayList<String> al, String dir) throws IOException {
		File file = null;
		String osName = System.getProperty("os.name");
		if (osName.contains("Linux")) {
			file = new File(dir + System.getProperty("file.separator") + "run.sh");
		} else if (osName.contains("Windows")) {
			file = new File(dir + System.getProperty("file.separator") + "run.bat");
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < al.size(); i++) {
			out.write("sqlldr dailykpi/Dk123@chqk control=" + al.get(i) + " errors=1000");
			out.write(System.getProperty("line.separator"));
		}
		if (osName.contains("Linux")) {

		} else {
			out.write("pause>nul");
		}
		out.close();
		return file;
	}
}
