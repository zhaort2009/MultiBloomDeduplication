package multibloomdeduplication;

import com.google.common.hash.BloomFilter;
import java.util.ArrayList;
import  java.util.Stack;

public class MultiBloomFilter {
	//层数L，分叉数N subFilter
	private int layer;
	private int subFilter;
	private int expectedInsertions;
	private double rate;
	private ArrayList<ArrayList<BloomFilter<Footprint>>> multiBloomFilter;
	
	public MultiBloomFilter (int layerNum, int subFilterNum, int insertion, double r) {
		layer = layerNum;
		subFilter = subFilterNum;
		expectedInsertions = insertion;
		rate = r;
		multiBloomFilter = new ArrayList<>(layer);
		create();
	}
	
	/*
	public MultiBloomFilter () {
		layer = 2;
		subFilter = 4;
		expectedInsertions = 100;
		rate = 0.03;
		multiBloomFilter = new ArrayList<>(layer);
		create();
	}
	*/
	private int create () {
		int i, j, insertion, count = 1;
		ArrayList<BloomFilter<Footprint>> bloomFilterList;
		BloomFilter<Footprint> bloomFilter;
		
		for (i = 0; i < layer; ++i) {
			bloomFilterList = new ArrayList<>(count);
			for (j = 0; j < count; ++j) {
				insertion = expectedInsertions / count;
				if ((expectedInsertions % count) != 0)
					++insertion;
				bloomFilter = BloomFilter.create(new FootprintFunnel(), insertion, rate);
				bloomFilterList.add(bloomFilter);
			}
			multiBloomFilter.add(bloomFilterList);
			count *= subFilter;
		}
		return 0;
	}
	
	public int put (Footprint fp, int index) {
		int i, j, insertion, count = 1;
		ArrayList<BloomFilter<Footprint>> bloomFilterList;
		
		for (i = 0; i < layer; ++i) {
			//先计算出每个过滤器的大小，当不能整除时，应该加 1
			insertion = expectedInsertions / count;
			if ((expectedInsertions % count) != 0)
				++insertion;
			//据此计算出应该插入到哪个过滤器
			j = index / insertion;
			bloomFilterList = multiBloomFilter.get(i);
			bloomFilterList.get(j).put(fp);
			count *= subFilter;
		}
		return 0;
	}
	
	public int mayContain (Footprint fp) {
		int i, beg, end;
		
		BloomFilterIndex filterIndex;
		ArrayList<BloomFilter<Footprint>> bloomFilterList;
		Stack<BloomFilterIndex> candidateFilters;
		
		//第一层过滤器首先入栈
		filterIndex = new BloomFilterIndex(0, 0);
		candidateFilters = new Stack<>();
		candidateFilters.push(filterIndex);
		
		while (!candidateFilters.isEmpty()) {
			
			//从栈中取出一个过滤器，判断fp是否存在
			filterIndex = candidateFilters.pop();
			bloomFilterList = multiBloomFilter.get(filterIndex.layer);
			if (bloomFilterList.get(filterIndex.index).mightContain(fp)) {
				//到达叶子节点则说明fp存在，此时需要返回索引值
				if (filterIndex.layer == layer - 1)
					return filterIndex.index;
				else {
					//没有到达叶子节点时，需要把该节点的所有子节点倒序入栈，这样下次循环时，子节点会顺序弹出
					beg = (filterIndex.index + 1) * subFilter - 1;
					end = filterIndex.index * subFilter;
					for (i = beg; i >= end; --i) {
						candidateFilters.push(new BloomFilterIndex(filterIndex.layer + 1, i));
					}
				}
			}
		}
		//所有的节点都检查过，没有找到，不存在
		return -1;
	}
}
