package multibloomdeduplication;

 class BloomFilterIndex {
	public int layer;
	public int index;
	public BloomFilterIndex () {
		layer = 0;
		index = 0;
	}
	
	public BloomFilterIndex (int l, int i) {
		layer = l;
		index = i;
	}
}
