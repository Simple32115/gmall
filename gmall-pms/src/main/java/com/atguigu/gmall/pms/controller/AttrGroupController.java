package com.atguigu.gmall.pms.controller;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.itguigu.gmall.pms.entity.AttrGroupEntity;
import com.itguigu.gmall.pms.vo.ItemGroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 属性分组; InnoDB free: 5120 kB
 *
 * @author MrZ
 * @email zww@atguigu.com
 * @date 2020-04-26 21:35:35
 */
@Api(tags = "属性分组; InnoDB free: 5120 kB 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @GetMapping("item/group/{spuId}/{catalogId}")
    public Resp<List<ItemGroupVO>> queryItemGroupBySpuIdCatId(@PathVariable("spuId")Long spuId , @PathVariable("catalogId")Long catalogId){
        List<ItemGroupVO> itemGroupVOS = this.attrGroupService.queryItemGroupBySpuIdCatId(spuId,catalogId);

        return Resp.ok(itemGroupVOS);
    }


    @ApiOperation("根据三级分类id查询分组及组下的规格参数")
    @GetMapping("withattrs/cat/{catId}")
    public Resp<List<AttrGroupVO>> queryByCid(@PathVariable("catId") Long cid) {

        List<AttrGroupVO> attrGroupVOS =
                this.attrGroupService.queryByCid(cid);
        return Resp.ok(attrGroupVOS);
    }


    @GetMapping("withattr/{gid}")
    public Resp<AttrGroupVO> queryById(@PathVariable("gid") Long gid) {

        AttrGroupVO attrGroupvos =
                this.attrGroupService.queryByGroupId(gid);

        return Resp.ok(attrGroupvos);
    }

    /**
     * 查询三级分类下的分组
     *
     * @param condition
     * @param catId
     * @return
     */
    @RequestMapping("{catId}")
    public Resp<PageVo> queryGroupByPage(QueryCondition condition,
                                         @PathVariable("catId") Long catId) {
        PageVo pageVo =
                this.attrGroupService.queryGroupByPage(condition,
                        catId);
        return Resp.ok(pageVo);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup =
                attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}
