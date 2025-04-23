
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 考勤
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/kaoqin")
public class KaoqinController {
    private static final Logger logger = LoggerFactory.getLogger(KaoqinController.class);

    @Autowired
    private KaoqinService kaoqinService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private LaoshiService laoshiService;
    @Autowired
    private XueshengService xueshengService;

    @Autowired
    private FudaoyuanService fudaoyuanService;
    @Autowired
    private YujingService yujingService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("辅导员".equals(role)){
            FudaoyuanEntity userId = fudaoyuanService.selectById(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
            if(userId == null)
                return R.error("查不到当前登录的辅导员");
            params.put("banjiTypes",userId.getBanjiTypes());
        }
        else if("老师".equals(role)){
            LaoshiEntity userId = laoshiService.selectById(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
            if(userId == null)
                return R.error("查不到当前登录的老师");
            params.put("banjiTypes",userId.getBanjiTypes());
        }
        else if("学生".equals(role))
            params.put("xueshengId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = kaoqinService.queryPage(params);

        //字典表数据转换
        List<KaoqinView> list =(List<KaoqinView>)page.getList();
        for(KaoqinView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        KaoqinEntity kaoqin = kaoqinService.selectById(id);
        if(kaoqin !=null){
            //entity转view
            KaoqinView view = new KaoqinView();
            BeanUtils.copyProperties( kaoqin , view );//把实体数据重构到view中

                //级联表
                LaoshiEntity laoshi = laoshiService.selectById(kaoqin.getLaoshiId());
                if(laoshi != null){
                    BeanUtils.copyProperties( laoshi , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setLaoshiId(laoshi.getId());
                }
                //级联表
                XueshengEntity xuesheng = xueshengService.selectById(kaoqin.getXueshengId());
                if(xuesheng != null){
                    BeanUtils.copyProperties( xuesheng , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setXueshengId(xuesheng.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody KaoqinEntity kaoqin, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,kaoqin:{}",this.getClass().getName(),kaoqin.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("学生".equals(role))
            kaoqin.setXueshengId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
//        else if("老师".equals(role))
//            kaoqin.setLaoshiId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<KaoqinEntity> queryWrapper = new EntityWrapper<KaoqinEntity>()
            .eq("xuesheng_id", kaoqin.getXueshengId())
            .eq("kecheng_types", kaoqin.getKechengTypes())
            .eq("kaoqin_time", new SimpleDateFormat("yyyy-MM-dd").format(kaoqin.getKaoqinTime()))
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        KaoqinEntity kaoqinEntity = kaoqinService.selectOne(queryWrapper);
        if(kaoqinEntity==null){

            DictionaryEntity dictionaryEntity = dictionaryService.selectOne(new EntityWrapper<DictionaryEntity>().eq("dic_code", "kaoqin_erji_types").eq("code_index", kaoqin.getKaoqinErjiTypes()));
            if(dictionaryEntity == null)
                return R.error("该二级类型没有分值,请去基础数据管理中的考勤类型中核实");

            kaoqin.setKaoqinDefen(Double.valueOf(dictionaryEntity.getBeizhu()));
            kaoqin.setInsertTime(new Date());
            kaoqin.setCreateTime(new Date());
            kaoqinService.insert(kaoqin);


            YujingEntity yujingEntity = new YujingEntity();
            yujingEntity.setCreateTime(new Date());
            yujingEntity.setInsertTime(new Date());
            yujingEntity.setXueshengId(kaoqin.getXueshengId());
            yujingEntity.setKechengTypes(kaoqin.getKechengTypes());
            yujingEntity.setYujingContent("在"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date())+"增加了一条异常考勤记录,请关注");
            yujingService.insert(yujingEntity);



            return R.ok();
        }else {
            return R.error(511,"该学生的该课程的该天已有考勤记录");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody KaoqinEntity kaoqin, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,kaoqin:{}",this.getClass().getName(),kaoqin.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("学生".equals(role))
//            kaoqin.setXueshengId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
//        else if("老师".equals(role))
//            kaoqin.setLaoshiId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<KaoqinEntity> queryWrapper = new EntityWrapper<KaoqinEntity>()
            .notIn("id",kaoqin.getId())
            .andNew()
            .eq("xuesheng_id", kaoqin.getXueshengId())
            .eq("kecheng_types", kaoqin.getKechengTypes())
            .eq("kaoqin_time", new SimpleDateFormat("yyyy-MM-dd").format(kaoqin.getKaoqinTime()))
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        KaoqinEntity kaoqinEntity = kaoqinService.selectOne(queryWrapper);
        if(kaoqinEntity==null){

            DictionaryEntity dictionaryEntity = dictionaryService.selectOne(new EntityWrapper<DictionaryEntity>().eq("dic_code", "kaoqin_erji_types").eq("code_index", kaoqin.getKaoqinErjiTypes()));
            if(dictionaryEntity == null)
                return R.error("该二级类型没有分值,请去基础数据管理中的考勤类型中核实");
            kaoqin.setKaoqinDefen(Double.valueOf(dictionaryEntity.getBeizhu()));
            kaoqinService.updateById(kaoqin);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"该学生的该课程的该天已有考勤记录");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        kaoqinService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<KaoqinEntity> kaoqinList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("../../upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            KaoqinEntity kaoqinEntity = new KaoqinEntity();
//                            kaoqinEntity.setXueshengId(Integer.valueOf(data.get(0)));   //学生 要改的
//                            kaoqinEntity.setLaoshiId(Integer.valueOf(data.get(0)));   //老师 要改的
//                            kaoqinEntity.setKechengTypes(Integer.valueOf(data.get(0)));   //课程 要改的
//                            kaoqinEntity.setKaoqinTypes(Integer.valueOf(data.get(0)));   //考勤类型 要改的
//                            kaoqinEntity.setKaoqinErjiTypes(Integer.valueOf(data.get(0)));   //二级考勤类型 要改的
//                            kaoqinEntity.setKaoqinDefen(data.get(0));                    //本次考勤得分 要改的
//                            kaoqinEntity.setKaoqinContent("");//详情和图片
//                            kaoqinEntity.setKaoqinTime(sdf.parse(data.get(0)));          //考勤日期 要改的
//                            kaoqinEntity.setInsertTime(date);//时间
//                            kaoqinEntity.setCreateTime(date);//时间
                            kaoqinList.add(kaoqinEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        kaoqinService.insertBatch(kaoqinList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }






}
