package com.scriptkill.controller;

import com.scriptkill.common.Result;
import com.scriptkill.entity.Clue;
import com.scriptkill.service.ClueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/director/clue")
public class DirectorClueController {

    @Autowired
    private ClueService clueService;

    @GetMapping("/script/{scriptId}")
    public Result<List<Clue>> listByScript(@PathVariable Long scriptId) {
        List<Clue> list = clueService.getByScriptId(scriptId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Clue> detail(@PathVariable Long id) {
        Clue clue = clueService.getById(id);
        return Result.success(clue);
    }

    @PostMapping
    public Result<Clue> create(@RequestBody Clue clue) {
        clueService.save(clue);
        return Result.success(clue);
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Clue clue) {
        boolean result = clueService.updateById(clue);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean result = clueService.removeById(id);
        return Result.success(result);
    }
}
