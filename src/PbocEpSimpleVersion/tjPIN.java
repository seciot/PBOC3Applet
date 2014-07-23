/**
 *------------------------------------------------------------------------------
 *  Project name    :  PBOC 2.0 EP Simple version
 *                      - Java Card Open Platform applet -
 *
 *  Platform        :  Java virtual machine
 *  Language        :  java 1.3.0-C
 *  Devl tool       :  Borland (c) JBuilder 4.0
 *
 *  Original author : Menghongwen@gmail.com 
 *  Date            : March, 2006
 *------------------------------------------------------------------------------
 */
package PbocEpSimpleVersion;

import javacard.framework.*;

public class tjPIN {
   private byte[]  szPIN = new byte[8];
   private short   usCount;
   private boolean bVerifyed;
   //------------------------------------------------
   public tjPIN() {
      Util.arrayFillNonAtomic(szPIN,(short)0,(short)8,(byte)0xFF);
      szPIN[0] =(byte)0x12;
      szPIN[1] =(byte)0x34;
      szPIN[2] =(byte)0x56;
      bVerifyed = false;
      usCount = constdef.CONST_RetryTimes;
   }
   //------------------------------------------------
   public void reset() {
      bVerifyed = false;
   }
   //------------------------------------------------
   public boolean verify(byte[] vdata){
      if (Util.arrayCompare(szPIN,(short)0,vdata,(short)0,(short)8)==(short)0) {
         usCount = constdef.CONST_RetryTimes;
         bVerifyed = true;
         return true;
      }
      if(usCount>(short)0) usCount--;
      return false;
   }
   //------------------------------------------------
   public boolean verifyChange(byte[] vdata){
      if (Util.arrayCompare(szPIN,(short)0,vdata,(short)0,(short)8)==(short)0) {
         Util.arrayCopyNonAtomic(vdata,(short)8,szPIN,(short)0,(short)8);
         usCount = constdef.CONST_RetryTimes;
         bVerifyed = true;
         return true;
      }
      if(usCount>(short)0) usCount--;
      return false;
   }
   //------------------------------------------------
   public void  setNewValue(byte[] vdata ){
       Util.arrayCopyNonAtomic(vdata,(short)0,szPIN,(short)0,(short)8);
       usCount = constdef.CONST_RetryTimes;
   }
   //------------------------------------------------
   public boolean certified() {
       return  bVerifyed;
   }
   //------------------------------------------------   
   public void resetCounter() {
	   usCount = constdef.CONST_RetryTimes;
       //Util.arrayFillNonAtomic(szPIN,(short)0,(short)8,(byte)0xFF);
	   //szPIN[0] =(byte)0x12;
	   //szPIN[1] =(byte)0x34;
	   //szPIN[2] =(byte)0x56;
	   bVerifyed = false;
   }
   //------------------------------------------------
   public short remainTimes() {
       return usCount;
   }
   //------------------------------------------------
}