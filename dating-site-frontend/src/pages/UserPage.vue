<template>
  <div v-if="user"> <!--如果用户不存在，就不显示-->
    <van-cell title="昵称" :value="user?.userName" />
    <van-cell title="账户"  :value="user.userAccount" />
    <van-cell title="头像"  arrow-direction="down" :value="user.avatarUrl" >
      <img style="height: 48px" :src="user.avatarUrl">
    </van-cell>
    <van-cell title="修改信息" is-link to="/user/update" />
    <van-cell title="我创建的队伍" is-link to="/user/team/create" />
    <van-cell title="我加入的队伍" is-link to="/user/team/join" />
  </div>

</template>
<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted,ref} from "vue";
import {getCurrentUser} from "../services/user.ts";

const user = ref();
//async是JavaScript中的一个关键字，用于声明一个异步函数，异步函数允许你使用await关键字等待异步操作的完成，而不会阻塞代码的执行
onMounted(async  ()=> {
  user.value = await getCurrentUser();

})
//跳转方法
const router = useRouter();
const toEdit = (editKey: string,editName: string,currentValue: string) =>{
  router.push({
    path:'/user/edit',
    query:{
      editKey,
      editName,
      currentValue,
    }
  })
}
</script>
<style scoped>

</style>
