package org.chenpeng.datascrean.execute;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Docmd {

	private void Executive2(String user,String psw,String Database,String fileRoute,String ctlfileName,String logfileName)
    {
        InputStream ins = null;
        //要执行的DOS命令  --数据库  用户名  密码  user/password@database
        String dos="sqlldr "+user+"/"+psw+"@"+Database+" control="+fileRoute+""+ctlfileName+" log="+fileRoute+""+logfileName;
        
        String[] cmd = new String[]
        { "cmd.exe", "/C", dos }; // 命令
        try
        {
            Process process = Runtime.getRuntime().exec(cmd);
            ins = process.getInputStream(); // 获取执行cmd命令后的信息
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                String msg = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                System.out.println(msg); // 输出
            }
            int exitValue = process.waitFor();
            if(exitValue==0)
            {
                System.out.println("返回值：" + exitValue+"\n数据导入成功");
                
            }else
            {
                System.out.println("返回值：" + exitValue+"\n数据导入失败");
                
            }
            
            process.getOutputStream().close(); // 关闭
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
