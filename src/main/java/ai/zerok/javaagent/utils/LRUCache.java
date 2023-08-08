package ai.zerok.javaagent.utils;

import java.util.*;

public class LRUCache{


    class Node {
        String key;
        String value;
        Node previous;
        Node next;
    }
    /**
     * Always add the new node right after head;
     */
    private void addNode(Node node){
        node.previous = head;
        node.next = head.next;

        head.next.previous = node;
        head.next = node;
    }

    /**
     * Remove an existing node from the linked list.
     */
    private void removeNode(Node node){
        Node previous = node.previous;
        Node next = node.next;

        previous.next = next;
        next.previous = previous;
    }

    /**
     * Move certain node in between to the head.
     */
    private void moveToHead(Node node){
        this.removeNode(node);
        this.addNode(node);
    }

    // pop the current tail.
    private Node removeFromTail(){
        Node res = tail.previous;
        this.removeNode(res);
        return res;
    }

    private Hashtable<String, Node> cache = new Hashtable<String, Node>();
    private int count;
    private int capacity;
    private Node head, tail;

    public LRUCache(int capacity) {
        this.count = 0;
        this.capacity = capacity;

        head = new Node();
        head.previous = null;

        tail = new Node();
        tail.next = null;

        head.next = tail;
        tail.previous = head;
    }

    public String get(String key) {
        Node node = cache.get(key);
        if(node == null){
            return null; // should raise exception here.
        }

        // move the accessed node to the head;
        this.moveToHead(node);

        return node.value;
    }


    public void put(String key, String value) {
        Node node = cache.get(key);

        if(node == null){

            Node newNode = new Node();
            newNode.key = key;
            newNode.value = value;

            this.cache.put(key, newNode);
            this.addNode(newNode);

            ++count;

            if(count > capacity){
                // pop the tail
                Node tail = this.removeFromTail();
                this.cache.remove(tail.key);
                --count;
            }
        }else{
            // update the value.
            node.value = value;
            this.moveToHead(node);
        }
    }



}
