package com.itguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品评价回复关系; InnoDB free: 5120 kB
 *
 * @author MrZ
 * @email zww@atguigu.com
 * @date 2020-04-26 21:35:35
 */
@ApiModel
@Data
@TableName("pms_comment_replay")
public class CommentReplayEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    @ApiModelProperty(name = "id", value = "id")
    private Long id;
    /**
     * 评论id
     */
    @ApiModelProperty(name = "commentId", value = "评论id")
    private Long commentId;
    /**
     * 回复id
     */
    @ApiModelProperty(name = "replyId", value = "回复id")
    private Long replyId;

}
