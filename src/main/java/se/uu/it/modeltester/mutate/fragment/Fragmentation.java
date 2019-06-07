package se.uu.it.modeltester.mutate.fragment;

import java.util.Arrays;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;

public class Fragmentation {
	private List<Fragment> fragments;

	public static Fragmentation generateFromExistingMessageFragments(
			List<DtlsHandshakeMessageFragment> fragments) {
		return new Fragmentation(fragments
				.stream()
				.map(f -> new Fragment(f.getFragmentOffset().getValue(), f
						.getFragmentLength().getValue()))
				.toArray(Fragment[]::new));
	}

	public Fragmentation(Fragment... fragments) {
		this.fragments = Arrays.asList(fragments);
	}

	public Fragmentation(List<Fragment> fragments) {
		this.fragments = fragments;
	}

	public List<Fragment> getFragments() {
		return fragments;
	}

	public int getSumOfLengths() {
		return fragments.stream().map(f -> f.getLength())
				.reduce(1, (c, d) -> c + d);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Fragmentation[");
		fragments.forEach(f -> builder.append(f.toString()).append(","));
		builder.setCharAt(builder.length() - 1, ']');
		return builder.toString();
	}
}
