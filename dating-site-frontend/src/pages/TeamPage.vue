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

    <van-tabs v-model:active="active" @change="onTabChange">
      <van-tab title="公开" name="public"/>
      <van-tab title="加密"name="secret"/>
    </van-tabs>

    <van-button class="add-button" icon="plus" type="primary"  @click="toAddTeam"/>
    <team-card-list :teamList="teamList"/>
    <van-empty v-if="teamList?.length < 1" description="数据为空"/>
  </div>
</template>
<script setup lang="ts">
  import {useRouter} from "vue-router";
  import TeamCardList from "../components/TeamCardList.vue";
  import {onMounted,ref} from "vue";
  import myAxios from "../plugins/myAxios.ts";
  import {showFailToast} from "vant";

  const router = useRouter();
  const active=ref("public")
  const searchText = ref('')


  /**
   * 切换查询状态
   * @param name
   */
  const onTabChange = (name) =>{
    if(name === 'public'){
      //查公开
      listTeam(searchText.value,0)
    }else{
      //查加密
      listTeam(searchText.value,2);
    }
  }

  //跳转到队伍页
  const toAddTeam = () => {
    router.push({
      path:"team/add"
    })
  }
  /**
   * 搜索队伍
   * @type {Ref<UnwrapRef<*[]>>}
   */
  const teamList = ref([]);
  const listTeam = async (val = '',status = 0) => {
    const res = await myAxios.get("/team/list", {
      params: {
        searchText: val,
        pageNum: 1,
        status
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