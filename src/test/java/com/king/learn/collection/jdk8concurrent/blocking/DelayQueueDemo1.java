package com.king.learn.collection.jdk8concurrent.blocking;

import lombok.NoArgsConstructor;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * https://www.cnblogs.com/shamo89/p/7055039.html
 */

public class DelayQueueDemo1 {
    public static void main(String[] args) throws InterruptedException {
        int studentNumber = 20;
        CountDownLatch countDownLatch = new CountDownLatch(studentNumber + 1);
        DelayQueue<Student> students = new DelayQueue<>();
        Random random = new Random();
        for (int i = 0; i < studentNumber; i++) {
            if (i == 0) {
                students.put(new Student("student" + (i + 1), 800, countDownLatch));

            } else {
                students.put(new Student("student" + (i + 1), 120/*30 + random.nextInt(120)*/, countDownLatch));

            }
        }
        Thread teacherThread = new Thread(new Teacher(students));
        students.put(new EndExam(students, 120, countDownLatch, teacherThread));
        teacherThread.start();
        countDownLatch.await();
        System.out.println(" 考试时间到，全部交卷！");
    }
}

@NoArgsConstructor
class Student implements Runnable, Delayed {

    private String name;
    private long workTime;
    private long submitTime;
    private boolean isForce = false;
    private CountDownLatch countDownLatch;

    public Student(String name, long workTime, CountDownLatch countDownLatch) {
        this.name = name;
        this.workTime = workTime;
        this.submitTime = TimeUnit.NANOSECONDS.convert(workTime, TimeUnit.NANOSECONDS) + System.nanoTime();
        this.countDownLatch = countDownLatch;
    }

    @Override
    public int compareTo(Delayed o) {
        if (!(o instanceof Student)) return 1;
        if (o == this) return 0;
        Student s = (Student) o;
        return Long.compare(this.workTime, s.workTime);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(submitTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public void run() {
        if (isForce) {
            System.out.println("isForce:" + isForce + " " + name + " 交卷, 希望用时" + workTime + "分钟" + " ,实际用时 120分钟");
        } else {
            System.out.println("isForce:" + isForce + " " + name + " 交卷, 希望用时" + workTime + "分钟" + " ,实际用时 " + workTime + " 分钟");
        }
        countDownLatch.countDown();
    }

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean isForce) {
        this.isForce = isForce;
    }

}

class EndExam extends Student {

    private DelayQueue<Student> students;
    private CountDownLatch countDownLatch;
    private Thread teacherThread;

    public EndExam(DelayQueue<Student> students, long workTime, CountDownLatch countDownLatch, Thread teacherThread) {
        super("强制收卷", workTime, countDownLatch);
        this.students = students;
        this.countDownLatch = countDownLatch;
        this.teacherThread = teacherThread;
    }


    @Override
    public void run() {
        teacherThread.interrupt();
        Student tmpStudent;
        for (Student student : students) {
            tmpStudent = student;
            tmpStudent.setForce(true);
            tmpStudent.run();
        }
        countDownLatch.countDown();
    }

}

class Teacher implements Runnable {
    private DelayQueue<Student> students;

    public Teacher(DelayQueue<Student> students) {
        this.students = students;
    }

    @Override
    public void run() {
        try {
            System.out.println(" test start");
            while (!Thread.interrupted()) {
                students.take().run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}