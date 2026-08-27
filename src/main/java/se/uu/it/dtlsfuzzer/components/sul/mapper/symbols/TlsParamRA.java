package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols;

import de.learnlib.ralib.data.DataType;

public enum TlsParamRA {
    EPOCH_I(new DataType("epoch_i")), EPOCH_O(new DataType("epoch_o"));

    private final DataType dataType;

    TlsParamRA(DataType dataType) {
        this.dataType = dataType;
    }

    public DataType getDataType() {
        return dataType;
    }
}
