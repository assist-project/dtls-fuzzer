package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import de.rub.nds.tlsattacker.core.util.ProviderUtil;

public class MainRA {

    public static void main(String[] args) {
        ProviderUtil.addBouncyCastleProvider();
        MultiBuilderRA mb = new MultiBuilderRA();
        String[] parentLoggers = { Main.class.getPackageName() };

        CommandLineParser<?> commandLineParser = new CommandLineParser<>(
            mb,
            mb,
            mb,
            mb
        );
        commandLineParser.setExternalParentLoggers(parentLoggers);

        commandLineParser.parse(args, true);
    }
}
