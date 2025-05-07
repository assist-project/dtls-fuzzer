package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.OutputMapperRA;
import de.learnlib.ralib.words.PSymbolInstance;
import java.util.List;

public class MockOutputMapperRA extends OutputMapperRA<PSymbolInstance, TlsProtocolMessage, TlsExecutionContextRA> {

    public MockOutputMapperRA(MapperConfig mapperConfig, OutputBuilder<PSymbolInstance> outputBuilder) {
        super(mapperConfig, outputBuilder);
    }

    @Override
    public PSymbolInstance receiveOutput(TlsExecutionContextRA context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'receiveOutput'");
    }

    @Override
    protected PSymbolInstance buildOutput(String name, List<TlsProtocolMessage> messages) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildOutput'");
    }

}
