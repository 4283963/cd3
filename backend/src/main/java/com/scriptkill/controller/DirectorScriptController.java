package com.scriptkill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scriptkill.common.Result;
import com.scriptkill.entity.Script;
import com.scriptkill.entity.ScriptRole;
import com.scriptkill.service.ScriptRoleService;
import com.scriptkill.service.ScriptService;
import com.scriptkill.vo.ClueTreeVO;
import com.scriptkill.vo.ScriptDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/director/script")
public class DirectorScriptController {

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private ScriptRoleService scriptRoleService;

    @Autowired
    private com.scriptkill.service.ClueService clueService;

    @GetMapping("/page")
    public Result<Page<Script>> page(@RequestParam(defaultValue = "1") Long current,
                                      @RequestParam(defaultValue = "10") Long size,
                                      @RequestParam(required = false) String keyword) {
        Page<Script> page = scriptService.getPage(current, size, keyword);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<ScriptDetailVO> detail(@PathVariable Long id) {
        ScriptDetailVO vo = scriptService.getDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/{scriptId}/roles")
    public Result<List<ScriptRole>> roles(@PathVariable Long scriptId) {
        List<ScriptRole> roles = scriptRoleService.getByScriptId(scriptId);
        return Result.success(roles);
    }

    @GetMapping("/{scriptId}/clue-tree")
    public Result<List<ClueTreeVO>> clueTree(@PathVariable Long scriptId) {
        List<ClueTreeVO> tree = clueService.getClueTree(scriptId);
        return Result.success(tree);
    }

    @PostMapping
    public Result<Script> create(@RequestBody Script script) {
        scriptService.save(script);
        return Result.success(script);
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Script script) {
        boolean result = scriptService.updateById(script);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean result = scriptService.removeById(id);
        return Result.success(result);
    }
}
