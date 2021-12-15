package se.uu.it.dtlsfuzzer.mapper;

/*
 * The phases involved in executing an input
 */
public enum Phase {
	MESSAGE_GENERATION,
	MESSAGE_PREPARATION,
	FRAGMENT_GENERATION,
	RECORD_GENERATION,
	RECORD_PREPARATION,
	RECORD_SENDING,
	OUTPUT_RECEIVE
}
