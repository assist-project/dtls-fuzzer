package de.rub.nds.modelfuzzer.sut;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import de.rub.nds.modifiablevariable.ModifiableVariable;
import de.rub.nds.tlsattacker.core.protocol.ModifiableVariableHolder;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.GenericReceiveAction;
import de.rub.nds.tlsattacker.core.workflow.action.executor.SendMessageHelper;
import de.rub.nds.tlsattacker.transport.exception.InvalidTransportHandlerStateException;
import de.rub.nds.tlsattacker.transport.socket.SocketState;
import de.rub.nds.tlsattacker.transport.tcp.ClientTcpTransportHandler;

public class InputExecutor {
	
	public TlsOutput execute(TlsInput input, State state) {
		ProtocolMessage message = input.generateMessage(state);
		sendMessage(message, state);
		input.postUpdate(null, state);
		TlsOutput output = readOutput(state);
		return output;
	}
	
    protected void sendMessage(ProtocolMessage message, State state) {
    	Record record = new Record();
        List<ProtocolMessage> messages = new LinkedList<>();
        List<AbstractRecord> records = new LinkedList<>();
        records.add(record);
        messages.add(message);
        stripFields(message);
        SendMessageHelper helper = new SendMessageHelper();
        try {
            helper.sendMessages(messages, records, state.getTlsContext());
        } catch (IOException ex) {
            try {
                state.getTlsContext().getTransportHandler().closeConnection();
            } catch (IOException E) {
                E.printStackTrace();
            }
        }
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

    protected TlsOutput readOutput(State state) {
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
    	
    	return new TlsOutput(action.getReceivedMessages(),
    			action.getReceivedRecords(),
    			extractSocketState(state)
    			);
    			
    }
    
    private SocketState extractSocketState(State state) {
        try {
            if (state.getTlsContext().getTransportHandler() instanceof ClientTcpTransportHandler) {
                SocketState socketState = (((ClientTcpTransportHandler) (state.getTlsContext().getTransportHandler()))
                        .getSocketState());
                return socketState;
            } else {
                return null;
            }
        } catch (InvalidTransportHandlerStateException ex) {
            return null;
        }
    }
}
