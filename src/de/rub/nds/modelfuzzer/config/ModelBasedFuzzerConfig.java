package de.rub.nds.modelfuzzer.config;


import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import de.rub.nds.modelfuzzer.sut.io.TlsSymbol;
import de.rub.nds.tlsattacker.core.config.TLSDelegateConfig;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;

public class ModelBasedFuzzerConfig extends TLSDelegateConfig{
	
	@Parameter(names = "-specification", required = true, description = "A model of the specification. For examples, look at './models/'")
    private String specification;
	
	@Parameter(names = "-alphabet", required = false, description = "A list of comma separated strings from which a custom alphabet is constructed. "
    		+ "If given, fuzzing will be restricted to these inputs, otherwise it will be applied to all inputs in the specification. ")
    private List<TlsSymbol> alphabet = null;
	
	@Parameter(names = "-output", required = false, description = "The file in which results should be saved")
    private String output;
	
	@Parameter(names = "-bound", required = false, description = "An optional bound on the total number of tests")
    private Integer bound = null;
	
	@Parameter(names = "-exhaustive", required = false, arity=0, description = "If provided, fuzzing a state is performed for all suffixes, "
			+ "and is not stopped once non-conformance is detected in a suffix")
	private Boolean exhaustive = Boolean.FALSE;
	
	@ParametersDelegate
    private SulDelegate sulDelegate;
    
    public ModelBasedFuzzerConfig(GeneralDelegate delegate) {
        super(delegate);
        sulDelegate = new SulDelegate();
        addDelegate(sulDelegate);
    }

    public SulDelegate getSulDelegate() {
        return sulDelegate;
    }
    
    public String getSpecification() {
    	return specification;
    }
    
    public List<TlsSymbol> getAlphabet() {
    	return alphabet;
    }
    
    public String getOutput() {
    	return output;
    }
    
    public Integer getBound() {
    	return bound;
    }
    
    public Boolean isExhaustive() {
    	return exhaustive;
    }
}
