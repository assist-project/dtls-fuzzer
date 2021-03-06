package se.uu.it.dtlsfuzzer.sut;

import java.io.IOException;
import java.io.OutputStream;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import se.uu.it.dtlsfuzzer.config.SulDelegate;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

// Included is the fun option to store the output of a process in the output generated. 
public class TlsProcessWrapper extends SulProcessWrapper<TlsInput, TlsOutput> {

	private StringOutputStream procOut;
	private boolean storeApplicationOutput;

	public TlsProcessWrapper(SUL<TlsInput, TlsOutput> sul,
			SulDelegate sulDelegate) {
		super(sul, sulDelegate);
		this.storeApplicationOutput = sulDelegate.isWithApplicationOutput();
		if (storeApplicationOutput) {
			procOut = new StringOutputStream();
			handler.redirectError(procOut);
			handler.redirectOutput(procOut);
		}
	}

	public void pre() {
		super.pre();
		if (storeApplicationOutput)
			procOut.clear();
	}

	@Override
	public TlsOutput step(TlsInput in) throws SULException {
		TlsOutput output = super.step(in);
		output.setAlive(super.isAlive());
		if (storeApplicationOutput) {
			String procOutput = procOut.getString();
			output.setApplicationOutput(procOutput);
			procOut.clear();
		}
		return output;
	}

	class StringOutputStream extends OutputStream {

		private StringBuilder mBuf;

		StringOutputStream() {
			mBuf = new StringBuilder();
		}

		public String getString() {
			return mBuf.toString();
		}

		@Override
		public void write(int arg0) throws IOException {
			mBuf.append((char) arg0);
		}

		public void clear() {
			mBuf = new StringBuilder();
		}

	}
}
