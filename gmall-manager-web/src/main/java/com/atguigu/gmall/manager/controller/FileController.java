package com.atguigu.gmall.manager.controller;


import com.atguigu.gmall.manager.components.FastDFSTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.StorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/file")
public class FileController {

    @Autowired
    FastDFSTemplate fastDFSTemplate;
    @ResponseBody
    @RequestMapping("/upload")
    public String fileUploader(@RequestParam("file") MultipartFile file){
        if(!file.isEmpty()){
            String originalFilename = file.getOriginalFilename();//获取文件的全名字,再截取扩展名
            String extsionName = StringUtils.substringAfterLast(originalFilename, ".");

            try {
                StorageClient storageClient = fastDFSTemplate.getStorageClient();
                try {
                    String[] strings = storageClient.upload_file(file.getBytes(), extsionName, null);
                    String path = fastDFSTemplate.getPath(strings);
                    return path;
                } catch (MyException e) {
                    log.error("上传失败,原因是:{}",e);
                }
            } catch (IOException e) {
                log.info("",e);
            }
        }
        return "error/error.JPG";
    }
}
