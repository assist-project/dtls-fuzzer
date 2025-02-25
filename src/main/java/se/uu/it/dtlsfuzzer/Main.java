package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import de.rub.nds.tlsattacker.core.util.ProviderUtil;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

public class Main {

    public static void main(String args[]) throws IOException, XMLStreamException {
        ProviderUtil.addBouncyCastleProvider();
        MultiBuilder mb = new MultiBuilder();
        // String[] parentLoggers = {Main.class.getPackageName()};

        CommandLineParser commandLineParser = new CommandLineParser(mb, mb, mb, mb);
        // commandLineParser.setExternalParentLoggers(parentLoggers);
        commandLineParser.parse(args);
    }
}
