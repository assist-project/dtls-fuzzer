package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import java.io.IOException;
import java.security.Security;
import javax.xml.stream.XMLStreamException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Main {

    public static void main(String args[]) throws IOException, XMLStreamException {
        Security.addProvider(new BouncyCastleProvider());
        MultiBuilder mb = new MultiBuilder();
        String[] parentLoggers = {Main.class.getPackageName()};

        CommandLineParser commandLineParser = new CommandLineParser(mb, mb, mb, mb);
        commandLineParser.setExternalParentLoggers(parentLoggers);
        commandLineParser.parse(args);
    }
}
