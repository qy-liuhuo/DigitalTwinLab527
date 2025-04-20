package com.mobinets.digitaltwinlab.service;


import com.mobinets.digitaltwinlab.dao.SceneMapper;
import com.mobinets.digitaltwinlab.entity.Scene;
import com.mobinets.digitaltwinlab.util.CommunityUtil;
import com.mobinets.digitaltwinlab.util.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SceneManagerService {

    @Autowired
    private SceneMapper sceneMapper;

    @Value("${digitaltwinlab.path.upload}")
    private String uploadFilePath;

    public Response addScene(String scene_name, MultipartFile scene_file, Integer camera_x, Integer camera_y, Integer camera_z) {
        String filePath = uploadScene(scene_file);
        if (filePath == null) {
            return Response.failure("上传场景文件失败");
        }else{
            Scene scene = new Scene();
            scene.setSceneName(scene_name);
            scene.setCreateTime(new Date());
            scene.setFilePath(filePath);
            scene.setCameraX(camera_x);
            scene.setCameraY(camera_y);
            scene.setCameraZ(camera_z);
            System.out.println(scene);
            sceneMapper.insertScene(scene);
            Map<String, Object> map = new HashMap<>();
            map.put("scene_name", scene_name);
            map.put("file_path", filePath);
            map.put("camera_x", camera_x);
            map.put("camera_y", camera_y);
            map.put("camera_z", camera_z);
            map.put("create_time", scene.getCreateTime());
            return Response.success(map);
        }
    }

    public String uploadScene(MultipartFile sceneFile) {
        //获取文件名称
        String filename = CommunityUtil.generateUUID();
        String destFilePath = String.format(System.getProperty("user.dir") + uploadFilePath+ "/%s", filename);
        File destPath = new File(destFilePath);
        //调用transferTo将上传的文件保存到指定的地址
        try{
            sceneFile.transferTo(destPath);
        }catch (IOException e){
            System.out.print(e);
            return null;
        }
        return filename;
    }

    public Response getAllScenes() {
        Map<String, Object> map = new HashMap<>();
        map.put("sceneList", sceneMapper.getAllScenes());
        System.out.print(map);
        return Response.success(map);
    }

    public Response getSceneDetail(int id) {
        Scene scene = sceneMapper.getSceneDetail(id);
        if (scene == null) {
            return Response.failure("场景不存在");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("scene", scene);
        return Response.success(map);
    }

    public ResponseEntity<byte[]> downloadSceneFile(String filename) {

        String destFilePath = String.format(System.getProperty("user.dir") + uploadFilePath+ "/%s", filename);
        // 获取模型文件
        //创建表示下载文件的File对象
        File file =new File(destFilePath);

        //创建用于生成响应结果的BodyBuilder对象，响应状态代码为200
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

        //设置响应正文长度
        builder.contentLength(file.length());

        //设置响应正文类型application/octet-stream二进制数据流，
        //这是最常见的文件下载类型
        builder.contentType(MediaType.APPLICATION_OCTET_STREAM);


        builder.header("Content-Disposition",
                    "inline;filename=" + filename);

        //返回包含下载文件数据的响应结果
        return builder.body(this.getBytesFromFile(file));

    }

    /** 把文件中的内容读入到一个字节数组中 */
    private byte[] getBytesFromFile(File file){
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            //获取文件大小
            int length=fileInputStream.available();
            //读取文件字节，存放在字节数组中
            int bytesRead=0;
            byte[]buff=new byte[length];
            while(bytesRead<length) {
                int result=
                        fileInputStream.read(buff,bytesRead,length-bytesRead);

                if(result==-1)
                    break;

                bytesRead+=result;
            }
            fileInputStream.close();
            return buff;
        }catch (IOException e){
            System.out.print(e);
            return null;
        }

    }


}
