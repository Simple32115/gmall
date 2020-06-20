package com.atguigu.gmall.pms.controller;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.service.SpuInfoService;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.itguigu.gmall.pms.entity.SpuInfoEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * spu信息; InnoDB free: 5120 kB
 *
 * @author MrZ
 * @email zww@atguigu.com
 * @date 2020-04-26 21:35:35
 */
@Api(tags = "spu信息; InnoDB free: 5120 kB 管理")
@RestController
@RequestMapping("pms/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${item.rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @RequestMapping()
    public Resp<PageVo> querySpuInfo(QueryCondition condition,
                                     @RequestParam("catId") Long catId) {

        PageVo pageVo = this.spuInfoService.queryspuInfo(condition, catId);

        return Resp.ok(pageVo);

    }

    @PostMapping("/page")
    public Resp<List<SpuInfoEntity>> querySpusByPage(@RequestBody QueryCondition queryCondition) {
        PageVo page = spuInfoService.queryPage(queryCondition);
        List<SpuInfoEntity> list = (List<SpuInfoEntity>)page.getList();
        return Resp.ok(list);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:spuinfo:list')")
    public Resp<PageVo> querySpuByPage(QueryCondition queryCondition) {
        PageVo page = spuInfoService.queryPage(queryCondition);

        return Resp.ok(page);
    }

    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('pms:spuinfo:info')")
    public Resp<SpuInfoEntity> info(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return Resp.ok(spuInfo);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:spuinfo:save')")
    public Resp<Object> save(@RequestBody SpuInfoVO spuInfoVO) {
        spuInfoService.saveSpuInfoVO(spuInfoVO);
        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:spuinfo:update')")
    public Resp<Object> update(@RequestBody SpuInfoEntity spuInfo) {
        spuInfoService.updateById(spuInfo);
        this.amqpTemplate.convertAndSend(EXCHANGE_NAME,"item.update",spuInfo.getId());
        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:spuinfo:delete')")
    public Resp<Object> delete(@RequestBody Long[] ids) {
        spuInfoService.removeByIds(Arrays.asList(ids));

        return Resp.ok(null);
    }

}
