package com.sixb.note.dto.folder;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateFolderRequestDto {

    private UUID parentFolderId;
    private String title;

}