import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Aes
{
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
	 * Reads a file's data by bytes and saving it's byte in a matrices of 4x4 (a block of 16 bytes).
	 *
	 * @param file the file to be read.
	 * @return a 3 dimensional matrix of data like so: numberOfBlocks x 4 x 4.
	 * @throws IOException if any error related to reading or parsing the file happens.
	 */
	private static byte[][][] readFile(final File file) throws IOException
	{
		try (FileInputStream fileInputStream=new FileInputStream(file))
		{
			final int numOfBlocks=(int) file.length()/16; // for separating to blocks of 128 bits (128 bits=16 bytes)
			final byte[][][] data=new byte[numOfBlocks][4][4];
			for (int i=0; i<numOfBlocks; i++)
				for (int j=0; j<data.length; j++)
					if (fileInputStream.read(data[i][j])==-1)
						throw new IOException(
								"Not enough bytes for hole block of 16 bytes in file "+file.getName()+"! (num of bytes: "+file.length()+')');
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
	 * @param msg          The original message.
	 * @param isToTheRight If {@code true}, shifts to the right, else, shifts to the left.
	 */
	private static void shiftRows(final byte[][] msg, final boolean isToTheRight)
	{
		for (int i=0; i<msg.length; i++)
			rotate(msg[i], isToTheRight ? i : -i);
	}

	/**
	 * Encrypts a file with AES-3-star algorithm.
	 *
	 * @param keysFile   A key file, should contain 3 keys 16 bytes long.
	 * @param inputFile  An input file, an original message, it's size (in bytes) is a multiplication of 16.
	 * @param outputFile The result of the encryption, a cipher file.
	 * @throws IOException If there is an error concerning any of the files.
	 */
	private static void encrypt(final File keysFile, final File inputFile, final File outputFile) throws IOException
	{
		final byte[][][]
				keys=readFile(keysFile),
				input=readFile(inputFile);
		//		if (!outputFile.delete())
		//			throw new IOException("can't delete "+outputFile.getName());
		if (!outputFile.createNewFile())
			throw new IOException("Can't create new file with name: "+outputFile.getName());

	}

	/**
	 * Decrypts a file with AES-3-star algorithm.
	 *
	 * @param keysFile   A key file, should contain 3 keys 16 bytes long.
	 * @param inputFile  An input file, an original message, it's size (in bytes) is a multiplication of 16.
	 * @param outputFile The result of the encryption, a cipher file.
	 * @throws IOException If there is an error concerning any of the files.
	 */
	private static void decrypt(final File keysFile, final File inputFile, final File outputFile) throws IOException
	{
		final byte[][][]
				keys=readFile(keysFile),
				input=readFile(inputFile);
		//		if (!outputFile.delete())
		//			throw new IOException("can't delete "+outputFile.getName());
		if (!outputFile.createNewFile())
			throw new IOException("Can't create new file with name: "+outputFile.getName());

	}

	public static void main(final String[] args) throws IOException
	{
		//		if (args.length<6)
		//			throw new IllegalArgumentException("Not enough args!");

		final List<String> argsList=Arrays.asList(args);
		final File file1, file2, file3=new File(args[argsList.indexOf("-o")+1]);
		;
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

		//TODO break encryption

		if (!file1.exists())
			throw new FileNotFoundException("File "+file1.getName()+" doesn't exist!");
		if (!file2.exists())
			throw new FileNotFoundException("File "+file2.getName()+" doesn't exist!");
		switch (args[0])
		{
			case "-e":
				encrypt(file1, file2, file3);
				break;
			case "-d":
				decrypt(file1, file2, file3);
				break;
			case "-b":

		}
	}
}