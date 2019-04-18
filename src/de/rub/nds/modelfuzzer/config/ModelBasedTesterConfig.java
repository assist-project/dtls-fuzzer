package de.rub.nds.modelfuzzer.config;


import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import de.rub.nds.modelfuzzer.sut.io.TlsSymbol;
import de.rub.nds.tlsattacker.core.config.TLSDelegateConfig;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;

public class ModelBasedTesterConfig extends TLSDelegateConfig{
	
	@Parameter(names = "-specification", required = false, description = "A model of the specification. For examples, look at './models/'. "
			+ "If no specification is given, active learning is run with the provided alphabet")
    private String specification = null;
	
	@Parameter(names = "-onlyLearn", required = false, description = "Only generates a model of the specification. Does not do conformance testing")
	private boolean onlyLearn = false; 
	
	@Parameter(names = "-inputs", required = false, description = "A list of comma separated input names from which a alphabet is constructed. "
    		+ "If given, fuzzing/learning will be restricted to these inputs. "
    		+ "Otherwise fuzzing will be applied to all inputs in the specification, while learning will be done on a set of pre-selected inputs. "
    		+ "The definitions for the input names can be provided, otherwise they are taken from a default .xml file.")
    private List<String> inputs = null;
	
	@Parameter(names = "-inputDefinitions", required = false, description = "An .xml file containing the definitions for each input name ")
    private String inputDefinitions = null;
	
	@Parameter(names = "-alphabet", required = false, description = "An .xml file with a custom alphabet. "
    		+ "If given, fuzzing/learning will be restricted to these inputs. Otherwise fuzzing will be applied to all inputs in the specification, while " 
    		+ "learning will be done on a set of pre-selected inputs. " )
	private String alphabet = null;

	@Parameter(names = "-output", required = false, description = "The file in which results should be saved")
    private String output = "output";
	
	@Parameter(names = "-bound", required = false, description = "An optional bound on the total number of tests")
    private Integer bound = null;
	
	@Parameter(names = "-exhaustive", required = false, arity=0, description = "If provided, testing a state is performed for all suffixes, "
			+ "and is not stopped once non-conformance is detected in a suffix")
	private Boolean exhaustive = Boolean.FALSE;
	
	@ParametersDelegate
    private SulDelegate sulDelegate;
	
	@ParametersDelegate 
	private LearningConfig learningConfig;
    
    public ModelBasedTesterConfig(GeneralDelegate delegate) {
        super(delegate);
        sulDelegate = new SulDelegate();
        addDelegate(sulDelegate);
        learningConfig = new LearningConfig();
    }

    public SulDelegate getSulDelegate() {
        return sulDelegate;
    }
    
    public LearningConfig getLearningConfig() {
    	return learningConfig;
    }
    
    public String getSpecification() {
    	return specification;
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

	public String getAlphabet() {
		return alphabet;
	}
	
	public boolean isOnlyLearn() {
		return onlyLearn;
	}
}
