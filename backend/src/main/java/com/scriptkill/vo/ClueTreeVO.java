package com.scriptkill.vo;

import java.io.Serializable;
import java.util.List;

public class ClueTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long scriptId;

    private Long parentId;

    private String name;

    private Integer type;

    private String content;

    private String resourceUrl;

    private String unlockPassword;

    private String unlockHint;

    private Integer isPublic;

    private Integer level;

    private Integer sortOrder;

    private Integer isPuzzle;

    private Integer puzzleRows;

    private Integer puzzleCols;

    private Integer puzzleTimeLimit;

    private List<ClueTreeVO> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScriptId() {
        return scriptId;
    }

    public void setScriptId(Long scriptId) {
        this.scriptId = scriptId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getUnlockPassword() {
        return unlockPassword;
    }

    public void setUnlockPassword(String unlockPassword) {
        this.unlockPassword = unlockPassword;
    }

    public String getUnlockHint() {
        return unlockHint;
    }

    public void setUnlockHint(String unlockHint) {
        this.unlockHint = unlockHint;
    }

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getIsPuzzle() {
        return isPuzzle;
    }

    public void setIsPuzzle(Integer isPuzzle) {
        this.isPuzzle = isPuzzle;
    }

    public Integer getPuzzleRows() {
        return puzzleRows;
    }

    public void setPuzzleRows(Integer puzzleRows) {
        this.puzzleRows = puzzleRows;
    }

    public Integer getPuzzleCols() {
        return puzzleCols;
    }

    public void setPuzzleCols(Integer puzzleCols) {
        this.puzzleCols = puzzleCols;
    }

    public Integer getPuzzleTimeLimit() {
        return puzzleTimeLimit;
    }

    public void setPuzzleTimeLimit(Integer puzzleTimeLimit) {
        this.puzzleTimeLimit = puzzleTimeLimit;
    }

    public List<ClueTreeVO> getChildren() {
        return children;
    }

    public void setChildren(List<ClueTreeVO> children) {
        this.children = children;
    }
}
