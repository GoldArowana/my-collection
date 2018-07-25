package com.king.learn.collection.mycollection.bloomfilter.demo10.predict;

public class ErrorPredict {


    /**
     * Ԥ���ڴ濿��
     */
    public static float getMemory(long length, int hash) {
        return ((length / 32) * 4 * hash) >> 20;
    }

    /**
     * ����ʱ�仹δ����
     */
    public static float getTimes(long length, int hash) {
        return (float) 220 / (12 * 12) * hash * hash + 200;
    }

    /*����֤����һ�ַ�ʽ��ڶ��ַ�ʽ�ǵȼ۵�*/

    /**
     * ��funtions�����������ĸ���
     */
    public static float getErrorChange(long k, int hash, long length) {
        return (float) Math.pow(1 - Math.pow(Math.E, -(double) k / length), hash);
    }

    /**
     * ��funtions�����������ĸ���
     */
    public static float getErrorStand(long k, int hash, long length) {
        return (float) Math.pow(1 - Math.pow(Math.E, -(double) k * hash / length), hash);
    }


    public static void getPredict(int hash, long length, int total) {
        System.out.println("��¡���������洢��������Ϊ: " + total);
        double error = 0;
        System.out.println("Ԥ��ռ���ڴ���: " + (int) getMemory(length, hash) + "M");

        for (int i = 0; i < total; i++)
            error += getErrorChange(i, hash, length);

        System.out.println("Ԥ�Ƴ������Ϊ: " + (float) error);
    }


    public static void main(String[] args) throws NumberFormatException {

        int hash = 0;
        Long length = 0L;
        int TOTAL = 10000000;

        if (args.length == 0) {
            hash = 8;
            length = 60000000L;
        } else if (args.length == 3) {
            hash = Integer.parseInt(args[0]);
            length = Long.parseLong(args[1]);
            TOTAL = Integer.parseInt(args[2]);
        } else {
            System.err.println("��������\n�������������...");
        }

        System.out.println("��¡���������洢��������Ϊ: " + TOTAL);
        double error = 0;
        System.out.println("Ԥ��ռ���ڴ���: " + (int) getMemory(length, hash) + "M");

        for (int i = 0; i < TOTAL; i++)
            error += getErrorChange(i, hash, length);

        System.out.println("Ԥ�Ƴ������Ϊ: " + (float) error);
    }

}
