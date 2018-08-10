package com.king.learn.collection.jdk8.reference.phantom;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * https://www.baeldung.com/java-phantom-reference
 */
public class PhantomReferenceTest {
    public static void main(String[] args) {
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
        List<LargeObjectFinalizer> references = new ArrayList<>();
        List<Object> largeObjects = new ArrayList<>();

        for (int i = 0; i < 3; ++i) {
            Object largeObject = new Object();
            largeObjects.add(largeObject);
            references.add(new LargeObjectFinalizer(largeObject, referenceQueue));
        }

        for (PhantomReference<Object> reference : references) {
            System.out.println(reference.isEnqueued());
        }

        largeObjects = null;
        System.gc();

        for (PhantomReference<Object> reference : references) {
            System.out.println(reference.isEnqueued());
        }

        for (Reference<?> referenceFromQueue; (referenceFromQueue = referenceQueue.poll()) != null; referenceFromQueue.clear()) {
            ((LargeObjectFinalizer) referenceFromQueue).finalizeResources();
        }
    }

    static class LargeObjectFinalizer extends PhantomReference<Object> {

        LargeObjectFinalizer(Object referent, ReferenceQueue<? super Object> q) {
            super(referent, q);
        }

        void finalizeResources() {
            System.out.println("clearing ...");
        }
    }
}
