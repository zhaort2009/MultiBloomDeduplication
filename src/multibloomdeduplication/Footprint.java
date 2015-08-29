package multibloomdeduplication;

public class Footprint {
	private byte[] fp;
	private long physicalBlockAdress;
	public void setFootprint (byte[] f) { fp = f; }
	public void setPhysicalBlockAdress (long pba) { physicalBlockAdress = pba; }
	public byte[] getFootprint () { return fp; }
}