<template>
  <!--  form表单组件-->
  <van-form @submit="onSubmit">
    <van-field
        v-model="editUser.currentValue"
        :name="editUser.editKey"
        :label="editUser.editName"
        :placeholder="`请输入${editUser.editName}`"

    />
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>

</template>
<script setup >
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";
import {getCurrentUser} from "../services/user.ts";


const route = useRoute();
const router = useRouter();
const editUser = ref({
  editKey : route.query.editKey,
  currentValue : route.query.currentValue,
  editName : route.query.editName,
})
const onSubmit = async () => {
  //获取用户信息，写在onSumbit外面加载不出来
  const currentUser = await getCurrentUser();
  if(!currentUser){
    showFailToast('用户未登录')
    return;
  }

   const res = await myAxios.post('/user/update',{
     id : currentUser.id,
    [editUser.value.editKey ]: editUser.value.currentValue,
  })
  if(res.code === 0 && res.data > 0){
    showSuccessToast('修改成功');
    router.back();
  }else{
    showFailToast('更新失败');
  }
};


</script>



<style scoped>

</style>