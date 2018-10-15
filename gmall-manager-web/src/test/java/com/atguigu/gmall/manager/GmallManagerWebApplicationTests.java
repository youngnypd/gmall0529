package com.atguigu.gmall.manager;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerWebApplicationTests {

	@Test
	public void contextLoads() throws IOException, MyException {
		//初始化流程
		//1、加载配置文件
		String file = this.getClass().getResource("/tracker.conf").getFile();
		//2、初始化fastdfs客户端配置
		ClientGlobal.init(file);

		//上传流程
		//1、创建TrackerClient
		TrackerClient trackerClient=new TrackerClient();
		//2、获取到和TrackerServer的连接
		TrackerServer trackerServer=trackerClient.getConnection();
		//3、根据TrackerServer返回的信息创建出操作Storage的客户端
		StorageClient storageClient=new StorageClient(trackerServer,null);

		//
		String localFilePath="E:\\迅雷下载\\照片\\9f55e8bf38724e92aed74cb48acf4962.jpeg";
		//4、使用StorageClient给Storage上传文件
		String[] upload_file = storageClient.upload_file(localFilePath,"jpg",null);

		for (int i = 0; i < upload_file.length; i++) {
			String s = upload_file[i];
			System.out.println("s = " + s);
		}
		/**
		 * s = group1
		 s = M00/00/00/wKifhFu_edqAONm7ACLptic-Vk0827.bmp
		 */
	}

}


