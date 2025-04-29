<template>
    <PersonComponents v-if="finishedInit" :PersonInfoVisible="PersonInfoVisible" :PersonData="PersonData"></PersonComponents>
    <MonitorComponents v-if="finishedInit" :MonitorVisible="MonitorVisible" :MonitorName="MonitorName"></MonitorComponents>
    <LightComponents v-if="finishedInit" :LightVisible="LightVisible" :LightName="LightName"></LightComponents>
    <AirConditionComponents v-if="finishedInit" :AirConditionVisible="AirConditionVisible" :AirConditionName="AirConditionName">
    </AirConditionComponents>
    <WeahterInfoVue style="position:absolute;right:0px;bottom: 0px;" v-if="displayWeather" ref="weather"></WeahterInfoVue>
    <div style="position:absolute;top: 0px;left:0px;z-index:-1" id="WebGL-output">
    </div>

    <el-button style="position:absolute; top:50%; right: 0%;" v-if="!menuDrawer" text type="info" @click="menuDrawer = true">
        <i style="font-size: 30px; color: #70a1ff;" :class="['icon', 'iconfont', ' icon-zuojiantou']"></i>
    </el-button>

    <div style="opacity: 0.7">
        <el-drawer v-model="menuDrawer" title="I am the title" :with-header="false" >    
        <el-form-item label="天气">
            <el-switch v-model="displayWeather"  class="pointerAble" />
        </el-form-item>
        <el-radio-group v-model="chooseComponent" @change="chooseComponentResponse()" class="pointerAble" size="large">
            <!-- <el-radio label="1">屏幕</el-radio> -->
            <el-radio label="2">灯光</el-radio>
            <el-radio label="3">空调</el-radio>
            <el-radio label="4">监控</el-radio>
        </el-radio-group>
    </el-drawer>
    </div>
    

    
</template>
<style>
.peopleLabel {
    background-color: rgb(87, 96, 111, 0.5);
    border-radius: 2px;
    padding: 3px;
    font-size: 3px;
    color: #a3bbdb
}
</style>
<script>
import http from '@/utils/request/http.js'
import { searchObject } from '@/utils/SearchObject.js';
import * as THREE from 'three';
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader.js";
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { CSS2DObject, CSS2DRenderer } from "three/examples/jsm/renderers/CSS2DRenderer"
import axios from 'axios'
import Queue from 'yocto-queue'
var beIntersectObjects = [];//用来存放需要射线检测的物体数组
var MemberList = [];//存放人物模型
var scene;//场景对象实例
var root,root2;
var controls;

var progress=0;
const velocity = 0.005;
const peoples = {};


