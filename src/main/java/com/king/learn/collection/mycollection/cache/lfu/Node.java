package com.king.learn.collection.mycollection.cache.lfu;

import lombok.*;

public abstract class Node {
    Node prev = null;
    Node next = null;
}

@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
class FrequencyNode extends Node {
    @NonNull int frequency;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    NodeList lfuCacheEntryList = new NodeList();
}


@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
class LFUCacheEntry<K, V> extends Node {
    @ToString.Include @EqualsAndHashCode.Include K key;
    @ToString.Include @EqualsAndHashCode.Include V value;
    FrequencyNode frequencyNode;
}
