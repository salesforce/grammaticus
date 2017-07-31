/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.settings;

import java.io.Serializable;
import java.util.*;

import com.force.i18n.commons.util.collection.ExpandableArray;

/**
 * SharedKeyMap is an implementation of the Map interface where many SharedKeyMap instances 
 * share a single copy of their key mappings. This is useful if you have many maps that all have
 * the same (or nearly the same) keys, because you don't have to store the keys over and over.
 * 
 * Because SharedKeyMaps only make sense when there are multiple maps in play, the only way
 * to create one is with the static factory methods createSharedKeyMaps and createEmptySharedKeyMaps,
 * which create a List of SharedKeyMaps that all share the same keys.
 * 
 * Internally, each SharedKeyMap points to a single Map that maps keys to integer indices. Then each
 * SharedKeyMap has its own ArrayList that contains its values, indexed by the indices mentioned above.
 * Removing from an individual SharedKeyMap doesn't affect the shared map, but adding to an individual
 * SharedKeyMap with a key that wasn't in the shared map will add that key to the shared map.
 * 
 * @author shansma
 */
public class SharedKeyMap<K, V> extends AbstractMap<K, V> implements Serializable {

    private final ExpandableArray<V> values;
    private final Map<K, Integer> keyToIndex;
    private int size = 0;

    /**
     * See HashMap.modCount
     */
    private transient volatile int modCount = 0;

    private static final Object NULL_MARKER = new Object();

    private static final long serialVersionUID = -1L;

    /**
     * Takes a list of Maps and returns a list of SharedKeyMaps with the same mappings and values.
     * 
     * @param maps - the maps to re-create as SharedKeyMaps
     */
    public static <K, V> List<SharedKeyMap<K, V>> createSharedKeyMaps(List<Map<K, V>> maps) {
        return createSharedKeyMaps(maps, false);
    }

    /**
     * Takes a list of Maps and returns a list of SharedKeyMaps with the same mappings and values.
     * 
     * @param maps - the maps to re-create as SharedKeyMaps
     * @param removeFromList - remove the passed-in maps from the list as they are processed, so they can be garbage collected.
     */
    public static <K, V> List<SharedKeyMap<K, V>> createSharedKeyMaps(List<Map<K, V>> maps, final boolean removeFromList) {
        Map<K, Integer> keyToIndex = new HashMap<K, Integer>();
        List<SharedKeyMap<K, V>> results = new ArrayList<SharedKeyMap<K, V>>(maps.size());

        // build the shared key maps
        for (Iterator<Map<K, V>> i = maps.iterator(); i.hasNext();) {
            Map<K, V> values = i.next();
            SharedKeyMap<K, V> newMap = new SharedKeyMap<K, V>(keyToIndex, values);
            results.add(newMap);
            if (removeFromList)
                i.remove();
        }

        return results;
    }

    /**
     * Creates a list of empty SharedKeyMaps that all use the specified keys. If you know ahead of time
     * what the keys in your maps will (likely) be, but don't yet have the maps created, you can call this
     * function and then use the maps it returns as you would use normal maps.
     * 
     * @param keys - the keys that all your maps will (likely) use
     * @param numberOfMaps - the number of maps this function will create for you
     */
    public static <K, V> List<SharedKeyMap<K, V>> createEmptySharedKeyMaps(List<K> keys, int numberOfMaps) {
        Map<K, Integer> keyToIndex = new HashMap<K, Integer>(keys.size() * 3 / 2);
        List<SharedKeyMap<K, V>> results = new ArrayList<SharedKeyMap<K, V>>(numberOfMaps);
        int index = 0;

        // build the key-to-index map
        for (K key : keys) {
            keyToIndex.put(key, index++);
        }

        // build the shared key maps
        for (int i = 0; i < numberOfMaps; i++) {
            Map<K, V> emptyMap = Collections.emptyMap();
            results.add(new SharedKeyMap<K, V>(keyToIndex, emptyMap));
        }

        return results;
    }

