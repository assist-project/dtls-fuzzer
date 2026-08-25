package se.uu.it.dtlsfuzzer;

import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import de.rub.nds.tlsattacker.core.util.ProviderUtil;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;

public class MainRA {

    public static void main(String[] args) {
        ProviderUtil.addBouncyCastleProvider();
        MultiBuilderRA mb = new MultiBuilderRA();
        String[] parentLoggers = { Main.class.getPackageName() };

        RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance> a;
        CommandLineParser<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> commandLineParser =
                new CommandLineParser<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>>(mb, null, mb, null, mb, mb);

        commandLineParser.setExternalParentLoggers(parentLoggers);

        commandLineParser.process(args, true);
    }
}
