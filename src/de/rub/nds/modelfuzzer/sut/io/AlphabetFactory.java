package de.rub.nds.modelfuzzer.sut.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import de.rub.nds.modelfuzzer.config.ModelBasedTesterConfig;
import net.automatalib.words.Alphabet;

public class AlphabetFactory {
	
	public static final String DEFAULT_ALPHABET = "/default_alphabet.xml";
	
	public static Alphabet<TlsInput> buildConfiguredAlphabet(ModelBasedTesterConfig config) throws FileNotFoundException, JAXBException, IOException, XMLStreamException {
		Alphabet<TlsInput> alphabet = null;
		if (config.getAlphabet() != null) {
			alphabet = AlphabetSerializer.read(new FileInputStream(config.getAlphabet()));
		} 
		return alphabet;
	}
	
	public static Alphabet<TlsInput> buildDefaultAlphabet() throws JAXBException, IOException, XMLStreamException {
		return AlphabetSerializer.read(AlphabetFactory.class.getResourceAsStream(DEFAULT_ALPHABET));
	}
}
