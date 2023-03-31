package com.grash.repository;

import com.grash.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
    Collection<Asset> findByCompanyId(Long id);

    Collection<Asset> findByParentAsset_Id(Long id);

    Collection<Asset> findByLocation_Id(Long id);

    Optional<Asset> findByNameAndCompanyId(String assetName, Long companyId);

    Optional<Asset> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Asset> findByNfcIdAndCompanyId(String nfcId, Long companyId);

    Optional<Asset> findByBarCodeAndCompanyId(String data, Long id);
}

