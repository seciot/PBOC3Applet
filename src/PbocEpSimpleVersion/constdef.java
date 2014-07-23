/**
 *------------------------------------------------------------------------------
 *  Project name    : smartpen
 *                      - Java Card Open Platform applet -
 *
 *  Platform        :  Java virtual machine
 *  Language        :  java 1.3.0-C
 *  Devl tool       :  Borland (c) JBuilder 4.0
 *
 *  Original author : Menghongwen@gmail.com 
 *  Date            : 2004, 11
 *------------------------------------------------------------------------------
 */
package PbocEpSimpleVersion;

public class constdef
{
   //----------- CLA Byte ------------------------------
   final static byte CUP_CLA   = (byte)0x80;
 
   //----------- INS Byte ------------------------------
   final static byte INS_CREATEFS     = (byte)0xE0;
   final static byte INS_VERIFY       = (byte)0x20;
   final static byte INS_CHALLENGE    = (byte)0x84;
   final static byte INS_CHANGE       = (byte)0x5E;
   final static byte INS_UNBLOCK      = (byte)0x24;
   final static byte INS_LOCKCARD     = (byte)0x26;
   final static byte INS_READ_BIN     = (byte)0xB0;
   final static byte INS_READ_REC     = (byte)0xB2;
   final static byte INS_UPDATE_BIN   = (byte)0xD6;
   final static byte INS_UPDATE_REC   = (byte)0xDC;
   final static byte INS_FIRELOCK     = (byte)0xF6;

   // transaction
   final static byte INS_GETBAL       = (byte)0x5C;
   final static byte INS_INITTRANS    = (byte)0x50;
   final static byte INS_PURCHASE     = (byte)0x54;
   final static byte INS_LOAD         = (byte)0x52;
   
   final static byte APP_BLOCK        = (byte)0x1E;
   final static byte APP_UNBLOCK      = (byte)0x18;
   final static byte CARD_BLOCK       = (byte)0x16;
   final static byte GET_TRANS_PROOF  = (byte)0x5A;
   
   //----------- SW Code ------------------------------
   final static short SW_E_INTERNAL   = (short)0x6581;
   final static short SW_E_FTYPE      = (short)0x6a02;
   final static short SW_E_PINBLKED   = (short)0x6a83;
   final static short SW_E_OPFTYPE    = (short)0x6981;
   final static short SW_E_REFDATA    = (short)0x6a88;
   final static short SW_E_SMDATA     = (short)0x6988;
   final static short SW_E_APPBLK     = (short)0x6A81;
   final static short SW_E_UPCARD     = (short)0x6A74;
   final static short SW_E_UPCARDSIO  = (short)0x6A78;
   //----------- constants ----------------------------
   final static short CONST_RetryTimes= (short)3;

}