    /**
     * Creates a list of empty SharedKeyMaps that all share a single (empty) key map.
     * 
     * @param numberOfMaps - the number of maps this function will create for you
     */
    public static <K, V> List<SharedKeyMap<K, V>> createEmptySharedKeyMaps(int numberOfMaps) {
        Map<K, Integer> keyToIndex = new HashMap<K, Integer>();
        List<SharedKeyMap<K, V>> results = new ArrayList<SharedKeyMap<K, V>>(numberOfMaps);

        // build the shared key maps
        for (int i = 0; i < numberOfMaps; i++) {
            Map<K, V> emptyMap = Collections.emptyMap();
            results.add(new SharedKeyMap<K, V>(keyToIndex, emptyMap));
        }

        return results;
    }

    /**
     * Creates a list of empty SharedKeyMaps that all use the key map of the specified SharedKeyMap.
     * 
     * @param numberOfMaps - the number of maps this function will create for you
     */
    public static <K, V> List<SharedKeyMap<K, V>> createEmptySharedKeyMaps(int numberOfMaps, SharedKeyMap<K, V> map) {
        if (map == null)
            return createEmptySharedKeyMaps(numberOfMaps);

        List<SharedKeyMap<K, V>> results = new ArrayList<SharedKeyMap<K, V>>(numberOfMaps);

        // build the shared key maps
        for (int i = 0; i < numberOfMaps; i++) {
            results.add(new SharedKeyMap<K, V>(map));
        }

        return results;
    }

    /**
     * Creates a new shared key map that shares the keymap of map.
     */
    public SharedKeyMap(SharedKeyMap<K, ? extends V> map) {
        this.keyToIndex = map.keyToIndex;
        this.values = new ExpandableArray<V>();
    }

    /**
     * Creates an empty, isolated SharedKeyMap.
     */
    public SharedKeyMap() {
        this(new HashMap<K, Integer>(), 16);
    }
    
    /**
     * Creates an empty, isolated SharedKeyMap, with control over
     * the initial size of the values array. This is useful when you have a SharedKeyMap
     * that you use as the canonical version to "clone" for other SharedKeyMaps
     * and you want to save the overhead of the unnecessary value arrays that would
     * remain empty.
     * 
     * @param initialValueCapacity if this is non-negative, then its used as
     *      the initial size for the values array. If its negative, then
     *      this SharedKeyMap cannot have any values stored in it.
     */
    public SharedKeyMap(int initialValueCapacity) {
        this(new HashMap<K, Integer>(), initialValueCapacity);
    }
    
    /**
     * Creates an empty, isolated SharedKeyMap, with control over
     * the key-to-index value map and also initial size of the values array. 
     * This is useful when you have a SharedKeyMap
     * that you use as the canonical version to "clone" for other SharedKeyMaps
     * and you want to save the overhead of the unnecessary value arrays that would
     * remain empty.
     * 
     * @param keyToIndex maps keys to their index in the values array. This 
     *      structure is shared across threads.
     * @param initialValueCapacity if this is non-negative, then its used as
     *      the initial size for the values array. If its negative, then
     *      this SharedKeyMap cannot have any values stored in it.
     */
    public SharedKeyMap(Map<K, Integer> keyToIndex, int initialValueCapacity) {
        this.keyToIndex = keyToIndex;
        if (initialValueCapacity >= 0) {
            this.values = new ExpandableArray<V>(initialValueCapacity);
        } else {
            this.values = null;
        }
    }
    
    private SharedKeyMap(Map<K, Integer> keyToIndex, Map<K, V> values) {
        this.keyToIndex = keyToIndex;
        if (this.keyToIndex.size() > 0) {
            this.values = new ExpandableArray<V>(this.keyToIndex.size());
        } else {
            this.values = new ExpandableArray<V>();
        }
        putAll(values);
    }

    @Override
    public int size() {
        return this.size;
    }

    public void trimToSize() {
        this.values.trimToSize();
    }
    
    /**
     * Same as get, but returns NULL_MARKER instead of null
     */
    private final V inner_get(Object key) {
        Integer index = this.keyToIndex.get(key);
        if (index == null)
            return null;
        if (this.values == null) return null;
        return this.values.get(index.intValue());
    }

