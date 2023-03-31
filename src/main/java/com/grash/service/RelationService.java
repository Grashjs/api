package com.grash.service;

import com.grash.dto.RelationPatchDTO;
import com.grash.dto.RelationPostDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.RelationMapper;
import com.grash.model.OwnUser;
import com.grash.model.Relation;
import com.grash.model.WorkOrder;
import com.grash.model.enums.RelationType;
import com.grash.model.enums.RelationTypeInternal;
import com.grash.model.enums.RoleType;
import com.grash.repository.RelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RelationService {
    private final RelationRepository relationRepository;
    private final WorkOrderService workOrderService;
    private final RelationMapper relationMapper;
    private final EntityManager em;

    @Transactional
    public Relation create(Relation relation) {
        Relation savedRelation = relationRepository.saveAndFlush(relation);
        em.refresh(savedRelation);
        return savedRelation;
    }


    @Transactional
    public Relation update(Long id, RelationPatchDTO relation) {
        if (relationRepository.existsById(id)) {
            Relation savedRelation = relationRepository.findById(id).get();
            Relation updatedRelation = relationRepository.saveAndFlush(relationMapper.updateRelation(savedRelation, relation));
            em.refresh(updatedRelation);
            return updatedRelation;
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Relation> getAll() {
        return relationRepository.findAll();
    }

    public void delete(Long id) {
        relationRepository.deleteById(id);
    }

    public Optional<Relation> findById(Long id) {
        return relationRepository.findById(id);
    }

    public Collection<Relation> findByCompany(Long id) {
        return relationRepository.findByCompanyId(id);
    }

    public boolean hasAccess(OwnUser user, Relation relation) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(relation.getChild().getCompanyId());
    }

    public boolean canCreate(OwnUser user, RelationPostDTO relationReq) {
        return canPatch(user, relationMapper.toPatchDto(relationReq));
    }

    public boolean canPatch(OwnUser user, RelationPatchDTO relationReq) {
        Long companyId = user.getCompany().getId();
        boolean first = workOrderService.isWorkOrderInCompany(relationReq.getParent(), companyId, true);
        boolean second = workOrderService.isWorkOrderInCompany(relationReq.getChild(), companyId, true);
        return first && second;
    }

    public Relation createPost(RelationPostDTO relationReq) {
        WorkOrder parent = relationReq.getParent();
        WorkOrder child = relationReq.getChild();
        RelationTypeInternal relationType = getRelationTypeInternal(relationReq.getRelationType());

        Collection<RelationType> toReverse = Arrays.asList(RelationType.BLOCKED_BY, RelationType.DUPLICATED_BY, RelationType.SPLIT_TO);
        if (toReverse.contains(relationReq.getRelationType())) {
            WorkOrder intermediate = child;
            child = parent;
            parent = intermediate;
        }
        Relation relation = Relation.builder()
                .parent(parent)
                .child(child)
                .relationType(relationType).build();
        relation.setCompanyId(relationReq.getCompany().getId());
        return create(relation);
    }

    private RelationTypeInternal getRelationTypeInternal(RelationType relationType) {
        switch (relationType) {
            case DUPLICATE_OF:
                return RelationTypeInternal.DUPLICATE_OF;
            case RELATED_TO:
                return RelationTypeInternal.RELATED_TO;
            case SPLIT_FROM:
                return RelationTypeInternal.SPLIT_FROM;
            case BLOCKS:
                return RelationTypeInternal.BLOCKS;
            case BLOCKED_BY:
                return RelationTypeInternal.BLOCKS;
            case DUPLICATED_BY:
                return RelationTypeInternal.DUPLICATE_OF;
            case SPLIT_TO:
                return RelationTypeInternal.SPLIT_FROM;
            default:
                return RelationTypeInternal.RELATED_TO;
        }
    }

    public Collection<Relation> findByWorkOrder(Long id) {
        Collection<Relation> whereParent = relationRepository.findByParent_Id(id);
        Collection<Relation> whereChild = relationRepository.findByChild_Id(id);

        return Stream.concat(whereParent.stream(), whereChild.stream())
                .collect(Collectors.toList());
    }

    public Collection<Relation> findByParentAndChild(Long parentId, Long childId) {
        return relationRepository.findByParent_IdAndChild_Id(parentId, childId);
    }

}
