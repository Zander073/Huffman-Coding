import java.util.*;
import java.util.Map.Entry;
import java.io.ByteArrayOutputStream; // Optional

/**
 * Huffman instances provide reusable Huffman Encoding Maps for
 * compressing and decompressing text corpi with comparable
 * distributions of characters.
 */
public class Huffman {
    
    // -----------------------------------------------
    // Construction
    // -----------------------------------------------

    private static HuffNode trieRoot;
    // TreeMap chosen here just to make debugging easier
    private static TreeMap<Character, String> encodingMap;
    // Character that represents the end of a compressed transmission
    private static final char ETB_CHAR = 23;
    // ETB key in encodingMap
    private static final char ETB_Key = '*';
    
    /**
     * Creates the Huffman Trie and Encoding Map using the character
     * distributions in the given text corpus
     * @param corpus A String representing a message / document corpus
     *        with distributions over characters that are implicitly used
     *        throughout the methods that follow. Note: this corpus ONLY
     *        establishes the Encoding Map; later compressed corpi may
     *        differ.
     */
    public Huffman (String corpus) {
        trieRoot = null;
    	encodingMap = new TreeMap<Character, String>();
    	
    	// Gets the frequency of each character in the String 'corpus' and returns a 
    	// mapping of each character and its associated frequency. 
    	HashMap<Character, Integer> corpusChars = getChars(corpus);
    	PriorityQueue<HuffNode> pq = new PriorityQueue<HuffNode>();
    	
    	// Adds the ETB_CHAR to the PQ to be encoded in the encoding map.
    	pq.add(new HuffNode(ETB_CHAR, 1));
    	
    	// Putting the characters and their relative frequencies in a priority queue.
    	for (Entry<Character, Integer> entry : corpusChars.entrySet()) {
    	    HuffNode HuffNode = new HuffNode(entry.getKey(), entry.getValue());
    	    pq.add(HuffNode);
    	}

        while (pq.size() > 1) {
            HuffNode x = pq.peek();
            pq.poll();
            HuffNode y = pq.peek();
            pq.poll();
          
            // '-' char represents parent node
            HuffNode temp = new HuffNode('-', (x.count + y.count));
            temp.left = x;
            temp.right = y;
            trieRoot = temp;
            pq.add(temp);
        }
        System.out.println(encodingMap);
        getCode(trieRoot, "");
    }
    
    
    // -----------------------------------------------
    // Compression
    // -----------------------------------------------
    
    /**
     * Compresses the given String message / text corpus into its Huffman coded
     * bitstring, as represented by an array of bytes. Uses the encodingMap
     * field generated during construction for this purpose.
     * @param message String representing the corpus to compress.
     * @return {@code byte[]} representing the compressed corpus with the
     *         Huffman coded bytecode. Formatted as:
     *         (1) the bitstring containing the message itself, (2) possible
     *         0-padding on the final byte.
     */
    public byte[] compress (String message) {
    	ByteArrayOutputStream output = new ByteArrayOutputStream();
    	String compressedMessage = "";
    	int i;
    	
    	for(i = 0; i < message.length(); i++) {
    	    char key = message.charAt(i);
    	    compressedMessage = compressedMessage + encodingMap.get(key);
    	}
    	compressedMessage = compressedMessage + encodingMap.get(ETB_Key); 
    	
    	// If the length of the bit-string is not divisible by 8, add right padding.
    	if(compressedMessage.length() % 8 != 0) {
    	    int closestNum = (compressedMessage.length() + 8) - (compressedMessage.length() % 8);
    	    compressedMessage = rightPadding(closestNum - compressedMessage.length(), compressedMessage);
    	}
   	for(i = 0; i < compressedMessage.length(); i++) {
    	    if((i + 1) % 8 == 0) {
    	        String temp = compressedMessage.substring((i + 1) - 8, (i + 1));
    		byte m = (byte) Integer.parseInt(temp, 2);
    		output.write(m);
    	    }
   	} 		    		
        return output.toByteArray();
    }
    
    
    // -----------------------------------------------
    // Decompression
    // -----------------------------------------------
    
