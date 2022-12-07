package com.xh.es.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.script.ScriptType;

import java.util.Map;

/**
 * @description: 想要使用Script 请先了解基础语法, 这里我参考了该博客 https://www.jianshu.com/p/7c77f0a73f7b
 *               ,在此感谢 {xingchendahai} 作者 -> url: https://www.jianshu.com/u/4424305b60b4
 *
 *    为了满足 {@link org.elasticsearch.script.Script} 的条件,
 *   构造 Script(ScriptType type, String lang, String idOrCode, Map<String, Object> params)
 *   自定义该对象内容参考 {@link com.blacktea.es.util.ESUtil} -> scriptCombination(Object var2)方法
 * @author: black tea
 * @date: 2021/9/10 11:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScriptDTO {

    private ScriptType type;

    private String lang;

    private String script;

    private Map<String,Object> params;


}
