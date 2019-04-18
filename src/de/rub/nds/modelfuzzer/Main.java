package de.rub.nds.modelfuzzer;

import java.security.Security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.rub.nds.modelfuzzer.config.ModelBasedTesterConfig;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import de.rub.nds.tlsattacker.util.UnlimitedStrengthEnabler;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    public static void main(String args[]) {
        UnlimitedStrengthEnabler.enable();
        Security.addProvider(new BouncyCastleProvider());
        ModelBasedTesterConfig config = new ModelBasedTesterConfig(new GeneralDelegate());
        JCommander commander = new JCommander(config);
        try {
            commander.parse(args);
            if (config.getGeneralDelegate().isHelp()) {
                commander.usage();
                return;
            }
            
            try {
            	ModelBasedTester tester = new ModelBasedTester(config);
                tester.startTesting();
            } catch (Exception E) {
                LOGGER.error("Encountered an exception. See debug for more info.");
                E.printStackTrace();
                //TODO ^^ what says here :)
                LOGGER.error(E);
            }
        } catch (ParameterException E) {
            LOGGER.error("Could not parse provided parameters. " + E.getLocalizedMessage());
            LOGGER.debug(E);
            commander.usage();
        }
    }
}
