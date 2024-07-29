package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;

import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;

import java.io.IOException;
import java.security.Security;
import javax.xml.stream.XMLStreamException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MainRA {

    public static void main(String args[]) throws IOException, XMLStreamException {
        Security.addProvider(new BouncyCastleProvider());
        MultiBuilder mb = new MultiBuilder();
        // String[] parentLoggers = {Main.class.getPackageName()};

        CommandLineParser<?> commandLineParser = new CommandLineParser<>(mb, mb, mb, mb);
        // commandLineParser.setExternalParentLoggers(parentLoggers);
        commandLineParser.parse(args);
    }
}
