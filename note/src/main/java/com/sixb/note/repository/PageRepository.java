package com.sixb.note.repository;

import com.sixb.note.entity.Page;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PageRepository extends Neo4jRepository<Page, String> {

    @Query("MATCH (p) WHERE p.isDeleted = true RETURN p")
    List<Page> findDeletedPages();

    @Query("MATCH (p:Page) WHERE p.id = $pageId RETURN p")
    Page findPageById(@Param("pageId") String pageId);

    @Query("MATCH (u:User {id: $userId})-[:Like]->(p:Page) RETURN p")
    List<Page> findAllLikedPagesByUserId(@Param("userId") String userId);

    @Query("MATCH (u:User {id: $userId})-[r:Like]->(p:Page {id: $itemId}) DELETE r")
    void deleteLikePage(@Param("userId") String userId, @Param("itemId") String itemId);
}
