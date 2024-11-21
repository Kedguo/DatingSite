<template>
  <van-skeleton title avatar :row="3" :loading="props.loading" v-for="user in userList">
    <van-card
        :desc="user.profile"
        :title="`${user.userName} (${'ID:'+user.id})`"
        :thumb="user.avatarUrl"
    >
      <template #tags>
        <van-tag plain type="danger" v-for="tag in user.tags" style="margin-right: 8px; margin-top: 8px" >
          {{tag}}
        </van-tag>
      </template>
      <template #footer>
        <van-button size="mini">联系我</van-button>
      </template>
    </van-card>
  </van-skeleton>
</template>

<script setup lang="ts">
import {userType} from "../models/user";
interface UserCardListProps{
  userList: userType[];
  loading:boolean;
}
const props = withDefaults(defineProps<UserCardListProps>(),{
  loading:true,
  //@ts-ignore
  userList: [] as userType[]
});
</script>
<style scoped>

</style>

