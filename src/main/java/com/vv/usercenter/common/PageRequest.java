package com.vv.usercenter.common;

import lombok.Data;
import org.apache.ibatis.javassist.SerialVersionUID;

import java.io.Serializable;

/**
 * 页面查询通用类
 *
 * @author vv先森
 * @create 2024-07-28 9:39
 */
@Data
public class PageRequest implements Serializable {

    private long pageSize = 10;

    private long pageNum = 1;


}
