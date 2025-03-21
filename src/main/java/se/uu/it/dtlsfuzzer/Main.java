package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import de.rub.nds.tlsattacker.core.util.ProviderUtil;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class Main {

    public static void main(String args[]) throws IOException, XMLStreamException {
        ProviderUtil.addBouncyCastleProvider();
        MultiBuilder mb = new MultiBuilder();
        // String[] parentLoggers = {Main.class.getPackageName()};

        CommandLineParser<MealyMachineWrapper<TlsInput, TlsOutput>> commandLineParser = new CommandLineParser<>(mb, mb, mb, mb);
        // commandLineParser.setExternalParentLoggers(parentLoggers);
        commandLineParser.parse(args);
    }
}
