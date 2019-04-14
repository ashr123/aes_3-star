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

	private static byte[][][] readFile(final File file) throws IOException
	{
		try (FileInputStream fileInputStream=new FileInputStream(file))
		{
			final int blockSize=(int) file.length()/16; // for separating to blocks of 128 bits (128 bits=16 bytes)
			final byte[][][] keys=new byte[blockSize][4][4];
			for (int i=0; i<blockSize; i++)
				for (int j=0; j<4; j++)
					if (fileInputStream.read(keys[i][j])==-1)
						throw new IOException("Not enough bits for 3 keys!!!");
			return keys;
		}
	}

	private static void addRoundKey(final byte[][] msg, final byte[][] key)
	{
		for (int i=0; i<4; i++)
			for (int j=0; j<4; j++)
				msg[i][j]=(byte) (msg[i][j] ^ key[i][j]);
	}

	private static void shiftRows(final byte[][] msg, final boolean isToTheRight)
	{
		for (int i=0; i<4; i++)
			rotate(msg[i], isToTheRight ? i : -i);
	}

	private static void encrypt(final File keysFile, final File inputFile, final File outputFile) throws IOException
	{
		final byte[][][]
				keys=readFile(keysFile),
				input=readFile(inputFile);
		//		if (!outputFile.delete())
		//			throw new IOException("can't delete "+outputFile.getName());
		if (!outputFile.createNewFile())
			throw new IOException("can't create new file with name: "+outputFile.getName());

	}

	private static void decrypt(final File keysFile, final File inputFile, final File outputFile) throws IOException
	{
		final byte[][][]
				keys=readFile(keysFile),
				input=readFile(inputFile);
		//		if (!outputFile.delete())
		//			throw new IOException("can't delete "+outputFile.getName());
		if (!outputFile.createNewFile())
			throw new IOException("can't create new file with name: "+outputFile.getName());

	}

	public static void main(final String[] args) throws IOException
	{
		//		if (args.length<6)
		//			throw new IllegalArgumentException("Not enough args!");

		final List<String> argsList=Arrays.asList(args);
		final File file1, file2, file3=new File(args[argsList.indexOf("-o")+1]);;
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