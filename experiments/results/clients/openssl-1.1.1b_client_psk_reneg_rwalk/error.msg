null
Non-determinism detected
full input:
PSK_SERVER_HELLO SERVER_HELLO_DONE CHANGE_CIPHER_SPEC FINISHED HELLO_VERIFY_REQUEST HELLO_REQUEST HELLO_VERIFY_REQUEST HELLO_VERIFY_REQUEST FINISHED PSK_SERVER_HELLO SERVER_HELLO_DONE CHANGE_CIPHER_SPEC FINISHED PSK_SERVER_HELLO SERVER_HELLO_DONE HELLO_REQUEST PSK_SERVER_HELLO HELLO_VERIFY_REQUEST PSK_SERVER_HELLO HELLO_VERIFY_REQUEST HELLO_VERIFY_REQUEST HELLO_VERIFY_REQUEST HELLO_VERIFY_REQUEST
full new output:
CLIENT_HELLO PSK_CLIENT_KEY_EXCHANGE|CHANGE_CIPHER_SPEC|FINISHED TIMEOUT TIMEOUT TIMEOUT CLIENT_HELLO CLIENT_HELLO CLIENT_HELLO+ TIMEOUT TIMEOUT PSK_CLIENT_KEY_EXCHANGE|CHANGE_CIPHER_SPEC|FINISHED TIMEOUT TIMEOUT TIMEOUT TIMEOUT CLIENT_HELLO TIMEOUT CLIENT_HELLO TIMEOUT CLIENT_HELLO CLIENT_HELLO CLIENT_HELLO|PSK_CLIENT_KEY_EXCHANGE|CHANGE_CIPHER_SPEC|UNKNOWN_MESSAGE TIMEOUT
old output:
CLIENT_HELLO PSK_CLIENT_KEY_EXCHANGE|CHANGE_CIPHER_SPEC|FINISHED TIMEOUT TIMEOUT TIMEOUT CLIENT_HELLO CLIENT_HELLO CLIENT_HELLO+ TIMEOUT TIMEOUT PSK_CLIENT_KEY_EXCHANGE|CHANGE_CIPHER_SPEC|FINISHED TIMEOUT TIMEOUT TIMEOUT TIMEOUT CLIENT_HELLO TIMEOUT CLIENT_HELLO TIMEOUT CLIENT_HELLO CLIENT_HELLO CLIENT_HELLO|PSK_CLIENT_KEY_EXCHANGE|CHANGE_CIPHER_SPECnull
	at se.uu.it.dtlsfuzzer.sut.ObservationTree.addObservation(ObservationTree.java:133)
	at se.uu.it.dtlsfuzzer.sut.ObservationTree.addObservation(ObservationTree.java:113)
	at se.uu.it.dtlsfuzzer.sut.CachingSULOracle.storeToCache(CachingSULOracle.java:68)
	at se.uu.it.dtlsfuzzer.sut.CachingSULOracle.processQueries(CachingSULOracle.java:56)
	at de.learnlib.datastructure.discriminationtree.model.AbstractDiscriminationTree.sift(AbstractDiscriminationTree.java:127)
	at de.learnlib.algorithms.ttt.base.BaseTTTDiscriminationTree.sift(BaseTTTDiscriminationTree.java:73)
	at de.learnlib.algorithms.ttt.base.AbstractTTTLearner.updateDTTargets(AbstractTTTLearner.java:890)
	at de.learnlib.algorithms.ttt.base.AbstractTTTLearner.closeTransitions(AbstractTTTLearner.java:799)
	at de.learnlib.algorithms.ttt.base.AbstractTTTLearner.closeTransitions(AbstractTTTLearner.java:764)
	at de.learnlib.algorithms.ttt.base.AbstractTTTLearner.refineHypothesisSingle(AbstractTTTLearner.java:215)
	at de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy.refineHypothesisSingle(TTTLearnerMealy.java:67)
	at de.learnlib.algorithms.ttt.base.AbstractTTTLearner.refineHypothesis(AbstractTTTLearner.java:162)
	at se.uu.it.dtlsfuzzer.learn.Extractor.extractStateMachine(Extractor.java:209)
	at se.uu.it.dtlsfuzzer.StateFuzzer.extractModel(StateFuzzer.java:45)
	at se.uu.it.dtlsfuzzer.StateFuzzer.startFuzzing(StateFuzzer.java:34)
	at se.uu.it.dtlsfuzzer.Main.processCommand(Main.java:119)
	at se.uu.it.dtlsfuzzer.Main.main(Main.java:72)
