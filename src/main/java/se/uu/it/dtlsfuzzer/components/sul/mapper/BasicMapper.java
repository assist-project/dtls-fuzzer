package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputMapper;

/*
 * Basically, it is a redundant class, because it is equivalent to:
 *
 * new MapperComposer(
 *     new BasicInputMapper(config, new ModelOutputs()),
 *     new OutpuMapper(config)
 * )
 */
public class BasicMapper extends MapperComposer {
    public BasicMapper(MapperConfig config) {
        super(
            new BasicInputMapper(config, new TlsOutputChecker()),
            new TlsOutputMapper(config)
        );
    }
}
