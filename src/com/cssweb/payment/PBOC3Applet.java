/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cssweb.payment;

import javacard.framework.*;

/**
 *
 * @author chenhf
 */
public class PBOC3Applet extends Applet implements ISO7816{
    
    private static final byte GET_BALANCE  = (byte) 0x50;
    private OwnerPIN ownerPin = null;

    /**
     * Installs this applet.
     * 
     * @param bArray
     *            the array containing installation parameters
     * @param bOffset
     *            the starting offset in bArray
     * @param bLength
     *            the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new PBOC3Applet();
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected PBOC3Applet() {
        byte PIN_TRY_LIMIT = 3;
        byte MAX_PIN_SIZE = 12;
        
        ownerPin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);
        
        register();
    }
    
    
    /**
     * 
     * @return 
     */
    public boolean select()
    {
        return true;
    }
    
    public void deselect()
    {
        
    }

    /**
     * Processes an incoming APDU.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU
     */
    public void process(APDU apdu) {
        //Insert your code here
        
        byte[] buffer = apdu.getBuffer();
        
        
        byte cla = buffer[ISO7816.OFFSET_CLA];
        byte ins = buffer[ISO7816.OFFSET_INS];
        byte p1 = buffer[ISO7816.OFFSET_P1];
        byte p2 = buffer[ISO7816.OFFSET_P2];
        
        
        
        
        switch (ins)
        {
            case GET_BALANCE:
                getBalance(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
    
    public void verifyPassword(APDU apdu)
    {
        JCSystem.beginTransaction();
        
    }
    /*
    byte[] buffer = apdu.getBuffer();
short bytes_left = (short) buffer[ISO.OFFSET_LC];
short readCount = apdu.setIncomingAndReceive();
while (bytes_left > 0) {
// Process received data in buffer; copy chunk to temp buf.
Util.arrayCopy(buffer, ISO.OFFSET_CDATA, tbuf, 0, readCount);
bytes_left -= readCount;
// Get more data
readCount = apdu.receiveBytes(ISO.OFFSET_CDDATA);
}
    */
    public void verifyPin(APDU apdu)
    {
        byte[] buffer = apdu.getBuffer();
        byte readBytes = (byte)apdu.setIncomingAndReceive();
        if (ownerPin.check(buffer, ISO7816.OFFSET_CDATA, readBytes)== false )
        {
            //ISOException.throwIt(ISO7816.SW_PINVERIFY_FAILED);
        }
    }
    
    public void getBalance(APDU apdu)
    {
        byte[] buffer = apdu.getBuffer();
        
        byte lc = buffer[ISO7816.OFFSET_LC];
        byte readBytes = (byte) apdu.setIncomingAndReceive();
        
        if (lc != readBytes)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        
        //byte buffer[ISO7816.OFFSET_CDATA];
        
        // 得到期望响应长度
        short le = apdu.setOutgoing();
        if (le < 2)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        
        // 设置实际返回长度
        byte actualLength = 2;
        apdu.setOutgoingLength(actualLength);
        
       short balance = 100;
       
        buffer[0] = (byte) (balance >> 8);
        buffer[1] = (byte) (balance & 0xFF);
        
        //Util.arrayCopy(buffer, le, buffer, le, le);
        
        apdu.sendBytes((byte)0, actualLength);
        
        // SW1 SW2由jcre返回， 代码不用实现
    }
}
