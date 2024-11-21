import {userType} from "../models/user";

//使用该文件存储已经登录的用户的信息状态
let currentUser : userType;
//设置值
const setCurrentUserState = (user : userType) => {
    currentUser = user;
}
//取值
const getCurrentUserState = () :  userType =>{
    return currentUser;
}
export {
    setCurrentUserState,
    getCurrentUserState,
}