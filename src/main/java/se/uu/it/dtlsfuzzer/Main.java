package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTestResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DifferentialReport;
import de.rub.nds.tlsattacker.core.util.ProviderUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class Main {

    public static void main(String args[]) throws IOException, XMLStreamException {
        ProviderUtil.addBouncyCastleProvider();
        MultiBuilder mb = new MultiBuilder();
        // String[] parentLoggers = {Main.class.getPackageName()};

        CommandLineParser<MealyMachineWrapper<TlsInput, TlsOutput>> commandLineParser = new CommandLineParser<MealyMachineWrapper<TlsInput, TlsOutput>>(mb, mb, mb, mb, mb, mb);
        // commandLineParser.setExternalParentLoggers(parentLoggers);

        List<ProcessResult<MealyMachineWrapper<TlsInput, TlsOutput>>> processResult = commandLineParser.process(args);
        for (ProcessResult<MealyMachineWrapper<TlsInput, TlsOutput>> result : processResult) {
            if (result.hasDiffTestResult() && !result.getDiffTestResult().isEmpty()) {
                DiffTestResult diffTestResult = result.getDiffTestResult();

                String outputDir = "output";
                String filename = "diff_" + diffTestResult.getModelAName() + "_vs_" + diffTestResult.getModelBName() + ".txt";
                Path outputPath = Paths.get(outputDir, filename);
                Path parent = outputPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                DifferentialReport<String, String> reportGenerator = new DifferentialReport<>(null, outputPath);
                reportGenerator.writeReport(diffTestResult.getDivergences(), diffTestResult.getModelAName() , diffTestResult.getModelBName());
            }
        }
    }
}
