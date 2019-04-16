import java.io.*;

import static java.rmi.server.LogStream.log;

public class AesTal
{

	/**
	 * make aes1 encrypt
	 *
	 * @param data to encrypt
	 * @param key  aes1 func
	 * @return encrypted data
	 */
	public static byte[][] aes1_encrypt(byte[][] data, byte[][] key)
	{
		byte ans[][]=new byte[data.length][data[0].length];

		ans=aes_shift_row(data);
		ans=aes_xor(ans, key);

		return ans;
	}

	/**
	 * for each A[i,j] in data <- A[i,j] xor KEY[i,j]
	 *
	 * @param data in byte
	 * @param key  in hexadecimal
	 * @return data xor key
	 */
	private static byte[][] aes_xor(byte[][] data, byte[][] key)
	{

		for (int i=0; i<data.length; i++)
			for (int j=0; j<data.length; j++)
				data[i][j]=(byte) ((data[i][j]) ^ (key[i][j]));

		return data;
	}


	private static byte[][] aes_shift_row(byte[][] arr)
	{
		shift(arr[1], 1);
		shift(arr[2], 2);
		shift(arr[3], 3);
		return arr;
	}


	private static void shift(byte[] arr, int distance)
	{
		for (int j=0; j<distance; j++)
		{

			byte h=arr[0];
			for (int i=0; i<arr.length-1; i++)
			{
				arr[i]=arr[i+1];
			}
			arr[arr.length-1]=h;
		}
	}

	/**
	 * @param data un encrypt
	 * @param key1
	 * @param key2
	 * @param key3
	 * @return aes3(data, key1, key2, key3)  ==  aes1(aes1(aes1(data,key1),key2),key3)
	 */
	public static byte[][] aes3_encrypt(byte[][] data, byte[][] key1, byte[][] key2, byte[][] key3)
	{
		byte[][] aes3=new byte[data[0].length][data[0].length];

		aes3=aes1_encrypt(data, key1);      //1st  time aes
		aes3=aes1_encrypt(aes3, key2);     //2nd   time aes
		aes3=aes1_encrypt(aes3, key3);    //3rd    time aes


		return aes3;

	}


	/**
	 * Read the given binary file, and return its contents as a byte array.
	 */
	static byte[] readAlternateImpl(String inputFileName)
	{

		File file=new File(inputFileName);

		byte[] result=null;
		try
		{
			InputStream input=new BufferedInputStream(new FileInputStream(file));
			result=readAndClose(input);
		}
		catch (FileNotFoundException ex)
		{
			log(ex);
		}
		return result;
	}

	/**
	 * Read an input stream, and return it as a byte array.
	 * Sometimes the source of bytes is an input stream instead of a file.
	 * This implementation closes aInput after it's read.
	 */
	static byte[] readAndClose(InputStream input)
	{
		//carries the data from input to output :
		byte[] bucket=new byte[32*1024];
		ByteArrayOutputStream result=null;
		try
		{
			try
			{
				//Use buffering? No. Buffering avoids costly access to disk or network;
				//buffering to an in-memory stream makes no sense.
				result=new ByteArrayOutputStream(bucket.length);
				int bytesRead=0;
				while (bytesRead!=-1)
				{
					//aInput.read() returns -1, 0, or more :
					bytesRead=input.read(bucket);
					if (bytesRead>0)
					{
						result.write(bucket, 0, bytesRead);
					}
				}
			}
			finally
			{
				input.close();
				//result.close(); this is a no-operation for ByteArrayOutputStream
			}
		}
		catch (IOException ex)
		{
			log(ex);
		}
		return result.toByteArray();
	}

	private static void log(Object thing)
	{
		System.out.println(String.valueOf(thing));
	}


	private static byte[][] convkey(byte[] arr)
	{
		byte[][] ans=new byte[3][16];

		for (int i=0; i<16; i++)
		{
			ans[0][i]=arr[i];
			ans[1][i]=arr[i+16];
			ans[2][i]=arr[i+32];
		}

		return ans;
	}

	private static byte[][] convfile(byte[] arr)
	{
		byte[][] ans=new byte[4][4];
		int count=0;
		for (int i=0; i<4; i++)
		{
			for (int j=0; j<4; j++)
			{
				ans[j][i]=arr[count];
				count++;

			}
		}

		return ans;
	}

	private static byte[][][] convfile_long(byte[] arr)
	{
		byte[][][] ans=new byte[arr.length/16][4][4];

		int count=0;

		for (int k=0; k<arr.length/16; k++)
		{


			for (int i=0; i<4; i++)
			{
				for (int j=0; j<4; j++)
				{
					ans[k][j][i]=arr[count];
					count++;

				}
			}
		}

		return ans;
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public static byte[][][] aes3_key_spliter(byte[][] key)
	{

		byte[][][] keys=new byte[3][key[0].length][key[0].length];
		for (int i=0; i<key[0].length; i++)
		{
			keys[0][0][i]=0;
			keys[0][1][i]=0;


		}


		return keys;
	}

	public static byte[][][] aes3_decrypt_key(byte[][] data, byte[][] encrypt_data)
	{
		byte[][][] keys=new byte[3][data.length][data[0].length];


		return keys;
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{

		byte[] f=readAlternateImpl("/Users/royash/Documents/aes_3-star/AES3_test_files/message_short");
		byte[] k=readAlternateImpl("/Users/royash/Documents/aes_3-star/AES3_test_files/key_short");
		byte[] c=readAlternateImpl("/Users/royash/Documents/aes_3-star/AES3_test_files/cipher_short");
		byte[][] key123=convkey(k);
		byte[][] ans=convfile(f);


		for (int r=0; r<11; r++)
		{


			ans=aes3_encrypt(convfile(f), convfile(key123[0]), convfile(key123[1]), convfile(key123[2]));
			for (int j=0; j<4; j++)
			{

				for (int i=0; i<4; i++)
				{
					System.out.print(""+ans[i][j]+"||"+c[i+4*j+r*16]+"||");
				}
				System.out.println();
				System.out.println("------");
			}


			System.out.println();
			System.out.println("------");
		}

	}

}
