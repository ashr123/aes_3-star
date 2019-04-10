


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;



public class Main {

	public static void main(String[] args) {
		
		String instruction ="",b="",path_1="",d="",path_in="",f="",path_out = "";
		try {
			 instruction = args[0]; //=="-e/-d/-b"
			 b = args[1];
			 path_1 = args[2];   //path to the keys, the key should be 384 bit (128*3) || denotes the path to the plain-text message 
			 d = args[3];
			 path_in = args[4];      //a path to a file we want to encrypt/decrypt || denotes the path to the cipher-text message 
			 f = args[5];
			 path_out = args[6];     //a path to the output file  || a path to the output file with the key(s) found. 
			 File file = new File(path_out);
			 file.createNewFile();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("c'ant read arguments !!!");
			return;
		}
		int in_type =0;
		if(instruction.contains("e")){
			in_type=1;
		}else if(instruction.contains("d")){
			in_type=2;
		}else if (instruction.contains("b")){
			in_type=3;
		}
	
		switch (in_type) {
		case 1: //: instruction to encrypt the input file 		
			byte[][]keys = read_to_bytes(3,path_1);	
			int in_size= (int)( new File(path_in)).length();
			//int out_size= (int)( new File(path_out)).length();
			byte[][]in_bytes =read_to_bytes(in_size/16,path_in);
			//byte[][]out_bytes =read_to_bytes(out_size/16,path_out);
			byte [][]plaintext= new byte[4][4];
			byte [][]cypher=new byte[4][4];
			byte key1[][]=make_matrix(keys[0]);
			byte key2[][]=make_matrix(keys[1]);
			byte key3[][]=make_matrix(keys[2]);
		
			for(int i=0;i<in_bytes.length;i++){
				
				plaintext=make_matrix(in_bytes[i]);
				cypher=encrypt(plaintext,key1,key2,key3);
				//continue
				write_cypher(cypher,path_out);
			}
			break;
		case 2: //instruction to decrypt the input file 
			byte[][]keys_d = read_to_bytes(3,path_1);	
			int in_size_d= (int)( new File(path_in)).length();
			//int out_size= (int)( new File(path_out)).length();
			byte[][]in_bytes_d =read_to_bytes(in_size_d/16,path_in);
			//byte[][]out_bytes =read_to_bytes(out_size/16,path_out);
			byte [][]plaintext_d= new byte[4][4];
			byte [][]cypher_d=new byte[4][4];
			byte key1_d[][]=make_matrix(keys_d[0]);
			byte key2_d[][]=make_matrix(keys_d[1]);
			byte key3_d[][]=make_matrix(keys_d[2]);
		
			for(int i=0;i<in_bytes_d.length;i++){
				cypher_d=make_matrix(in_bytes_d[i]);
				plaintext_d=decrypt(cypher_d,key1_d,key2_d,key3_d);
				//continue
				write_cypher(plaintext_d,path_out);
			}
			
			break;
		case 3://: instruction to break the encryption algorithm 
			byte []plaintext_arr= new byte[16];
			byte [][]plaintext_matrix= new byte[4][4];
			byte [] cyphertext_arr=new byte[16];
			byte [][] cyphertext_matrix=new byte[4][4];
			//path_1 is path-to-message in this case
			BufferedInputStream bufferedInput_in = null;
			try {
				bufferedInput_in = new BufferedInputStream(new FileInputStream(path_1));
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				bufferedInput_in.read(plaintext_arr);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			plaintext_matrix=make_matrix(plaintext_arr);
			
			BufferedInputStream bufferedInput_cypher = null;
			try {
				//path_in is the path-to-cypher in this case
				bufferedInput_cypher = new BufferedInputStream(new FileInputStream(path_in));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				bufferedInput_cypher.read(cyphertext_arr);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cyphertext_matrix=make_matrix(cyphertext_arr);
			
			break_encrypt(plaintext_matrix,cyphertext_matrix,path_out);
			break;
		default:
			break;
		}
	  

	}
	
	

	public static void write_cypher(byte[][] cypther, String path_out)  {
		
		try (FileOutputStream output = new FileOutputStream(path_out, true)) {
			for (int i = 0; i < cypther.length; i++) {
				for (int j = 0; j < cypther.length; j++) {
					  output.write(cypther[j][i]);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static byte[][] make_matrix(byte[] input_file){
		byte[][] s_matrix = new byte[4][4];
		int k =0;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				s_matrix[j][i] = input_file[k];
				k++;
			}
		}
		return s_matrix;
	}
	
	public static byte[][] read_to_bytes(int intervals, String file_name){
		byte [][] bytes = new  byte[intervals][16];
		BufferedInputStream bufferedInput = null;
		try {
			bufferedInput = new BufferedInputStream(new FileInputStream(file_name));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int i =0;
		while(intervals>0){
			bufferedInput.mark(16*i);
			try {
				bufferedInput.read(bytes[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i++;
			intervals--;
			
		}
		try {
			bufferedInput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes;
	}

	
	public static byte[][] encrypt(byte[][] input_file,byte[][] key_1,byte[][] key_2,byte[][] key_3) {
		//AES1
		byte[][] cypher1=shiftRows_to_left(input_file);
		addRoundKey(cypher1,key_1);
		//AES2
		byte[][] cypher2=shiftRows_to_left(cypher1);
		addRoundKey(cypher2,key_2);
		//AES3
		byte[][] cypher3=shiftRows_to_left(cypher2);
		addRoundKey(cypher3,key_3);
		
		
		return cypher3;
	}
	public static byte[][] decrypt(byte[][] cypther_d, byte[][] key1_d, byte[][] key2_d, byte[][] key3_d) {
		addRoundKey(cypther_d,key3_d);
		byte[][] cypher1=shiftRows_to_right(cypther_d);
		
		//AES2
		addRoundKey(cypher1,key2_d);
		byte[][] cypher2=shiftRows_to_right(cypher1);
		
		//AES3
		addRoundKey(cypher2,key1_d);
		byte[][] cypher3=shiftRows_to_right(cypher2);
		
		
		return cypher3;
	}
	
	
	
	
	public static void addRoundKey(byte[][] s,byte[][] key) {
		// TODO Auto-generated method stub
		for (int i = 0; i < key.length; i++) {
			for (int j = 0; j < key.length; j++) {
				s[i][j] = (byte) (s[i][j]  ^ key[i][j]) ;
				
			}
		}

	}
	public static byte[][] shiftRows_to_left(byte[][] s){ 
	
		byte[][] shifted_matrix = new byte[4][4];
		for (int i = 0; i < 4; i++) {//first row the same
			shifted_matrix[0][i] = s[0][i];
		}
		shifted_matrix[1][3] = s[1][0];
		shifted_matrix[1][0] = s[1][1];
		shifted_matrix[1][1] = s[1][2];
		shifted_matrix[1][2] = s[1][3];
		
		shifted_matrix[2][2] = s[2][0];
		shifted_matrix[2][3] = s[2][1];
		shifted_matrix[2][0] = s[2][2];
		shifted_matrix[2][1] = s[2][3];
		
		shifted_matrix[3][1] = s[3][0];
		shifted_matrix[3][2] = s[3][1];
		shifted_matrix[3][3] = s[3][2];
		shifted_matrix[3][0] = s[3][3];
		
		
		
		return shifted_matrix;
	}
	
	
	public static byte[][] shiftRows_to_right(byte[][] s){ //rotate_side==1 right 
		
		byte[][] shifted_matrix = new byte[4][4];
		for (int i = 0; i < 4; i++) {//first row the same
			shifted_matrix[0][i] = s[0][i];
		}
		shifted_matrix[1][1] = s[1][0];
		shifted_matrix[1][2] = s[1][1];
		shifted_matrix[1][3] = s[1][2];
		shifted_matrix[1][0] = s[1][3];
		
		shifted_matrix[2][2] = s[2][0];
		shifted_matrix[2][3] = s[2][1];
		shifted_matrix[2][0] = s[2][2];
		shifted_matrix[2][1] = s[2][3];
		
		shifted_matrix[3][3] = s[3][0];
		shifted_matrix[3][0] = s[3][1];
		shifted_matrix[3][1] = s[3][2];
		shifted_matrix[3][2] = s[3][3];
		
		
		
		return shifted_matrix;
	}

	public static void break_encrypt(byte [][]plaintext,byte [][]cypher,String output){
		SecureRandom random = new SecureRandom();
		byte[] key1 = new byte[16];
		random.nextBytes(key1);
		byte[] key2 = new byte[16];
		random.nextBytes(key2);
		while(Arrays.equals(key1,key2)){
			random.nextBytes(key1);
			random.nextBytes(key2);
		}
		byte[][] key1_matrix=make_matrix(key1);
		byte[][] key2_matrix=make_matrix(key2);
		byte[][] key3_matrix =encrypt(plaintext,key1_matrix,key2_matrix,cypher);
		write_cypher(key1_matrix,output);
		write_cypher(key2_matrix,output);
		write_cypher(key3_matrix,output);
	}
	
	
}
