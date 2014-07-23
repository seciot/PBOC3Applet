/**
 * Title:        linearfix definition for smartPen
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      Gemplus Goldpac Co., Limited
 *
 * @author       Meng hongwen<alfredmeng@eastday.com>
 * @version 1.0
 */
package PbocEpSimpleVersion;

import javacard.framework.*;

public class tjelinearfix {
    private byte[] vector;
    private Object[] mArray;
    private short recNum, recLength;
    private boolean bCyclic;
    private short   usCursor;

    public byte acr,acw; 

    //------------------------------------------------
    public tjelinearfix(short n, short l, byte aacr, byte aacw) {

       recNum = n;
       recLength= l;
       mArray = new Object[recNum];
       acr = aacr;
       acw = aacw;
       bCyclic = false;
       usCursor = (short)0;
    }
    //------------------------------------------------
    public final short recordNumber() {
        return recNum;
    }
    //------------------------------------------------
    public final short recordLength() {
        return recLength;
    }
    //------------------------------------------------
    public final void cyclicMode()
    {
        bCyclic = true;
        usCursor = (short)0;
    }
    //------------------------------------------------
    public final boolean isCyclicMode()
    {
        return bCyclic;
    }
    //------------------------------------------------
    public final short readrec(byte rp, byte[] buff)
    {
        short recp = (short)(rp&0x0ff);
        byte[]  pdata;

        if (bCyclic) {
           if ( usCursor >= recp ) recp = (short)(usCursor-recp);
           else recp = (short)(recNum + usCursor - recp );
        } else  recp--;

        if (recp < (short)0 ) return (short)0;

        if(mArray[recp]==null) {
           Util.arrayFillNonAtomic(buff,(short)0, recLength, (byte)0xff);
        } else {
           pdata = (byte[])mArray[recp];
           Util.arrayCopyNonAtomic(pdata, (short)0, buff, (short)0, recLength);
        }
        return recLength;
    }
    //------------------------------------------------
    public final boolean writerec(byte rp, byte[] data)
    {
        short recp = (short)(rp&0x0ff);
        byte[]  pbuf;

        if (bCyclic) recp = usCursor;
        else  recp--;

        if(mArray[recp]==null) {
           pbuf = new byte[recLength];

           if(pbuf==null) return false;
           mArray[recp] = pbuf;

        } else pbuf = (byte[])mArray[recp];

        Util.arrayCopyNonAtomic(data, (short)0, pbuf, (short)0, recLength);

        if (bCyclic) {
           usCursor++;
           if ( usCursor >=recNum) usCursor = (short)0;
        }
        return true;
    }
    //------------------------------------------------
}