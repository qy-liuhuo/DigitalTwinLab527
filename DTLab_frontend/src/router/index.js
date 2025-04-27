import { createRouter, createWebHistory } from 'vue-router'
import GlobalView from "../views/GlobalView.vue"
import TourView from '../views/TourView.vue'
import PersonView from '../views/PersonView.vue'

import ConferenceRoomView from '../views/ConferenceRoomView.vue'
import DeviceManager from '../views/admin/DeviceManager.vue'
import UserInfo from "../views/admin/UserInfo.vue"
import ModelEdit from "../views/admin/ModelEdit.vue"
import DeviceLog from "../views/admin/DeviceLog.vue"
import StaffManage from "../views/admin/StaffManager.vue"
import UserLog from "../views/users/UserLog.vue"
import UserReg from "../views/users/UserReg.vue"
import UserCenter from "../views/users/UserCenter.vue"
import SceneManage from "../views/admin/SceneManagement.vue"
import OutView from '@/views/OutView.vue'

const routes = [
  {
    path: '/GlobalView',
    name: '室内视图',
    meta: {
      id: 'GlobalView',
      hasChildren: false,
      show: true,
      icon: 'icon-quanjufangda2',
      accessLevel: 0,
    },
    component: GlobalView
  },
  {
    path: '/outView',
    name: '室外视图',
    meta: {
      id: 'outView',
      hasChildren: false,
      show: true,
      icon: 'icon-quanjufangda2',
      accessLevel: 0,
    },
    component: OutView
  },
  {
    path: '/TourView',
    name: '游览视图',
    meta: {
      id: 'TourView',
      hasChildren: false,
      show: true,
      icon: 'icon-liulan',
      accessLevel: 0,
    },
    component: TourView
  },
  // {
  //   path: '/PersonView',
  //   name: '人员视图',
  //   meta: {
  //     id: 'PersonView',
  //     hasChildren: false,
  //     show: true,
  //     icon: 'icon-renyuan-',
  //     accessLevel: 1,
  //   },
  //   component: PersonView
  // },
  {
    path: '/ConferenceRoomView',
    name: '会议室视图',
    meta: {
      id: 'ConferenceRoomView',
      hasChildren: false,
      show: true,
      icon: 'icon-huiyishi',
      accessLevel: 1,
    },
    component: ConferenceRoomView
  },
  {
    path: '/scene/:id',
    name: 'SceneView',
    component: () => import('../views/SceneView.vue'),
    meta: { accessLevel: 2 }
  },
  {
    path: '/admin',
    name: '系统管理',
    meta: {
      id: 'admin',
      hasChildren: true,
      icon: 'icon-xitongguanli',
      accessLevel: 2,
    },
    children: [
      {
        path: '/DeviceManager',
        name: '设备管理',
        meta: {
          id: 'DeviceManager',
          hasChildren: false,
          show: true,
          icon: 'icon-ico_shebeiguanli_shebeixinxiweihu',
          accessLevel: 2,
        },
        component: DeviceManager
      },
      {
        path: '/SceneManage',
        name: '场景管理',
        meta: {
          id: 'SceneManage',
          hasChildren: false,
          show: true,
          icon: 'icon-yonghu',
          accessLevel: 2,
        },
        component: SceneManage
      },
      {
        path: '/ModelEdit',
        name: '模型管理',
        meta: {
          id: 'ModelEdit',
          hasChildren: false,
          show: true,
          icon: 'icon-model-lib',
          accessLevel: 2,
        },
        component: ModelEdit
      },
      {
        path: '/StaffManage',
        name: '成员管理',
        meta: {
          id: 'StaffManage',
          hasChildren: false,
          show: true,
          icon: 'icon-yonghu',
          accessLevel: 2,
        },
        component: StaffManage
      },
      {
        path: '/UserInfo',
        name: '用户管理',
        meta: {
          id: 'UserInfo',
          hasChildren: false,
          show: true,
          icon: 'icon-yonghu',
          accessLevel: 2,
        },
        component: UserInfo
      },
      // {
      //   path: '/DeviceLog',
      //   name: '设备日志',
      //   meta: {
      //     id: 'DeviceLog',
      //     hasChildren: false,
      //     show: true,
      //     icon: 'icon-rizhi',
      //     accessLevel: 2,
      //   },
      //   component: DeviceLog
      // },
    ]
  },
  {
    path: '/UserLog',
    name: '用户登录',
    meta: {
      id: 'UserLog',
      hasChildren: false,
      show: false,
      accessLevel: 0,
    },
    component: UserLog
  },
  {
    path: '/',
    name: '用户登录',
    meta: {
      id: 'UserLog',
      hasChildren: false,
      show: false,
      accessLevel: 0,
    },
    component: UserLog
  },
  {
    path: '/UserReg',
    name: '用户注册',
    meta: {
      id: 'UserReg',
      hasChildren: false,
      show: false,
      accessLevel: 0,
    },
    component: UserReg
  },
  {
    path: '/UserCenter',
    name: '个人中心',
    meta: {
      id: 'UserCenter',
      hasChildren: false,
      show: false,
      accessLevel: 0,
    },
    component: UserCenter
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
