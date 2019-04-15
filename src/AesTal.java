public class Aes {

    /**
     * make aes1 encrypt
     *
     * @param data to encrypt
     * @param key  aes1 func
     * @return encrypted data
     */
    public static byte[][] aes1_encrypt(byte[][] data, byte[][] key) {
        byte ans[][] = new byte[data.length][data[0].length];

        ans = aes_shift_row(data);
        ans = aes_xor(ans, key);

        return ans;
    }

    /**
     * for each A[i,j] in data <- A[i,j] xor KEY[i,j]
     *
     * @param data in byte
     * @param key  in hexadecimal
     * @return data xor key
     */
    private static byte[][] aes_xor(byte[][] data, byte[][] key) {

        for (int i = 0; i < data.length; i++)
            for (int j = 0; j < data.length; j++)
                data[i][j] = (byte) ((data[i][j]) ^ (key[i][j]));

        return data;
    }

    /**
     * @param data for aes shifting
     * @return data | for each row <- row SHL
     */
    private static byte[][] aes_shift_row(byte[][] data) {
        return null;
    }

    /**
     * @param data un encrypt
     * @param key1
     * @param key2
     * @param key3
     * @return aes3(data, key1, key2, key3)  ==  aes1(aes1(aes1(data,key1),key2),key3)
     */
    public static byte[][] aes3_encrypt(byte[][] data, byte[][] key1, byte[][] key2, byte[][] key3) {
        byte[][] aes3 = new byte[data.length][data.length];

        aes3 = aes1_encrypt(data, key1);      //1st  time aes
        aes3 = aes1_encrypt(aes3, key2);     //2nd   time aes
        aes3 = aes1_encrypt(aes3, key3);    //3rd    time aes


        return aes3;

    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static byte[][][] aes3_key_spliter(byte[][] key) {

        byte[][][] keys = new byte[3][key.length][key[0].length];


        return keys;
    }

    public static byte[][][] aes3_decrypt_key(byte[][] data, byte[][] encrypt_data) {
        byte[][][] keys = new byte[3][data.length][data[0].length];


        return keys;
    }


    public static void main(String[] args) {


    }

}
