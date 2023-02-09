package se.uu.it.dtlsfuzzer.sut.input.xml;

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
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ExtensionMessage;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;

public class AlphabetSerializer {
	private static JAXBContext context;

	private static synchronized JAXBContext getJAXBContext()
			throws JAXBException, IOException {
		if (context == null) {
			context = JAXBContext.newInstance(Alphabet.class,
					TlsInput.class, ExtensionMessage.class,
					TlsMessage.class, ModificationFilter.class,
					VariableModification.class, ModifiableVariable.class);
		}
		return context;
	}

	public static net.automatalib.words.Alphabet<TlsInput> read(InputStream alphabetStream)
			throws JAXBException, IOException, XMLStreamException {
		Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
		XMLInputFactory xif = XMLInputFactory.newFactory();
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		XMLStreamReader xsr = xif.createXMLStreamReader(new InputStreamReader(
				alphabetStream));
		Alphabet xmlAlphabet = (Alphabet) unmarshaller.unmarshal(xsr);
		return new ListAlphabet<TlsInput>(xmlAlphabet.getWords());
	}

	public static void write(OutputStream alphabetStream,
	        net.automatalib.words.Alphabet<TlsInput> alphabet) throws JAXBException, IOException {
		Marshaller m = getJAXBContext().createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		Alphabet xmlAlphabet = new Alphabet(new ArrayList<>(alphabet));
		m.marshal(xmlAlphabet, alphabetStream);
	}
}
