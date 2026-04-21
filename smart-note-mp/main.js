import App from './App'
import uviewPlus from 'uview-plus' // 引入 uView-plus 组件库

// #ifndef VUE3
// 由于你创建的是 Vue3 项目，这段代码逻辑其实不会执行，但为了结构完整予以保留
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'

export function createApp() {
  const app = createSSRApp(App)
  
  // 使用 uview-plus 插件
  app.use(uviewPlus)
  
  return {
    app
  }
}
// #endif