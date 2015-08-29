package multibloomdeduplication;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public class FootprintFunnel implements Funnel<Footprint> {
	public void funnel (Footprint from, PrimitiveSink into) {
		into.putBytes(from.getFootprint());
	}
}
