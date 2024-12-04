import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import _ from 'lodash';
var baseURL = '';
if (process.env.VUE_APP_HOST == "dtlab.qylh.xyz:9003") {
    baseURL = 'https://dtlab.qylh.xyz/dtlab'
}
else {
    //baseURL = 'http://' + process.env.VUE_APP_HOST + '/dtlab'
    baseURL = 'http://' + process.env.VUE_APP_HOST

}
const service = axios.create({
    baseURL: baseURL,
    timeout: 10000,
    headers: {
        // 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
        'Content-Type' : "application/json"
    },
    withCredentials: true
})

//loading对象
let loading;

//当前正在请求的数量
let needLoadingRequestCount = 0;

//显示loading
function showLoading(target) {
    // 后面这个判断很重要，因为关闭时加了抖动，此时loading对象可能还存在，
    // 但needLoadingRequestCount已经变成0.避免这种情况下会重新创建个loading
    if (needLoadingRequestCount === 0 && !loading) {
        loading = ElLoading.service({
            lock: true,
            text: "Loading...",
            background: 'rgba(255, 255, 255, 0.5)',
            target: target || "body"
        });
    }
    needLoadingRequestCount++;
}

//隐藏loading
function hideLoading() {
    needLoadingRequestCount--;
    needLoadingRequestCount = Math.max(needLoadingRequestCount, 0); //做个保护
    if (needLoadingRequestCount === 0) {
        //关闭loading
        toHideLoading();
    }
}

//防抖：将 300ms 间隔内的关闭 loading 便合并为一次。防止连续请求时， loading闪烁的问题。
var toHideLoading = _.debounce(() => {
    if (loading != null) {
        loading.close();
    }
    loading = null;
}, 300);


// 请求拦截器
service.interceptors.request.use(
    config => {
        //判断当前请求是否设置了不显示Loading
        if (config.showLoading !== false) {
            showLoading(config.headers.loadingTarget);
        }
        return config
    },
    error => {
        //判断当前请求是否设置了不显示Loading
        if (config.showLoading !== false) {
            hideLoading();
        }
        return Promise.reject(error)
    })



// 响应拦截器
service.interceptors.response.use(
    response => {
        hideLoading();
        return Promise.resolve(response.data)
    },
    error => {
        hideLoading();
        //错误信息
        ElMessage({
            message: error.message,
            type: 'error',
            duration: 3 * 1000
        })
        return Promise.reject(error)
    })
//导出
export default service