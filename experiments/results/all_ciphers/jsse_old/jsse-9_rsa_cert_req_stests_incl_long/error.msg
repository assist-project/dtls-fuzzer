null
Non-determinism detected
full input:
RSA_CLIENT_HELLO Alert(WARNING,CLOSE_NOTIFY) RSA_CLIENT_HELLO RSA_CLIENT_HELLO RSA_CLIENT_HELLO
full new output:
HELLO_VERIFY_REQUEST Alert(WARNING,CLOSE_NOTIFY) TIMEOUT SOCKET_CLOSED SOCKET_CLOSED
old output:
HELLO_VERIFY_REQUEST Alert(WARNING,CLOSE_NOTIFY) TIMEOUT TIMEOUTnull
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
	at se.uu.it.dtlsfuzzer.learn.Extractor.extractStateMachine(Extractor.java:215)
	at se.uu.it.dtlsfuzzer.DtlsFuzzer.extractModel(DtlsFuzzer.java:162)
	at se.uu.it.dtlsfuzzer.DtlsFuzzer.startTesting(DtlsFuzzer.java:59)
	at se.uu.it.dtlsfuzzer.Main.main(Main.java:51)
