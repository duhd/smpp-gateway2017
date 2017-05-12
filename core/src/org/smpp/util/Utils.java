package org.smpp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.PatternSyntaxException;


/**
 * Created by IntelliJ IDEA.
 * User: Hoang Dinh Du
 * Date: 29/06/2012
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    public static String MD5Hash(String data) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(data.getBytes());
            byte result[] = md5.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aResult : result) {
                String s = Integer.toHexString(aResult);
                int length = s.length();
                if (length >= 2) {
                    sb.append(s.substring(length - 2, length));
                } else {
                    sb.append("0");
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String accentUCS2VN(String ucs2VN) {
        String accent = null;
        char ch180 = (char) 180;
        try {
            accent = ucs2VN.replaceAll("(à|ả|ã|á|ạ|ă|ằ|ẳ|ẵ|ắ|ặ|â|ầ|ẩ|ẫ|ấ|ậ|ä|å|ã|ạ|ả)", "a").replaceAll("(À|Ả|Ã|Á|Ạ|Ă|Ằ|Ẳ|Ẵ|Ắ|Ặ|Â|Ầ|Ẩ|Ẫ|Ấ|Ậ|Ä|Å)", "A")
                    .replaceAll("đ", "d").replaceAll("Đ", "D")
                    .replaceAll("(è|ẻ|ẽ|é|ẹ|ê|ề|ể|ễ|ế|ệ|ẻ)", "e").replaceAll("(È|Ẻ|Ẽ|É|Ẹ|Ê|Ề|Ể|Ễ|Ế|Ệ)", "E")
                    .replaceAll("(ì|ỉ|ĩ|í|ị)", "i").replaceAll("(Ì|Ỉ|Ĩ|Í|Ị)", "I")
                    .replaceAll("(ò|ỏ|õ|ó|ọ|ô|ồ|ổ|ỗ|ố|ộ|ơ|ờ|ở|ỡ|ớ|ợ|ö|ó)", "o").replaceAll("(Ò|Ỏ|Õ|Ó|Ọ|Ô|Ồ|Ổ|Ỗ|Ố|Ộ|Ơ|Ờ|Ở|Ỡ|Ớ|Ợ|Ö)", "O")
                    .replaceAll("(ù|ủ|ũ|ú|ụ|ư|ừ|ử|ữ|ứ|ự|ü|ủ)", "u").replaceAll("(Ù|Ủ|Ũ|Ú|Ụ|Ư|Ừ|Ử|Ữ|Ứ|Ự|Ü)", "U")
                    .replaceAll("(ỳ|ý|ỵ|ỷ|ỹ)", "y").replaceAll("(Ỳ|Ý|Ỵ|Ỷ|Ỹ)", "Y")
                    .replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("Ç", "C").replaceAll("ç", "c")
                    .replaceAll("(‘|’|" + ch180 + ") ", "'").replaceAll("(̃|̣|̉|́|̀)", "");
        } catch (PatternSyntaxException ex) {
            // Syntax error in the regular expression
        } catch (IllegalArgumentException ex) {
            // Syntax error in the replacement text (unescaped $ signs?)
        } catch (IndexOutOfBoundsException ex) {
            // Non-existent backreference used the replacement text
        }

        return accent;
    }

    public static String bytesToHexString(byte[] theData) {
        StringBuilder hexStrBuff = new StringBuilder(theData.length * 2);

        for (byte aTheData : theData) {
            String hexByteStr = Integer.toHexString(aTheData & 0xff).toUpperCase();
            if (hexByteStr.length() == 1) {
                hexStrBuff.append("0");
            }
            hexStrBuff.append(hexByteStr);
        }
        return hexStrBuff.toString();
    }

    public static byte[] hexStringToBytes(String theHexString) {
        byte[] data = new byte[theHexString.length() / 2];
        for (int i = 0; i < data.length; i++) {
            String a = theHexString.substring(i * 2, i * 2 + 2);
            data[i] = (byte) Integer.parseInt(a, 16);
        }
        return data;
    }

    /**
     * Expands a compressed GSM message in a readable text message
     * (7 bit data -> 1 character)
     *
     * @param indata text string in GSM standard alphabet
     * @return text string in Unicode
     */
    public static String expand(byte[] indata) {
        int x, n, y, Bytebefore, Bitshift;
        String msg = "";
        byte data[] = new byte[indata.length + 1];

        for (n = 1; n < data.length; n++) {
            data[n] = indata[n - 1];
        }

        Bytebefore = 0;
        for (n = 1; n < data.length; n++) {
            x = (int) (0x000000FF & data[n]);   // get a byte from the SMS
            Bitshift = (n - 1) % 7;               // calculate number of neccssary bit shifts
            y = x;
            y = y << Bitshift;                  // shift to get a conversion 7 bit compact GSM -> Unicode
            y = y | Bytebefore;                 // add bits from the byte before this byte
            y = y & 0x0000007F;                 // delete all bits except bit 7 ... 1 of the byte
            msg = msg + convertGSM2Unicode(y);  // conversion: 7 bit GSM character -> Unicode
            if (Bitshift == 6) {
                Bitshift = 1;
                y = x;
                y = y >>> Bitshift;                 // shift to get a conversion 7 bit compact GSM -> Unicode
                y = y & 0x0000007F;                 // delete all bits except bit 7 ... 1 of the byte
                msg = msg + convertGSM2Unicode(y);  // conversion: 7 bit GSM character -> Unicode
                Bytebefore = 0;
            }  // if
            else {
                Bytebefore = x;
                Bitshift = 7 - Bitshift;
                Bytebefore = Bytebefore >>> Bitshift;  // shift to get a conversion 7 bit compact GSM -> Unicode
                Bytebefore = Bytebefore & 0x000000FF;  // mask for one byte
            }  // else
        }  // for
        return msg;
    }  // expand

    /**
     * Compress a readable text message into the GSM standard alphabet
     * (1 character -> 7 bit data)
     *
     * @param data text string in Unicode
     * @return text string in GSM standard alphabet
     */
    public static byte[] compress(byte[] data) {
        int l;
        int n;  // length of compressed data
        byte[] comp;

        // calculate length of message
        l = data.length;
        n = (l * 7) / 8;
        if ((l * 7) % 8 != 0) {
            n++;
        }  // if

        comp = new byte[n];
        int j = 0;   // index in data
        int s = 0;   // shift from next data byte
        for (int i = 0; i < n; i++) {
            comp[i] = (byte) ((data[j] & 0x7F) >>> s);
            s++;
            if (j + 1 < l) {
                comp[i] += (byte) ((data[j + 1] << (8 - s)) & 0xFF);
            }  // if
            if (s < 7) {
                j++;
            }  // if
            else {
                s = 0;
                j += 2;
            }  // else
        } // for
        return comp;
    }  // compress

    /**
      * Convert one GSM standard alphabet character into a Unicode character
      * @param b one GSM standard alphabet character
      * @return one Unicode character
      */
     public static char convertGSM2Unicode(int b) {
       char c;

       if ((b >= 0x41) && (b <= 0x5A)) {    // character is between "A" and "Z"
         c = (char) b;
         return c;
       }  // if
       if ((b >= 0x61) && (b <= 0x7A)) {    // character is between "a" and "z"
         c = (char) b;
         return c;
       }  // if
       if ((b >= 0x30) && (b <= 0x39)) {    // character is between "0" and "9"
         c = (char) b;
         return c;
       }  // if

       switch (b) {
         case 0x00 : c = '@'; break;
         case 0x02 : c = '$'; break;
         case 0x0A : c = '\n'; break;
         case 0x0D : c = '\r'; break;
         case 0x11 : c = '_'; break;
         case 0x1E : c = 'ß'; break;
         case 0x20 : c = ' '; break;
         case 0x21 : c = '!'; break;
         case 0x22 : c = '\"'; break;
         case 0x23 : c = '#'; break;
         case 0x25 : c = '%'; break;
         case 0x26 : c = '&'; break;
         case 0x27 : c = '\''; break;
         case 0x28 : c = '('; break;
         case 0x29 : c = ')'; break;
         case 0x2A : c = '*'; break;
         case 0x2B : c = '+'; break;
         case 0x2C : c = ','; break;
         case 0x2D : c = '-'; break;
         case 0x2E : c = '.'; break;
         case 0x2F : c = '/'; break;
         case 0x3A : c = ':'; break;
         case 0x3B : c = ';'; break;
         case 0x3C : c = '<'; break;
         case 0x3D : c = '='; break;
         case 0x3E : c = '>'; break;
         case 0x3F : c = '?'; break;
         case 0x5B : c = 'Ä'; break;
         case 0x5C : c = 'Ö'; break;
         case 0x5E : c = 'Ü'; break;
         case 0x5F : c = '§'; break;
         case 0x7B : c = 'ä'; break;
         case 0x7C : c = 'ö'; break;
         case 0x7E : c = 'ü'; break;
         default:    c = '?'; break;
       }  // switch

       return c;
     }  // convertGSM2Unicode
}
