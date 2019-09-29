package com.iflytek.kafka.timer;

import com.iflytek.kafka.sftp.SFTPUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerUtil {

    private String filePath;//上传到该目录
    private String tempPath;//本地临时存储目录
    private String userName;//sftp用户名
    private String passWd;//sftp密码
    private String host;//sftp地址
    private int port;//sftp端口
    private String nodeName;//集群节点名称

    public TimerUtil(String nodeName,String filePath,String tempPath,String userName,String passWd,String host,int port){
        this.nodeName = nodeName;
        this.filePath = filePath;
        this.tempPath =tempPath;
        this.userName = userName;
        this.passWd = passWd;
        this.host = host;
        this.port = port;
    }

    public boolean exec(){


        Boolean flag = false;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //先判断上传目录
                System.out.println("exec job start >>>>>>>>>>>>>>>>>>>>>>>>");
                System.out.println("当前时间 >>>>>"+TimerUtil.now());
                File file = new File(tempPath);
                try {
                    if(file.isDirectory()){
                        String[] filelist =  file.list();
                        SFTPUtil util = new SFTPUtil(userName,passWd,host,port);//连接sftp
                        util.login();//登录sftp
                        for (int i = 0; i < filelist.length; i++) {
                            File readfile = new File(tempPath + File.separator+ filelist[i]);
                            String fileName = readfile.getName();//日志名称格式 dics_hl-2019092515-node1.log
                            System.out.println("current fileName is: >>>"+fileName);
                            String now =  TimerUtil.now();
                            String [] names = fileName.split("-");//根据名称截取日志信息
                            System.out.println("日志时间:"+names[1]);
                            System.out.println("当前时间:"+now);
                            if(TimerUtil.compare(now,names[1])){//如果当前还有以前产生的日志文件
                                System.out.println("开始上传日志》》》》》》");
                                String year = names[1].substring(0,4);
                                String yearMd = names[1].substring(0,8);
                                String uploadDir = "/"+names[0]+"/"+year+"/"+yearMd;//生成上传文件夹目录
                                System.out.println("uploadFile: "+uploadDir);
                                InputStream stream = new FileInputStream(readfile);
                                util.upload(filePath,uploadDir,readfile.getName(),stream);//上传文件
                                System.out.println("上传完成。上传路径: "+filePath+uploadDir);
                                if(stream !=null){
                                    stream.close();
                                }
                                readfile.delete();//上传后删除临时存储日志
                                System.out.println("临时存储日志已删除完成");
                            }
                        }
                        util.logout();//登出sftp
                    }
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
                System.out.println("exec job end >>>>>>>");
            }
        },2000,10*60*1000);

        return flag;
    }


    private static boolean compare(String now,String logTime){
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
        try {
            Date d1 = df.parse(now);
            Date d2 = df.parse(logTime);
            if(d2.before(d1)){
              return true;
            }else{
                return false;
            }
        }catch (ParseException e){
           System.out.println(e.getMessage());
        }
        return false;

    }


    private static String now(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
        return df.format(calendar.getTime());
    }

}
