package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

/**
 * The mapper component is responsible with executing an input.
 * Given an input symbol, the mapper should:
 * <ol>
 * 	<li> generate a corresponding packet </li>
 * 	<li> send it to the SUT </li>
 * 	<li> receive the response </li>
 * 	<li> convert it into an appropriate response </li>
 * </ol>
 *
 */
public interface Mapper {
    public TlsOutput execute(TlsInput input, State state, ExecutionContext context);
}
