package de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.converters;

import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.dtos.BeverageDTO;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.dtos.IncentiveDTO;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.dtos.PromotionalGiftDTO;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.dtos.TrialPackageDTO;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.entities.BeverageEntity;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.entities.IncentiveEntity;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.entities.PromotionalGiftEntity;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.entities.TrialPackageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BeverageMapper extends GenericMapper<BeverageEntity, BeverageDTO>{

    @Override
    @Mapping(source = "incentiveEntity", target = "incentiveDTO")
    BeverageDTO convertEntityToDTO(BeverageEntity beverageEntity);

    @Override
    @Mapping(source = "incentiveDTO", target = "incentiveEntity")
    BeverageEntity convertDTOToEntity(BeverageDTO beverageDTO);

    default IncentiveDTO convertIncEntityToDTO(IncentiveEntity incentiveEntity){
        if (incentiveEntity == null){
            return null;
        }

        if (incentiveEntity.getClass().equals(PromotionalGiftEntity.class)){
            return promoEntityToDto((PromotionalGiftEntity) incentiveEntity);
        }
        else {
            return trialPackageEntityToDto((TrialPackageEntity) incentiveEntity);
        }
    }

    default IncentiveEntity convertIncDTOToEntity(IncentiveDTO incentiveDTO){
        if (incentiveDTO == null){
            return null;
        }

        if (incentiveDTO.getClass().equals(PromotionalGiftDTO.class)){
            return promoDtoToEntity((PromotionalGiftDTO)incentiveDTO);
        }
        else {
            return trialPackageDtoToEntity((TrialPackageDTO)incentiveDTO);
        }
    }

    PromotionalGiftEntity promoDtoToEntity(PromotionalGiftDTO dto);
    PromotionalGiftDTO promoEntityToDto(PromotionalGiftEntity entity);

    TrialPackageEntity trialPackageDtoToEntity(TrialPackageDTO dto);
    TrialPackageDTO trialPackageEntityToDto(TrialPackageEntity entity);
}
