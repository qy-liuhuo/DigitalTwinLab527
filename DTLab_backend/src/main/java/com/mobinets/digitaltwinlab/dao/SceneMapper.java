package com.mobinets.digitaltwinlab.dao;

import com.mobinets.digitaltwinlab.entity.Scene;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;


@Mapper
public interface SceneMapper {

    public int insertScene(Scene scene);

    public List<Scene> getAllScenes();

    public Scene getSceneDetail(int id);


}
