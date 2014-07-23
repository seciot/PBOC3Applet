/**
 * Title:        apdu
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      Gemplus Goldpac Co., Limited
 *
 * @author       Meng Hongwen<alfredmeng@eastday.com>
 * @version 1.0
 */

package PbocEpSimpleVersion;

import javacard.framework.*;

public class tjuapdu {
   public   byte    cla, ins, p1, p2;
   public   short   lc, le;
   public   byte[]  pdata;
   public   byte[]  ucTemp256;
   //------------------------------------------------
   public  tjuapdu() {

       pdata = JCSystem.makeTransientByteArray((short)512, JCSystem.CLEAR_ON_DESELECT);
       ucTemp256 = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_DESELECT);
   }

   //------------------------------------------------
   public boolean APDUContainData() {

      switch (ins) {
        case constdef.INS_VERIFY:
        case constdef.INS_CHANGE:
        case constdef.INS_UPDATE_BIN:
        case constdef.INS_UPDATE_REC:
        case constdef.INS_UNBLOCK:
        case constdef.INS_INITTRANS:
        case constdef.INS_PURCHASE:
        case constdef.INS_LOAD:
        case constdef.APP_BLOCK:
        case constdef.APP_UNBLOCK:
        case constdef.CARD_BLOCK:
        case constdef.GET_TRANS_PROOF:
             return true;

        case constdef.INS_READ_BIN:
        case constdef.INS_READ_REC:
        case constdef.INS_CREATEFS:
        case constdef.INS_GETBAL:
        case constdef.INS_FIRELOCK:

      }
      return false;
  }
}