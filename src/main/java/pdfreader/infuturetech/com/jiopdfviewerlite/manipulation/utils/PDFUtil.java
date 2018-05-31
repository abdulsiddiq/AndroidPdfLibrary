package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.utils;

import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.PDFResourceLoader;


/**
 * Created by Krypto on 21-02-2018.
 */

public class PDFUtil
{
    public static final int IO_BUFFER_SIZE = 8 * 1024;
    public static final String ID_SEPERATOR = ":";
    public static final String PATH_SEPERATOR = "/";

    public static void fileProcessor( int cipherMode, String key, byte[] inputBytes, File outputFile )
    {
        try
        {
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);
            outputStream.close();

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException e)
        {
            e.printStackTrace();
        }
    }




    public static void fileEncryption(FileInputStream inFile, File outputFile,String password) throws IOException
    {

        try
        {
            SecureRandom srandom = new SecureRandom();
            byte[] salt = new byte[8];
            srandom.nextBytes(salt);
            SecretKeyFactory factory = null;
            try
            {
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            }catch (NoSuchAlgorithmException exception)
            {
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            }
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), "AES");
            byte[] iv = new byte[128/8];
            srandom.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            FileOutputStream out = new FileOutputStream(outputFile);
            out.write(salt);
            out.write(iv);


            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);

            try (FileInputStream in = inFile) {
                processFile(ci, in, out);
            }
            out.close();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            inFile.close();
        }
    }

    static private void processFile( Cipher ci, InputStream in, OutputStream out)
            throws javax.crypto.IllegalBlockSizeException,
            javax.crypto.BadPaddingException,
            java.io.IOException
    {
        byte[] ibuf = new byte[1024];
        int len;
        byte[] obuf = null;
        while ((len = in.read(ibuf)) != -1) {
            obuf = ci.update(ibuf, 0, len);
            if ( obuf != null ) out.write(obuf);
        }
        obuf = ci.doFinal();
        if ( obuf != null ) out.write(obuf);

        obuf = null;
    }

    public static void fileDecryption(String password,File inputFile,FileOutputStream fos ) throws IOException
    {

        try
        {
            FileInputStream in = new FileInputStream(inputFile);
            byte[] salt = new byte[8], iv = new byte[128/8];
            in.read(salt);
            in.read(iv);

            SecretKeyFactory factory;
            try
            {
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            }catch (NoSuchAlgorithmException exception)
            {
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            }
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(iv));

            try (FileOutputStream out = fos){
                processFile(ci, in, out);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            fos.flush();
            fos.close();
        }
    }



    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    public static long getUsableSpace(File path) {
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }


    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    public static boolean isExternalStorageRemovable() {
        return true;
    }


    public static int getItemId(String combinedId)
    {
        return Integer.parseInt(combinedId.split(ID_SEPERATOR)[0]);
    }

    public static int getIndexFrom(String combinedId)
    {
        return Integer.parseInt(combinedId.split(ID_SEPERATOR)[1]);
    }

    public static String combineId(String itemId, int idToCombine)
    {
        return itemId+ID_SEPERATOR+idToCombine;
    }

    public static String getDownloadFolder(int itemId)
    {
        return PDFResourceLoader.getExternalFilesDir(null) +PATH_SEPERATOR+itemId;
    }

    public static String getFilePath(String combineId)
    {
        return getDownloadFolder(getItemId(combineId))+PATH_SEPERATOR+combineId+".pdf";
    }
}
