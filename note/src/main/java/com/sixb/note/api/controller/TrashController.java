package com.sixb.note.api.controller;

import com.sixb.note.api.service.TrashService;
import com.sixb.note.dto.trash.TrashRequestDto;
import com.sixb.note.dto.trash.TrashResponseDto;
import com.sixb.note.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
public class TrashController {

	private final TrashService trashService;

	@GetMapping
	public ResponseEntity<TrashResponseDto> getDeletedItems(@RequestParam long userId) {
		TrashResponseDto deletedItems = trashService.findDeletedItems(userId);
		return ResponseEntity.ok(deletedItems);
	}

	@PatchMapping
	public ResponseEntity<String> recoverItem(@RequestBody TrashRequestDto trashRequestDto) {
		try {
			trashService.recoverItem(trashRequestDto);
			return ResponseEntity.ok("복원 완료");
		} catch (NotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

}
