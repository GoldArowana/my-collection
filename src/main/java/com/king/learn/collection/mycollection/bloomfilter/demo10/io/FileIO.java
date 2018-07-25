package com.king.learn.collection.mycollection.bloomfilter.demo10.io;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileIO {
    public static List<String> fileNameList = new ArrayList<String>();
    public List<String> fileList;
    private String fileName;
    private String pathString;

    public FileIO() {
        fileName = new String();
        fileList = new ArrayList<String>();
    }

    /**
     * ��ָ������д��ָ���ļ���
     * ��׷�ӵķ�ʽд��
     *
     * @param fileWriter �ļ�·��
     * @param context    �洢����
     * @param bool       �Ƿ�׷��д��
     */
    public static void FileWrite(String fileName, String context, boolean bool) {
        try {
            FileWriter fileWriter = new FileWriter(fileName, bool);
            fileWriter.write(context);
            fileWriter.flush();
        } catch (Exception e) {
        }
    }

    /**
     * ��ָ������д��ָ���ļ���
     * ��׷�ӵķ�ʽд��
     *
     * @param fileWriter �ļ�·��
     * @param context    �洢����
     */
    public static void FileWrite(String fileName, String context) {
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            fileWriter.write(context);
            fileWriter.flush();
        } catch (Exception e) {
        }
    }

    /**
     * @param path    �ļ�·��
     * @param suffix  ��׺��
     * @param isdepth �Ƿ������Ŀ¼
     * @return ����Ҫ����ļ�·�����б�
     */
    public static List<String> getListFiles(String path, String suffix, boolean isdepth) {
        File file = new File(path);
        return FileIO.listFile(file, suffix, isdepth);
    }

    public static List<String> getListFiles(String path, String suffix) {
        File file = new File(path);
        return FileIO.listFile(file, suffix);
    }

    public static List<String> listFile(File f, String suffix) {
        return FileIO.listFile(f, suffix, true);
    }

    /**
     * ��ȡĿ¼����Ŀ¼��ָ���ļ�����·�� ���ŵ�һ���������淵�ر���
     *
     * @author juefan
     */
    public static List<String> listFile(File f, String suffix, boolean isdepth) {
        //��Ŀ¼��ͬʱ��Ҫ������Ŀ¼
        if (f.isDirectory() && isdepth == true) {
            //listFiles()�Ƿ���Ŀ¼�µ��ļ�·������
            File[] t = f.listFiles();
            for (int i = 0; i < t.length; i++) {
                listFile(t[i], suffix, isdepth);
            }
        } else {
            String filePath = f.getAbsolutePath();
            if (suffix == "" || suffix == null) {
                fileNameList.add(filePath);
            } else {
                //���һ��.(����׺��ǰ���.)������
                int begIndex = filePath.lastIndexOf(".");
                //System.out.println("����Ϊ :"+begIndex);
                String tempsuffix = "";
                //��ֹ���ļ���ȴû�к�׺���������ļ�
                if (begIndex != -1) {
                    //tempsuffixȡ�ļ��ĺ�׺
                    tempsuffix = filePath.substring(begIndex + 1, filePath.length());
                }
                if (tempsuffix.equals(suffix)) {
                    fileNameList.add(filePath);
                }
            }
        }
        return fileNameList;
    }

    /**
     * ����Ҫ��ȡ���ļ����ļ�·��
     */
    public void setFileName(String fileString) {
        this.fileName = fileString;
    }

    /**
     * �����ļ�·��
     */
    public void setPath(String path) {
        this.pathString = path;
    }

    /**
     * ��ȡ�ļ����ڵ������ļ�����
     */
    public void setFileNameList() {
        for (File file : new File(pathString).listFiles()) {
            fileNameList.add(file.getAbsolutePath());
        }
    }

    /**
     * ��fileList���鸴�Ƴ���
     * ����ԭ�е�fileList���
     */
    public List<String> cloneList() {
        List<String> tmpList = new ArrayList<String>();
        tmpList.addAll(fileList);
        fileList.clear();
        return tmpList;
    }

    /**
     * ��ȡ�ļ��ڵ����ݲ��洢��fileList
     */
    public void FileRead() {
        FileRead("gbk");
    }

    /**
     * ��ȡ�ļ��ڵ�����
     * ��fileName�ļ��ڵ����ݰ��д洢��fileList������
     */
    public void FileRead(String code) {
        fileList.clear();
        try {
            Scanner fileScanner = new Scanner(new File(fileName), code);
            while (fileScanner.hasNextLine()) {
                fileList.add(fileScanner.nextLine());
            }
        } catch (Exception e) {
        }
    }

    /**
     * ��ȡ�ļ����б��������������
     */
    public void FileListRead() {
        FileListRead("gbk");
    }

    /**
     * ��ȡ�ļ����б��������������
     *
     * @param code �ļ������ʽ
     */
    public void FileListRead(String code) {
        System.out.println("�����ļ���ȡ����......");
        try {
            for (String file : fileNameList) {
                if (new File(file).exists()) {
                    Scanner fileScanner = new Scanner(new File(file), code);
                    while (fileScanner.hasNextLine()) {
                        fileList.add(fileScanner.nextLine());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("�ļ�������!");
        }
        System.out.println("���� " + fileNameList.size() + " ���ļ�");
    }

}
