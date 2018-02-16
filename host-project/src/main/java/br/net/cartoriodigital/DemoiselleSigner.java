package br.net.cartoriodigital;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.demoiselle.signer.chain.icp.brasil.provider.impl.ICPBrasilUserHomeProviderCA;
import org.demoiselle.signer.core.keystore.loader.KeyStoreLoader;
import org.demoiselle.signer.core.keystore.loader.factory.KeyStoreLoaderFactory;
import org.demoiselle.signer.policy.engine.factory.PolicyFactory.Policies;
import org.demoiselle.signer.policy.impl.cades.SignerAlgorithmEnum;
import org.demoiselle.signer.policy.impl.cades.factory.PKCS7Factory;
import org.demoiselle.signer.policy.impl.cades.pkcs7.PKCS7Signer;

public class DemoiselleSigner {

	private static final Logger LOGGER = Logger.getLogger(DemoiselleSigner.class.getName());

	public static KeyStoreLoader keyStoreLoader;
	public static KeyStore keyStore;
	public static String alias;
	public static X509Certificate certificate;
	public static PrivateKey privateKey;
	// public static PinHandler pinHandler;
	public static String password;
	public static Certificate[] certificateChain;

	public static void sign(String text, Path pathP7s, Policies police, SignerAlgorithmEnum algorithm)
			throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException {

		LOGGER.log(Level.INFO, "===== " + police.toString() + " =====");

		byte[] content = text.getBytes();

		PKCS7Signer signer = PKCS7Factory.getInstance().factoryDefault();

		// if (pinHandler == null) {
		// pinHandler = new PinHandler("Ações");
		// }

		if (keyStoreLoader == null) {
			keyStoreLoader = KeyStoreLoaderFactory.factoryKeyStoreLoader();
			// keyStoreLoader.setCallbackHandler(pinHandler);
			keyStore = keyStoreLoader.getKeyStore();
			alias = keyStore.aliases().nextElement();
			certificate = (X509Certificate) keyStore.getCertificate(alias);
			privateKey = (PrivateKey) keyStore.getKey(alias, null);
			certificateChain = keyStore.getCertificateChain(alias);
		}

		signer.setCertificates(certificateChain);
		signer.setPrivateKey(privateKey);

		signer.setSignaturePolicy(police);
		signer.setAlgorithm(algorithm);

		byte[] sign = signer.doAttachedSign(content);

		System.out.println(">>>>>>>>>> Validando a assinatura");

		// Valida
		signer.checkAttachedSignature(sign);

		ByteArrayInputStream bis = new ByteArrayInputStream(sign);

		Files.copy(bis, pathP7s, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

	}

}
