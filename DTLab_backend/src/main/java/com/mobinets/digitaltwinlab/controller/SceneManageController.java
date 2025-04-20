package com.mobinets.digitaltwinlab.controller;


import com.mobinets.digitaltwinlab.service.SceneManagerService;
import com.mobinets.digitaltwinlab.util.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/scene")
public class SceneManageController {

    @Autowired
    private SceneManagerService sceneManagerService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Response addScene(@RequestParam("scene_name") String scene_name,
                             @RequestParam("scene_file") MultipartFile scene_file, @RequestParam("camera_x") Integer camera_x, @RequestParam("camera_y") Integer camera_y, @RequestParam("camera_z") Integer camera_z) {
        return sceneManagerService.addScene(scene_name, scene_file, camera_x, camera_y, camera_z);
    }

    @RequestMapping("/allScenes")
    public Response getAllScenes() {
        return sceneManagerService.getAllScenes();
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public Response getSceneDetail(@RequestParam("id") int id) {
        return sceneManagerService.getSceneDetail(id);
    }

    // 下载场景文件
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadScene(@RequestParam("filename") String filename) {
        return sceneManagerService.downloadSceneFile(filename);
    }



}
