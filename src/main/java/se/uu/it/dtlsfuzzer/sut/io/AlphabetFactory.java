package se.uu.it.dtlsfuzzer.sut.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.automatalib.words.Alphabet;
import se.uu.it.dtlsfuzzer.config.DtlsFuzzerConfig;

public class AlphabetFactory {
	private static final Logger LOGGER = LogManager
			.getLogger(AlphabetFactory.class);

	public static final String DEFAULT_ALPHABET = "/default_alphabet.xml";

	public static Alphabet<TlsInput> buildAlphabet(DtlsFuzzerConfig config) {
		Alphabet<TlsInput> alphabet = null;
		if (config.getAlphabet() != null) {
			try {
				alphabet = AlphabetFactory.buildConfiguredAlphabet(config);
			} catch (JAXBException | IOException | XMLStreamException e) {
				LOGGER.fatal("Failed to instantiate alphabet");
				LOGGER.fatal(e.getMessage());
				System.exit(0);
			}
		} else {
			try {
				alphabet = AlphabetFactory.buildDefaultAlphabet();
			} catch (JAXBException | IOException | XMLStreamException e) {
				LOGGER.fatal("Failed to instantiate default alphabet");
				LOGGER.fatal(e.getMessage());
				System.exit(0);
			}
		}

		return alphabet;
	}

	public static File getAlphabetFile(DtlsFuzzerConfig config) {
		if (config.getAlphabet() != null) {
			return new File(config.getAlphabet());
		} else {
			return new File(AlphabetFactory.class.getResource(DEFAULT_ALPHABET)
					.getFile());
		}
	}

	public static Alphabet<TlsInput> buildDefaultAlphabet()
			throws JAXBException, IOException, XMLStreamException {
		return AlphabetSerializer.read(AlphabetFactory.class
				.getResourceAsStream(DEFAULT_ALPHABET));
	}

	public static Alphabet<TlsInput> buildConfiguredAlphabet(
			DtlsFuzzerConfig config) throws FileNotFoundException,
			JAXBException, IOException, XMLStreamException {
		Alphabet<TlsInput> alphabet = null;
		if (config.getAlphabet() != null) {
			alphabet = AlphabetSerializer.read(new FileInputStream(config
					.getAlphabet()));
		}
		return alphabet;
	}

}
