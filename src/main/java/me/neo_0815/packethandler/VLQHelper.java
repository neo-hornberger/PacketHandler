package me.neo_0815.packethandler;

import java.math.BigInteger;

import static java.math.BigInteger.ZERO;

/**
 * LE: <a href=
 * "https://en.wikipedia.org/wiki/LEB128">https://en.wikipedia.org/wiki/LEB128</a><br/>
 * BE: <a href=
 * "https://en.wikipedia.org/wiki/Variable-length_quantity">https://en.wikipedia.org/wiki/Variable-length_quantity</a>
 */
public class VLQHelper {
	
	public static class VLQInt {
		private static final int LOWER_7_MASK = 0b01111111;
		private static final int SIGN_BIT_MASK = 0b10000000;
		private static final int HSEC_BIT_MASK = 0b01000000;
		private static final int INV_ZERO = ~0;
		
		public static void encodeULE(final ByteBuffer buf, int i) {
			boolean more;
			byte temp;
			
			do {
				temp = (byte) (i & LOWER_7_MASK);
				i >>>= 7;
				
				more = i != 0;
				if(more) temp |= SIGN_BIT_MASK;
				
				buf.write(temp);
			}while(more);
		}
		
		public static int decodeULE(final ByteBuffer buf) {
			int result = 0;
			int shift = 0;
			byte read;
			
			do {
				read = buf.read();
				result |= (read & LOWER_7_MASK) << shift;
				shift += 7;
			}while((read & SIGN_BIT_MASK) != 0);
			
			return result;
		}
		
		public static void encodeSLE(final ByteBuffer buf, int i) {
			boolean more;
			byte temp;
			
			do {
				temp = (byte) (i & LOWER_7_MASK);
				i >>= 7;
				
				more = !(i == 0 && (temp & HSEC_BIT_MASK) == 0 || i == -1 && (temp & HSEC_BIT_MASK) != 0);
				if(more) temp |= SIGN_BIT_MASK;
				
				buf.write(temp);
			}while(more);
		}
		
		public static int decodeSLE(final ByteBuffer buf) {
			int result = 0;
			int shift = 0;
			byte read;
			
			do {
				read = buf.read();
				result |= (read & LOWER_7_MASK) << shift;
				shift += 7;
			}while((read & SIGN_BIT_MASK) != 0);
			
			if(shift < Integer.SIZE && (read & HSEC_BIT_MASK) != 0) result |= INV_ZERO << shift;
			
			return result;
		}
	}
	
	public static class VLQLong {
		private static final long LOWER_7_MASK = VLQInt.LOWER_7_MASK;
		private static final long SIGN_BIT_MASK = VLQInt.SIGN_BIT_MASK;
		private static final long HSEC_BIT_MASK = VLQInt.HSEC_BIT_MASK;
		private static final long INV_ZERO = VLQInt.INV_ZERO;
		
		public static void encodeULE(final ByteBuffer buf, long i) {
			boolean more;
			byte temp;
			
			do {
				temp = (byte) (i & LOWER_7_MASK);
				i >>>= 7;
				
				more = i != 0;
				if(more) temp |= SIGN_BIT_MASK;
				
				buf.write(temp);
			}while(more);
		}
		
		public static long decodeULE(final ByteBuffer buf) {
			long result = 0;
			int shift = 0;
			byte read;
			
			do {
				read = buf.read();
				result |= (read & LOWER_7_MASK) << shift;
				shift += 7;
			}while((read & SIGN_BIT_MASK) != 0);
			
			return result;
		}
		
		public static void encodeSLE(final ByteBuffer buf, long i) {
			boolean more;
			byte temp;
			
			do {
				temp = (byte) (i & LOWER_7_MASK);
				i >>= 7;
				
				more = !(i == 0 && (temp & HSEC_BIT_MASK) == 0 || i == -1 && (temp & HSEC_BIT_MASK) != 0);
				if(more) temp |= SIGN_BIT_MASK;
				
				buf.write(temp);
			}while(more);
		}
		
		public static long decodeSLE(final ByteBuffer buf) {
			long result = 0;
			int shift = 0;
			byte read;
			
			do {
				read = buf.read();
				result |= (read & LOWER_7_MASK) << shift;
				shift += 7;
			}while((read & SIGN_BIT_MASK) != 0);
			
			if(shift < Long.SIZE && (read & HSEC_BIT_MASK) != 0) result |= INV_ZERO << shift;
			
			return result;
		}
	}
	
	public static class VLQBigInt {
		private static final BigInteger LOWER_7_MASK = BigInt(VLQInt.LOWER_7_MASK);
		private static final BigInteger NEG_ONE = BigInt(-1);
		private static final BigInteger INV_ZERO = BigInt(VLQInt.INV_ZERO);
		
		private static final int SIGN_BIT_MASK = VLQInt.SIGN_BIT_MASK;
		private static final int HSEC_BIT_MASK = VLQInt.HSEC_BIT_MASK;
		
		public static void encodeULE(final ByteBuffer buf, BigInteger bi) {
			boolean more;
			byte temp;
			
			do {
				temp = bi.and(LOWER_7_MASK).byteValue();
				bi = bi.shiftRight(7); // TODO should be an arithmetic right shift
				
				more = !bi.equals(ZERO);
				if(more) temp |= SIGN_BIT_MASK;
				
				buf.write(temp);
			}while(more);
		}
		
		public static BigInteger decodeULE(final ByteBuffer buf) {
			BigInteger result = ZERO;
			int shift = 0;
			byte read;
			
			do {
				read = buf.read();
				result = result.or(BigInt(read).and(LOWER_7_MASK).shiftLeft(shift));
				shift += 7;
			}while((read & SIGN_BIT_MASK) != 0);
			
			return result;
		}
		
		public static void encodeSLE(final ByteBuffer buf, BigInteger bi) {
			boolean more;
			byte temp;
			
			do {
				temp = bi.and(LOWER_7_MASK).byteValue();
				bi = bi.shiftRight(7);
				
				more = !(bi.equals(ZERO) && (temp & HSEC_BIT_MASK) == 0 || bi.equals(NEG_ONE) && (temp & HSEC_BIT_MASK) != 0);
				if(more) temp |= SIGN_BIT_MASK;
				
				buf.write(temp);
			}while(more);
		}
		
		public static BigInteger decodeSLE(final ByteBuffer buf) {
			BigInteger result = ZERO;
			int shift = 0;
			byte read;
			
			do {
				read = buf.read();
				result = result.or(BigInt(read).and(LOWER_7_MASK).shiftLeft(shift));
				shift += 7;
			}while((read & SIGN_BIT_MASK) != 0);
			
			if(shift < Integer.MAX_VALUE && (read & HSEC_BIT_MASK) != 0) result = result.or(INV_ZERO.shiftLeft(shift));
			
			return result;
		}
		
		private static BigInteger BigInt(final int value) {
			return BigInteger.valueOf(value);
		}
	}
}