    /**
     * Decompresses the given compressed array of bytes into their original,
     * String representation. Uses the trieRoot field (the Huffman Trie) that
     * generated the compressed message during decoding.
     * @param compressedMsg {@code byte[]} representing the compressed corpus with the
     *        Huffman coded bytecode. Formatted as:
     *        (1) the bitstring containing the message itself, (2) possible
     *        0-padding on the final byte.
     * @return Decompressed String representation of the compressed bytecode message.
     */
    public String decompress (byte[] compressedMsg) {
    	String cm = "";
    	String message = "";
    	int i;
    	
    	// Gets the compressed message and formats the bits into a bit-string.
    	for(i = 0; i < compressedMsg.length; i++) {
    	    byte b = compressedMsg[i];
    	    String temp = String.format("%" + 8 + "s", Integer.toBinaryString(b)).replaceAll(" ", "0");
    	    if(temp.length() > 8) {
    	        int lower = temp.length() - 8;
    		int upper = temp.length();
            	temp = temp.substring(lower, upper);
            	cm = cm + temp;
            } else {
            	cm = cm + temp;
            }
    	}
    	
    	// Traverses Huffman trie for each bit in the bit-string 'cm'.
    	for(i = 0; i < cm.length();) {
    	    HuffNode node = trieRoot;
    	    while(!node.isLeaf()) {
    	        if(cm.charAt(i) == '0') {
    	        	node = node.left;
    	        } else {
    	        	node = node.right;
    	        }
    	    i++;
    	    }
    	    if(node.character == ETB_CHAR) {
    	        break;
    	    } else {
    	    	message = message + node.character;
    	    }
    	}
        return message;
    }
    
    // Creates a HashMap for the given string's character's relative frequencies <Character, (Integer) Frequency>. 
    public static HashMap<Character, Integer> getChars(String s){
    	HashMap<Character, Integer> chars = new HashMap<Character, Integer>();
    	for(int i = 0; i < s.length(); i++) {
    	    Character temp = s.charAt(i);
    	    if(!chars.containsKey(temp)) {
    		chars.put(temp, 1);
    	    } else {
    		chars.put(temp, chars.get(temp) + 1);
    	    }
    	}
    	return chars;
    }
    
    // Gets the bit string of each character. Left sub trees are assigned '0' and right sub trees are assigned '1'
    public static void getCode(HuffNode root, String s) {
        if(root.isLeaf() && Character.isDefined(root.character)) {
	    if(root.character == ETB_CHAR) {
		// If encountered character is ETB, represent as a '*'.
		encodingMap.put(ETB_Key, s);
	    } else {
		encodingMap.put(root.character, s);
	    }
	    return;
	}
	getCode(root.left, s + "0");
	getCode(root.right, s + "1");
    }
    
    // Adds n zeros to end of String s. 
    public static String rightPadding(int n, String s) {
    	String output = "";
    	for(int i = 0; i < n; i++) {
    	    output = output + "0";
    	}
    	return s + output;
    }
    
    // -----------------------------------------------
    // Huffman Trie
    // -----------------------------------------------
    
    /**
     * Huffman Trie Node class used in construction of the Huffman Trie.
     * Each node is a binary (having at most a left and right child), contains
     * a character field that it represents, and a count field that holds the 
     * number of times the node's character (or those in its subtrees) appear 
     * in the corpus.
     */
    private static class HuffNode implements Comparable<HuffNode> {
        
        HuffNode left, right;
        char character;
        int count;
        
        HuffNode (char character, int count) {
            this.count = count;
            this.character = character;
        }
        
        public boolean isLeaf () {
            return this.left == null && this.right == null;
        }
        
        public int compareTo (HuffNode other) {
            if(this.count != other.count) {
            	return this.count - other.count;
            } else {
            	@SuppressWarnings("deprecation")
            	Character x = new Character(this.character);
            	@SuppressWarnings("deprecation")
            	Character y = new Character(other.character);
            	return x.compareTo(y);
            }
        }
    }

}
