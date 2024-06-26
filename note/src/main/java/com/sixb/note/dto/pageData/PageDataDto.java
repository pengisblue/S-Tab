package com.sixb.note.dto.pageData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDataDto {

	private List<PathDto> paths;
	private List<FigureDto> figures;
	private List<TextBoxDto> textBoxes;
	private List<ImageDto> images;

}
