package com.codigo.msexamenexp.util;


import com.codigo.msexamenexp.aggregates.constants.Constants;
import com.codigo.msexamenexp.aggregates.request.RequestEnterprises;
import com.codigo.msexamenexp.entity.DocumentsTypeEntity;
import com.codigo.msexamenexp.repository.DocumentsTypeRepository;
import com.codigo.msexamenexp.repository.EnterprisesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EnterprisesValidations {
    private final DocumentsTypeRepository typeRepository;
    private final EnterprisesRepository enterprisesRepository;

    public EnterprisesValidations(DocumentsTypeRepository typeRepository, EnterprisesRepository enterprisesRepository) {
        this.typeRepository = typeRepository;
        this.enterprisesRepository = enterprisesRepository;
    }

    public boolean validateInput(RequestEnterprises requestEnterprises){
        if(requestEnterprises == null){
            return false;
        }
        log.info("DATO1: " + requestEnterprises.getDocumentsTypeEntity()+ " - DATO2: " + typeRepository.findByCodType(Constants.COD_TYPE_RUC).getIdDocumentsType());
        if(requestEnterprises.getDocumentsTypeEntity() != typeRepository.findByCodType(Constants.COD_TYPE_RUC).getIdDocumentsType()
            || requestEnterprises.getNumDocument().length() != Constants.LENGTH_RUC){
            return false;
        }
        if(isNullOrEmpty(requestEnterprises.getNumDocument())){
            return false;
        }
        if(existsEnterprise(requestEnterprises.getNumDocument())){
            return false;
        }

        return true;
    }
    public boolean validateInputUpdate(RequestEnterprises requestEnterprises){
        if(requestEnterprises == null){
            return false;
        }
        if(requestEnterprises.getDocumentsTypeEntity() != typeRepository.findByCodType(Constants.COD_TYPE_RUC).getIdDocumentsType()
                || requestEnterprises.getNumDocument().length() != Constants.LENGTH_RUC){
            return false;
        }
        if(isNullOrEmpty(requestEnterprises.getBusinessName()) && isNullOrEmpty(requestEnterprises.getNumDocument()) && isNullOrEmpty(requestEnterprises.getTradeName())){
            return false;
        }
        return true;
    }
    public boolean existsEnterprise(String numDocument){
        return enterprisesRepository.existsByNumDocument(numDocument);
    }
    public boolean isNullOrEmpty(String data){
        return data == null || data.isEmpty();
    }
}
