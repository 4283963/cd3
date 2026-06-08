package com.scriptkill.vo;

import java.io.Serializable;

public class PlayerClueVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long clueId;

    private String name;

    private Integer type;

    private String content;

    private String resourceUrl;

    private Integer isUnlocked;

    private String unlockHint;

    private Integer level;

    private Long parentId;

    private Boolean hasChildren;

    private Integer isPuzzle;

    private Integer puzzleStatus;

    private String puzzleStartTime;

    private Integer puzzleTimeLimit;

    private Integer puzzleRows;

    private Integer puzzleCols;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClueId() {
        return clueId;
    }

    public void setClueId(Long clueId) {
        this.clueId = clueId;
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

    public Integer getIsUnlocked() {
        return isUnlocked;
    }

    public void setIsUnlocked(Integer isUnlocked) {
        this.isUnlocked = isUnlocked;
    }

    public String getUnlockHint() {
        return unlockHint;
    }

    public void setUnlockHint(String unlockHint) {
        this.unlockHint = unlockHint;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public Integer getIsPuzzle() {
        return isPuzzle;
    }

    public void setIsPuzzle(Integer isPuzzle) {
        this.isPuzzle = isPuzzle;
    }

    public Integer getPuzzleStatus() {
        return puzzleStatus;
    }

    public void setPuzzleStatus(Integer puzzleStatus) {
        this.puzzleStatus = puzzleStatus;
    }

    public String getPuzzleStartTime() {
        return puzzleStartTime;
    }

    public void setPuzzleStartTime(String puzzleStartTime) {
        this.puzzleStartTime = puzzleStartTime;
    }

    public Integer getPuzzleTimeLimit() {
        return puzzleTimeLimit;
    }

    public void setPuzzleTimeLimit(Integer puzzleTimeLimit) {
        this.puzzleTimeLimit = puzzleTimeLimit;
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
}
