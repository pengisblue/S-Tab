package com.sixb.note.api.service;


import com.sixb.note.dto.note.CreateNoteRequestDto;
import com.sixb.note.dto.note.CreateNoteResponseDto;
import com.sixb.note.dto.pageData.PageDataDto;
import com.sixb.note.entity.*;
import com.sixb.note.exception.NotFoundException;
import com.sixb.note.repository.*;
import com.sixb.note.util.IdCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final FolderRepository folderRepository;
    private final PageRepository pageRepository;
    private final SpaceRepository spaceRepository;


    public CreateNoteResponseDto createNote(CreateNoteRequestDto request) {
        // 새로운 노트 생성
        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setTotalPageCnt(1);// 초기 페이지 수는 1으로 설정

        String formattedNoteId = IdCreator.create("n");
//        UUID formattedNoteId = UUID.randomUUID();
        note.setNoteId(formattedNoteId);
        // 새로운 페이지 생성
        Page page = new Page();
        String formattedPageId = IdCreator.create("p");
        page.setPageId(formattedPageId);
        page.setNoteId(formattedNoteId);
        page.setTemplate(String.valueOf(request.getTemplate()));
        page.setColor(String.valueOf(request.getColor()));
        page.setDirection(request.getDirection());
        page.setPageData("{\"paths\": [], \"figures\": [],\"textBoxes\": [], \"images\": []}");

        LocalDateTime now = LocalDateTime.now();
        page.setCreatedAt(now);
        page.setUpdatedAt(now);
        note.setCreatedAt(now);
        note.setUpdatedAt(now);

        // 노트와 페이지 연결
        note.setPage(page);

        String parentFolderId = request.getParentFolderId();

        if (parentFolderId != null) {
            // 부모 폴더 ID가 주어진 경우
            Folder optionalFolder = folderRepository.findFolderById(parentFolderId);
            if (optionalFolder!=null) {
                // 부모 폴더가 있는 경우 노트를 해당 폴더에 연결
                Folder parentFolder = optionalFolder;
                parentFolder.getNotes().add(note);
                folderRepository.save(parentFolder);
            } else {
                // 부모 폴더를 찾지 못한 경우에 대한 처리
                String spaceId = parentFolderId;
                if (spaceId != null) {
                    // spaceId로 스페이스를 찾아서 노트를 연결
                    Space optionalSpace = spaceRepository.findSpaceById(spaceId);
                    if (optionalSpace != null) {
                        optionalSpace.getNotes().add(note);
                        spaceRepository.save(optionalSpace);
                    }
                }
            }
        }
        // 저장
        noteRepository.save(note);

        // mongodb에 데이터 만들기
        // 필기 데이터 저장
//        PageDataDto pageData = PageDataDto.builder()
//                .id(formattedPageId)
//                .build();
//
//        pageDataRepository.save(pageData);


        // 응답 DTO 구성
        CreateNoteResponseDto response = new CreateNoteResponseDto();
        response.setNoteId(note.getNoteId());
        response.setTitle(note.getTitle());
        response.setTotalPageCnt(note.getTotalPageCnt());
        response.setLiked(false);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        response.setIsDeleted(false);

        // 페이지 DTO 설정
        CreateNoteResponseDto.PageDto pageDto = new CreateNoteResponseDto.PageDto();
        pageDto.setPageId(page.getPageId());
        pageDto.setColor(request.getColor());
        pageDto.setTemplate(request.getTemplate());
        pageDto.setDirection(request.getDirection());
        pageDto.setBookmarked(false);
        pageDto.setCreatedAt(LocalDateTime.now());
        pageDto.setUpdatedAt(LocalDateTime.now());
        pageDto.setIsDeleted(false);

        response.setPage(pageDto);

        return response;
    }

    //노트 이름 변경
    public void updateNoteTitle(String noteId, String newTitle) throws NotFoundException {
        Note note = noteRepository.findNoteById(noteId);
        if (note == null) {
            throw new NotFoundException("노트 이름 수정 실패");
        }
        noteRepository.updateNoteTitle(noteId, newTitle);
    }

    //노트 삭제
    public boolean deleteNote(String noteId) {
        Note note = noteRepository.findNoteById(noteId);
        if (note != null) {
            note.setIsDeleted(true);
            noteRepository.save(note);
            return true;
        }
        return false;
    }
}
