<template>
  <div id="teamPage">
    <van-search
        v-model="searchText"
        show-action
        placeholder="搜索队伍"
        @search="onSearch"
        :clearable=true
    >
      <template #action>
        <van-button size="small" type="primary" plain  @click="onSearch(searchText)">搜索</van-button>
      </template>
    </van-search>



    <team-card-list :team-list="teamList"/>
    <van-empty v-if="teamList?.length < 1" description="数据为空"/>
  </div>
</template>
<script setup >
import {useRouter} from "vue-router";
import TeamCardList from "../components/TeamCardList.vue";
import {onMounted,ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast} from "vant";

const router = useRouter();
const searchText = ref('')

/**
 * 搜索队伍
 * @type {Ref<UnwrapRef<*[]>>}
 */
const teamList = ref([]);
const listTeam = async (val = '') => {
  const res = await myAxios.get("/team/list/my/join", {
    params: {
      searchText: val,
      pageNum: 1,
    },
  });
  if (res?.code === 0) {
    teamList.value = res.data;
  } else {
    showFailToast('加载队伍失败，请刷新重试')
  }
}
onMounted(() => {
  listTeam();
})
const onSearch = (val) => {
  listTeam(val);
}

</script>

<style scoped>

</style>