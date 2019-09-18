import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.MessageDigest;

public class RsaSign {
	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("You must specify an operating mode and a file to act on");
			System.exit(0);
		} else {
			if(args[0].equals("s")) {
				sign(args[1]);
			} else if(args[0].equals("v")) {
				verify(args[1]);
			} else {
				System.out.println("Invalid operating mode provided\nIndicate 's' to sign a file or 'v' to verify a signed file");
				System.exit(1);
			}
		}
	}

	private static void sign(String file) {
		byte[] hash = hash(file);

		try {
			ObjectInputStream privKey = new ObjectInputStream(new FileInputStream("privkey.rsa"));

			byte[] dData = (byte[]) privKey.readObject();
			byte[] nData = (byte[]) privKey.readObject();
			privKey.close();

			LargeInteger d = new LargeInteger(dData);
			LargeInteger n = new LargeInteger(nData);
			LargeInteger hashedMessage = new LargeInteger(hash);

			LargeInteger decryption = hashedMessage.modularExp(d, n);
			byte[] signature = decryption.getVal();

			ObjectOutputStream sigFile = new ObjectOutputStream(new FileOutputStream(file + ".sig"));
			sigFile.writeObject(signature);
			sigFile.close();
		} catch(Exception e) {
			System.out.println(e.toString());
			System.exit(1);
		}
		System.out.println("File signed successfully");
	}

	private static void verify(String file) {
		byte[] hash = hash(file);
		byte[] signature = null;
		LargeInteger e = null;
		LargeInteger n = null;
		ObjectInputStream sigFile = null;

		try {
			sigFile = new ObjectInputStream(new FileInputStream(file + ".sig"));
		} catch(IOException exc) {
			System.out.println("Could not find corresponding .sig file");
			System.exit(1);
		}

		try {
			signature = (byte[]) sigFile.readObject();
			sigFile.close();

			ObjectInputStream publicKey = new ObjectInputStream(new FileInputStream("pubkey.rsa"));

			byte[] eData = (byte[]) publicKey.readObject();
			byte[] nData = (byte[]) publicKey.readObject();
			publicKey.close();

			e = new LargeInteger(eData);
			n = new LargeInteger(nData);
		} catch (Exception exc) {
			System.out.println(exc.toString());
			System.exit(1);
		}

		LargeInteger encryption = new LargeInteger(signature);
		encryption = encryption.modularExp(e, n);
		LargeInteger hashFromFile = new LargeInteger(hash);
		LargeInteger diff = hashFromFile.subtract(encryption);

		if(diff.isZero()) {
			System.out.println("The signature was valid");
		} else {
			System.out.println("The signature was invalid");
		}
	}

	private static byte[] hash(String file) {
		byte[] digest = null;
		try {
			Path path = Paths.get(file);
			byte[] data = Files.readAllBytes(path);

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(data);

			digest = md.digest();
		} catch(Exception e) {
			System.out.println(e.toString());
			System.exit(1);
		}
		return digest;
	}
}