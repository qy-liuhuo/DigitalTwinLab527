<template>
  <div class="scene-management">
    <!-- 场景列表表格 -->
    <el-table 
      :data="scenes" 
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
    <el-table-column 
      prop="scene_name" 
      label="场景名称" 
      min-width="300"
      header-align="center"
      align="center"></el-table-column>
    <el-table-column 
      label="相机位置" 
      prop="cameraPosition" 
      min-width="300"
      header-align="center"
      align="center"></el-table-column>
    <el-table-column 
      label="创建时间" 
      min-width="300"
      header-align="center"
      align="center">
      <template #default="scope">
        {{ new Date(scope.row.createdAt).toLocaleString() }}
      </template>
    </el-table-column>
    <el-table-column label="操作" m
      in-width="300"
      header-align="center"
      align="center">
        <template #default="scope">
          <el-button @click="showDetail(scope.row)">查看</el-button>
          <el-button type="primary" @click="editScene(scope.row)">编辑</el-button>
        </template>
    </el-table-column>
    <!-- 操作列保持不变 -->
  </el-table>
    <!-- 添加场景按钮 -->
    <el-button 
      type="primary" 
      class="add-button"
      @click="showDialog = true">
      添加新场景
    </el-button>

    <!-- 场景编辑对话框 -->
    <el-dialog v-model="showDialog" title="场景配置">
      <el-form 
        :model="newScene" 
        :rules="formRules"
        ref="formRef"
        label-width="120px">
        
        <!-- 场景名称 -->
        <el-form-item label="场景名称" prop="scene_name" required>
          <el-input v-model="newScene.scene_name" />
        </el-form-item>

        <!-- 模型文件 -->
        <el-form-item label="模型文件" prop="modelFile" required>
          <el-upload
            :auto-upload="false"
            :on-change="handleFileUpload"
            :disabled="loadingModel"
            accept=".glb,.gltf">
            <el-button type="primary" :loading="loadingModel">
              选择模型文件
            </el-button>
          </el-upload>
        </el-form-item>

        <!-- 模型尺寸 -->
        <el-form-item label="模型尺寸">
          <el-text v-if="newScene.modelSize.width > 0">
            宽: {{ newScene.modelSize.width }}m &nbsp;
            高: {{ newScene.modelSize.height }}m &nbsp;
            深: {{ newScene.modelSize.depth }}m
          </el-text>
          <el-text v-else type="info">未识别到模型尺寸</el-text>
        </el-form-item>

        <!-- 相机位置 -->
        <el-form-item label="相机位置 (X,Y,Z)" prop="cameraPosition" required>
          <el-input 
            v-model="newScene.cameraPosition"
            placeholder="例如：0, 5, 10" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="addScene">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader'
import { Group, Box3 } from 'three'
import axios from 'axios'
import { ElMessage } from 'element-plus';
import http from '@/utils/request/http';



export default {
  data() {
    return {
      scenes: [],
      showDialog: false,
      newScene: {
        scene_name: '',
        scene_file: null,
        cameraPosition: '0,5,10',
        modelSize: { width: 0, height: 0, depth: 0 }
      },
      loadingModel: false,
      formRules: {
        name: [
          { required: true, message: '请输入场景名称', trigger: 'blur' }
        ],
        modelFile: [
          { required: true, message: '请上传模型文件', trigger: 'change' }
        ],
        cameraPosition: [
          { required: true, message: '请输入相机位置', trigger: 'blur' },
          { 
            pattern: /^[-+]?[\d]+(\.\d+)?\s*,\s*[-+]?[\d]+(\.\d+)?\s*,\s*[-+]?[\d]+(\.\d+)?$/,
            message: '格式示例：0,5,10' 
          }
        ]
      }
    }
  },
  mounted() {
    this.fetchScenes()
  },
  methods: {
    showDetail(scene) {
      console.log('查看场景详情:', scene)
      this.$router.push({
        name: 'SceneView',
        params: { id: scene.id }
      })
    },
    handleFileUpload(file) {
      this.newScene.modelFile = file.raw
      this.$refs.formRef.validateField('modelFile')
      this.loadModelDimensions(file.raw)
    },
    async fetchScenes() {
      http.get('/scene/allScenes').then(res => {
        if (res.code == 200) {
          console.log(res)
          res.data.sceneList.forEach(scene => {
            this.scenes.push({
              id: scene.id,
              scene_name: scene.sceneName,
              cameraPosition: `${scene.cameraX},${scene.cameraY},${scene.cameraZ}`,
              createdAt: scene.createTime
            })
          })
        } 
      }).catch(err => {
        console.log(err)
      })
    },

    async loadModelDimensions(file) {
      const loader = new GLTFLoader()
      this.loadingModel = true
      
      try {
        const gltf = await new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = (e) => {
            loader.parse(e.target.result, '', resolve, reject)
          }
          reader.readAsArrayBuffer(file)
        })

        const box = new Group()
        box.add(gltf.scene)
        box.updateMatrixWorld(true)
        
        const bbox = new Box3().setFromObject(box)
        this.newScene.modelSize = {
          width: (bbox.max.x - bbox.min.x).toFixed(2),
          height: (bbox.max.y - bbox.min.y).toFixed(2),
          depth: (bbox.max.z - bbox.min.z).toFixed(2)
        }
        
      } catch (error) {
        console.error('模型解析失败:', error)
        this.$message.error('模型解析失败: ' + error.message)
      } finally {
        this.loadingModel = false
      }
    },

    addScene() {
      this.$refs.formRef.validate(valid => {
        if (valid) {
          const formData = new FormData()
          formData.append('scene_name', this.newScene.scene_name)
          formData.append('scene_file', this.newScene.modelFile)
          formData.append('camera_x', this.newScene.cameraPosition.split(',')[0])
          formData.append('camera_y', this.newScene.cameraPosition.split(',')[1])
          formData.append('camera_z', this.newScene.cameraPosition.split(',')[2])
          axios.post('http://' + process.env.VUE_APP_HOST + '/dtlab/scene/add', formData, {Headers: { 'Content-Type': 'multipart/form-data' }}
          ).then(res => {
          console.log(res)
          if (res.data.code == 200) {
              this.scenes.push({
              ...this.newScene,
              id: res.data.data.id,
              createdAt: res.data.data.create_time
            })
            
            this.showDialog = false
            this.$message.success('场景创建成功')
          }
          else {
            ElMessage({
              showClose: true,
              duration: 0,
              message: res.data.message,
              type: 'error',
            })
          }
        }).catch(err => {
          console.log(err)
        })
        Object.assign(this.newScene, {
          name: '',
          modelFile: null,
          cameraPosition: '0,5,10',
          modelSize: { width: 0, height: 0, depth: 0 }
        })
        }
      })
    },

    editScene(scene) {
      Object.assign(this.newScene, scene)
      this.showDialog = true
      this.$nextTick(() => {
        this.$refs.formRef.clearValidate()
      })
    }
  }
}
</script>

<style scoped>
.add-button {
  margin-top: 20px;
}
.el-form-item {
  margin-bottom: 22px;
}
</style>