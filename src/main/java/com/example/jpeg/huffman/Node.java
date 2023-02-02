package com.example.jpeg.huffman;


public class Node  implements Comparable<Node>
{
    private Node firstChild, secondChild;
    private int value;
    private char ch;

    public Node() {}

    public Node(char ch) {
        this.ch = ch;
    }

    public Node(int v, char c) {
        value = v;
        ch = c;
    }

    public Node(Node fc, Node sc) {
        firstChild = fc;
        secondChild = sc;
        value = fc.getValue() + sc.getValue();
    }

    public boolean isLeaf() {
        return firstChild == null && secondChild == null;
    }

    public int getValue() {
        return value;
    }

    public char getCh() {
        return ch;
    }

    public void setFirstChild(Node firstChild) {
        this.firstChild = firstChild;
    }

    public void setSecondChild(Node secondChild) {
        this.secondChild = secondChild;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setCh(char ch) {
        this.ch = ch;
    }

    public Node getFirstChild() {
        return firstChild;
    }

    public Node getSecondChild() {
        return secondChild;
    }

    @Override
    public int compareTo(Node n) {
        return Integer.compare(value, n.getValue());
    }
}
