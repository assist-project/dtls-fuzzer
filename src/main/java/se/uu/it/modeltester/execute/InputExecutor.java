package se.uu.it.modeltester.execute;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import de.rub.nds.modifiablevariable.ModifiableVariable;
import de.rub.nds.tlsattacker.core.protocol.ModifiableVariableHolder;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.GenericReceiveAction;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public abstract class InputExecutor {
	public TlsOutput execute(TlsInput input, State state) {
		ProtocolMessage message = input.generateMessage(state);
//		stripFields(message);
		sendMessage(message, state);
		input.preUpdate(state);
		TlsOutput output = receiveOutput(state);
		input.postUpdate(output, state);
		return output;
	}
	
	protected abstract void sendMessage(ProtocolMessage message, State state);
	
    protected TlsOutput receiveOutput(State state) {
        try {
            if (state.getTlsContext().getTransportHandler().isClosed()) {
                return TlsOutput.socketClosed();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return TlsOutput.socketClosed();
        }
        try {
            GenericReceiveAction action = new GenericReceiveAction("client");
            action.execute(state);
            
            return extractOutput(state, action);
        } catch (Exception E) {
            E.printStackTrace();
            return TlsOutput.socketClosed();
        }
    }
    
    private TlsOutput extractOutput(State state, GenericReceiveAction action) {
    	return new TlsOutput(action.getReceivedMessages());
    }
    

	protected void stripFields(ProtocolMessage message) {
        List<ModifiableVariableHolder> holders = new LinkedList<>();
        holders.addAll(message.getAllModifiableVariableHolders());
        for (ModifiableVariableHolder holder : holders) {
            List<Field> fields = holder.getAllModifiableVariableFields();
            for (Field f : fields) {
                f.setAccessible(true);

                ModifiableVariable mv = null;
                try {
                    mv = (ModifiableVariable) f.get(holder);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
                if (mv != null) {
                    if (mv.getModification() != null || mv.isCreateRandomModification()) {
                        mv.setOriginalValue(null);
                    } else {
                        try {
                            f.set(holder, null);
                        } catch (IllegalArgumentException | IllegalAccessException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
