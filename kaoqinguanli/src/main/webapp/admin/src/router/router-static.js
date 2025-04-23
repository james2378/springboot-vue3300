import Vue from 'vue';
//配置路由
import VueRouter from 'vue-router'
Vue.use(VueRouter);
//1.创建组件
import Index from '@/views/index'
import Home from '@/views/home'
import Login from '@/views/login'
import NotFound from '@/views/404'
import UpdatePassword from '@/views/update-password'
import pay from '@/views/pay'
import register from '@/views/register'
import center from '@/views/center'

     import users from '@/views/modules/users/list'
    import dictionary from '@/views/modules/dictionary/list'
    import fudaoyuan from '@/views/modules/fudaoyuan/list'
    import gonggao from '@/views/modules/gonggao/list'
    import kaoqin from '@/views/modules/kaoqin/list'
    import laoshi from '@/views/modules/laoshi/list'
    import xuesheng from '@/views/modules/xuesheng/list'
    import yujing from '@/views/modules/yujing/list'
    import dictionaryBanji from '@/views/modules/dictionaryBanji/list'
    import dictionaryGonggao from '@/views/modules/dictionaryGonggao/list'
    import dictionaryKaoqin from '@/views/modules/dictionaryKaoqin/list'
    import dictionaryKaoqinErji from '@/views/modules/dictionaryKaoqinErji/list'
        import dictionaryKaoqinErjiAddOrUpdate from '@/views/modules/dictionaryKaoqinErji/add-or-update'//二级
    import dictionaryKecheng from '@/views/modules/dictionaryKecheng/list'
    import dictionarySex from '@/views/modules/dictionarySex/list'





//2.配置路由   注意：名字
const routes = [{
    path: '/index',
    name: '首页',
    component: Index,
    children: [{
      // 这里不设置值，是把main作为默认页面
      path: '/',
      name: '首页',
      component: Home,
      meta: {icon:'', title:'center'}
    }, {
      path: '/updatePassword',
      name: '修改密码',
      component: UpdatePassword,
      meta: {icon:'', title:'updatePassword'}
    }, {
      path: '/pay',
      name: '支付',
      component: pay,
      meta: {icon:'', title:'pay'}
    }, {
      path: '/center',
      name: '个人信息',
      component: center,
      meta: {icon:'', title:'center'}
    } ,{
        path: '/users',
        name: '管理信息',
        component: users
      }
    ,{
        path: '/dictionaryBanji',
        name: '班级',
        component: dictionaryBanji
    }
    ,{
        path: '/dictionaryGonggao',
        name: '公告类型',
        component: dictionaryGonggao
    }
    ,{
        path: '/dictionaryKaoqin',
        name: '考勤类型',
        component: dictionaryKaoqin
    }
    ,{
        path: '/dictionaryKaoqinErji',
        name: '二级考勤类型',
        component: dictionaryKaoqinErji
    }
    ,{
        path: '/dictionaryKaoqinErjiAddOrUpdate',
        name: '二级考勤类型的新增修改页面',
        component: dictionaryKaoqinErjiAddOrUpdate
    }
    ,{
        path: '/dictionaryKecheng',
        name: '课程',
        component: dictionaryKecheng
    }
    ,{
        path: '/dictionarySex',
        name: '性别类型',
        component: dictionarySex
    }


    ,{
        path: '/dictionary',
        name: '字典',
        component: dictionary
      }
    ,{
        path: '/fudaoyuan',
        name: '辅导员',
        component: fudaoyuan
      }
    ,{
        path: '/gonggao',
        name: '公告',
        component: gonggao
      }
    ,{
        path: '/kaoqin',
        name: '考勤',
        component: kaoqin
      }
    ,{
        path: '/laoshi',
        name: '老师',
        component: laoshi
      }
    ,{
        path: '/xuesheng',
        name: '学生',
        component: xuesheng
      }
    ,{
        path: '/yujing',
        name: '预警',
        component: yujing
      }


    ]
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: {icon:'', title:'login'}
  },
  {
    path: '/register',
    name: 'register',
    component: register,
    meta: {icon:'', title:'register'}
  },
  {
    path: '/',
    name: '首页',
    redirect: '/index'
  }, /*默认跳转路由*/
  {
    path: '*',
    component: NotFound
  }
]
//3.实例化VueRouter  注意：名字
const router = new VueRouter({
  mode: 'hash',
  /*hash模式改为history*/
  routes // （缩写）相当于 routes: routes
})

export default router;
