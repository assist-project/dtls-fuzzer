package se.uu.it.jsse.examples.dtlsclientserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {

    /**
     * Creates a DTLS server or client.
     * 
     */
    public static void main(String args[])
    {
    	DtlsClientServerConfig config = new DtlsClientServerConfig();
    	JCommander commander = new JCommander(config);
    	ThreadStarter ts = null;
    	try {
    		commander.parse(args);


        	sslContext = getDTLSContext(config);
            
        	if (config.getThreadStarterIpPort() == null) {
                DtlsClientServer dtlsHarness = new DtlsClientServer(config, sslContext);
	        	dtlsHarness.run();
        	} else {
                // the server port is dynamically allocated in this case
        		if (!config.isClient()) {
        			config.setPort(0);
        		}
                ts = new ThreadStarter(config, sslContext);
        		ts.run();
        	}
        	
    	} catch(ParameterException e) {
			System.out.println("Could not parse provided parameters. " + e.getLocalizedMessage());
			commander.usage();
			return;
		} catch(Exception e) {
			System.out.println("Exception encountered");
			System.out.println(e.getLocalizedMessage());
			if (ts != null) {
				try {
					ts.close();
				} catch (IOException e1) {
				}
			}
		}
    }
	
	private static SSLContext sslContext;
	
	// get DTSL context
	static SSLContext getDTLSContext(DtlsClientServerConfig config) throws GeneralSecurityException, IOException {
		KeyStore ks = KeyStore.getInstance("JKS");
		KeyStore ts = KeyStore.getInstance("JKS");

		try (FileInputStream fis = new FileInputStream(config.getKeyLocation())) {
			ks.load(fis, config.getKeyPassword().toCharArray());
		}

		try (FileInputStream fis = new FileInputStream(config.getTrustLocation())) {
			ts.load(fis, config.getTrustPassword().toCharArray());
		}

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, config.getKeyPassword().toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ts);

		SSLContext sslCtx = SSLContext.getInstance("DTLS");
		sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		return sslCtx;
	}
	
}
