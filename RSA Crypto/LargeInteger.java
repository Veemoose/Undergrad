import java.util.*;
import java.math.BigInteger;

public class LargeInteger {
	
	private final byte[] ONE = {(byte) 0, (byte) 1};
	private final byte[] ZERO = {(byte) 0, (byte) 0};

	private byte[] val;

	/**
	 * Construct the LargeInteger from a given byte array
	 * @param b the byte array that this LargeInteger should represent
	 */
	public LargeInteger(byte[] b) {
		val = b;
	}

	/**
	 * Construct the LargeInteger by generatin a random n-bit number that is
	 * probably prime (2^-100 chance of being composite).
	 * @param n the bitlength of the requested integer
	 * @param rnd instance of java.util.Random to use in prime generation
	 */
	public LargeInteger(int n, Random rnd) {
		val = BigInteger.probablePrime(n, rnd).toByteArray();
	}
	
	/**
	 * Return this LargeInteger's val
	 * @return val
	 */
	public byte[] getVal() {
		return val;
	}

	/**
	 * Return the number of bytes in val
	 * @return length of the val byte array
	 */
	public int length() {
		return val.length;
	}

	/** 
	 * Add a new byte as the most significant in this
	 * @param extension the byte to place as most significant
	 */
	public void extend(byte extension) {
		byte[] newv = new byte[val.length + 1];
		newv[0] = extension;
		for (int i = 0; i < val.length; i++) {
			newv[i + 1] = val[i];
		}
		val = newv;
	}

	/**
	 * If this is negative, most significant bit will be 1 meaning most 
	 * significant byte will be a negative signed number
	 * @return true if this is negative, false if positive
	 */
	public boolean isNegative() {
		return (val[0] < 0);
	}

	/**
	 * Computes the sum of this and other
	 * @param other the other LargeInteger to sum with this
	 */
	public LargeInteger add(LargeInteger other) {
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		}
		else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		// Actually compute the add
		int carry = 0;
		byte[] res = new byte[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			// Be sure to bitmask so that cast of negative bytes does not
			//  introduce spurious 1 bits into result of cast
			carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

			// Assign to next byte
			res[i] = (byte) (carry & 0xFF);

			// Carry remainder over to next byte (always want to shift in 0s)
			carry = carry >>> 8;
		}

		LargeInteger res_li = new LargeInteger(res);
	
		// If both operands are positive, magnitude could increase as a result
		//  of addition
		if (!this.isNegative() && !other.isNegative()) {
			// If we have either a leftover carry value or we used the last
			//  bit in the most significant byte, we need to extend the result
			if (res_li.isNegative()) {
				res_li.extend((byte) carry);
			}
		}
		// Magnitude could also increase if both operands are negative
		else if (this.isNegative() && other.isNegative()) {
			if (!res_li.isNegative()) {
				res_li.extend((byte) 0xFF);
			}
		}