    @Override
    public boolean containsKey(Object key) {
        return inner_get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        if (this.values == null) return false;
        
        value = maskNull(value);
        for (int i = 0; i < this.values.size(); i++) {
            if (value.equals(this.values.get(i)))
                return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        return unmaskNull(inner_get(key));
    }

    @Override
    public V put(K key, V value) {
        Integer index = this.keyToIndex.get(key);
        if (index == null) {
            // add the key to our key-to-index map
            index = this.keyToIndex.get(key);
            if (index == null) {
                index = Integer.valueOf(this.keyToIndex.size());
                this.keyToIndex.put(key, index);
            }
        }
        return setAt(index.intValue(), value);
    }

    private V setAt(int index, V value) {
        if (this.values == null) throw new UnsupportedOperationException();

        this.modCount++;
        value = maskNull(value);
        V oldValue = this.values.get(index);
        this.values.set(index, value);
        if (oldValue == null) {
            this.size++;
        }
        this.values.set(index, value);
        return unmaskNull(oldValue);
    }

    @Override
    public V remove(Object key) {
        Integer index = this.keyToIndex.get(key);
        if (index == null)
            return null;
        return removeAt(index.intValue());
    }

    private V removeAt(int index) {
        if (this.values == null) throw new UnsupportedOperationException();
        
        this.modCount++;
        V oldValue = this.values.get(index);
        if (oldValue != null) {
            this.size--;
        }
        this.values.set(index, null);
        return unmaskNull(oldValue);
    }

    /**
     * Clears out the values in this instance of a SharedKeyMap; does not affect the contents of
     * the key-to-index map.
     */
    @Override
    public void clear() {
        if (this.values == null) throw new UnsupportedOperationException();
        
        this.modCount++;
        this.size = 0;
        this.values.clear();
    }

    @Override
    public Set<K> keySet() {
        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                final Iterator<K> backingIterator = SharedKeyMap.this.keyToIndex.keySet().iterator();
                return new Iterator<K>() {
                    private K lookaheadNext = null; // the value we will return the next time next() is called
                    private K currentNext = null; // the value we returned the most recent time next() was called
                    private int expectedModCount = SharedKeyMap.this.modCount;

                    @Override
                    public boolean hasNext() {
                        if (SharedKeyMap.this.modCount != expectedModCount)
                            throw new ConcurrentModificationException();

                        if (lookaheadNext != null)
                            return true;
                        while (backingIterator.hasNext()) {
                            lookaheadNext = backingIterator.next();
                            if (SharedKeyMap.this.containsKey(lookaheadNext))
                                return true;
                        }
                        return false;
                    }

                    @Override
                    public K next() {
                        if (!hasNext())
                            throw new NoSuchElementException();
                        this.currentNext = lookaheadNext;
                        lookaheadNext = null;
                        return this.currentNext;
                    }

                    @Override
                    public void remove() {
                        if (SharedKeyMap.this.modCount != expectedModCount)
                            throw new ConcurrentModificationException();

                        if (this.currentNext == null)
                            throw new NoSuchElementException();
                        SharedKeyMap.this.remove(this.currentNext);
                        this.expectedModCount = SharedKeyMap.this.modCount;
                    }
                };
            }

            @Override
            public int size() {
                return SharedKeyMap.this.size();
            }

            @Override
            public boolean contains(Object o) {
                return containsKey(o);
            }
        };
    }

    private static <T> T unmaskNull(T o) {
        if (o == NULL_MARKER)
            return null;
        return o;
    }

    @SuppressWarnings("unchecked")
    private static <T> T maskNull(T o) {
        if (o == null)
            return (T)NULL_MARKER;
        return o;
    }

    /**
     * The getKey function on the MapEntries returned by this iterator takes O(keys) time,
     * so don't call it if you can avoid it.
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        final Set<K> keySet = keySet();
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Iterator<K> keyIterator = keySet.iterator();
                return new Iterator<Entry<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return keyIterator.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        final K key = keyIterator.next();
                        return new Map.Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return key;
                            }

                            @Override
                            public V getValue() {
                                return SharedKeyMap.this.get(key);
                            }

                            @Override
                            public V setValue(V newValue) {
                                // this is an optional operation
                                throw new UnsupportedOperationException();
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        keyIterator.remove();
                    }
                };
            }

            @Override
            public int size() {
                return SharedKeyMap.this.size();
            }
        };
    }

    /**
     * Returns the number of entries in the keymap. This isn't something you should generally care about,
     * but if you're the curious type, here it is. It's also useful if you're serializing SharedKeyMaps
     * one by one -- since the keymap is always appended to, and entries are never modified or removed,
     * keyMapSize() will change whenever the contents of the keymap have changed (and thus need to be
     * re-serialized).
     */
    public int keyMapSize() {
        return this.keyToIndex.size();
    }

}
