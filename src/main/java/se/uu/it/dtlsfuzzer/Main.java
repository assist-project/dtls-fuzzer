package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import java.io.IOException;
import java.security.Security;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    private static String ARGS_FILE = "command.args";


    public static void main(String args[]) throws IOException, JAXBException, XMLStreamException {
        Security.addProvider(new BouncyCastleProvider());
        MultiBuilder mb = new MultiBuilder();
        CommandLineParser parser = new CommandLineParser(mb, mb, mb, mb, new String [] { Main.class.getPackageName()});
        parser.parse(args);
    }
}
