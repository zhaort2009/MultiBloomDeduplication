package multibloomdeduplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.System;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import com.google.common.hash.BloomFilter;

public class main {
	//这个域是数据块的大小，相当于CDC，或者固定分块算法的块大小
	final int BLOCKSIZE = 512;
	//第二个域:要分块的文件的输入流
	FileInputStream dataInputStream;
	//第三个域:绑定到指纹值表的输出流
	FileOutputStream hashLogOutputStream;
	//第四个域:绑定到指纹指表的输入流，只有一个，用它创建一个新文件先
	FileInputStream hashLogInputStream;
	//应该会用它产生随机的一个什么
	Random rand=new Random(1);
	RandomAccessFile inAccessFile;
	//创建数据输入流
	int CreateDataInputStream(String filePath) {
		File file = new File (filePath);
		
		if (!file.exists() || !file.isFile()) {
			System.out.println("Failed: file not exist!");
			return -1;
		}
		try {
			dataInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	//创建指纹指表输出流;它会给域赋值
	
	int CreateHashLogOutputStream(String filePath) {
		
		File file = new File(filePath);
		
		if (file.exists()) {
			System.out.println("hash log exist, will be overwritten");
		}
		
		try {
			hashLogOutputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	//创建指纹指表输入流，它会给域赋值 ；这个filePath应该和输出流
	//新建的文件是一个文件，并且这个文件要已经存在
	
	int CreateHashLogInputStream(String filePath) {
		File file = new File (filePath);
		
		if (!file.exists() || !file.isFile()) {
			System.out.println("Failed: file not exist!");
			return -1;
		}
		try {
			hashLogInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	//返回块号，会返回一个整型值，4个字节
	int GetBlockNum() {
		return rand.nextInt();
	}
	//去重算法，这个调用了前面的几个算法，先根据磁盘上已经有的一个文件filePath
	//创建要去重的文件的输入流。还要有一个参数是指纹指表文件的名字，然后用输出流创建
	//然后创建一个局部变量字节数组，用来存储这个需要去重的文件的每个块，已经固定分块了
	//还有一个局部变量字节数组hashInfo，应该是指纹指表中的每一条记录。8+20
	//messageDigest对象用来调用SHA1算法来计算指纹值
	//footprint对象用来实例化一个指纹值表条目，一个具体的条目而已
	//一个实例化的多层布隆过滤器
	//第几个条目，有个index，footprintIndex
	//hashlogIndex是干嘛的。
	void deduplication (String filePath, String hashLogName) {
		
		CreateDataInputStream(filePath);
		CreateHashLogOutputStream(hashLogName);
		
		byte[] buf = new byte[BLOCKSIZE];
		byte[] hashInfo;
		int i, j;
		MessageDigest messageDigest;
		Footprint footprint = new Footprint();
		MultiBloomFilter multiBloomFilter = new MultiBloomFilter(4, 4, 100000, 0.0001);
		
		int  footprintIndex, hashlogIndex = 0;
		//输入流的read方法的后两个参数0，都是buf数组的下标参数，第三个参数
		//不能比buf的大小小。读入到buf的哪个位置。这样相当于将要被去重的文件
		//每512个字节读入buf，现在只读入了第一个块while-1//TODO 这里要加循环，计算其指纹值，并产生了一个int型，int可以
		//直接付给long，不用转换。
		//这就是把实例化footprint一个条目了，其实师兄这个和我的没啥大区别，
		//只是有些重复的条目不存入指纹指表就行了，一样的
		//存一个条目hashlogindex增加1，后面put的时候要算出是位于指纹指表的哪部分
		try {
			dataInputStream.read(buf, 0, BLOCKSIZE);
			messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(buf);
			byte[] digest = messageDigest.digest();
			int blockNum = GetBlockNum();
			
			footprint.setFootprint(digest);
			footprint.setPhysicalBlockAdress(blockNum);
			footprintIndex = multiBloomFilter.mayContain(footprint);
			if (footprintIndex < 0) {
				//过滤器中不存在，应写入hashlog，然后加入过滤器
				hashInfo = new byte[digest.length + 4];
				for (i = 0; i < digest.length; ++i)
					hashInfo[i] = digest[i];
				for (j = 0; i < hashInfo.length; ++i){
					hashInfo[i] = (byte)((blockNum >> j) & 0xff);
					j=j+8;
					}
				++hashlogIndex;
				hashLogOutputStream.write(hashInfo);
				multiBloomFilter.put(footprint, hashlogIndex);		
			}
			else {
				//过滤器中存在，应读出指纹值，判断是否为重复数据块
				//Insert方法利用footprintIndex读出指纹值表的相应部分Insert方法
				//Lookup方法查找是否在指纹值表中，//TODO 这里先实现一个没有LRU算法的
				//就是单纯的布隆过滤器，就是只有一个磁盘块大小512个字节，一个扇区大小
				//就等于不缓存，要缓存就要先检查
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("test");
		System.out.println("tttt");
	}
}