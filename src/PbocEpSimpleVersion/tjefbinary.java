/**
 * Title:        Transparent file for smartPen
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      Gemplus Goldpac Co., Limited
 *
 * @author       Meng hongwen<alfredmeng@eastday.com>
 * @version 1.0
 */

package PbocEpSimpleVersion;

import javacard.framework.*;

public class tjefbinary {
    protected byte[] vector;

    public short fsize;
    public byte acr,acw;
    //------------------------------------------------
    public tjefbinary(byte[] ptr, short siz, byte ac_r, byte ac_w )
    {
       vector = ptr;
       fsize = siz;
       acr = ac_r;
       acw = ac_w;
    }
    //------------------------------------------------
    public final short readbin(short off, short len, byte[] buff)
    {
       Util.arrayCopyNonAtomic(vector, off, buff, (short)0, len);
       return len;
    }
    //------------------------------------------------
    public final void writebin(short off, short dl, byte[] data)
    {
       Util.arrayCopyNonAtomic(data, (short)0, vector, off, dl);
    }
    //------------------------------------------------
}