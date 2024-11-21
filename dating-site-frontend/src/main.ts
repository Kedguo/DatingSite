import { createApp } from 'vue';
import App from './App.vue'
import * as VueRouter from 'vue-router';
import Vant from 'vant'
import { Search } from 'vant';
import 'vant/lib/index.css';
import routes from "./configs/route.ts";
import '../global.css'
// 2. 引入组件样式
const app = createApp(App);
app.use(Vant)

const router = VueRouter.createRouter({
    // 4. 内部提供了 history 模式的实现。不使用hash模式了
    history: VueRouter.createWebHistory(),
    routes, // `routes: routes` 的缩写
})
app.use(router)
app.use(Search)
app.mount('#app')







