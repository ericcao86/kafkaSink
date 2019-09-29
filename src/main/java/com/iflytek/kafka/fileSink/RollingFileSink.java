package com.iflytek.kafka.fileSink;

/**
 * @author cyh
 * @Date 16:45 2019/7/26
 * @description
 * @since 2.0
 */


import com.iflytek.kafka.timer.TimerUtil;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RollingFileSink extends AbstractSink implements Configurable {
    private static final Logger logger = LoggerFactory.getLogger(RollingFileSink.class);
    private static final String PROP_KEY_ROOTPATH = "sink.directory";
    private static final String PROP_KEY_BASE_PATH = "sink.tempPath";
    private static final String PROP_KEY_USERNAME = "sink.userName";
    private static final String PROP_KEY_PASSWD = "sink.passWd";
    private static final String PROP_KEY_HOST = "sink.host";
    private static final String PROP_KEY_PORT = "sink.port";
    private static final String PROP_KEY_NODENAME= "sink.nodeName";
    private String fileName;
    private String filePath;//上传到该目录
    private File path;
    private String tempPath;//临时存储目录
    private String userName;//sftp用户名
    private String passWd;//sftp密码
    private String host;//sftp地址
    private int port;//sftp端口
    private String nodeName;//当前集群节点名称
    private int count = 0;//计数器

    //    private static final SimpleDateFormat timeFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat timeFormater = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat timeFormater1 = new SimpleDateFormat("HH");



    @Override
    public void configure(Context context) {

        filePath = context.getString(PROP_KEY_ROOTPATH);
        userName = context.getString(PROP_KEY_USERNAME);
        passWd = context.getString(PROP_KEY_PASSWD);
        host = context.getString(PROP_KEY_HOST);
        port = Integer.parseInt(context.getString(PROP_KEY_PORT));
        tempPath = context.getString(PROP_KEY_BASE_PATH);
        nodeName = context.getString(PROP_KEY_NODENAME);


    }

    @Override
    public Status process() throws EventDeliveryException {
        System.out.println("current count: "+count);
        if(count == 0){
            TimerUtil util = new TimerUtil(nodeName,filePath,tempPath,userName,passWd,host,port);
            util.exec();
            count++;
        }
        Channel ch = getChannel();
        //get the transaction
        Transaction txn = ch.getTransaction();
        Event event = null;
        //begin the transaction
        txn.begin();
        while (true) {
            event = ch.take();
            if (event != null) {
                break;
            }
        }
        try {

            logger.debug("Get event.");

            String body = new String(event.getBody());
            System.out.println("current body is >>>>>>>>"+body);
            String rs[] =body.split("@");
            String dayTime = rs[0].substring(0,8);
            String hourTime =  rs[0].substring(8,10);

            path = new File(tempPath);
            if (!path.exists()) {
                path.mkdirs();
            }
            fileName = path +File.separator +rs[4] +"-"+dayTime+hourTime+"-"+ nodeName+ ".log";
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            BufferedWriter pw=null;
            String res = body.replace("@","|");
            try {
                fos = new FileOutputStream(file, true);
                OutputStreamWriter osw = new OutputStreamWriter(fos,"Utf8");
                pw = new BufferedWriter(osw);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {

                pw.write(new String(res.getBytes(),"utf-8"));
                pw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
                try {
                    pw.close();
                    fos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }


            txn.commit();
            return Status.READY;
        } catch (Throwable th) {
            txn.rollback();

            if (th instanceof Error) {
                throw (Error) th;
            } else {
                throw new EventDeliveryException(th);
            }
        } finally {
            txn.close();
        }
    }



}
