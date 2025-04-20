<template>
  <div ref="container" class="scene-container"></div>
</template>

<script>
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader'
import * as THREE from 'three'
import axios from 'axios'
import http from '@/utils/request/http'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'

export default {
  // 移除data中的Three.js对象
  data() {
    return {}
  },

  async mounted() {
    // 直接挂载到实例而非响应式系统
    this.scene = null
    this.camera = null
    this.renderer = null
    this.controls = null
    
    await this.initScene()
    this.initRenderer()
    this.animate()
    
    // 添加窗口resize监听
    window.addEventListener('resize', this.onWindowResize)
  },

  methods: {
    // 添加窗口resize处理
    onWindowResize() {
      this.camera.aspect = this.$refs.container.clientWidth / this.$refs.container.clientHeight
      this.camera.updateProjectionMatrix()
      this.renderer.setSize(this.$refs.container.clientWidth, this.$refs.container.clientHeight)
    },

    async initScene() {
      const { id } = this.$route.params
      const {data} = await http.get(`/scene/detail`, { id: id })
      // 初始化Three.js场景
      this.scene = new THREE.Scene()
      this.camera = new THREE.PerspectiveCamera(75, 
        this.$refs.container.clientWidth / this.$refs.container.clientHeight, 
        0.1, 
        1000
      )
      // 设置相机位置
      this.camera.position.set(data.scene.cameraX, data.scene.cameraY, data.scene.cameraZ)
      
      // 添加看向场景中心
      this.camera.lookAt(0, 0, 0)
      
      // 或者根据模型位置调整
      // this.camera.lookAt(gltf.scene.position) 在模型加载后调用
      
      // 加载模型
      const loader = new GLTFLoader()
      const gltf = await loader.loadAsync( 'http://' + process.env.VUE_APP_HOST + '/dtlab/scene/download?filename=' + data.scene.filePath)
      this.scene.add(gltf.scene)
      
      // 在场景初始化后添加光照
      const ambientLight = new THREE.AmbientLight(0xffffff, 0.5) // 环境光
      this.scene.add(ambientLight)
      
      const directionalLight = new THREE.DirectionalLight(0xffffff, 1) // 平行光
      directionalLight.position.set(5, 5, 5)
      this.scene.add(directionalLight)
    },

    initRenderer() {
      this.renderer = new THREE.WebGLRenderer({ antialias: true })
      this.renderer.setSize(this.$refs.container.clientWidth, this.$refs.container.clientHeight)
      this.$refs.container.appendChild(this.renderer.domElement)
      
      // 添加轨道控制器
      this.controls = new OrbitControls(this.camera, this.renderer.domElement)
      this.controls.enableDamping = true
    },

    animate() {
      requestAnimationFrame(this.animate)
      this.controls.update()
      this.renderer.render(this.scene, this.camera)
    }
  },

  beforeDestroy() {
    // 添加更彻底的清理
    if (this.renderer) {
      this.renderer.dispose()
      this.renderer.forceContextLoss()
      this.renderer.domElement = null
      this.renderer = null
    }
    window.removeEventListener('resize', this.onWindowResize)
  }
}
</script>

<style scoped>
.scene-container {
  width: 100vw;
  height: 100vh;
}
</style>