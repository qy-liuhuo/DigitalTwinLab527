import request from '@/utils/request/request.js'
import qs from 'qs'

const http = {
    /**
     * methods: 请求
     * @param url 请求地址 
     * @param params 请求参数
     */
    get(url, params, showLoading = true) {
        const config = {
            method: 'get',
            url: url,
            showLoading
        }
        if (params) config.params = params
        return request(config)
    },
    post(url, params, showLoading = true) {
        const config = {
            method: 'post',
            url: url,
            showLoading
        }
        // if (params) config.data = qs.stringify(params)
        // console.log(config.data)
        if (params) config.data = JSON.stringify(params)
        return request(config)
    }
}
//导出
export default http