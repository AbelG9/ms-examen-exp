package com.codigo.msexamenexp.service.impl;

import com.codigo.msexamenexp.aggregates.request.RequestEnterprises;
import com.codigo.msexamenexp.aggregates.response.ResponseBase;
import com.codigo.msexamenexp.aggregates.constants.Constants;
import com.codigo.msexamenexp.aggregates.response.ResponseSunat;
import com.codigo.msexamenexp.config.RedisService;
import com.codigo.msexamenexp.entity.DocumentsTypeEntity;
import com.codigo.msexamenexp.entity.EnterprisesEntity;
import com.codigo.msexamenexp.entity.EnterprisesTypeEntity;
import com.codigo.msexamenexp.feignClient.SunatClient;
import com.codigo.msexamenexp.repository.DocumentsTypeRepository;
import com.codigo.msexamenexp.repository.EnterprisesRepository;
import com.codigo.msexamenexp.repository.EnterprisesTypeRepository;
import com.codigo.msexamenexp.service.EnterprisesService;
import com.codigo.msexamenexp.util.EnterprisesValidations;
import com.codigo.msexamenexp.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class EnterprisesServiceImpl implements EnterprisesService {

    private final EnterprisesRepository enterprisesRepository;
    private final EnterprisesValidations enterprisesValidations;
    private final DocumentsTypeRepository documentsTypeRepository;
    private final EnterprisesTypeRepository enterprisesTypeRepository;
    private final Util util;
    private final SunatClient sunatClient;
    private final RedisService redisService;

    @Value("${token.api.sunat}")
    private String tokenSunat;

    @Value("${time.expiration.sunat.info}")
    private String timeExpirationSunatInfo;

    public EnterprisesServiceImpl(EnterprisesRepository enterprisesRepository, EnterprisesValidations enterprisesValidations, DocumentsTypeRepository documentsTypeRepository, EnterprisesTypeRepository enterprisesTypeRepository, Util util, SunatClient sunatClient, RedisService redisService) {
        this.enterprisesRepository = enterprisesRepository;
        this.enterprisesValidations = enterprisesValidations;
        this.documentsTypeRepository = documentsTypeRepository;
        this.enterprisesTypeRepository = enterprisesTypeRepository;
        this.util = util;
        this.sunatClient = sunatClient;
        this.redisService = redisService;
    }

    @Override
    public ResponseBase getInfoSunat(String numero) {
        ResponseSunat sunat = getExecutionSunat(numero);
        if (sunat != null) {
            return new ResponseBase(Constants.CODE_SUCCESS, Constants.MESS_SUCCESS, Optional.of(sunat));
        } else {
            return new ResponseBase(Constants.CODE_ERROR_DATA_NOT, Constants.MESS_NON_DATA_SUNAT, Optional.empty());
        }
    }

    @Override
    public ResponseBase createEnterprise(RequestEnterprises requestEnterprises) {
        boolean validate = enterprisesValidations.validateInput(requestEnterprises);
        if(validate){
            EnterprisesEntity enterprisesEntity = getEntity(requestEnterprises);
            enterprisesRepository.save(enterprisesEntity);
            String redisData = util.convertToJsonEntity(enterprisesEntity);
            redisService.saveKeyValue(Constants.REDIS_KEY_INFO_SUNAT+enterprisesEntity.getNumDocument(),redisData,Integer.valueOf(timeExpirationSunatInfo));
            return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(enterprisesEntity));
        }else{
            return new ResponseBase(Constants.CODE_ERROR_DATA_INPUT,Constants.MESS_ERROR_DATA_NOT_VALID,null);
        }
    }

    @Override
    public ResponseBase findOneEnterprise(String doc) {
        String redisCache = redisService.getValueByKey(Constants.REDIS_KEY_INFO_SUNAT+doc);
        if(redisCache != null){
            EnterprisesEntity enterprisesEntity = util.convertFromJson(redisCache, EnterprisesEntity.class);
            return new ResponseBase(Constants.CODE_SUCCESS, Constants.MESS_SUCCESS, Optional.of(enterprisesEntity));
        } else {
            EnterprisesEntity enterprisesEntity = enterprisesRepository.findByNumDocument(doc);
            if (enterprisesEntity != null) {
                String redisData = util.convertToJsonEntity(enterprisesEntity);
                redisService.saveKeyValue(Constants.REDIS_KEY_INFO_SUNAT+doc,redisData,Integer.valueOf(timeExpirationSunatInfo));
                return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(enterprisesEntity));
            } else {
                return new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_NON_DATA, Optional.empty());
            }
        }
    }

    @Override
    public ResponseBase findAllEnterprises() {
        Optional allEnterprises = Optional.of(enterprisesRepository.findAll());
        if(allEnterprises.isPresent()){
            return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,allEnterprises);
        }
        return new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_ZERO_ROWS,Optional.empty());
    }

    @Override
    public ResponseBase updateEnterprise(Integer id, RequestEnterprises requestEnterprises) {
        boolean existsEnterprise = enterprisesRepository.existsById(id);
        if (existsEnterprise) {
            Optional<EnterprisesEntity> enterprises = enterprisesRepository.findById(id);
            boolean validationEntity = enterprisesValidations.validateInputUpdate(requestEnterprises);
            if(validationEntity){
                EnterprisesEntity enterprisesUpdate = getEntityUpdate(requestEnterprises,enterprises.get());
                enterprisesRepository.save(enterprisesUpdate);
                return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,Optional.of(enterprisesUpdate));
            }else {
                return new ResponseBase(Constants.CODE_ERROR_DATA_INPUT,Constants.MESS_ERROR_DATA_NOT_VALID,Optional.empty());
            }
        } else {
            return new ResponseBase(Constants.CODE_ERROR_EXIST, Constants.MESS_ERROR_NOT_UPDATE, Optional.empty());
        }
    }

    @Override
    public ResponseBase delete(Integer id) {
        Optional<EnterprisesEntity> enterprises = enterprisesRepository.findById(id);
        if (enterprises.isPresent()) {
            EnterprisesEntity enterprisesDelete = getEntityDelete(enterprises.get());
            enterprisesRepository.save(enterprisesDelete);
            return new ResponseBase(Constants.CODE_SUCCESS, Constants.MESS_SUCCESS, Optional.of(enterprisesDelete));
        } else {
            return new ResponseBase(Constants.CODE_ERROR_EXIST, Constants.MESS_NON_DATA, Optional.empty());
        }
    }

    private EnterprisesEntity getEntity(RequestEnterprises requestEnterprises){
        EnterprisesEntity entity = new EnterprisesEntity();
        entity.setStatus(Constants.STATUS_ACTIVE);

        ResponseSunat responseSunat = getExecutionSunat(requestEnterprises.getNumDocument());
        if (responseSunat != null) {
            entity.setNumDocument(responseSunat.getNumeroDocumento());
            entity.setBusinessName(responseSunat.getRazonSocial());
            entity.setTradeName(requestEnterprises.getTradeName() != null ? requestEnterprises.getTradeName() : responseSunat.getRazonSocial());
        }

        entity.setEnterprisesTypeEntity(getEnterprisesType(requestEnterprises));
        entity.setDocumentsTypeEntity(getDocumentsType(requestEnterprises));
        entity.setUserCreate(Constants.AUDIT_ADMIN);
        entity.setDateCreate(getTimestamp());
        return entity;
    }
    private EnterprisesEntity getEntityUpdate(RequestEnterprises requestEnterprises, EnterprisesEntity enterprisesEntity){
        enterprisesEntity.setNumDocument(requestEnterprises.getNumDocument());
        enterprisesEntity.setBusinessName(requestEnterprises.getBusinessName());
        enterprisesEntity.setTradeName(requestEnterprises.getTradeName());
        enterprisesEntity.setEnterprisesTypeEntity(getEnterprisesType(requestEnterprises));
        enterprisesEntity.setUserModif(Constants.AUDIT_ADMIN);
        enterprisesEntity.setDateModif(getTimestamp());
        return enterprisesEntity;
    }
    private EnterprisesEntity getEntityDelete(EnterprisesEntity enterprisesEntity){
        enterprisesEntity.setStatus(Constants.STATUS_INACTIVE);
        enterprisesEntity.setUserDelete(Constants.AUDIT_ADMIN);
        enterprisesEntity.setDateDelete(getTimestamp());
        return enterprisesEntity;
    }

    private EnterprisesTypeEntity getEnterprisesType(RequestEnterprises requestEnterprises){
        EnterprisesTypeEntity typeEntity = enterprisesTypeRepository.findById(requestEnterprises.getEnterprisesTypeEntity()).get();
        return typeEntity;
    }

    private DocumentsTypeEntity getDocumentsType(RequestEnterprises requestEnterprises){
        DocumentsTypeEntity typeEntity = documentsTypeRepository.findByCodType(Constants.COD_TYPE_RUC);
        return typeEntity;
    }

    private Timestamp getTimestamp(){
        long currentTime = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(currentTime);
        return timestamp;
    }
    public ResponseSunat getExecutionSunat(String numero){
        String authorization = "Bearer "+tokenSunat;
        ResponseSunat responseSunat = sunatClient.getInfoSunat(numero, authorization);
        return responseSunat;
    }
}
