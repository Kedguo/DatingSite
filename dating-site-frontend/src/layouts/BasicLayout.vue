<template xmlns:id="http://www.w3.org/1999/xhtml">
  <van-nav-bar
      :title="title"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
      @click-right="onClickRight"
      >
    <template #right>
      <van-icon name="search" size="18" />
    </template>
  </van-nav-bar>

  <div id="contend">
  <router-view />
  </div>

  <van-tabbar route @change="onChange">
    <van-tabbar-item to="/" icon="home-o" name="index">主页</van-tabbar-item>
    <van-tabbar-item  to="/team"icon="search"name="team">队伍</van-tabbar-item>
    <van-tabbar-item  to="/user"icon="friends-o"name="user">个人</van-tabbar-item>
  </van-tabbar>
</template>

<script setup >
import {useRouter} from "vue-router";
import routes from "../configs/route.ts";
import {ref} from 'vue';

const router = useRouter();
const DEFAULT_TITLE = 'Dating-site交友';
const title = ref(DEFAULT_TITLE);
/**
 * 根据路由切换标题
 */
router.beforeEach(( to,from)=> {
  const toPath =to.path;
  const route = routes.find( (route) =>{
   return toPath == route.path;
  })
  title.value = route?.title ?? DEFAULT_TITLE;
})
const onClickLeft = () => {
  router.back();
};
const onClickRight = () =>{
  router.push('/search')
};
</script>

<style scoped>
/*在公共布局加上该距离，所有的页面都会加上该间距*/
#contend {
  padding-bottom: 50px;
}
</style>