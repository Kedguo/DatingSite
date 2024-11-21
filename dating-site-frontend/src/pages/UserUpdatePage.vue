<template>
  <div v-if="user"> <!--如果用户不存在，就不显示-->
    <!--  个人信息显示 Cell单元格-->
    <van-cell title="昵称" is-link to="/user/edit" :value="user.userName " @click="toEdit('userName','昵称',user.userName)"/>
    <van-cell title="账户"  :value="user.userAccount" />
    <van-cell title="头像" is-link to="/user/edit" arrow-direction="down" :value="user.avatarUrl" >
      <img style="height: 48px" :src="user.avatarUrl">
    </van-cell>
    <van-cell title="性别" is-link to="/user/edit" arrow-direction="down" :value="user.gender" @click="toEdit('gender','性别',user.gender)"/>
    <van-cell title="电话" is-link to="/user/edit" arrow-direction="down" :value="user.phone" @click="toEdit('phone','电话',user.phone)"/>
    <van-cell title="邮箱" is-link to="/user/edit" arrow-direction="down" :value="user.email" @click="toEdit('email','邮箱',user.email)"/>
    <van-cell title="星球编号" :value="user.planetCode" />
    <van-cell title="注册时间" :value="user.createTime" />
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
