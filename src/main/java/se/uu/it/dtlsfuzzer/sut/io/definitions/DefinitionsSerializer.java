package se.uu.it.dtlsfuzzer.sut.io.definitions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.rub.nds.modifiablevariable.ModifiableVariable;
import de.rub.nds.modifiablevariable.ModificationFilter;
import de.rub.nds.modifiablevariable.VariableModification;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ExtensionMessage;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;

public class DefinitionsSerializer {
	private static JAXBContext context;

	private static synchronized JAXBContext getJAXBContext()
			throws JAXBException, IOException {
		if (context == null) {
			context = JAXBContext.newInstance(Definitions.class,
					InputDefinition.class, TlsInput.class,
					ExtensionMessage.class, ProtocolMessage.class,
					ModificationFilter.class, VariableModification.class,
					ModifiableVariable.class);
		}
		return context;
	}

	public static Definitions read(InputStream alphabetStream)
			throws JAXBException, IOException, XMLStreamException {
		Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
		XMLInputFactory xif = XMLInputFactory.newFactory();
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		XMLStreamReader xsr = xif.createXMLStreamReader(new InputStreamReader(
				alphabetStream));
		Definitions definitions = (Definitions) unmarshaller.unmarshal(xsr);
		return definitions;
	}

	public static void write(OutputStream alphabetStream,
			Definitions definitions) throws JAXBException, IOException {
		Marshaller m = getJAXBContext().createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(definitions, alphabetStream);
	}
}
