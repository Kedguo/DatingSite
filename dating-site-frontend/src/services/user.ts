import myAxios from "../plugins/myAxios.ts";
import { setCurrentUserState} from "../states/user.ts";

export const getCurrentUser = async() =>{
    const  res = await myAxios.get('/user/current');

    //如果之前已经存储过 currentUser就直接返回之前的数据，就算更新个人信息也不会显示最新的数据
     /*const currentUser = getCurrentUserState();
     if(currentUser){
         return currentUser;
    }*/

    // 不存在则从远程获取
    if(res.code === 0){
        setCurrentUserState(res.data);
        return res.data;
    }
    //没有用户信息返回空
    return null;
}
