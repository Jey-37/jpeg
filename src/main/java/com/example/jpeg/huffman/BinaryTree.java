package com.example.jpeg.huffman;

import java.util.*;


public class BinaryTree
{
	private Node root;

	private BinaryTree(Node r) {
		root = r;
	}

	public static BinaryTree buildTree(char[] data) {
		Map<Character, Integer> freqMap = new HashMap<>(Integer.min(700, data.length));
		for (char c : data)
			freqMap.compute(c, (k, v) -> (v == null) ? 1 : v+1);

		Queue<Node> nodes = new PriorityQueue<>(freqMap.size());
		for (var item : freqMap.entrySet())
			nodes.add(new Node(item.getValue(), item.getKey()));

		Node fc, sc;
		while (nodes.size() > 1) {
			fc = nodes.poll();
			sc = nodes.poll();
			nodes.add(new Node(fc, sc));
		}

		return new BinaryTree(nodes.poll());
	}

	public Map<Character, BinaryStream> getCodes() {
		Map<Character, BinaryStream> codes = new HashMap<>();
		getCodes(codes, new BinaryStream(), root);
		return codes;
	}

	private void getCodes(Map<Character, BinaryStream> codes, BinaryStream code, Node node) {
		if (node.isLeaf()) {
			codes.put(node.getCh(), code);
		} else {
			var bs1 = new BinaryStream(code).addZero();
			getCodes(codes, bs1, node.getFirstChild());
			var bs2 = new BinaryStream(code).addOne();
			getCodes(codes, bs2, node.getSecondChild());
		}
	}

	public BinaryStream getBinaryDesc() {
		return getBinaryDesc(root);
	}

	private BinaryStream getBinaryDesc(Node node) {
		BinaryStream res = new BinaryStream();
		if (node.isLeaf()) {
			res.addOne().add((short)node.getCh());
		} else {
			res.addZero().add(getBinaryDesc(node.getFirstChild())).add(getBinaryDesc(node.getSecondChild()));
		}
		return res;
	}

	public static BinaryTree unpackTree(BinaryStream bits) {
		Node root = unpackNode(bits);
		return new BinaryTree(root);
	}

	private static Node unpackNode(BinaryStream bits) {
		boolean currBit = bits.getBit();
		if (currBit) {
			return new Node((char)bits.getShort());
		} else {
			Node node = new Node();

			node.setFirstChild(unpackNode(bits));
			node.setSecondChild(unpackNode(bits));

			return node;
		}
	}

	public char decodeChar(BinaryStream bits) {
		Node curNode = root;
		while (!curNode.isLeaf()) {
			curNode = bits.getBit() ? curNode.getSecondChild() : curNode.getFirstChild();
		}
		return curNode.getCh();
	}
}
