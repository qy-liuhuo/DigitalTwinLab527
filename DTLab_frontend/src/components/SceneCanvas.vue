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
import PersonComponents from './PersonComponents.vue'
import MonitorComponents from './MonitorComponents.vue'
import LightComponents from './LightComponents.vue'
import AirConditionComponents from './AirConditionComponents.vue'
import { searchObject } from '@/utils/SearchObject.js';
import * as THREE from 'three';
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader.js";
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { CSS2DObject, CSS2DRenderer } from "three/examples/jsm/renderers/CSS2DRenderer"
import WeahterInfoVue from './WeahterInfo.vue'
import axios from 'axios'
var beIntersectObjects = [];//用来存放需要射线检测的物体数组
var MemberList = [];//存放人物模型
var scene;//场景对象实例
var root,root2;
var controls;
var peopleLabelRenderer; //人物label渲染器

var progress=0;
const velocity = 0.005;

export default {
    name: 'SceneCanvas',
    components: {
        PersonComponents,
        MonitorComponents,
        LightComponents,
        AirConditionComponents,
        WeahterInfoVue,
    },
    data() {
        return {
            peoples:{},
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
        peopleLabelRenderer = new CSS2DRenderer()
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
        this.container.addEventListener('click', this.onMouseClick, false); //鼠标点击事件监听器
         // 创建 EventSource 连接到后端的 SSE 端点
        this.eventSource = new EventSource('http://127.0.0.1:8082/sse/connect/?clientId=test', {
            withCredentials: true
        });
        // 监听消息事件
        this.eventSource.onmessage = (event) => {
            var parsedData = JSON.parse(event.data);
            console.log('SSE message:', parsedData);
            parsedData['targets'].forEach(target => {
                if (this.peoples[target['pos_id']] == null) {
                    this.load_new_person(target['pos_id'], target['coordinates']);
                    
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
            await axios.get('http://127.0.0.1:8082/sse/close/?clientId=test', {
                withCredentials: true
            });
            this.eventSource.close();
        }
    },
    methods: {
        //增加人物Label
        addPeopleLabel(people, model) {
            var name_id = "Member" + people['member_id'].toString().padStart(3, '0')
            var peopleLabelDiv = document.createElement('div');
            peopleLabelDiv.className = 'peopleLabel';
            //peopleLabelDiv.style.color ='#139bf0';
            peopleLabelDiv.textContent = people['name'];
            var peopleLabel = new CSS2DObject(peopleLabelDiv);
            peopleLabel.name = name_id + "_label";//设置name
            peopleLabel.visible = people['visible'];
            peopleLabel.position.set(0, 5, 0);
            model.add(peopleLabel);
            peopleLabel.layers.set(0);
        },
        //人物Label渲染器初始化
        peopleLabelRendererInit() {
                peopleLabelRenderer.setSize(window.innerWidth, window.innerHeight);
                peopleLabelRenderer.domElement.style.position = "absolute";
                peopleLabelRenderer.domElement.style.top = 0;
                peopleLabelRenderer.domElement.id = "peopleLabels";
                console.log(peopleLabelRenderer.domElement)
                document.getElementById("WebGL-output").appendChild(peopleLabelRenderer.domElement);
                peopleLabelRenderer.render(scene, this.camera);
                this.peopleLabelControls = new OrbitControls(this.camera, peopleLabelRenderer.domElement);
                // peopleLabelControls.minDistance = 0;
                // peopleLabelControls.maxDistance = 20;
            this.peopleLabelControls.maxPolarAngle = Math.PI / 2;
            
        },
        //更改人物模型及其label可见性
        setMemberVisible(merberName, visible) {
            var temp = searchObject(root, merberName)
            temp.visible = visible;
            var tempLabel = searchObject(temp, (merberName + "_label"))
            tempLabel.visible = visible;
        },
        runThree() {
           
            scene = new THREE.Scene();
            this.camera = new THREE.PerspectiveCamera(63, window.innerWidth / window.innerHeight, 0.1, 1000);/////////////
            // position and point the camera to the center of the scene
            this.camera.position.set(-15, 6, 10); //设置相机位置
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
            // const earthDiv = document.getElementById('testDiv');
            // const earthLabel = new CSS2DObject(earthDiv);
            // earthLabel.visible=false
            // scene.add(earthLabel);
            // earthLabel.layers.set(0);
            // labelRenderer.setSize(window.innerWidth, window.innerHeight);
            // labelRenderer.domElement.style.position = 'absolute';
            // labelRenderer.domElement.style.top = '0px';
            // document.getElementById("WebGL-output").appendChild(labelRenderer.domElement);
            // labelRenderer.render(scene, this.camera);
            // const controls = new OrbitControls(this.camera, labelRenderer.domElement);
            // controls.minDistance = 5;
            // controls.maxDistance = 100;
        },
        peopelesMove(){
            
        },
        moveOnCurve() {
            var curve = new THREE.CatmullRomCurve3([
                new THREE.Vector3(0, 0, 0),
                new THREE.Vector3(3, 0, 0),
                new THREE.Vector3(3, 0, 3),
                new THREE.Vector3(0, 0, 3)
            ]);
            curve.curveType = "catmullrom";
            curve.closed = true;//设置是否闭环
            curve.tension = 0.1; //设置线的张力，0为无弧度折线
            if(!this.testPeople){
                return 
            }
            if (progress <= 1 - velocity) {
            const point = curve.getPointAt(progress); //获取样条曲线指定点坐标
            const pointBox = curve.getPointAt(progress + velocity); //获取样条曲线指定点坐标
            if (point && pointBox) {
                this.testPeople.position.set(point.x, point.y, point.z);
                //this.testPeople.lookAt(pointBox.x, pointBox.y, pointBox.z);//因为这个模型加载进来默认面部是正对Z轴负方向的，所以直接lookAt会导致出现倒着跑的现象，这里用重新设置朝向的方法来解决。
                
                var targetPos = pointBox   //目标位置点
                var offsetAngle = Math.PI //目标移动时的朝向偏移

                // //以下代码在多段路径时可重复执行
                var mtx = new THREE.Matrix4()  //创建一个4维矩阵
                // .lookAt ( eye : Vector3, target : Vector3, up : Vector3 ) : this,构造一个旋转矩阵，从eye 指向 target，由向量 up 定向。
                mtx.lookAt(this.testPeople.position, targetPos, root.up) //设置朝向
                mtx.multiply(new THREE.Matrix4().makeRotationFromEuler(new THREE.Euler(0, offsetAngle, 0)))
                var toRot = new THREE.Quaternion().setFromRotationMatrix(mtx)  //计算出需要进行旋转的四元数值
                this.testPeople.quaternion.slerp(toRot, 0.2)
            }
            progress += velocity;
            } else {
            progress = 0;
            }
		},
        animate() {
            if (this.peopleLabelControls != null) {
                if (this.camera.position.x > this.box.max.x || this.camera.position.x < this.box.min.x) {
                    this.peopleLabelControls.reset();
                } else if (this.camera.position.z > this.box.max.z || this.camera.position.z < this.box.min.z) {
                    console.log("z overflow")
                    this.peopleLabelControls.reset();
                } else if (this.camera.position.y < this.box.min.y) {
                    this.peopleLabelControls.reset();
                } else {
                    this.peopleLabelControls.saveState();
                }
            }
            //this.controls.update()
            this.renderer.render(scene, this.camera)
            peopleLabelRenderer.render(scene, this.camera)
            requestAnimationFrame(this.animate)
            var time = this.clock.getDelta();
            if (this.mixer) {
                this.mixer.update(time);
            }
            this.moveOnCurve();

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
                // let res = await http.get("model/getLatestModel")
                // var path='http://file.dtlab.qylh.xyz/model/'+res.data.modelInfo.fileName
                let path="/static/Lab.glb"
                // if(this.$modelLocation==0){
                //     path="/static/Lab.glb"
                // } 
                const gltf = await loadPromise(path);
                root = gltf.scene.children[0];//注意！！！为了保持层级不变，gltf最外空一层
                console.log(root)
                //root = gltf.scene;
                gltf.scene.name = "LabScene";
                console.log("gltf.scene.children", gltf.scene.children)
                //beIntersectObjects.push(gltf.scene.children);
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
               
                
                //渲染人物模型和标签
                http.get("person/getAllStaff").then(res => {
                    res.data.staffs.forEach(staff => {
                        var name = "Member" + this.$store.state[staff.nameEn].seat_id.toString().padStart(3, '0')
                        this.setMemberVisible(name, this.$store.state[staff.nameEn].visible);
                        this.addPeopleLabel(this.$store.state[staff.nameEn], searchObject(root, name));
                    });
                    this.peopleLabelRendererInit()
                    this.PersonData = this.$store.getters.allPerson;
                }).catch(err => {
                    console.log(err)
                })
            }catch(e){
                console.log(e)
            }
            const loader = new GLTFLoader();
            loader.load("/static/person_test.glb", (gltf)=>{
                gltf.scene.scale.set(0.37, 0.37, 0.37);
                scene.add(gltf.scene);
                this.testPeople=gltf.scene
                // 调用动画
                this.mixer = new THREE.AnimationMixer(gltf.scene);
                this.mixer.clipAction(gltf.animations[11]).play();
            });
        },
        load_new_person(person_id, initial_position){
            const loader = new GLTFLoader();
            loader.load("/static/person_test.glb", (gltf)=>{
                // 设置模型大小
                gltf.scene.scale.set(0.37, 0.37, 0.37);
                scene.add(gltf.scene);
                // 设置初始位置
                gltf.scene.position.set(initial_position['x'], initial_position['z'], -initial_position['y'])
                this.peoples[person_id] = {"initial_position": initial_position, "model": gltf.scene, "tracks": [initial_position]};
                // 调用动画
                this.mixer.clipAction(gltf.animations[11]).play();
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

            var lightpos = [
                [-8.7, 3.7, -4],
                [-5.7, 3.7, -4],
                [-2.7, 3.7, -4],
                [1.7, 3.7, -4],
                [4.7, 3.7, -4],
                [7.7, 3.7, -4],
                [-8.7, 3.7, 0],
                [-5.7, 3.7, 0],
                [-2.7, 3.7, 0],
                [1.7, 3.7, 0],
                [4.7, 3.7, 0],
                [7.7, 3.7, 0],
                [-8.7, 3.7, 4],
                [-5.7, 3.7, 4],
                [-2.7, 3.7, 4],
                [1.7, 3.7, 4],
                [4.7, 3.7, 4],
                [7.7, 3.7, 4]
            ];

            for(var i=0;i<3;i++){
                var group = new THREE.Group();//声明组容器
                group.name = "Light"+(i+1).toString();
                //先设为false
                group.visible=false;
                scene.add(group);
                var rectLight = [];
                var BoxGeometry = new THREE.BoxGeometry(1, 0.01, 1);
                var planeMaterial = new THREE.MeshBasicMaterial({ color: 0xffffff, wireframe: true });
                rectLight.push(new THREE.Mesh(BoxGeometry, planeMaterial))
                for (let j = 0; j < 6; j++) {
                    rectLight.push(rectLight[0].clone())
                    rectLight[j].position.set(lightpos[i * 6 + j][0], lightpos[i * 6 + j][1], lightpos[i * 6 + j][2]);
                    rectLight[j].name = group.name+"_"+ (j + 1).toString();
                    group.add(rectLight[j]);
                }
            }
        },
        onMouseClick(event) {
            // 将鼠标位置归一化为设备坐标。x 和 y 方向的取值范围是 (-1 to +1)
            this.mouse.x = (event.clientX / this.container.clientWidth) * 2 - 1;
            this.mouse.y = -(event.clientY / this.container.clientHeight) * 2 + 1;
            this.raycaster.setFromCamera(this.mouse, this.camera);
            // 计算物体和射线的焦点
            let intersects = this.raycaster.intersectObjects(beIntersectObjects, true);
            //let intersects = this.raycaster.intersectObjects(scene.children, true);
            // 获取选中最近的 Mesh 对象
            //if (intersects.length !== 0 && intersects[0].object instanceof THREE.Mesh) {
            if (intersects.length !== 0) {
                let selectObjectName = intersects[0].object.name;
                console.log("CLICK!", intersects[0].object);
                const object = { selectObjectName }
                if(selectObjectName.substr(0, 12)=='AirCondition'){
                    this.clickAirCondition(intersects[0].object);
                }
                else if(selectObjectName.substr(0, 5)=='Light'){
                    this.clickLight(intersects[0].object);
                }
            }
        },
        onWindowResize() {
            this.camera.aspect = window.innerWidth / window.innerHeight;
            this.camera.updateProjectionMatrix();
            this.renderer.setSize(window.innerWidth, window.innerHeight);
            peopleLabelRenderer.setSize(window.innerWidth, window.innerHeight);
        },
        clickPersonComponents() {
            console.log("PersonInfoVisible", this.PersonInfoVisible);
            this.PersonInfoVisible = !this.PersonInfoVisible;
            console.log("PersonInfoVisible", this.PersonInfoVisible);
        },
        //操作菜单部分
        chooseComponentResponse() {
            console.log("chooseComponentResponse")
            switch (this.chooseComponent) { //想要判断的变量
                case '1':
                    this.clickMonitorComponents();
                    break
                case '2':
                    this.clickLightComponents();
                    break
                case '3':
                    this.clickAirConditionComponents();
                    break
            }
        },
        //灯光
        clickLightComponents() {
            beIntersectObjects = [];
            var lightList = this.$store.state.lightList;
            lightList.forEach((item)=>{
                var lightGroupe=scene.getObjectByName(item);
                lightGroupe.children.forEach((l)=>{
                    beIntersectObjects.push(l);
                })
            })
            // var group = scene.getObjectByName("Set_ThreejsLight");
            // group.traverse(function (child) {
            //     if (child.name.substr(0, 5) == "Light") {
            //         beIntersectObjects.push(child);
            //     }
            // })
            console.log("beIntersectObjects", beIntersectObjects);
        },
        clickLight(chooseLight) {
            console.log("clickLight");
            this.LightVisible = !this.LightVisible;
            this.LightName = chooseLight.parent.name;//"chooseLight"+id,用作vuex注册
        },
        closeLight() {
            this.LightVisible = !this.LightVisible;
            this.LightName = "";
        },
        //空调
        clickAirConditionComponents() {
            beIntersectObjects = [];
            root.traverse(function (child) {
                if (child.name.substr(0, 12) == "AirCondition") {
                    beIntersectObjects.push(child);
                }
            })
            console.log("beIntersectObjects", beIntersectObjects);
        },
        clickAirCondition(chooseAirCondition) {
            console.log("clickAirCondition");
            this.AirConditionVisible = !this.AirConditionVisible;
            this.AirConditionName = chooseAirCondition.name.substr(0, 15);
        },
        closeAirCondition() {
            this.AirConditionVisible = !this.AirConditionVisible;
            this.AirConditionName = "";
        },
        getObject(name){
            return scene.getObjectByName(name)
        },
        getScene(){
            return scene;
        },
        getRenderer()
        {
            return this.renderer;
        }
        
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