		// Note that result will always be the same size as biggest input
		//  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
		return res_li;
	}

	/**
	 * Negate val using two's complement representation
	 * @return negation of this
	 */
	public LargeInteger negate() {
		byte[] neg = new byte[val.length];
		int offset = 0;

		// Check to ensure we can represent negation in same length
		//  (e.g., -128 can be represented in 8 bits using two's 
		//  complement, +128 requires 9)
		if (val[0] == (byte) 0x80) { // 0x80 is 10000000
			boolean needs_ex = true;
			for (int i = 1; i < val.length; i++) {
				if (val[i] != (byte) 0) {
					needs_ex = false;
					break;
				}
			}
			// if first byte is 0x80 and all others are 0, must extend
			if (needs_ex) {
				neg = new byte[val.length + 1];
				neg[0] = (byte) 0;
				offset = 1;
			}
		}

		// flip all bits
		for (int i  = 0; i < val.length; i++) {
			neg[i + offset] = (byte) ~val[i];
		}

		LargeInteger neg_li = new LargeInteger(neg);
	
		// add 1 to complete two's complement negation
		return neg_li.add(new LargeInteger(ONE));
	}

	/**
	 * Implement subtraction as simply negation and addition
	 * @param other LargeInteger to subtract from this
	 * @return difference of this and other
	 */
	public LargeInteger subtract(LargeInteger other) {
		return this.add(other.negate());
	}

	/**
	 * Compute the product of this and other
	 * @param other LargeInteger to multiply by this
	 * @return product of this and other
	 */
	public LargeInteger multiply(LargeInteger other) {
		if(isZero()) {
			return this;
		} else if(other.isZero()) {
			return other;
		}

		boolean oneNegativeTerm = false;
		byte[] multiplicandBytes, multiplierBytes;

		if(val.length < other.length()) { // let the mutiplicand be the larger number so that we have to shift through the multiplier fewer times
			multiplicandBytes = other.getVal();
			multiplierBytes = val;
		} else {
			multiplicandBytes = val;
			multiplierBytes = other.getVal();
		}

		int normalSize = multiplicandBytes.length;

		LargeInteger tempMultiplicand = new LargeInteger(multiplicandBytes);
		LargeInteger multiplier = new LargeInteger(multiplierBytes);

		if(tempMultiplicand.isNegative() && multiplier.isNegative()) { // multiplying two negatives or two positives gives a positive product
			tempMultiplicand = tempMultiplicand.negate();
			multiplier = multiplier.negate();

		} else if(tempMultiplicand.isNegative()) {
			tempMultiplicand = tempMultiplicand.negate();
			oneNegativeTerm = true; // in either case, we do the multiplication as positive, then flip the sign later

		} else if(multiplier.isNegative()) {
			multiplier = multiplier.negate();
			oneNegativeTerm = true;
		}

		byte[] productBytes = new byte[2*normalSize];
		LargeInteger product = new LargeInteger(productBytes);


		byte[] maxShiftMultiplicand = new byte[2*normalSize];
		byte[] temp = tempMultiplicand.getVal();

		for(int i = (temp.length - 1); i >= 0; i--) {
			maxShiftMultiplicand[i + temp.length] = temp[i]; // The multiplicand will likely increase in byte length, but never more than twice it's original size
		}
		LargeInteger multiplicand = new LargeInteger(maxShiftMultiplicand);

		while(!multiplier.isZero()) {
			if(multiplier.oddLastBit()) {
				product = product.add(multiplicand);
			} 
			multiplicand = multiplicand.shiftLeftArithmetic();
			multiplier = multiplier.shiftRightArithmetic();
		}

		if(oneNegativeTerm) {
			product = product.negate();	// if only one argument was negative, then flip the sign
		}
		//product.trim();           // Ideally this would be uncommented, but it's inclusion seems to cause completely impractical runtimes for signing and verifying,
					// so we're gonna sacrifice some memory and possible runtime elsewhere
		return product;
	}
	
	/**
	 * Run the extended Euclidean algorithm on this and other
	 * @param other another LargeInteger
	 * @return an array structured as follows:
	 *   0:  the GCD of this and other
	 *   1:  a valid x value
	 *   2:  a valid y value
	 * such that this * x + other * y == GCD in index 0
	 */
	public LargeInteger[] XGCD(LargeInteger other) {
		XGCD_Struct result = new XGCD_Struct();
		result.a = new LargeInteger(val);
		result.b = new LargeInteger(other.getVal());

		recursiveXGCD(result);

		LargeInteger[] xgcd = new LargeInteger[3];
		xgcd[0] = result.d;
		xgcd[1] = result.x;
		xgcd[2] = result.y;

		for(int i = 0; i < 3; i++) {
			xgcd[i].trim();
		}
		return xgcd;
	}

	private void recursiveXGCD(XGCD_Struct result) {
		if(result.b.isZero()) {
			result.x = new LargeInteger(ONE);   // x = 1
			result.y = new LargeInteger(ZERO);	// y = 0
			result.d = result.a;
			return;
		}

		LargeInteger[] division = result.a.divide(result.b);
		LargeInteger mod = division[1];
		LargeInteger quotient = division[0];

		XGCD_Struct previous = new XGCD_Struct();
		previous.a = result.b;
		previous.b = mod;
		recursiveXGCD(previous);
		result.x = previous.y;
		result.y = previous.x.subtract((quotient.multiply(previous.y)));
		result.d = previous.d;
	}
	/**
	  *   a = this
	  *   b = other
	  *   x and y are the x and y from XGCD
	  *   d is the GCD
	  */
	private class XGCD_Struct {
		LargeInteger a, b, x, y, d;

		public void print() {// for debugging
			System.out.print("a: "); printBytes(a); 
			System.out.print("b: "); printBytes(b);
			System.out.print("x: "); printBytes(x); 
			System.out.print("y: "); printBytes(y);
			System.out.print("d: "); printBytes(d);
			System.out.println("\n");
		}
	}

	/**
	  * Compute the result of raising this to the power of y mod n
	  * @param y exponent to raise this to
	  * @param n modulus value to use
	  * @return this^y mod n
	  * Based on modularExp algorithm depicted in class slides
	  */
	public LargeInteger modularExp(LargeInteger y, LargeInteger n) {
		LargeInteger one = new LargeInteger(ONE);
		if(y.subtract(one).isZero()) {
			return this.mod(n);	// y was 1, so just return this mod n
		} else if(y.isZero()) {
			return one;	// y is zero, and any number to the 0th power is 1
		}

		LargeInteger result = one;
		LargeInteger temp = new LargeInteger(val);

		while(!y.isZero()) {			
			if(y.oddLastBit()) {
				result = result.multiply(temp);
				result = result.mod(n);
			}
			y = y.shiftRightArithmetic();
			temp = temp.multiply(temp);
			temp = temp.mod(n);
		}
		result.trim();
		return result;
	}

	/**
	 * Compute the quotient and remainder of this and other
	 * @param other LargeInteger to divide with this
	 * @return an array structured as follows:
	 *   0:  the quotient of this and other (always >= 1)
	 *   1:  the remainder of the division
	 */
	public LargeInteger[] divide(LargeInteger other) {
		boolean oneNegativeTerm = false;
		byte[] dividendBytes = val;
		byte[] divisorBytes = other.getVal();
		
		LargeInteger dividend = new LargeInteger(dividendBytes);
		LargeInteger tempDivisor = new LargeInteger(divisorBytes);

		// same as for multiply. Do division with positives, negate the quotient at the end if necessary
		if(dividend.isNegative() & tempDivisor.isNegative()) {
			dividend = dividend.negate();
			tempDivisor = tempDivisor.negate();

		} else if(dividend.isNegative()) {
			dividend = dividend.negate();
			oneNegativeTerm = true;

		} else if(tempDivisor.isNegative()) {
			tempDivisor = tempDivisor.negate();
			oneNegativeTerm = true;
		}

		LargeInteger tempDiff = dividend.subtract(tempDivisor);
		// for integer divison, if , if a < b, then a/b = 0 remainder b
		if(tempDiff.isNegative()) {
			LargeInteger[] res = new LargeInteger[2];
			res[0] = new LargeInteger(ZERO);
			res[1] = this;
			return res;
		}

		// to be honest I forget why these two loops are necessary, but my code doesn't work if I comment them out
		while(tempDivisor.length() < dividend.length()) {
			tempDivisor.extend((byte) 0);
		}
		while(dividend.length() < tempDivisor.length()) {
			dividend.extend((byte) 0);
		}

		byte[] maxShiftDivisor = new byte[tempDivisor.length() + dividend.length()];
		byte[] tempBytes = tempDivisor.getVal();

		for(int i = (tempBytes.length - 1); i >= 0; i--) {
			maxShiftDivisor[i + tempBytes.length] = tempBytes[i];  // We need room to shift the divisor all the way over to the left for our divison algorithm
		}
		LargeInteger divisor = new LargeInteger(maxShiftDivisor);

		int numOfShiftBits = 8*dividend.length();
		for(int i = 0; i < numOfShiftBits; i++) {
			divisor = divisor.shiftLeftArithmetic();
		}

		LargeInteger remainder = new LargeInteger(dividend.getVal());
		byte[] quotientBytes = new byte[dividend.length()];
		LargeInteger quotient = new LargeInteger(quotientBytes);
		LargeInteger one = new LargeInteger(ONE);

		for(int i = 0; i < numOfShiftBits; i++) {
			divisor = divisor.shiftRightArithmetic();

			LargeInteger diff = remainder.subtract(divisor);
			if(diff.isNegative()) {
				// append 0 to quotient
				quotient = quotient.shiftLeftArithmetic();
			} else {
				remainder = remainder.subtract(divisor);
				// append 1 to quotient
				quotient = quotient.shiftLeftArithmetic();
				quotient = quotient.add(one);
			}
		}

		if(oneNegativeTerm) {
			quotient = quotient.negate();
		}
		quotient.trim(); 
		remainder.trim();
		LargeInteger[] result = new LargeInteger[2];
		result[0] = quotient; 
		result[1] = remainder;
		return result;
	}

	public LargeInteger mod(LargeInteger other) {
		LargeInteger diff = this.subtract(other);

		if(diff.isNegative()) {
			return this;	// if a < b, then a mod b = a
		}
		LargeInteger[] division = this.divide(other);
		return division[1];
	}

	/**
	 *  Arithmetic shift left, byte by byte. Carries up MSB from previous byte
	 *  BITS SHIFTED LEFT BEYOND VAL[0] WILL BE LOST
	 */
	public LargeInteger shiftLeftArithmetic() {
		byte[] tempBytes = new byte[val.length];
		for(int i = 0; i < tempBytes.length; i++) {
	 		tempBytes[i] = val[i];
		}
	 	int last = tempBytes.length - 1;
	 	int previousMSB = tempBytes[last] & 0x80; 
	 	tempBytes[last] <<= 0x01;
	 	boolean transferBit = false;	// check if we need to carry the MSB of the previous byte over into the next byte after shifting

	 	for(int i = tempBytes.length - 2; i >= 0; i--) {
	 		transferBit = (previousMSB == 0x80);

	 		previousMSB = tempBytes[i] & 0x80;
	 		tempBytes[i] <<= 0x01;

	 		if(transferBit) {
	 			tempBytes[i] |= 0x01;
	 		}
	 	}
	 	LargeInteger result = new LargeInteger(tempBytes);
	 	return result;
	}

	/**
	 *   Arithmetic shift right, byte by byte. Tracks LSB from previous byte to determine if sign extension is needed.
	 */
	public LargeInteger shiftRightArithmetic() {
	 	byte[] tempBytes = new byte[val.length];
	 	for(int i = 0; i < tempBytes.length; i++) {
	 		tempBytes[i] = val[i];
	 	}

	 	int lastBitInByte = tempBytes[0] & 0x01;
	 	tempBytes[0] >>= 0x01;
	 	boolean transferBit = (lastBitInByte == 1);	// check if we need to carry the LSB of the previous byte over into the next byte after shifting

	 	for(int i = 1; i < tempBytes.length; i++) {
	 		lastBitInByte = tempBytes[i] & 0x01;
	 		tempBytes[i] >>>= 0x01;
	 		tempBytes[i] &= 127;	// because logical shifting isn't zero-extending, for some reason

	 		if(transferBit) {
	 			tempBytes[i] |= 0x80;
	 		}
	 		transferBit = (lastBitInByte == 1);
	 	}
	 	LargeInteger result = new LargeInteger(tempBytes);
	 	return result;
	}
	/**
	 *  Trims leading bytes of all 0's or 1's so that only one sign byte remains
	 */
	private void trim() {
	 	int index = 0;

	 	if(this.isNegative()) {
	 		for(int i = 0; i < val.length; i++) {
	 			if(val[i] != (byte) 0xFF) {
	 				index = i-1;	// this is the index of the signed byte that we're saving
	 				break;
	 			}
	 		}
	 	} else {
	 		for(int i = 0; i < val.length; i++) {
	 			if(val[i] != 0x0) {
	 				index = i-1;	// same as above comment
	 				break;
	 			}
	 		}
	 	}
	 	if(index == 0) return;	// if exactly one byte of padding exists, then no trimming is required
	 	else if (index < 0) { extend((byte) 0); return; }	// if no padding exists, then add one byte of padding

	 	int newSize = val.length - index;

	 	byte[] newVal = new byte[newSize];
	 	for(int i = 0; i < newSize; i++) {
	 		newVal[i] = val[i+index];
	 	}
	 	val = newVal;
	}

	public boolean isZero() {
	 	for(byte b : val) {
	 		if((b & (byte) 0xFF) != 0x0) {
	 			return false;
	 		}
	 	}
	 	return true;
	}

	/**
	 *  Determine if LargeInt is odd by looking at last bit
	 *  @return true if lastBit & 0x01 is 1, false if else
	 */
	private boolean oddLastBit() {
		return (val[val.length - 1] & (byte) 0x01) == 1;
	}

	public void printBytes(LargeInteger a) {
	 	byte[] b = a.getVal();
	 	for(int i = 0; i < b.length; i++) {
		 	System.out.print(String.format("%8s", Integer.toBinaryString(b[i] & 0xFF)).replace(' ', '0'));
		 	System.out.print(" ");
		}
		System.out.println();
	}
}
