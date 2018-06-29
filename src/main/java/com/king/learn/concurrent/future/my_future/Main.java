package com.king.learn.concurrent.future.my_future;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();

        // 调用了之后会立即返回一个FutureData, 这个data就是FutureData
        Data data = client.request(4);

        // 用sleep来模拟主线程正在处理其他事情
        Thread.sleep(0);

        // getResult来获取真实数据
        //     |- 如果这时候真实数据没准备好, 那么就wait, 等待notify, 然后获取到真实数据
        //     |- 如果这时候真实数据准备好了, 那么就可以直接获取到了
        System.out.println("数据=" + data.getResult());
    }
}