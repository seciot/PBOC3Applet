/**
 * 
 *------------------------------------------------------------------------------
 *  @Project name    :  PBOC 2.0 EP Simple version
 *                      - Java Card applet -
 *
 *  @Platform        :  Java virtual machine
 *  @Language        :  1.3.0-C
 *  @Devltool        :  Borland (c) JBuilder 4.0
 *
 *  @Originalauthor  : Menghongwen@gmail.com 
 *  @Date            : Tue Apr 04 10:44:36 CST 2006
 *------------------------------------------------------------------------------
 */

package PbocEpSimpleVersion;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class CupPBOCEP extends javacard.framework.Applet
{

	// APDU object
	private tjuapdu  apduin;
    // filesystem ready flag
    private boolean bFSReady;
    // personalized flag
    private boolean bPersoed;
    // Blocked flag
    private boolean bCardBlocked;
    private boolean bAppBlocked;
    private boolean bAppBLKALWS;
    
    // owner PIN
    private tjPIN  pinOwner,pinUnBlock;

    // FILE System
    private tjefbinary ef15, ef16,ef17;
    private tjelinearfix ef18,ef19;

    //------------------------------------------------
    protected CupPBOCEP(byte[] buffer, short offset, byte length)
    {
        register();

        bPersoed = false;
        apduin = new tjuapdu();

        pinOwner = new tPIN();
        pinUnBlock = new tPIN();
        bFSReady = false;
        bAppBlocked = false;
        bCardBlocked = false;
        bAppBLKALWS = false;
    }
    //------------------------------------------------
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException
    {
        new CupPBOCEP (bArray, bOffset, (byte)bLength );
    }
    //------------------------------------------------
    public boolean select()
    {
        pinOwner.reset();
        if (ef17!=null) ((tjpbocIDU)ef17).reset();
        return true;
    }
    //------------------------------------------------
    public void deselect()
    {
        return;
    }
    //------------------------------------------------
    public void process(APDU apdu) throws ISOException
    {
        byte[] apduBuffer;
        short  dl;
        boolean  rc=false;
        short bytesRead;
        short echoOffset;

        apduBuffer = apdu.getBuffer();
        
        if (selectingApplet())  { // return FCI
            apduBuffer[0] = (byte)0x6F;
            apduBuffer[1] = (byte)0x22;
            apduBuffer[2] = (byte)0x84;
            apduBuffer[3] = (byte)0x10;
            apduBuffer[4] = (byte)0xd1;
            apduBuffer[5] = (byte)0x56;
            apduBuffer[6] = (byte)0x00;
            apduBuffer[7] = (byte)0x00;
            apduBuffer[8] = (byte)0x01;
            apduBuffer[9] = (byte)0x45;
            apduBuffer[10] = (byte)0x44;
            apduBuffer[11] = (byte)0x2f;
            apduBuffer[12] = (byte)0x45;
            apduBuffer[13] = (byte)0x50;
            apduBuffer[14] = (byte)0x00;
            apduBuffer[15] = (byte)0x00;
            apduBuffer[16] = (byte)0x00;
            apduBuffer[17] = (byte)0x00;
            apduBuffer[18] = (byte)0x00;
            apduBuffer[19] = (byte)0x00;
            apduBuffer[20] = (byte)0xA5;
            apduBuffer[21] = (byte)0x0f;
            apduBuffer[22] = (byte)0x50;
            apduBuffer[23] = (byte)0x08;
            apduBuffer[24] = (byte)'M';
            apduBuffer[25] = (byte)'o';
            apduBuffer[26] = (byte)'b';
            apduBuffer[27] = (byte)'i';
            apduBuffer[28] = (byte)'l';
            apduBuffer[29] = (byte)'e';
            apduBuffer[30] = (byte)'E';
            apduBuffer[31] = (byte)'P';
            apduBuffer[32] = (byte)0x9F;
            apduBuffer[33] = (byte)0x08;
            apduBuffer[34] = (byte)0x01;
            apduBuffer[35] = (byte)0x02;

            apdu.setOutgoingAndSend((short)0, (short)36);
            if(bAppBlocked||bCardBlocked||bAppBLKALWS) ISOException.throwIt(constdef.SW_E_APPBLK);
            return;
        }

        if(bCardBlocked||bAppBLKALWS) ISOException.throwIt((short)0x9303);
        
        apduin.cla = (byte)apduBuffer[ISO7816.OFFSET_CLA];
        apduin.ins = (byte)apduBuffer[ISO7816.OFFSET_INS];
        apduin.p1 = (byte)apduBuffer[ISO7816.OFFSET_P1];
        apduin.p2 = (byte)apduBuffer[ISO7816.OFFSET_P2];
        apduin.lc = (short)(apduBuffer[ISO7816.OFFSET_LC]& 0x0FF);
        
        if( apduin.APDUContainData()) {

           bytesRead = apdu.setIncomingAndReceive();
           echoOffset = (short)0;

           while ( bytesRead > 0 ) {
              Util.arrayCopyNonAtomic(apduBuffer, ISO7816.OFFSET_CDATA, apduin.pdata, echoOffset, bytesRead);
              echoOffset += bytesRead;
              bytesRead = apdu.receiveBytes(ISO7816.OFFSET_CDATA);
           }
           apduin.lc = echoOffset;

        } else {
           apduin.le = apduin.lc;
           apduin.lc = (short)0;
        }

        rc = handleEvent(apdu.getBuffer());

        if (rc) {
           dl = apduin.le;
           if(dl>(short)0) {
              Util.arrayCopyNonAtomic(apduin.pdata,(short)0, apduBuffer,(short)0,dl);
              apdu.setOutgoingAndSend((short)0, apduin.le);
           }
        }
    }
    //------------------------------------------------
    public boolean handleEvent(byte[] apb) throws ISOException
    {
        if (bFSReady) {
        	if(!bAppBlocked) {
        	switch (apduin.ins) {
             case constdef.INS_VERIFY:      return verify_pin();
             case constdef.INS_CHANGE:      return change_pin();
             case constdef.INS_UNBLOCK:     return unblock_pin();
             case constdef.INS_LOCKCARD:    return lock_card();
             case constdef.INS_READ_BIN:    return read_update_binary((byte)'R');
             case constdef.INS_UPDATE_BIN:  return read_update_binary((byte)'U');
             case constdef.INS_READ_REC:    return read_record();
             case constdef.INS_UPDATE_REC:  return update_record();
             case constdef.INS_GETBAL:      return get_balance();
             case constdef.INS_INITTRANS:
                if( apduin.cla != constdef.CUP_CLA )
                   ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
                if ( apduin.p1 ==(byte)0x1) return init_purchase();
                if ( apduin.p1 ==(byte)0x0) return init_load();
                ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
                break;
             case constdef.INS_PURCHASE:    return debit_purchase();
             case constdef.INS_LOAD:        return credit_load();
             case constdef.GET_TRANS_PROOF: return get_tp();
             case constdef.APP_BLOCK:       return app_block();
             case constdef.APP_UNBLOCK:     return app_unblock();
             case constdef.CARD_BLOCK:      return card_block();
             case constdef.INS_CHALLENGE:   return getcha();
            } // end of switch
        	} else {
        		switch (apduin.ins) {
                  case constdef.APP_UNBLOCK:     return app_unblock();
                  case constdef.CARD_BLOCK:      return card_block();
                  case constdef.INS_CHALLENGE:   return getcha();
                  default: ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED); 
        		} // end switch
        	} // end of else
        } else { // create file system
           if (apduin.ins==constdef.INS_CREATEFS)
               if ( apduin.p1 ==(byte)0x0)  return create_fs();
        }

        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        return false;
    }
    //------------------------------------------------
    public boolean create_fs() throws ISOException
    // create file system
    {
        byte[] ptr=null;

        if( apduin.cla != constdef.CUP_CLA )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if ( apduin.p1 != (byte)0x00 || apduin.p2 != (byte)0x0)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        ptr = new byte[30];
        if(ptr==null)
            ISOException.throwIt(ISO7816.SW_FILE_FULL);
        ef15 = new tjefbinary(ptr,(short)30, (byte)0x00,(byte)0x02);

        ptr = new byte[55];
        if(ptr==null)
            ISOException.throwIt(ISO7816.SW_FILE_FULL);
        ef16 = new tjefbinary(ptr,(short)55,  (byte)0x00,(byte)0x02);
        ef18 = new tjelinearfix((short)10, (short)0x17,(byte)0x01,(byte)0xff);
        ef19 = new tjelinearfix((short)20, (short)0x12,(byte)0xFF,(byte)0xFF);  
        ef17 = new tjpbocIDU(ef19,ef18);

        Util.arrayFillNonAtomic(apduin.ucTemp256,(short)0,(short)32,(byte)0x11);
        apduin.ucTemp256[0] = (byte)0x06;
        apduin.ucTemp256[1] = (byte)0x01;
        ef19.writerec((byte)0x01,apduin.ucTemp256);
        
        ef18.cyclicMode();
        bFSReady = true;
        apduin.le = (short)0;
        return true;
    }
    //------------------------------------------------
    public boolean verify_pin() throws ISOException
    // PBOC 2.0 verify command
    {
        if( apduin.cla != (byte)0x0 )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if (apduin.lc>(short)8)
           ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        if ( apduin.p1 != (byte)0 && apduin.p1 != (byte)0 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (pinOwner.remainTimes() == (short)0)
              ISOException.throwIt(constdef.SW_E_PINBLKED);

        if (apduin.lc<(short)8)
           Util.arrayFillNonAtomic(apduin.pdata,apduin.lc,(short)(8-apduin.lc),(byte)0xFF);

        if(!pinOwner.verify(apduin.pdata))
              ISOException.throwIt((short)((short)0x6c00 + pinOwner.remainTimes()));

        apduin.le = (short)0;
        return true;
    }
    //------------------------------------------------
    public boolean reload_pin() throws ISOException
    // PBOC 2.0 reload PIN
    {
    	short rc; 
    	
    	Util.arrayCopyNonAtomic(apduin.pdata,(short)(apduin.lc-4),apduin.ucTemp256,(short)0,(short)4);
    	rc = ((tjpbocIDU)ef17).unwrap_apdu((byte)0x05,apduin,true);
    	
    	if(rc==(short)1) 	ISOException.throwIt(constdef.SW_E_REFDATA);
    	if(rc==(short)2) 	ISOException.throwIt(constdef.SW_E_SMDATA);
    	
    	Util.arrayFillNonAtomic(apduin.ucTemp256,(short)0,(short)8,(byte)0xFF);
    	Util.arrayCopyNonAtomic(apduin.pdata,(short)0,apduin.ucTemp256,(short)0,(short)(apduin.lc-4));
    	pinOwner.setNewValue(apduin.ucTemp256);
        return true;
    }
    //------------------------------------------------    
    public boolean change_pin() throws ISOException
    // PBOC 2.0 change pin command
    {
        short  i,j;

        apduin.le = (short)0;
        
        if( apduin.cla != constdef.CUP_CLA )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        
        if ( apduin.p2 != (byte)0x0)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc<(short)0x06)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        
        
        if ( apduin.p1 == (byte)0x0) return reload_pin(); 
        if ( apduin.p1 != (byte)0x01)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc>(short)0x0d)
           ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        if (pinOwner.remainTimes() == (short)0)
             ISOException.throwIt(constdef.SW_E_PINBLKED);

        // initilize parameter
        i = (short)0;
        j  = (short)0;
        Util.arrayFillNonAtomic(apduin.ucTemp256,(short)0,(short)16,(byte)0xFF);

        // retrieve old pin
        for(; i<apduin.lc;i++) {
           if ( apduin.pdata[i] !=(byte)0xFF)
              apduin.ucTemp256[j++] = apduin.pdata[i];
           else break;
        }

        i++; // skip 0xFF
        // padding 0xff
        while(j <(short)8)
            j++;

        // padding new pin
        for(; i<apduin.lc;i++)
           apduin.ucTemp256[j++] = apduin.pdata[i];

        if(!pinOwner.verifyChange(apduin.ucTemp256))
             ISOException.throwIt((short)((short)0x6c00 + pinOwner.remainTimes()));

        return true;
    }
    //------------------------------------------------
    public boolean unblock_pin() throws ISOException
    {
        short rc;
        
    	if( apduin.cla != (byte)0x84 )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if ( apduin.p1 != (byte)0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if ( apduin.p2 != (byte)0x01)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc!=(short)0x0c)
           ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        rc = ((tjpbocIDU)ef17).unwrap_apdu((byte)0x04,apduin,false);
        switch(rc) {
           case (short)0x01: ISOException.throwIt(constdef.SW_E_REFDATA);            
           case (short)0x02: ISOException.throwIt(constdef.SW_E_SMDATA);        	
        }

        if (pinOwner.remainTimes() > (short)0)
        	ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        Util.arrayFillNonAtomic(apduin.ucTemp256,(short)0,(short)8,(byte)0xFF);
        Util.arrayCopyNonAtomic(apduin.pdata,(short)0,apduin.ucTemp256,(short)0,apduin.lc);
        
        if(!pinOwner.verify(apduin.ucTemp256))
        	ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        pinOwner.resetCounter();
        apduin.le = (short)0;
        return true;
    }
    //------------------------------------------------
    public boolean lock_card() throws ISOException
    {
        if( apduin.cla != constdef.CUP_CLA )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if ( apduin.p1 != (byte)0 && apduin.p2 != (byte)0 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc!=(short)0x0)
           ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        if (bPersoed)
           ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        bPersoed = true;
        return true;
    }
    //------------------------------------------------
    public boolean getcha() throws ISOException
    {
        if( apduin.cla != (byte)0x0 )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

       if ( apduin.p1 != (byte)0 && apduin.p1 != (byte)0 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

       if (apduin.le!=(short)4)
           ISOException.throwIt(ISO7816.SW_WRONG_DATA);

       if ( ef17 == null )
           ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

       ((tjpbocIDU)ef17).getrnd(apduin.pdata);
       apduin.le = (short)4;
       return true;
    }
    //------------------------------------------------
    private boolean check_ac(byte ac)
    {
        if (!bPersoed) return true;

        if ( ac == (byte)0x0)  return true;
        if ( ac == (byte)0x01) return pinOwner.certified();
        if ( ac == (byte)0x02) if( (byte)(apduin.cla&0x0F) == (byte)0x04) return true;
        return false;
    }
    //------------------------------------------------
    private boolean operate_file(byte opt, tjefbinary fp) throws ISOException
    {
        short offset,rc;
        
        if ( opt == (byte)'R' ) {
           if ( (short)(apduin.p2+apduin.le) > fp.fsize ) {
              offset = (short)(fp.fsize-apduin.p2);
              ISOException.throwIt((short)(0x6c00 + offset));
           }
           apduin.lc = (short)(apduin.p2&0x0FF);
           if (!check_ac(fp.acr))
               ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);

           fp.readbin(apduin.lc,apduin.le,apduin.pdata);
           return true;
        } else if ( opt == (byte)'U' ) { // update binary

           offset = (short)(apduin.p2&0x0FF);
           if ( (short)(offset+apduin.lc) > fp.fsize )
               ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

           if (!check_ac(fp.acw)) 
        	   ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);

           if( apduin.cla == (byte)0x04) {
         	   rc = ((tjpbocIDU)ef17).unwrap_apdu((byte)0x06,apduin,false);
               switch(rc) {
                  case (short)0x01: ISOException.throwIt(constdef.SW_E_REFDATA);            
                  case (short)0x02: ISOException.throwIt(constdef.SW_E_SMDATA);        	
               }
           }
          
           fp.writebin(offset,apduin.lc,apduin.pdata);
           if (fp == ef17)
                ((tjpbocIDU)ef17).updateEDC();

           apduin.le = (short)0;
           return true;
        }
        ISOException.throwIt(constdef.SW_E_INTERNAL);
        return false;
    }
    //------------------------------------------------
    public boolean read_update_binary(byte opt ) throws ISOException
    {
        byte  fid;

        if( (apduin.cla != (byte)0x0) && (apduin.cla != (byte)0x04))
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
       
        fid = (byte)(apduin.p1&0x80);
        if (fid == (byte)0x80) {
            fid = (byte)(apduin.p1 & (byte)0x1f);
        } else ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        switch (fid) {
            case (byte)21:  return operate_file(opt,ef15);
            case (byte)22:  return operate_file(opt,ef16);
            case (byte)23:  return operate_file(opt,ef17);
        }

        ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
        return false;
    }
    //------------------------------------------------
    public boolean read_record() throws ISOException
    {
      byte  tb;

      if( apduin.cla != (byte)0x0)
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
     
      if( apduin.p1 ==(byte)0 )
          ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

      tb = (byte)((apduin.p2&0x0f8)>>3);

      if (tb != (byte)0x18)
          ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);

      if ( (short)(apduin.p1) > ef18.recordNumber() )
          ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);

      if (!check_ac(ef18.acr))
           ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);

      if( apduin.le > (short)0 ) {
            if ( apduin.le != ef18.recordLength() )
               ISOException.throwIt((short)(0x6c00 + ef18.recordLength()));
      }

      apduin.le = ef18.readrec(apduin.p1,apduin.pdata);

      return true;
    }
    //------------------------------------------------
    public boolean update_record() throws ISOException
    {
      byte  tb;
      short rc;
      tjelinearfix  fp=null;

      if( (apduin.cla != (byte)0x0) && (apduin.cla != (byte)0x04))
          ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
      
      tb = (byte)((apduin.p2&0x0f8)>>3);
      if (tb == (byte)0x18) fp = ef18;
      else if (tb == (byte)0x19) fp = ef19;
      else ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);

      if ( fp.isCyclicMode()) {
         if( apduin.p1 !=(byte)0 ) ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
      } else {
         if( apduin.p1 ==(byte)0 ) ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
      }

      if ( (short)(apduin.p1) > fp.recordNumber() )
          ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);

      if (!check_ac(fp.acw))
          ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
      
      if( apduin.cla == (byte)0x04) {
    	  rc = ((tjpbocIDU)ef17).unwrap_apdu((byte)0x06,apduin,false);
          switch(rc) {
             case (short)0x01: ISOException.throwIt(constdef.SW_E_REFDATA);            
             case (short)0x02: ISOException.throwIt(constdef.SW_E_SMDATA);        	
          }
      }

      if( apduin.lc > (short)0 ) {
            if ( apduin.lc != fp.recordLength() )
               ISOException.throwIt((short)(0x6c00 + fp.recordLength()));
      } else ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

      if(!fp.writerec(apduin.p1,apduin.pdata))
          ISOException.throwIt(constdef.SW_E_INTERNAL);

      apduin.le = (short)0;
      return true;
    }
    //------------------------------------------------
    public boolean get_balance() throws ISOException
    {
        if( apduin.cla != constdef.CUP_CLA )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if ( apduin.p1 != (byte)0 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if ( apduin.p2 != (byte)0x02 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc!=(short)0x0)
           ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        //JCSystem.beginTransaction();
        ((tjpbocIDU)ef17).getBalanceEP(apduin.pdata);
        //JCSystem.commitTransaction();
        apduin.le = (short)4;
        return true;
    }
    //------------------------------------------------
    public boolean init_purchase() throws ISOException
    {
        if ( apduin.p2 != (byte)0x02 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc!=(short)0x0b)
           ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        //JCSystem.beginTransaction();
        short rc = ((tjpbocIDU)ef17).init4purchase(apduin.pdata);
        //JCSystem.commitTransaction();
        if ( rc > (short)0 )
           switch(rc) {
              case (short)1: ISOException.throwIt((short)0x9401);
              case (short)2: ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
              case (short)3: ISOException.throwIt((short)0x9403);
              default:       ISOException.throwIt(constdef.SW_E_INTERNAL);
           }

        apduin.le = (short)0x0F;
        return true;
    }
    //------------------------------------------------
    public boolean debit_purchase() throws ISOException
    {
        if( apduin.cla != constdef.CUP_CLA )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
  
        if ( apduin.p1 != (byte)0x1 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if ( apduin.p2 != (byte)0x00 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc!=(short)0x0F)
           ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
 
        if (ef17==null)
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        if ( !((tjpbocIDU)ef17).checkStateMachine((byte)0x0)) {
            //JCSystem.abortTransaction();
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        //JCSystem.beginTransaction();
        short rc = ((tjpbocIDU)ef17).debit4purchase(apduin.pdata);
        //JCSystem.commitTransaction();
        if ( rc > (short)0 )
           switch(rc) {
              case (short)1: ISOException.throwIt((short)0x6901);
              case (short)2: ISOException.throwIt((short)0x9302);
              case (short)4: ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
              default:       ISOException.throwIt(constdef.SW_E_INTERNAL);
           }
         
        apduin.le = (short)0x8;
        return true;
    }
    //------------------------------------------------
    public boolean init_load() throws ISOException
    {
        if ( apduin.p2 != (byte)0x02 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc!=(short)0x0b)
           ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        if(!pinOwner.certified())
           ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);

        short rc = ((tjpbocIDU)ef17).init4load(apduin.pdata);

        if ( rc > (short)0 )
           switch(rc) {
              case (short)1: ISOException.throwIt((short)0x9401);
              case (short)2: ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED); // overflow
              default:       ISOException.throwIt(constdef.SW_E_INTERNAL);
           }

        apduin.le = (short)0x10;
        return true;
    }
    //------------------------------------------------
    public boolean credit_load() throws ISOException
    {
        if( apduin.cla != constdef.CUP_CLA )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if ( apduin.p1 != (byte)0x0 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if ( apduin.p2 != (byte)0x0 )
           ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc!=(short)0x0b)
           ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        if (ef17==null)
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        if ( !((tjpbocIDU)ef17).checkStateMachine((byte)0x01))
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        short rc = ((tjpbocIDU)ef17).credit4load(apduin.pdata);

        if ( rc > (short)0 )
           switch(rc) {
             case (short)1: ISOException.throwIt((short)0x9302);
             default:       ISOException.throwIt(constdef.SW_E_INTERNAL);
           }

        apduin.le = (short)0x4;
        return true;
    }
    //------------------------------------------------
    public boolean get_tp() throws ISOException
    {
        if( apduin.cla != constdef.CUP_CLA )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if ( apduin.p1 != (byte)0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if (apduin.lc!=(short)0x02)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        if ( !((tjpbocIDU)ef17).can_gtp())
        	ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        
        if ( !((tjpbocIDU)ef17).match_tn(apduin.p2,Util.makeShort(apduin.pdata[0],apduin.pdata[1])))
        	ISOException.throwIt((short)0x9406);
        
        ((tjpbocIDU)ef17).copy_tac(apduin.pdata);
        apduin.le = (short)0x8;
    	return true;
    }
    //------------------------------------------------
    public boolean app_block() throws ISOException
    {
    	short rc;
        
    	if( apduin.cla != (byte)0x84 )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if ( apduin.p1 != (byte)0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        
        if ( apduin.lc != (byte)0x04)
        	ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        
        rc = ((tjpbocIDU)ef17).unwrap_apdu((byte)0x06,apduin,false);
        switch(rc) {
           case (short)0x01: ISOException.throwIt(constdef.SW_E_REFDATA);            
           case (short)0x02: ISOException.throwIt(constdef.SW_E_SMDATA);        	
        }
        
        if (apduin.p2 ==(byte)0x0) bAppBlocked = true; 
        else if (apduin.p2 ==(byte)0x01) bAppBLKALWS  = true;
        else ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        
        apduin.le = (short)0x0;
    	return true;
    }
    //------------------------------------------------
    public boolean app_unblock() throws ISOException
    {
    	short rc;

    	if( apduin.cla != (byte)0x84  )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if ( apduin.p1 != (byte)0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if ( apduin.p2 != (byte)0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if ( apduin.lc != (byte)0x04)
        	ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        
        if(bAppBLKALWS)
        	ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        
        rc = ((tjpbocIDU)ef17).unwrap_apdu((byte)0x06,apduin,false);
        switch(rc) {
           case (short)0x01: ISOException.throwIt(constdef.SW_E_REFDATA);            
           case (short)0x02: ISOException.throwIt(constdef.SW_E_SMDATA);        	
        }
        
        if(bAppBlocked) bAppBlocked = false;
        else ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        
        apduin.le = (short)0x0;
    	return true;
    }
    //------------------------------------------------
    public boolean card_block() throws ISOException
    {
    	short rc;

    	if( apduin.cla != (byte)0x84 )
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        if ( apduin.p1 != (byte)0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        if ( apduin.p2 != (byte)0x00)
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);

        if ( apduin.lc != (byte)0x04)
        	ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
       
        rc = ((tjpbocIDU)ef17).unwrap_apdu((byte)0x06,apduin,false);
        switch(rc) {
           case (short)0x01: ISOException.throwIt(constdef.SW_E_REFDATA);            
           case (short)0x02: ISOException.throwIt(constdef.SW_E_SMDATA);        	
        }
        
        bCardBlocked = true;
        apduin.le = (short)0x0;
    	return true;
    }
    //------------------------------------------------
}
