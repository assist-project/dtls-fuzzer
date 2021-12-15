package se.uu.it.dtlsfuzzer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import com.google.common.base.Strings;

public abstract class ExportableResult {
	
	private static String SECTION_DELIM = Strings.repeat("-", 80);
	private static String TITLE_DELIM = Strings.repeat("=", 80);

	public void export(Writer writer) {
		PrintWriter pw = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter(writer);
		doExport(pw);
		pw.close();
	}
	
	protected abstract void doExport(PrintWriter pw);
	
	protected void subsection(String subsection, PrintWriter pw) {
		pw.println();
		pw.println("== " + subsection + " ==");
		pw.println();
	}
	
	protected void section(String section, PrintWriter pw) {
		pw.println();
		pw.println(SECTION_DELIM);
		pw.println(section);
		pw.println(SECTION_DELIM);
		pw.println();
	}
	
	protected void title(String title, PrintWriter pw) {
		pw.println();
		pw.println(TITLE_DELIM);
		pw.println(title);
		pw.println(TITLE_DELIM);
		pw.println();
	}
	
	public String exportToString() {
		StringWriter sw = new StringWriter();
		export(sw);
		return sw.toString();
	}
}
