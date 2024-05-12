package com.sixb.note.entity;

import com.sixb.note.common.BaseTimeEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;


@Getter
@Setter
@Node("Page")
public class Page extends BaseTimeEntity {

    @Id
    private String id;

    @Property("template")
    private String template;

    @Property("color")
    private String color;

    @Property("direction")
    private Integer direction;

    @Property("pdfUrl")
    private String pdfUrl;

    @Property("pdfPage")
    private Integer pdfPage;

    @Property("pageData")
    private String pageData;

    @Relationship(type = "NextPage")
    private Page NextPage;
}
