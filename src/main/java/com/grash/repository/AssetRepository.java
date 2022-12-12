package com.grash.repository;

import com.grash.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Collection<Asset> findByCompany_Id(Long id);

    Collection<Asset> findByParentAsset_Id(Long id);

    Collection<Asset> findByLocation_Id(Long id);
}

