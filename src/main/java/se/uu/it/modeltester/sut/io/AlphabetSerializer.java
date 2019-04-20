package se.uu.it.modeltester.sut.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

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
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;

public class AlphabetSerializer {
	private static JAXBContext context;

	private static synchronized JAXBContext getJAXBContext()
			throws JAXBException, IOException {
		if (context == null) {
			context = JAXBContext.newInstance(AlphabetPojo.class,
					TlsInput.class, ExtensionMessage.class,
					ProtocolMessage.class, ModificationFilter.class,
					VariableModification.class, ModifiableVariable.class);
		}
		return context;
	}

	public static Alphabet<TlsInput> read(InputStream alphabetStream)
			throws JAXBException, IOException, XMLStreamException {
		Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
		XMLInputFactory xif = XMLInputFactory.newFactory();
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		XMLStreamReader xsr = xif.createXMLStreamReader(new InputStreamReader(
				alphabetStream));
		AlphabetPojo alphabetPojo = (AlphabetPojo) unmarshaller.unmarshal(xsr);
		return new ListAlphabet<TlsInput>(alphabetPojo.getWords());
	}

	public static void write(OutputStream alphabetStream,
			Alphabet<TlsInput> alphabet) throws JAXBException, IOException {
		Marshaller m =  getJAXBContext().createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		AlphabetPojo alphabetPojo = new AlphabetPojo(new ArrayList<>(alphabet));
		m.marshal(alphabetPojo, alphabetStream);
	}
}
