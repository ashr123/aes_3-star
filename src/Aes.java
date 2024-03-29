import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Aes
{
	/**
	 * Transposes a matrix of bytes.
	 *
	 * @param arr The bytes matrix.
	 */
	private static void transpose(final byte[][] arr)
	{
		for (int i=0; i<arr.length; i++)
			for (int j=i+1; j<arr[i].length; j++)
			{
				if (i==j)
					continue;
				final byte temp=arr[j][i];
				arr[j][i]=arr[i][j];
				arr[i][j]=temp;
			}
	}

	/**
	 * @param arr the array to be set
	 * @see java.util.ArrayList#set(int, Object)
	 */
	private static byte set(final byte[] arr, final int index, final byte element)
	{
		Objects.checkIndex(index, arr.length);
		final byte oldValue=arr[index];
		arr[index]=element;
		return oldValue;
	}

	/**
	 * @see java.util.Collections#rotate(List, int)
	 */
	private static void rotate(final byte[] arr, int distance)
	{
		if (arr.length==0)
			return;
		distance=distance%arr.length;
		if (distance<0)
			distance+=arr.length;
		if (distance==0)
			return;

		for (int cycleStart=0, nMoved=0; nMoved!=arr.length; cycleStart++)
		{
			byte displaced=arr[cycleStart];
			int i=cycleStart;
			do
			{
				i+=distance;
				if (i >= arr.length)
					i-=arr.length;
				displaced=set(arr, i, displaced);
				nMoved++;
			} while (i!=cycleStart);
		}
	}

	/**
	 * Reads a file's data by bytes and saving it's bytes in a matrices of 4x4 (a block of 16 bytes).
	 *
	 * @param file The file to be read.
	 * @return A 3 dimensional matrix of data like so: numberOfBlocks x 4 x 4.
	 * @throws IOException If any error related to reading or parsing the file happens.
	 */
	private static byte[][][] readFile(final File file) throws IOException
	{
		try (FileInputStream fileInputStream=new FileInputStream(file))
		{
			final byte[][][] data=new byte[(int) file.length()/16][4][4];// for separating to blocks of 128 bits (128 bits=16 bytes)
			for (final byte[][] block : data)
			{
				for (final byte[] line : block)
					if (fileInputStream.read(line)==-1)
						throw new IOException(
								"Not enough bytes for hole block of 16 bytes in file "+file.getName()+"! (num of bytes: "+file.length()+')');
				transpose(block); // from lines to columns
			}
			return data;
		}
	}

	/**
	 * Incorporates a key into the message.
	 *
	 * @param msg The original message.
	 * @param key The key to incorporate.
	 */
	private static void addRoundKey(final byte[][] msg, final byte[][] key)
	{
		for (int i=0; i<msg.length; i++)
			for (int j=0; j<msg[i].length; j++)
				msg[i][j]=(byte) (msg[i][j] ^ key[i][j]);
	}

	/**
	 * Shifts every line right or left by it's line number.
	 *
	 * @param msg         The original message.
	 * @param isToTheLeft If {@code true}, shifts to the left, else, shifts to the right.
	 */
	private static void shiftRows(final byte[][] msg, final boolean isToTheLeft)
	{
		for (int i=0; i<msg.length; i++)
			rotate(msg[i], isToTheLeft ? -i : i);
	}

	private static byte[] spliterator(final byte result)
	{
		for (byte i=Byte.MIN_VALUE; i<Byte.MAX_VALUE; i++)
			for (byte j=Byte.MIN_VALUE; j<Byte.MAX_VALUE; j++)
				for (byte k=Byte.MIN_VALUE; k<Byte.MAX_VALUE; k++)
					if ((i ^ j ^ k)==result)
						return new byte[]{i, j, k};
		return new byte[]{0, 0, 0};
	}

	/**
	 * Writes a matrix of bytes to file.
	 *
	 * @param outputFile The file to be written
	 * @param input      The matrix.
	 * @throws IOException If any problem occurs with the writing of with the file.
	 */
	private static void writeMatrixToFile(final File outputFile, final byte[][][] input) throws IOException
	{
		try (FileOutputStream fileOutputStream=new FileOutputStream(outputFile, true))
		{
			for (final byte[][] block : input)
			{
				transpose(block); // from columns to lines (for convenience)
				for (final byte[] line : block)
					fileOutputStream.write(line);
			}
		}
	}

	/**
	 * Encrypts a file with AES-3-star algorithm.
	 *
	 * @param keysFile  A key file, should contain 3 keys 16 bytes long.
	 * @param inputFile An input file, an original message, it's size (in bytes) is a multiplication of 16.
	 * @throws IOException If there is an error concerning any of the files.
	 */
	private static byte[][][] encryptOrDecrypt(final File keysFile, final File inputFile,
	                                           final boolean isEncrypting) throws IOException
	{
		final byte[][][]
				keys=readFile(keysFile),
				input=readFile(inputFile);
		for (final byte[][] block : input)
			if (isEncrypting)
				for (final byte[][] key : keys)
				{
					shiftRows(block, true);
					addRoundKey(block, key);
				}
			else
				for (int i=keys.length-1; i >= 0; i--)
				{
					addRoundKey(block, keys[i]);
					shiftRows(block, false);
				}
		return input;
	}

//	private static byte[][][] encryptOrDecrypt2(final byte[][][] keys, final File inputFile,
//	                                            final boolean isEncrypting) throws IOException
//	{
//		final byte[][][] input=readFile(inputFile);
//		for (final byte[][] block : input)
//			if (isEncrypting)
//				for (final byte[][] key : keys)
//				{
//					shiftRows(block, true);
//					addRoundKey(block, key);
//				}
//			else
//				for (int i=keys.length-1; i >= 0; i--)
//				{
//					addRoundKey(block, keys[i]);
//					shiftRows(block, false);
//				}
//		return input;
//	}

	public static void main(final String[] args) throws IOException
	{
		if (args.length<6)
			throw new IllegalArgumentException("Not enough args!");

		final List<String> argsList=Arrays.asList(args);
		final File file1, file2, outputFile=new File(args[argsList.indexOf("-o")+1]);
		if (args[0].equals("-e") || args[0].equals("-d"))
		{
			file1=new File(args[argsList.indexOf("-k")+1]);
			file2=new File(args[argsList.indexOf("-i")+1]);
		}
		else
		{
			file1=new File(args[argsList.indexOf("-m")+1]);
			file2=new File(args[argsList.indexOf("-c")+1]);
		}

		if (!file1.exists())
			throw new FileNotFoundException("File "+file1.getName()+" doesn't exist!");
		if (!file2.exists())
			throw new FileNotFoundException("File "+file2.getName()+" doesn't exist!");
		if (outputFile.exists())
			if (!outputFile.delete())
				throw new IOException("can't delete "+outputFile.getName());
		if (!outputFile.createNewFile())
			throw new IOException("Can't create new file with name: "+outputFile.getName());
		switch (args[0])
		{
			case "-e":
				writeMatrixToFile(outputFile, encryptOrDecrypt(file1, file2, true));
				break;
			case "-d":
				writeMatrixToFile(outputFile, encryptOrDecrypt(file1, file2, false));
				break;
			case "-b":
				writeMatrixToFile(outputFile, breakEncryption(file1, file2));
		}
//		byte[][][]
//				cipher=encryptOrDecrypt2(breakEncryption(file1, file2), file1, true),
//				trueCipher=readFile(file2);
	}

	private static byte[][][] aes3KeySpliter(final byte[][] key)
	{

		final byte[][][] keys=new byte[3][key.length][key[0].length];

		//FIRST ROW
		for (int j=0; j<key.length; j++)
		{
			final byte[] splt=spliterator(key[0][j]);
			keys[0][0][j]=splt[0];
			keys[1][0][j]=splt[1];
			keys[2][0][j]=splt[2];
		}


		//2 ROW

		byte[] splt=spliterator(key[1][0]);
		keys[0][1][2]=splt[0];
		keys[1][1][1]=splt[1];
		keys[2][1][0]=splt[2];

		splt=spliterator(key[1][1]);
		keys[0][1][3]=splt[0];
		keys[1][1][2]=splt[1];
		keys[2][1][1]=splt[2];

		splt=spliterator(key[1][2]);
		keys[0][1][0]=splt[0];
		keys[1][1][3]=splt[1];
		keys[2][1][2]=splt[2];

		splt=spliterator(key[1][3]);
		keys[0][1][1]=splt[0];
		keys[1][1][0]=splt[1];
		keys[2][1][3]=splt[2];


		//3ROW

		splt=spliterator(key[2][0]);
		keys[0][2][0]=splt[0];
		keys[1][2][2]=splt[1];
		keys[2][2][0]=splt[2];

		splt=spliterator(key[2][1]);
		keys[0][2][1]=splt[0];
		keys[1][2][3]=splt[1];
		keys[2][2][1]=splt[2];

		splt=spliterator(key[2][2]);
		keys[0][2][2]=splt[0];
		keys[1][2][0]=splt[1];
		keys[2][2][2]=splt[2];

		splt=spliterator(key[2][3]);
		keys[0][2][3]=splt[0];
		keys[1][2][1]=splt[1];
		keys[2][2][3]=splt[2];


		//4ROW

		splt=spliterator(key[3][0]);
		keys[0][3][2]=splt[0];
		keys[1][3][3]=splt[1];
		keys[2][3][0]=splt[2];

		splt=spliterator(key[3][1]);
		keys[0][3][3]=splt[0];
		keys[1][3][0]=splt[1];
		keys[2][3][1]=splt[2];

		splt=spliterator(key[3][2]);
		keys[0][3][0]=splt[0];
		keys[1][3][1]=splt[1];
		keys[2][3][2]=splt[2];

		splt=spliterator(key[3][3]);
		keys[0][3][1]=splt[0];
		keys[1][3][2]=splt[1];
		keys[2][3][3]=splt[2];


		return keys;
	}

	private static byte[][][] breakEncryption(File dataFile, File cipherFile) throws IOException
	{
		byte[][] data=readFile(dataFile)[0],
				cipher=readFile(cipherFile)[0];
		for (int i=0; i<3; i++)
			shiftRows(data, true);
		addRoundKey(data, cipher);
		return aes3KeySpliter(data);
	}

//	public static void main1(String[] args) throws IOException
//	{
//
//		final File
//				msg_short=new File("/Users/royash/Documents/aes_3-star/AES3_test_files/message_short"),
//				cipher=new File("/Users/royash/Documents/aes_3-star/AES3_test_files/cipher_short");
//		byte[][] c=readFile(cipher)[0];
//
//
//		byte[][][] x=breakEncryption(msg_short, cipher);
//
//
//		byte[][] ans=encryptOrDecrypt2(x, msg_short, true)[0];
//		for (int j=0; j<4; j++)
//		{
//
//			for (int i=0; i<4; i++)
//			{
//				if (ans[i][j]!=c[i][j])
//					System.out.println(""+ans[i][j]+"||"+c[i][j]+"||");
//			}
//			System.out.println();
//			System.out.println("------");
//		}
//
//
//	}
}