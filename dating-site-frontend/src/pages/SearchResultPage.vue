<template>
    <user-card-list :user-list="userList" :loading="loading" />
    <van-empty v-if="!userList || userList.length < 1" description="搜索结果为空" />
</template>

<script setup >
import {onMounted, ref} from "vue";
import {useRoute} from "vue-router";
import myAxios from "../plugins/myAxios.ts";
import {Toast} from "vant";
import qs from 'qs';
import UserCardList from "../components/UserCardList.vue";


const route = useRoute();
const {tags} = route.query;

const userList = ref([]);
const loading = ref(true);  // 初始化加载状态为true

onMounted(async () => {
  try {
    const response = await myAxios.get('/user/search/tags', {
      params: { tagNameList: tags },
      paramsSerializer: params => qs.stringify(params, { indices: false })
    });
    console.log('/user/search/tags succeed', response);
    const userListData = response.data;
    if (userListData) {
      userList.value = userListData.map(user => ({
        ...user,
        tags: JSON.parse(user.tags || '[]')  // 安全地解析标签，防止解析错误
      }));
    }
    loading.value = false;  // 数据加载完成，设置加载状态为false
  } catch (error) {
    console.error('/user/search/tags failed', error);
    Toast.fail('请求失败');
    loading.value = false;  // 确保即使在出错时也能更新加载状态
  }
});


</script>

<style scoped>

</style>
