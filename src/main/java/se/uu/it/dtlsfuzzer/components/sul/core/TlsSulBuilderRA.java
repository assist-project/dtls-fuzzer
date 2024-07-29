package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import de.learnlib.ralib.words.PSymbolInstance;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsInputMapperRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsMapperComposerRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsOutputMapperRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilderRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputCheckerRA;

public class TlsSulBuilderRA implements SulBuilder<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> {

    private TlsSulAdapter sulAdapter = null;

    public TlsSulBuilderRA() {
    }

    @Override
    public TlsSulRA build(SulConfig sulConfig,
            com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks cleanupTasks) {

        TlsOutputBuilderRA outputBuilder = new TlsOutputBuilderRA();
        TlsOutputCheckerRA outputChecker = new TlsOutputCheckerRA();

        DtlsOutputMapperRA outputMapper = new DtlsOutputMapperRA(sulConfig.getMapperConfig(), outputBuilder);
        DtlsInputMapperRA inputMapper = new DtlsInputMapperRA(sulConfig.getMapperConfig(), outputChecker);

        DtlsMapperComposerRA mapperComposer = new DtlsMapperComposerRA(inputMapper, outputMapper);

        TlsSulRA tlsSul = new TlsSulRA((TlsSulConfig) sulConfig, sulConfig.getMapperConfig(), mapperComposer,
                cleanupTasks);

        if (sulConfig.getSulAdapterConfig().getAdapterPort() != null) {
            if (sulAdapter == null) {
                sulAdapter = new TlsSulAdapter(sulConfig.getSulAdapterConfig(), cleanupTasks,
                        sulConfig.isFuzzingClient());
            }
            tlsSul.setSulAdapter(sulAdapter);
        }
        return tlsSul;
    }
}
