import java.util.*;
import java.math.*;
import java.io.*;

public class RsaKeyGen {
	private static final byte[] UNITY = {(byte) 0, (byte) 1};
	private static final LargeInteger ONE = new LargeInteger(UNITY);

	public static void main(String[] args) {
		Random rand = new Random();

		LargeInteger p = new LargeInteger(256, rand);
		LargeInteger q = new LargeInteger(256, rand);
		LargeInteger n = p.multiply(q);

		p = p.subtract(ONE); 
		q = q.subtract(ONE);
		LargeInteger phiN = p.multiply(q);	// (p - 1)*(q - 1)
		LargeInteger e = new LargeInteger(8, rand);
		LargeInteger[] res = phiN.XGCD(e);

		while(!isValid(e, phiN, res[0])) {// calculate new e unless e < phi(n) and GCD(phi(n), e) == 1
			e = new LargeInteger(8, rand);
			res = phiN.XGCD(e);
		}
		LargeInteger d = findValidD(e, phiN); // d = (e^-1) mod phi(n) ==> e*d = k*phi(n) + 1 for some integer k

		byte[] eData = e.getVal();
		byte[] nData = n.getVal();
		byte[] dData = d.getVal();

		// I'm lazy about handling exceptions but hey, so was the example code given for hashing
		try {
			ObjectOutputStream publicKey = new ObjectOutputStream(new FileOutputStream("pubkey.rsa"));
			publicKey.writeObject(eData);
			publicKey.writeObject(nData);
			publicKey.close();
		} catch(IOException exc) {
			System.out.println(exc.toString());
			System.exit(1);
		}

		try {
			ObjectOutputStream privateKey = new ObjectOutputStream(new FileOutputStream("privkey.rsa"));
			privateKey.writeObject(dData);
			privateKey.writeObject(nData);
			privateKey.close();
		} catch(IOException exc) {
			System.out.println(exc.toString());
			System.exit(1);
		}
		System.out.println("Keys successfully generated");
	}

	private static boolean isValid(LargeInteger e, LargeInteger phiN, LargeInteger gcd) {
		LargeInteger diff = e.subtract(phiN);		
		LargeInteger otherDiff = gcd.subtract(ONE);
		return (diff.isNegative() && otherDiff.isZero());
			 // is e < phi(n)?  |  is GCD(phi(n), e) == 1 ?
	}

	private static LargeInteger findValidD(LargeInteger e, LargeInteger phiN) { // solve e*d = k*phi(n) + 1 for d, starting at k = 1
		LargeInteger rightHandSide = phiN.add(ONE);		// right hand side of the equation, i.e. k*phi(n) + 1
		LargeInteger testCase = rightHandSide.mod(e);

		while(!testCase.isZero()) {// i.e. while the right hand side is not a multiple of e. If it is a mutiple, then d is an integer
			rightHandSide = rightHandSide.add(phiN);
			testCase = rightHandSide.mod(e);
		}
		LargeInteger[] division = rightHandSide.divide(e);
		LargeInteger result = division[0];
		return result;
	}
}


