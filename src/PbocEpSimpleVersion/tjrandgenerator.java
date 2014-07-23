/**
 * Title:        random generator for smartpen
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      Gemplus Goldpac Co., Limited
 *
 * @author       Meng Hongwen<alfredmeng@eastday.com>
 * @version 1.0
 */

package PbocEpSimpleVersion;

import javacard.framework.*;
import javacard.security.* ;

public final class tjrandgenerator {

   private boolean  bvaild;
   private byte    size;
   private byte[]   v;
   private RandomData rd;

   //------------------------------------------------
   public tjrandgenerator()
   {
       bvaild = false;
       size = (byte)4;
       v = JCSystem.makeTransientByteArray((short)32,   JCSystem.CLEAR_ON_DESELECT);
       rd = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
   }
   //------------------------------------------------
   public final void generateSecureRnd(byte len)
   {
      if(len>=(byte)4 || len <=(byte)64 ) size = len;
      rd.generateData(v,(short)0 ,(short)size);
      bvaild = true;
   }
   //------------------------------------------------
   public final byte getRndValue(byte[] bf)
   {
      if(bvaild) {
         Util.arrayCopyNonAtomic(v,(short)0, bf, (short)0, (short)size);
         return size;
      } else return (byte)0;
   }
   //------------------------------------------------
   public final byte getRndValue(byte[] bf,short boff)
   {
      if(bvaild) {
         Util.arrayCopyNonAtomic(v,(short)0, bf, boff, (short)size);
         return size;
      } else return (byte)0;
   }
   //------------------------------------------------
   public final void revokeRnd()
   {
      bvaild = false;
   }
   //------------------------------------------------
   public final boolean isvaild()
   {
      return bvaild;
   }
   //------------------------------------------------
   public final byte sizeOfRnd()
   {
      return size;
   }
   //------------------------------------------------
}