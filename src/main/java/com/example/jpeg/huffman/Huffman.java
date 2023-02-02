package com.example.jpeg.huffman;

import com.example.jpeg.huffman.exception.DecodingException;

import java.util.Map;


public class Huffman
{
    public static byte[] code(char[] inData) {
        BinaryTree tree = BinaryTree.buildTree(inData);

        Map<Character, BinaryStream> codeTable = tree.getCodes();

        BinaryStream binDesc = tree.getBinaryDesc();

        BinaryStream outStream = new BinaryStream(binDesc.length()+32+inData.length*5);
        outStream.add(binDesc).add(inData.length);
        for (char c : inData)
            outStream.add(codeTable.get(c));

        return outStream.toArray();
    }

    public static char[] decode(byte[] inBytes) throws DecodingException {
        try {
            BinaryStream bitStream = new BinaryStream(inBytes);
            BinaryTree tree = BinaryTree.unpackTree(bitStream);

            int textLength = bitStream.getInt();

            char[] text = new char[textLength];
            for (int i = 0; i < textLength; ++i)
                text[i] = tree.decodeChar(bitStream);

            return text;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DecodingException("File is broken");
        }
    }
}
