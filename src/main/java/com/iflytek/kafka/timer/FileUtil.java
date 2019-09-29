package com.iflytek.kafka.timer;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.*;
import java.util.Vector;

public class FileUtil {

    public static void uploadFile(ChannelSftp sftp,String sPath, String dPath){

        try {

            if (isDirExist(sftp, dPath)) {
                String createPath = dPath + sPath.substring(sPath.lastIndexOf(File.separator) + File.separator.length());
                if (isDirExist(sftp, createPath)) {
                    deleteSFTP(sftp, createPath);
                }
            } else {
                createDir(sftp, dPath);
            }
            sftp.cd(dPath);
            File file = new File(sPath);
            copyFile(sftp, file, sftp.pwd());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sftp.disconnect();
        }

    }

    /**
     * 判断目录是否存在
     *
     * @param directory
     * @return
     */
    public static boolean isDirExist(ChannelSftp sftp,String directory) {
        try {
            Vector<?> vector = sftp.ls(directory);
            return (null != vector);
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 删除stfp文件
     *
     * @param directory：要删除文件所在目录
     */
    public static void deleteSFTP(ChannelSftp sftp, String directory) {
        try {
            if (isDirExist(sftp,directory)) {
                Vector<ChannelSftp.LsEntry> vector = sftp.ls(directory);
                if (vector.size() == 1) { // 文件，直接删除
                    sftp.rm(directory);
                } else if (vector.size() == 2) { // 空文件夹，直接删除
                    sftp.rmdir(directory);
                } else {
                    String fileName = "";
                    // 删除文件夹下所有文件
                    for (ChannelSftp.LsEntry en : vector) {
                        fileName = en.getFilename();
                        if (".".equals(fileName) || "..".equals(fileName)) {
                            continue;
                        } else {
                            deleteSFTP(sftp,directory + "/" + fileName);
                        }
                    }
                    // 删除文件夹
                    sftp.rmdir(directory);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建目录
     *
     * @param createPath
     * @return
     */
    public static void createDir(ChannelSftp sftp, String createPath) {
        try {
            if (isDirExist(sftp,createPath)) {
                sftp.cd(createPath);
            }
            String pathArry[] = createPath.split("/");
            StringBuffer filePath = new StringBuffer("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path + "/");
                if (isDirExist(sftp,filePath.toString())) {
                    sftp.cd(filePath.toString());
                } else {
                    // 建立目录
                    sftp.mkdir(filePath.toString());
                    // 进入并设置为当前目录
                    sftp.cd(filePath.toString());
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(ChannelSftp sftp, File file, String pwd) {

        if (file.isDirectory()) {
            File[] list = file.listFiles();
            try {
                try {
                    String fileName = file.getName();
                    sftp.cd(pwd);
                    System.out.println("正在创建目录:" + sftp.pwd() + "/" + fileName);
                    sftp.mkdir(fileName);
                    System.out.println("目录创建成功:" + sftp.pwd() + "/" + fileName);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                pwd = pwd + "/" + file.getName();
                try {

                    sftp.cd(file.getName());
                } catch (SftpException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            for (int i = 0; i < list.length; i++) {
                copyFile(sftp, list[i], pwd);
            }
        } else {

            try {
                sftp.cd(pwd);

            } catch (SftpException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            System.out.println("正在复制文件:" + file.getAbsolutePath());
            InputStream instream = null;
            OutputStream outstream = null;
            try {
                outstream = sftp.put(file.getName());
                instream = new FileInputStream(file);

                byte b[] = new byte[1024];
                int n;
                try {
                    while ((n = instream.read(b)) != -1) {
                        outstream.write(b, 0, n);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } catch (SftpException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    outstream.flush();
                    outstream.close();
                    instream.close();

                } catch (Exception e2) {
                    // TODO: handle exception
                    e2.printStackTrace();
                }
            }
        }
    }

}
