/**
 * Title:        internal routines
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      Gemplus Goldpac Co., Limited
 *
 * @author       Meng Hongwen<alfredmeng@eastday.com>
 * @version 1.0
 */


package PbocEpSimpleVersion;

import javacard.framework.*;
import javacardx.crypto.*;

public class tjpbocIDU extends tjefbinary {
    private tjrandgenerator r;
    private penCipher       c;
    private tjelinearfix    k,l;
    private byte[] pStateMachine;
    private byte[] pTemp41;
    private byte[] pTemp42;   
    private byte[] pTemp81; 
    private byte[] pTemp82;
    private byte[] pTemp16;
    private byte[] pTemp32;
    private boolean  gtp_ready;
    private byte     gtp_tt;
    private short    gtp_ntonoff;
    private byte[]   gtp_tac = new byte[8];
    
    public tjpbocIDU(tjelinearfix pkey, tjelinearfix pl) {
       super(new byte[32],(short)32,(byte)0xFF,(byte)0xff);   
       pStateMachine = JCSystem.makeTransientByteArray((short)2, JCSystem.CLEAR_ON_DESELECT);
       pTemp41 = JCSystem.makeTransientByteArray((short)4, JCSystem.CLEAR_ON_DESELECT);
       pTemp42 = JCSystem.makeTransientByteArray((short)4, JCSystem.CLEAR_ON_DESELECT);
       pTemp81 = JCSystem.makeTransientByteArray((short)8, JCSystem.CLEAR_ON_DESELECT);
       pTemp82 = JCSystem.makeTransientByteArray((short)8, JCSystem.CLEAR_ON_DESELECT);
       pTemp16 = JCSystem.makeTransientByteArray((short)32, JCSystem.CLEAR_ON_DESELECT);
       pTemp32 = JCSystem.makeTransientByteArray((short)32, JCSystem.CLEAR_ON_DESELECT);
       r = new tjrandgenerator();
       c = new penCipher();
       k = pkey;
       l = pl;
       gtp_ready =false;
    }
    //------------------------------------------------
    public final void reset()
    {
        pStateMachine[0] = (byte)0;
        pStateMachine[1] = (byte)0;
    }
    //------------------------------------------------
    public void getrnd(byte[] rc) {
        r.generateSecureRnd((byte)4);
        r.getRndValue(rc);
    }
    //------------------------------------------------
    public final void updateEDC()
    {
        short lr,l2;
        lr = Util.makeShort(vector[1],vector[2]);
        l2 = Util.makeShort(vector[3],vector[4]);
        lr = (short)(lr^l2);
        l2 = Util.makeShort(vector[5],vector[6]);
        lr = (short)(lr^l2);
        l2 = Util.makeShort(vector[7],vector[8]);
        lr = (short)(lr^l2);
        Util.setShort(vector,(short)9,lr);
    }
    //------------------------------------------------
    public final boolean checkEDC()
    {
        short lr,l2,usTobeChecked;
        lr = Util.makeShort(vector[1],vector[2]);
        l2 = Util.makeShort(vector[3],vector[4]);
        lr = (short)(lr^l2);
        l2 = Util.makeShort(vector[5],vector[6]);
        lr = (short)(lr^l2);
        l2 = Util.makeShort(vector[7],vector[8]);
        lr = (short)(lr^l2);
        usTobeChecked = Util.makeShort(vector[9],vector[10]);
        if(lr==usTobeChecked) return true;
        return false;
    }
    //------------------------------------------------
    public final void getBalanceEP(byte[] pResult4)
    {
        Util.arrayCopyNonAtomic(vector,(short)1,pResult4,(short)0,(short)4);
    }
    //------------------------------------------------
    private final short increase(byte[] saDest,byte a)
    {
        short  i,t1,t2,ads;

        ads = (short)0;
        for(i=3; i>=0; i--) {
          t1 = (short)(vector[(short)(1+i)]&0x0ff);
          t2 = (short)(saDest[i]&0x0ff);

          t1 = (short)(t1 + t2 + ads);
          if ( a >(byte)0 )
             vector[(short)(1+i)] = (byte)(t1 % 256);
          ads = (short)(t1 / 256);
        }
        return ads;
    }
    //------------------------------------------------
    private final short comparedata(byte[] saDest, byte d)
    {
        short  i,t1,t2,pos,ads;

        ads = 0;
        for(i=3; i>=0; i--) {
           t1 = (short)(vector[(short)(1+i)]&0x0ff);
           t2 = (short)(saDest[i]&0x0ff);

           if ( ads > (short)0 ) {
              if ( t1 > (short)0 ) {
                 t1--;
                 ads = (short)0;
              } else {
                 t1 = (short)255;
                 ads = (short)1;
              }
           }

           if ( t1 >= t2 ) {
              t1=(short)(t1-t2);
           } else {
              t1 = (short)(t1 + 256 - t2);
              ads = (short)1;
           }
           if (d > (byte)0x0)
             vector[(short)(1+i)] = (byte)t1;
       }
       return ads;
    }
    //------------------------------------------------
    public final boolean checkStateMachine(byte cs)
    {
        if(pStateMachine[cs] == (byte)0x1 ) return true;
        return false;
    }
    //------------------------------------------------
    private final short loadKey(byte kp, byte ik, byte[] kv )
    {
        byte i,n;
        boolean bf=false;

        n = (byte)k.recordNumber();
        for(i=(byte)0x1; i<=n; i++ ){
           k.readrec(i,kv);
           if ( kv[0] == kp ) {
               bf = true;
               if (kv[1]==ik)
                  return (short)0;
           }
        }
        if (bf) return (short)3;

        return (short)2;
    }
    //------------------------------------------------
    public final short init4purchase(byte[] data )
    {
        short rc;

        Util.arrayCopyNonAtomic(data,(short)1,pTemp42,(short)0,(short)4); // amount
        Util.arrayCopyNonAtomic(data,(short)5,pTemp81, (short)0,(short)6); // tid

        rc = loadKey((byte)0x01,data[0],pTemp32);
        if ( rc!=(short)0 ) return rc;
        Util.arrayCopyNonAtomic(pTemp32,(short)2,pTemp16,(short)0,(short)16); // key

        rc = loadKey((byte)0x03,data[0],pTemp32);
        if ( rc!=(short)0 ) return rc;
        Util.arrayCopyNonAtomic(pTemp32,(short)2,pTemp16,(short)16,(short)16); // key

        rc = comparedata(pTemp42, (byte)0x0);
        if ( rc!=(short)0 ) return rc;
        r.generateSecureRnd((byte)4);

        // bal4  nt_off2  limit_overdraw3 vk1 algo1 rnd4
        Util.arrayCopyNonAtomic(vector,(short)1,data,(short)0,(short)4);
        Util.arrayCopyNonAtomic(vector,(short)7,data,(short)4,(short)2);
        Util.arrayFillNonAtomic(data,(short)6,(short)3,(byte)0x0);
        Util.arrayCopyNonAtomic(vector,(short)11,data,(short)9,(short)2);

        r.getRndValue(data,(short)11);

        pStateMachine[0] = (byte)0x1;
        pStateMachine[1] = (byte)0x0;

        gtp_tac[0] = (byte)0x0;
        gtp_tac[1] = (byte)0x0;
        gtp_tac[2] = (byte)0x0;
        gtp_tac[3] = (byte)0x0;
        gtp_tt = (byte)0x6;
        gtp_ready = false;
        return (short)0;
    }
    //------------------------------------------------
    public final short debit4purchase(byte[] data )
    {
        short rc;
        // NT_TERM_OFF4  DATE4 Time3

        r.getRndValue(pTemp32);
        Util.arrayCopyNonAtomic(vector,(short)7,pTemp32,(short)4,(short)2);
        Util.arrayCopyNonAtomic(data,(short)2, pTemp32, (short)6,(short)2);
        c.tripledes(pTemp16,pTemp32,(short)0,(short)8,pTemp82,(short)0,Cipher.MODE_ENCRYPT);
        Util.arrayCopyNonAtomic(pTemp42,(short)0,pTemp32,(short)0,(short)4);
        pTemp32[4] = (byte)0x06;
        Util.arrayCopyNonAtomic(pTemp81,(short)0,pTemp32,(short)5,(short)6);
        Util.arrayCopyNonAtomic(data,(short)4,pTemp32,(short)11,(short)7);
        c.gmac4(penCipher.ALG_DES,pTemp82,pTemp32,(short)0x012,pTemp41);
        
        if ( Util.arrayCompare(pTemp41,(short)0,data,(short)0xb,(short)4)!=(byte)0)
            return (short)2;

        // decrease amount
        rc = comparedata(pTemp42, (byte)0x1);
        if ( rc!=(short)0 ) return (short)3;

        // log it
        pTemp32[0] = vector[7];
        pTemp32[1] = vector[8];
        pTemp32[2] = (byte)0x0;
        pTemp32[3] = (byte)0x0;
        pTemp32[4] = (byte)0x0;
        pTemp32[5] = pTemp42[0];
        pTemp32[6] = pTemp42[1];
        pTemp32[7] = pTemp42[2];
        pTemp32[8] = pTemp42[3];
        pTemp32[9] = (byte)0x6;
        pTemp32[10] = pTemp81[0];
        pTemp32[11] = pTemp81[1];
        pTemp32[12] = pTemp81[2];
        pTemp32[13] = pTemp81[3];
        pTemp32[14] = pTemp81[4];
        pTemp32[15] = pTemp81[5];
        Util.arrayCopyNonAtomic(data,(short)4,pTemp32,(short)16,(short)7);

        // write to log
        if (!l.writerec((byte)0x1,pTemp32) )
           return (short)8;

        // increase counter

        rc = Util.makeShort(vector[7],vector[8]);
        gtp_ntonoff =rc;
        rc++;
        if ( rc > (short)256 ) rc = (short)1;
        Util.setShort(vector,(short)7,rc);

        Util.arrayCopyNonAtomic(pTemp42,(short)0,pTemp32,(short)0,(short)4);
        c.gmac4(penCipher.ALG_DES,pTemp82,pTemp32,(short)0x4,pTemp41);
        Util.arrayCopyNonAtomic(pTemp42,(short)0,pTemp32,(short)0,(short)4);
        pTemp32[4] = (byte)0x06;
        Util.arrayCopyNonAtomic(pTemp81,(short)0,pTemp32,(short)5, (short)6);
        Util.arrayCopyNonAtomic(data,(short)0,   pTemp32,(short)11,(short)4);
        Util.arrayCopyNonAtomic(data,(short)4,pTemp32,(short)15,(short)4);
        Util.arrayCopyNonAtomic(data,(short)8,pTemp32,(short)19,(short)3);
        Util.arrayCopyNonAtomic(pTemp16,(short)16,pTemp82,(short)0,(short)8);
        c.xorblock8(pTemp82,pTemp16,(short)24);
        c.gmac4(penCipher.ALG_DES,pTemp82,pTemp32,(short)0x16,data);
        Util.arrayCopyNonAtomic(pTemp41,(short)0,data,(short)4,(short)4);
        pStateMachine[0] = (byte)0x0;

        Util.arrayCopyNonAtomic(data,(short)0,gtp_tac,(short)4,(short)4);
        gtp_ready =true;
        
        return (short)0;
    }
    //------------------------------------------------
    public final short init4load(byte[] data )
    {
        short rc;
        // IK   AMT4   TID6
        Util.arrayCopyNonAtomic(data,(short)1,pTemp42,(short)0,(short)4); // amount
        Util.arrayCopyNonAtomic(data,(short)5,pTemp81, (short)0,(short)6); // tid

        rc = loadKey((byte)0x02,data[0],pTemp32);
        if ( rc!=(short)0 ) return rc;
        Util.arrayCopyNonAtomic(pTemp32,(short)2,pTemp16,(short)0,(short)16); // key

        rc = loadKey((byte)0x03,data[0],pTemp32);
        if ( rc!=(short)0 ) return rc;
        Util.arrayCopyNonAtomic(pTemp32,(short)2,pTemp16,(short)16,(short)16); // key

        // check overflow
        rc = increase(pTemp42, (byte)0x0);
        if ( rc!=(short)0 ) return (short)2;

        r.generateSecureRnd((byte)4);
        r.getRndValue(pTemp32);
        pTemp32[4] = vector[5];
        pTemp32[5] = vector[6];
        pTemp32[6] = (byte)0x80;
        pTemp32[7] = (byte)0x0;
        c.tripledes(pTemp16,pTemp32,(short)0,(short)8,pTemp82,(short)0,Cipher.MODE_ENCRYPT); // session key

        // gen MAC1
        Util.arrayCopyNonAtomic(vector,(short)1,pTemp32,(short)0,(short)4);
        Util.arrayCopyNonAtomic(data,  (short)1,pTemp32,(short)4,(short)4);
        pTemp32[8] = (byte)0x02;
        Util.arrayCopyNonAtomic(data,  (short)5,pTemp32,(short)9,(short)6);
        c.gmac4(penCipher.ALG_DES,pTemp82,pTemp32,(short)0xF,pTemp41);
        // BAL4  nt_xx_on2  VK ALG RND_ICC4 Mac14
        Util.arrayCopyNonAtomic(vector,(short)1, data,(short)0,(short)4);
        Util.arrayCopyNonAtomic(vector,(short)5, data,(short)4,(short)2);
        Util.arrayCopyNonAtomic(vector,(short)15,data,(short)6,(short)2);
        r.getRndValue(pTemp32);
        data[8] =  pTemp32[0];
        data[9] =  pTemp32[1];
        data[10]=  pTemp32[2];
        data[11]=  pTemp32[3];
        Util.arrayCopyNonAtomic(pTemp41,(short)0,data,(short)12,(short)4);
        
        pStateMachine[0] = (byte)0x0;
        pStateMachine[1] = (byte)0x1;

        gtp_tac[0] = (byte)0x0;
        gtp_tac[1] = (byte)0x0;
        gtp_tac[2] = (byte)0x0;
        gtp_tac[3] = (byte)0x0;
        gtp_tt = (byte)0x2;
        gtp_ready = false;
        return (short)0;
    }
    //------------------------------------------------
    public final short credit4load(byte[] data )
    {
        short rc;

        // DATE4 TIME3  MAC24
        Util.arrayCopyNonAtomic(pTemp42,(short)0,pTemp32,(short)0,(short)4); // amount
        pTemp32[4] = (byte)0x02;
        Util.arrayCopyNonAtomic(pTemp81,(short)0,pTemp32,(short)5,(short)6); // tid
        Util.arrayCopyNonAtomic(data, (short)0, pTemp32, (short)11,(short)7); // datetime
        c.gmac4(penCipher.ALG_DES,pTemp82,pTemp32,(short)0x12,pTemp41);
        if(Util.arrayCompare(data,(short)7,pTemp41,(short)0,(short)4)!=(byte)0)
           return (short)1;

        // increase amount
        rc = increase(pTemp42, (byte)0x1);
        if ( rc!=(short)0 ) return (short)8;

        // increase counter
        // SAVE Counter to pTemp41;
        pTemp41[0] = vector[5];
        pTemp41[1] = vector[6]; 

        rc = Util.makeShort(vector[5],vector[6]);
        gtp_ntonoff =rc; // save for GTP
        rc++;
        if ( rc > (short)256 ) rc = (short)1;
        Util.setShort(vector,(short)5,rc);

       // log
        pTemp32[0] = pTemp41[0];
        pTemp32[1] = pTemp41[1];

        pTemp32[2] = (byte)0x0;
        pTemp32[3] = (byte)0x0;
        pTemp32[4] = (byte)0x0;

        pTemp32[5] = pTemp42[0];
        pTemp32[6] = pTemp42[1];
        pTemp32[7] = pTemp42[2];
        pTemp32[8] = pTemp42[3];

        pTemp32[9] = (byte)0x2;
        pTemp32[10] = pTemp81[0];
        pTemp32[11] = pTemp81[1];
        pTemp32[12] = pTemp81[2];
        pTemp32[13] = pTemp81[3];
        pTemp32[14] = pTemp81[4];
        pTemp32[15] = pTemp81[5];
        Util.arrayCopyNonAtomic(data,(short)0,pTemp32,(short)16,(short)7);

        // write to log
        if (!l.writerec((byte)0x1,pTemp32) )
           return (short)8;

        //TAC
        Util.arrayCopyNonAtomic(vector,(short)1, pTemp32,(short)0,(short)4);
        pTemp32[4] = pTemp41[0];
        pTemp32[5] = pTemp41[1];
        Util.arrayCopyNonAtomic(pTemp42,(short)0,pTemp32,(short)6,(short)4);
        pTemp32[10] = (byte)0x2;
        Util.arrayCopyNonAtomic(pTemp81,(short)0,pTemp32,(short)11,(short)6);
        Util.arrayCopyNonAtomic(data,(short)0, pTemp32,(short)17,(short)7);
// TAC KEY
        Util.arrayCopyNonAtomic(pTemp16,(short)16,pTemp82,(short)0,(short)8);
        c.xorblock8(pTemp82,pTemp16,(short)24);
        c.gmac4(penCipher.ALG_DES,pTemp82,pTemp32,(short)0x18,data);
        pStateMachine[1] = (byte)0x0;
        
        Util.arrayCopyNonAtomic(data,(short)0,gtp_tac,(short)4,(short)4);
        gtp_ready =true;
        return (short)0;
    }
    //------------------------------------------------
    public final boolean can_gtp()
    {
        return gtp_ready; 
    }
    //------------------------------------------------
    public final boolean match_tn(byte tt, short us)
    {
    	if((gtp_tt ==tt)&&(gtp_ntonoff==us)) return true;
    	return false;
    }
    //------------------------------------------------
    public final void copy_tac(byte[] data)
    {
    	Util.arrayCopyNonAtomic(gtp_tac,(short)0,data,(short)0,(short)8);
    	gtp_ready =false;
    }
    //------------------------------------------------
    public final short unwrap_apdu(byte kid, tjuapdu apdu,boolean bRPK)
    {
        short rc;

        rc = loadKey(kid,(byte)0x01,pTemp32); // in PBOC Spec. the keyindex is not specified
        if ( rc!=(short)0 ) return (short)0x01;
        
        Util.arrayFillNonAtomic(pTemp81,(short)0,(short)8,(byte)0x0);

        if(bRPK) { 
            Util.arrayCopyNonAtomic(pTemp32,(short)2, pTemp16,(short)0,(short)8); 
            Util.arrayCopyNonAtomic(pTemp32,(short)10,pTemp82,(short)0,(short)8);
            c.xorblock8(pTemp16,pTemp82,(short)0);    // session key
        } else { 
           Util.arrayCopyNonAtomic(pTemp32,(short)2,pTemp16,(short)0,(short)16); // key
           if (!r.isvaild()) return (short)0x01;
           r.getRndValue(pTemp81);
        }
        
        apdu.ucTemp256[0] = apdu.cla;
        apdu.ucTemp256[1] = apdu.ins;
        apdu.ucTemp256[2] = apdu.p1;
        apdu.ucTemp256[3] = apdu.p2;
        apdu.ucTemp256[4] = (byte)apdu.lc;
        Util.arrayCopyNonAtomic(apdu.pdata,(short)0,apdu.ucTemp256,(short)5,apdu.lc);
        if(bRPK) 
        	c.gmac4(penCipher.ALG_DES,pTemp16,apdu.ucTemp256,(short)(apdu.lc+1),pTemp82,pTemp81);	
        else 
            c.gmac4(penCipher.ALG_3DES,pTemp16,apdu.ucTemp256,(short)(apdu.lc+1),pTemp82,pTemp81);

        rc = (short)(apdu.lc-4);
        if(Util.arrayCompare(apdu.pdata,rc,pTemp82,(short)0,(short)4)!=(byte)0) 
        	return (short)0x02;

        if(!bRPK){
           if(rc>(short)0) { 
              c.tripledes(pTemp16,apdu.pdata,(short)0,rc,apdu.ucTemp256,(short)0,Cipher.MODE_DECRYPT);
              apdu.lc = (short)(apdu.ucTemp256[0]&0x0ff);
              Util.arrayCopyNonAtomic(apdu.ucTemp256,(short)1,apdu.pdata,(short)0,apdu.lc);
           }
        }
        
    	return (byte)0x0;
    }
    //------------------------------------------------
    //------------------------------------------------
}
