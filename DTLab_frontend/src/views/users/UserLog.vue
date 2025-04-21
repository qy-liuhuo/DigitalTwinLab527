<template>
  <div id='building'>
    <div class="UserLog"  style="text-align: -webkit-center">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>实验室数字孪生系统</span>
          </div>
        </template>

          <el-form :model="form" label-width="60px">
            <el-form-item label="用户名">
              <el-input v-model="info.username" placeholder="请输入用户名"/>
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="info.password" placeholder="请输入密码"/>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width: 47%" @click="login" >登陆</el-button>
              <el-button type="success" style="width: 47%" @click="register">注册</el-button>
            </el-form-item>
            <el-form-item>
              <el-button style="width: 100%" @click="visitor">我是游客</el-button>
            </el-form-item>
          </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus';
import axios from 'axios';
import http from '@/utils/request/http.js'

export default {
  name: "UserLog",
  data(){
    return {
      info: {
        campusNum:'',
        username: '',
        password: '',
        email: ''
      }
    }
  },
  methods:{
    async login(){
        var data = {
          username: this.info.username,
          password: this.info.password,
          rememberMe: true
        }
        let res=await http.post("/login",data)
        if (res.code == 200) {
            ElMessage({
              showClose: true,
              duration: 3000,
              message: '登录成功',
              type: 'success',
            })

            //this.$store.commit("initUserInfo", res.data.userInfo, res.data.ticket);
            //存储cookie
            this.$cookies.set('username', res.data.userInfo.username)
            this.$cookies.set('userId', res.data.userInfo.id)
            this.$cookies.set('userType', res.data.userInfo.type)
            this.$cookies.set('headerUrl',res.data.userInfo.headerUrl)
            //websocket初始化
            if (this.$host == "dtlab.qylh.xyz:9003") {
              this.$websocket.setUrl("wss://dtlab.qylh.xyz/dtlab/ws/webClient/" + res.data.userInfo.id)
            }
            else {
              this.$websocket.setUrl("ws://" + this.$host + "/ws/dtlab/ws/webClient/"+ res.data.userInfo.id)
            }
            this.$websocket.start()
            this.$router.push('GlobalView')
          }
          else {
            ElMessage({
              showClose: true,
              duration: 3000,
              message: res.message,
              type: 'error',
            })
          }
          
        
    },
    register(){
        console.log(this.info.username + 'register!')
        // this.$router.push({ name: 'UserReg'})
        this.$router.push('UserReg')
    },
    visitor(){
        console.log(this.info.username + 'visitor!')
        // this.$router.push({ name: 'ModelView'})
        this.$router.push('GlobalView')
        //websocket初始化
        if (this.$host == "dtlab.qylh.xyz:9003") {
          this.$websocket.setUrl("wss://dtlab.qylh.xyz/dtlab/ws/webClient/" + "visitor")
        }
        else {
          this.$websocket.setUrl("ws://" + this.$host + "/ws/dtlab/ws/webClient/" + "visitor")
        }
        this.$websocket.start()
    }
  }
}
</script>

<style>
  #building{
  background:url("../../assets/background2.jpg");
  width:100%;
  height:100%;
  position:fixed;
 
  background-size:100% 100%;
}

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .text {
    font-size: 14px;
  }
  
  .box-card {
    width: 360px;
    padding: 10px;
    margin-top: 5%;
    opacity: 0.9;
  }
</style>
