package com.grash.repository;

import com.grash.model.Relation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface RelationRepository extends JpaRepository<Relation, Long> {
    @Query("SELECT r from Relation r where r.child.company.id = :x ")
    Collection<Relation> findByCompany_Id(@Param("x") Long id);

    Collection<Relation> findByParent_Id(Long id);

    Collection<Relation> findByChild_Id(Long id);

    Collection<Relation> findByParent_IdAndChild_Id(Long parentId, Long childId);
}
