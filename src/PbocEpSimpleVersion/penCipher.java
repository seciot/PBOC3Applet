/**
 * Title:        cipher class
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      Gemplus Goldpac Co., Limited
 *
 * @author       Meng hongwen<alfredmeng@eastday.com>
 * @version 1.0
 */

package PbocEpSimpleVersion;

import javacard.framework.*;
import javacardx.crypto.*;
import javacard.security.*;

public  class penCipher
{
    public final static byte  ALG_DES      = (byte)0x1;
    public final static byte  ALG_3DES     = (byte)0x3;

    private byte[] tbuf1;
    private byte[] tbuf2; 

    //------------------------------------------------
    public penCipher()
    {
        desEngine = Cipher.getInstance(Cipher.ALG_DES_ECB_NOPAD, false);
        desKey = KeyBuilder.buildKey(KeyBuilder.TYPE_DES, KeyBuilder.LENGTH_DES, false);
        des3Key= KeyBuilder.buildKey(KeyBuilder.TYPE_DES, KeyBuilder.LENGTH_DES3_2KEY, false);
        tbuf1 = JCSystem.makeTransientByteArray((short)8,  JCSystem.CLEAR_ON_DESELECT);
        tbuf2 = JCSystem.makeTransientByteArray((short)8,  JCSystem.CLEAR_ON_DESELECT);
    }
    //------------------------------------------------
    public final void cdes(byte[] akey, short kOff, byte[] data, short dOff, short dLen, byte[] r, short rOff, byte mode)
    {
        ((DESKey)desKey).setKey( akey, kOff);

        desEngine.init(desKey, mode);
        desEngine.doFinal(data, dOff, dLen, r, rOff);
    }
    //------------------------------------------------
    public final void  tripledes(byte[] key3, byte[] data, short dOff, short dLen, byte[] r, short rOff, byte mode)
    {
       ((DESKey)des3Key).setKey(key3, (short)0 );

       desEngine.init(des3Key,mode);
       desEngine.doFinal(data, dOff, dLen, r, rOff);
    }
    //------------------------------------------------
    public final void xorblock8(byte[] d1,byte[] d2, short d2_off)
    {
       for(short i=(short)0; i<(short)8; i++ ) d1[i] ^= d2[(short)(d2_off+i)];
    }
    //------------------------------------------------
    public final void notblock8(byte[] d1 )
    {
       for(short i=(short)0; i<(short)8; i++ ) d1[i] = (byte)(~d1[i]);
    }
    //------------------------------------------------
    public final short  pbocpadding(byte[] data, short len)
    {
       short f;

       data[len] = (byte)0x80;
       len ++;

       f = (short)(len%8);
       f = (short)(8-f);

       if ( f != (short)8 )
          Util.arrayFillNonAtomic(data,len, f, (byte)0);
       else  f = (short)0;

       return (short)(len+f);
    }
    //------------------------------------------------
    public final void  gmac4(byte alg,byte[] key, byte[] data, short dl, byte[] mac,byte[] icv)
    {
       dl = pbocpadding(data,dl);

       Util.arrayCopyNonAtomic(icv,(short)0,tbuf1,(short)0,(short)8);

       for(short i=(short)0;i<dl;i+=(short)8) {
          xorblock8(tbuf1,data,i);
          cdes(key, (short)0, tbuf1, (short)0, (short)8,tbuf2, (short)0, Cipher.MODE_ENCRYPT);
          Util.arrayCopyNonAtomic(tbuf2, (short)0, tbuf1, (short)0,(short)8);
       }

       if ( alg == ALG_3DES ) {
         cdes(key, (short)8, tbuf1,(short)0, (short)8,tbuf2, (short)0, Cipher.MODE_DECRYPT);
         cdes(key, (short)0, tbuf2,  (short)0, (short)8,tbuf1,  (short)0, Cipher.MODE_ENCRYPT);
       }

       Util.arrayCopyNonAtomic(tbuf1, (short)0, mac, (short)0, (short)4);
    }
    //------------------------------------------------
    public final void  gmac4(byte alg,byte[] key, byte[] data, short dl, byte[] mac)
    {
       dl = pbocpadding(data,dl);

       Util.arrayFillNonAtomic(tbuf1,(short)0,(short)8,(byte)0);

       for(short i=(short)0;i<dl;i+=(short)8) {
          xorblock8(tbuf1,data,i);
          cdes(key, (short)0, tbuf1, (short)0, (short)8,tbuf2, (short)0, Cipher.MODE_ENCRYPT);
          Util.arrayCopyNonAtomic(tbuf2, (short)0, tbuf1, (short)0,(short)8);
       }

       if ( alg == ALG_3DES ) {
         cdes(key, (short)8, tbuf1,(short)0, (short)8,tbuf2, (short)0, Cipher.MODE_DECRYPT);
         cdes(key, (short)0, tbuf2,  (short)0, (short)8,tbuf1,  (short)0, Cipher.MODE_ENCRYPT);
       }

       Util.arrayCopyNonAtomic(tbuf1, (short)0, mac, (short)0, (short)4);
    }
    //------------------------------------------------
    public final void diversify(byte[] MxK, byte[] factor, byte[] DxK)
    {
       Util.arrayCopyNonAtomic(factor, (short)0, tbuf2, (short)0, (short)8);

       tripledes(MxK,tbuf2,(short)0,(short)8,tbuf1,(short)0, Cipher.MODE_ENCRYPT);
       Util.arrayCopy(tbuf1, (short)0, DxK, (short)0, (short)8);
       notblock8(tbuf2);
       tripledes(MxK,tbuf2,(short)0,(short)8,tbuf1,(short)0,Cipher.MODE_ENCRYPT);
       Util.arrayCopyNonAtomic(tbuf1, (short)0, DxK, (short)8, (short)8);
    }
    //------------------------------------------------
    public final short PBEncrypt(byte alg, byte[] key, byte[] data, short len, byte[] res)
    {
       Util.arrayCopyNonAtomic(data,(short)0, res, (short)1, len);
       res[0] = (byte)len;

       if (((short)(len+1)%(short)8) > (short)0 )
          len = pbocpadding(res,(short)(len+1));
       else len = (short)(len+1);

       if ( alg == ALG_3DES ) {
           tripledes(key, data, (short)0,len, res, (short)0, Cipher.MODE_ENCRYPT);
       } else {
           cdes(key, (short)0, data, (short)0, len, res,(short)0, Cipher.MODE_ENCRYPT);
       }

       return len;
    }
    //------------------------------------------------
    public final short PBDecrypt(byte alg, byte[] key,byte[] data, short doff, short len, byte[] res )
    {
       if ( alg == ALG_3DES )
             tripledes(key, data, doff,len, res, (short)0, Cipher.MODE_DECRYPT);
       else
             cdes(key,(short)0, data,doff,len, res,(short)0,Cipher.MODE_DECRYPT);

       len = res[0];
       Util.arrayCopyNonAtomic(res, (short)1, res, (short)0, len);
       return len;
    }
    //------------------------------------------------
    private Cipher desEngine;
    private Key    desKey, des3Key;
}
