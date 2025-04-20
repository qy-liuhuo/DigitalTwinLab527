package com.mobinets.digitaltwinlab.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class Scene {

    private int id;

    private String sceneName;

    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String filePath;

    private int cameraX;

    private int cameraY;

    private int cameraZ;
}
