/**
 * 用户类别
 */
export type userType= {
    id: number;
    userName: string;
    userAccount: string;
    gender: number;
    avatarUrl?: string;
    phone: string;
    profile ?: string;
    email: string;
    userState: string;
    userRole: string;
    tags: string[];
    createTime: Date;
    planetCode:string;
}