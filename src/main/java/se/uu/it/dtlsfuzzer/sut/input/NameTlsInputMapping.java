package se.uu.it.dtlsfuzzer.sut.input;

import java.util.Collection;
import java.util.LinkedHashMap;

public class NameTlsInputMapping extends LinkedHashMap<String, TlsInput>{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NameTlsInputMapping(Collection<TlsInput> inputs) {
        super();
        inputs.forEach(i -> put(i.name(), i));
    }

    public TlsInput getInput(String name) {
        return get(name);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InputMapping: \n");
        builder.append(super.toString());
        return builder.toString();
    }
}
