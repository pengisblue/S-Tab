package com.sixb.note.dto.folder;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class FolderListResponseDto {
    private List<FolderInfo> folders;

    @Getter
    @Setter
    public static class FolderInfo {
        private String folderId;
        private String title;
    }
}