export default {
    components: {
    },
    data() {
        return {
            testPeople:null,
            menuDrawer:false,
            finishedInit:false,//用于阻塞子组件的mounted
            camera: null, //相机实例
            renderer: null, //渲染器实例
            renderEnabled: true,
            eventSource: null,
            mixer: null,//混合器实例
            clock: new THREE.Clock(),//时钟对象
            raycaster: new THREE.Raycaster(),
            mouse: new THREE.Vector2(),
            container: "",
            timer: null,//定时器
            objectModal: null, //模型实例
            loading: true,
            personData: null,
            activeName: "first",//视图选择
            PersonInfoVisible: false,
            displayWeather:true,
            chooseComponent: null,
            MonitorVisible: false,
            MonitorName: null,
            LightVisible: false,
            LightName: null,
            AirConditionVisible: false,
            AirConditionName: null,
            peopleLabelControls:null,
            box:{
                min: {
                    x: -30,
                    y: 0,
                    z: -20,
                },
                max: {
                    x: 20,
                    z: 20,
                },
            },
        };
    },
    async created(){
        THREE.Cache.enabled = true;
        this.runThree()
        this.animate()
        await this.loadGltf()
        this.initEnv()
        window.addEventListener('resize', this.onWindowResize);
        this.finishedInit=true
    },
    mounted() {
        // add the output of the renderer to the html element
        this.container = document.getElementById('WebGL-output');
        this.container.appendChild(this.renderer.domElement);//body元素中插入canvas对象
         // 创建 EventSource 连接到后端的 SSE 端点
        this.eventSource = new EventSource('http://' + process.env.VUE_APP_HOST + '/dtlab/sse/connect?clientId=test', {
            withCredentials: true
        });
        // 监听消息事件
        this.eventSource.onmessage = (event) => {
            var parsedData = JSON.parse(event.data);
            console.log('SSE message:', parsedData);
            parsedData['targets'].forEach(target => {
                if (peoples[target['pos_id']] == null) {
                    this.load_new_person(target['pos_id'], target['coordinates'], target['time']);
                }else{
                    peoples[target['pos_id']]['tracks_queue'].enqueue([new THREE.Vector3(-target['coordinates']['x'], target['coordinates']['z'], -target['coordinates']['y']), target['time']]);
                }
            });
        };

        // 处理错误事件
        this.eventSource.onerror = (error) => {
            console.error('SSE error:', error);
        };
    },
    async beforeUnmount() {
        // 在组件销毁时关闭 SSE 连接
        if (this.eventSource) {
            await axios.get('http://' + process.env.VUE_APP_HOST + '/dtlab/sse/close?clientId=test', {
                withCredentials: true
            });
            this.eventSource.close();
        }
    },
    methods: {
        runThree() {
           
            scene = new THREE.Scene();
            this.camera = new THREE.PerspectiveCamera(63, window.innerWidth / window.innerHeight, 0.1, 1000);/////////////
            // position and point the camera to the center of the scene
            this.camera.position.set(10, 40, 30); //设置相机位置
            this.camera.lookAt(scene.position);
            this.renderer = new THREE.WebGLRenderer({ antialias: true });
            this.renderer.outputEncoding = THREE.sRGBEncoding;//rgb颜色
            this.renderer.setClearColor(new THREE.Color(0xeeeeee));//设置背景颜色
            this.renderer.setPixelRatio(window.devicePixelRatio);//清晰度
            this.renderer.setSize(window.innerWidth, window.innerHeight);
            this.renderer.shadowMapEnabled = true;//有阴影
            this.renderer.shadowMap.enabled = true;
            //轨道控制器
            controls = new OrbitControls(this.camera, this.renderer.domElement);
            controls.enableDamping = true;
        },
        peopelesMove(delta){
            for(var people_id in peoples){
                var model = peoples[people_id]['model'];
                peoples[people_id]['animationMix'].update(delta);
                if(peoples[people_id]['target_position'] == null){
                    var new_position = peoples[people_id]['tracks_queue'].dequeue();
                    if(new_position == undefined){
                        continue;
                    }
                    peoples[people_id]['target_position'] = new_position[0];
                    peoples[people_id]['speed'] = new_position[0].clone().sub(peoples[people_id]['current_position']).divideScalar(new_position[1] - peoples[people_id]['time']);
                    peoples[people_id]['time'] = new_position[1];
                }else{
                    var target_position = peoples[people_id]['target_position'];
                    var model = peoples[people_id]['model'];
                    var current_position = model.position;
                    var distance = target_position.distanceTo(current_position);
                    if(distance < 0.1){
                        peoples[people_id]['current_position'] = target_position;
                        peoples[people_id]['target_position'] = null;
                        model.position.set(target_position.x, target_position.y, target_position.z);
                        continue;
                    }
                    var speed = peoples[people_id]['speed'];
                    var new_position = current_position.clone().add(speed.clone().multiplyScalar(delta));
                    model.position.set(new_position.x, new_position.y, new_position.z);
                    var mtx = new THREE.Matrix4()  //创建一个4维矩阵
                    mtx.lookAt(model.position, target_position, root.up) //设置朝向
                    mtx.multiply(new THREE.Matrix4().makeRotationFromEuler(new THREE.Euler(0, Math.PI, 0)))
                    var toRot = new THREE.Quaternion().setFromRotationMatrix(mtx)  //计算出需要进行旋转的四元数值
                    model.quaternion.slerp(toRot, 0.2)

                }
            }
        },
        animate() {
            this.renderer.render(scene, this.camera)
            requestAnimationFrame(this.animate)
            var time = this.clock.getDelta();
            if (this.mixer) {
                this.mixer.update(time);
            }
            this.peopelesMove(time)
        },
        async loadGltf() {
            function loadPromise(path) {
                var loader = new GLTFLoader();
                return new Promise((resolve, reject) =>{
                    loader.load(path, object => {
                        resolve(object)
                    })
                })
            }
            try{
                let path="/static/ximen.glb"
                const gltf = await loadPromise(path);
                root = gltf.scene;//注意！！！为了保持层级不变，gltf最外空一层
                gltf.scene.name = "ximen";
                root.scale.set(1, 1, 1);
                root.position.set(0, 0, 0);
                root.castShadow = true;
                root.traverse(function (child) {
                    if (child.isMesh) {
                        child.frustumCulled = false;
                        //模型阴影
                        child.castShadow = true;
                        //材质
                        child.material.side = THREE.FrontSide;
                        if (child.name.substr(0, 4) == "hole") {
                            child.visible = false;
                        }
                        if (child.name.substr(0, 5) == "floor") {
                            child.receiveShadow = true;
                        }
                        if (child.name == "ceiling" || child.name.substr(0, 4) =="door") { //只在游览视图中显示
                            child.visible = false;
                        }
                    }
                    if (child.name == "Person") {
                        MemberList.push(child.children);
                    }

                })

                scene.add(root) //scene: soptlight + root

            }catch(e){
                console.log(e)
            }
        },
        load_new_person(person_id, initial_position, time){
            const loader = new GLTFLoader();
            loader.load("/static/person_test.glb", (gltf)=>{
                // 设置模型大小
                gltf.scene.scale.set(0.37, 0.37, 0.37);
                scene.add(gltf.scene);
                var position = new THREE.Vector3(-initial_position['x'], initial_position['z'], -initial_position['y']);
                // 设置初始位置
                gltf.scene.position.set(position.x, position.y, position.z);
                peoples[person_id] = {"initial_position": position, "current_position": position, "target_position":null, "model": gltf.scene, "animationMix":new THREE.AnimationMixer(gltf.scene)  ,"tracks_queue": new Queue(), "speed":0, "time":time};
                // 调用动画
                peoples[person_id]['animationMix'].clipAction(gltf.animations[11]).play();
            });
        },
        initEnv() {
            //根据昼夜光线调整点光源
            //环境光线,强度随时间改变
            const envLight = new THREE.AmbientLight( 0x404040,1 ); //
            scene.add( envLight );
            //实验室光线，强度随灯光开关改变
            var labLight = new THREE.DirectionalLight( 0xffffff, 0.5 );
            labLight.position.set(-10, 50, 10);
            //labLight.castShadow = true;
            scene.add(labLight);
        },
        onWindowResize() {
            this.camera.aspect = window.innerWidth / window.innerHeight;
            this.camera.updateProjectionMatrix();
            this.renderer.setSize(window.innerWidth, window.innerHeight);
        },
    }
}
</script>
<style>
.demo-tabs>.el-tabs__content {
    color: #6b778c;
    font-size: 32px;
    font-weight: 600;
}

.box-card3 {
    opacity: 0.8;
    pointer-events: none;
    margin-right: 10px;
}
</style>