<template>
    <!-- <button @click="changeSky('晴')">背景测试</button>
    <button @click="changeSky('多云')">背景测试</button> -->
    <!-- <el-card  modal="false" :close-on-click-modal="false" style="width:200px;text-align:left;">
        <template #header>
            <div class="card-header">
                <span>天气</span>
            </div>
        </template>
        <div class="text item">
            <p>地点：{{weatherInfo.city}}</p>
            <p>天气：{{weatherInfo.weather}}</p>
            <p>气温：{{weatherInfo.temperature}}℃</p>
            <p>湿度：{{weatherInfo.humidity}}%</p>
            <p>风向：{{weatherInfo.winddirection}}</p>
            <p>风力：{{weatherInfo.windpower}}</p>
        </div>
    </el-card> -->
    <div class="text item" style="padding-right: 30px;text-align: left; padding-bottom: 10px;">
            <p>地点：{{weatherInfo.city}}</p>
            <p>天气：{{weatherInfo.weather}}</p>
            <p>气温：{{weatherInfo.temperature}}℃</p>
            <p>湿度：{{weatherInfo.humidity}}%</p>
            <p>风向：{{weatherInfo.winddirection}}</p>
            <p>风力：{{weatherInfo.windpower}}</p>
    </div>
</template>

<script>
import axios from 'axios'
import * as THREE from 'three';
import http from '@/utils/request/http.js'
export default {
    data() {
        return{
            weatherInfo:{
                city:null,
                weather:null,
                temperature:null,
                humidity:null,
                winddirection:null,
                windpower:null
            }
        }
    },
    mounted() {
        this.getWeatherInfo();
        setInterval(()=>{
            this.getWeatherInfo();
        }, 1000*600); // 每隔 10分钟更新一次天气
    },
    methods: {
        setSky(texture) {
            const crt = new THREE.WebGLCubeRenderTarget(texture.image.height)
            crt.fromEquirectangularTexture(this.$parent.getRenderer(), texture)
            this.$parent.getScene().background = crt.texture
        },
        getWeatherInfo()
        {
            // 原接口失效，后期如有需要请更新后端接口
            // http.get("weather/getWeather",{},false).then(res=>{
            //     this.weatherInfo = res.data.weatherInfo;
            //     this.changeSky(this.weatherInfo.weather)
            // }).catch(err=>{
            //     console.log(err);
            // })
        },
        changeSky(weather){
            var myDate = new Date();
            var hours=myDate.getHours();//获取当前小时数(0-23)
            var skyName="day.png"
            if(hours>=5 && hours<=8){
                skyName="morning.png"
            }
            else if(hours>8&&hours<18){
                switch (weather) {
                    case "晴":
                        skyName = "sunny.png"
                        break;
                    case "阴":
                        skyName = "overcast.png"
                        break;
                    case "多云":
                        skyName = "cloudy.png"
                    case "雾":
                        skyName = "fog.png"
                        break;
                    case "沙尘暴":
                        skyName ="sandStorm.png"
                    default:
                        skyName = "day.png"
                }
            }
            else if(hours>=18 && hours <=19){
                skyName="evening.png"
            }
            else{
                skyName="night.png"
            }
            var textureLoader = new THREE.TextureLoader()
            textureLoader.load('/static/skybox/png/' + skyName,
                (texture) => {
                    this.setSky(texture)
                }
            )
        }
    }
}

</script>

<style>

</style>
