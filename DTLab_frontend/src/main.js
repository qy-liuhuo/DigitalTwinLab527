import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from "./store"
// 引入依赖
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import websockConnect from "@/utils/websocket/websocket.js"
import './assets/iconfont/iconfont.css'
import VueCookie from 'vue-cookies'

import ActivityCalendar from "vue-activity-calendar";
import "vue-activity-calendar/style.css"; 

var app = createApp(App)
app.config.globalProperties.$host = process.env.VUE_APP_HOST 
app.config.globalProperties.$modelLocation = process.env.VUE_APP_MODEL_LOCATION

var websocket = new websockConnect()
app.config.globalProperties.$websocket = websocket
app.use(ActivityCalendar); 
app.use(store)
app.use(VueCookie)
// 使用ElementPlus
app.use(ElementPlus)
app.use(router).mount('#app')
