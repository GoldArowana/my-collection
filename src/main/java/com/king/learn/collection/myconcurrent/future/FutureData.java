package com.king.learn.collection.myconcurrent.future;

public class FutureData implements Data {
    // 真实数据RealData的引用.
    private RealData realData = null;

    public synchronized void setRealData(RealData realData) {
        // 如果this.realData不是空, 说明已经准备好了, 直接return
        if (this.realData != null)
            return;
        this.realData = realData;
        notifyAll();
    }

    @Override
    public synchronized int getResult() throws InterruptedException {
        // 如果this.realData是null, 说明数据还没准备好, 应该等待
        if (this.realData == null) {
            wait();
        }
        return realData.getResult();
    }
}

