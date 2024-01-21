package com.codigo.msexamenexp.service.impl;

import com.codigo.msexamenexp.aggregates.constants.Constants;
import com.codigo.msexamenexp.aggregates.request.RequestEnterprises;
import com.codigo.msexamenexp.aggregates.response.ResponseBase;
import com.codigo.msexamenexp.aggregates.response.ResponseSunat;
import com.codigo.msexamenexp.config.RedisService;
import com.codigo.msexamenexp.entity.DocumentsTypeEntity;
import com.codigo.msexamenexp.entity.EnterprisesEntity;
import com.codigo.msexamenexp.entity.EnterprisesTypeEntity;
import com.codigo.msexamenexp.entity.common.Audit;
import com.codigo.msexamenexp.feignclient.SunatClient;
import com.codigo.msexamenexp.repository.DocumentsTypeRepository;
import com.codigo.msexamenexp.repository.EnterprisesRepository;
import com.codigo.msexamenexp.repository.EnterprisesTypeRepository;
import com.codigo.msexamenexp.util.EnterprisesValidations;
import com.codigo.msexamenexp.util.Util;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

class EnterprisesServiceImplTest {
    @Mock
    EnterprisesRepository enterprisesRepository;
    @Mock
    EnterprisesValidations enterprisesValidations;
    @Mock
    DocumentsTypeRepository documentsTypeRepository;
    @Mock
    EnterprisesTypeRepository enterprisesTypeRepository;
    @Mock
    Util util;
    @Mock
    SunatClient sunatClient;
    @Mock
    RedisService redisService;

    @InjectMocks
    EnterprisesServiceImpl enterprisesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        enterprisesService = new EnterprisesServiceImpl(
                enterprisesRepository,
                enterprisesValidations,
                documentsTypeRepository,
                enterprisesTypeRepository,
                util,
                sunatClient,
                redisService
        );
    }

    @Test
    void getInfoSunatSucceed() {
        String numero = "20602356459";
        ResponseSunat responseSunat = new ResponseSunat(
                "POLLOS GORDOS S.A.C.",
                "6",
                "20602356459",
                "ACTIVO",
                "HABIDO",
                "AV. PETIT THOUARS NRO 9988 URB. VALLECITO ",
                "123456",
                "AV.",
                "PETIT THOUARS",
                "URB.",
                "VALLECITO",
                "9988",
                "-",
                "-",
                "-",
                "-",
                "-",
                "MIRAFLORES",
                "AREQUIPA",
                "AREQUIPA",
                true
        );
        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_SUCCESS, Constants.MESS_SUCCESS, Optional.of(responseSunat));

        Mockito.when(sunatClient.getInfoSunat(anyString(), anyString())).thenReturn(responseSunat);

        ResponseBase responseBaseObtained = enterprisesService.getInfoSunat(numero);

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(responseBaseObtained.getData(), responseBaseExpected.getData());
    }

    @Test
    void getInfoSunatError() {
        String numero = "20602356459";

        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_ERROR_DATA_NOT, Constants.MESS_NON_DATA_SUNAT, Optional.empty());

        Mockito.when(sunatClient.getInfoSunat(anyString(), anyString())).thenReturn(null);

        ResponseBase responseBaseObtained = enterprisesService.getInfoSunat(numero);

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(responseBaseObtained.getData(), responseBaseExpected.getData());
    }

    @Test
    void createEnterpriseSucceed() {
        boolean validateEnterprise = true;
        RequestEnterprises requestEnterprises = new RequestEnterprises(
                "20602356459",
                "Los Pollos Gordos",
                "POLLOS GORDOS S.A.C.",
                2,
                3
        );
        ResponseSunat responseSunat = new ResponseSunat(
                "POLLOS GORDOS S.A.C.",
                "6",
                "20602356459",
                "ACTIVO",
                "HABIDO",
                "AV. PETIT THOUARS NRO 9988 URB. VALLECITO ",
                "123456",
                "AV.",
                "PETIT THOUARS",
                "URB.",
                "VALLECITO",
                "9988",
                "-",
                "-",
                "-",
                "-",
                "-",
                "MIRAFLORES",
                "AREQUIPA",
                "AREQUIPA",
                true
        );
        EnterprisesTypeEntity enterprisesTypeEntity = new EnterprisesTypeEntity(
                2,
                "02",
                "SAC",
                1
        );
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity(
                3,
                "06",
                "RUC",
                1
        );
        EnterprisesEntity enterprisesEntity = new EnterprisesEntity(
                1,
                "20602356459",
                "Los Pollos Gordos",
                "POLLOS GORDOS S.A.C.",
                1,
                enterprisesTypeEntity,
                documentsTypeEntity
        );
        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_SUCCESS, Constants.MESS_SUCCESS, Optional.of(enterprisesEntity));

        EnterprisesServiceImpl spy = Mockito.spy(enterprisesService);

        Mockito.when(enterprisesValidations.validateInput(Mockito.any(RequestEnterprises.class))).thenReturn(validateEnterprise);
        Mockito.doReturn(responseSunat).when(spy).getExecutionSunat(anyString());
        Mockito.when(enterprisesTypeRepository.findById(anyInt())).thenReturn(Optional.of(enterprisesTypeEntity));
        Mockito.when(enterprisesRepository.save(Mockito.any(EnterprisesEntity.class))).thenReturn(enterprisesEntity);
        Mockito.when(documentsTypeRepository.findById(anyInt())).thenReturn(Optional.of(documentsTypeEntity));
        ResponseBase responseBaseObtained = spy.createEnterprise(requestEnterprises);

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
    }

    @Test
    void createEnterpriseError() {
        RequestEnterprises requestEnterprises = new RequestEnterprises(
                "20602356459",
                "Los Pollos Gordos",
                "POLLOS GORDOS S.A.C.",
                2,
                1
        );

        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_ERROR_DATA_INPUT,Constants.MESS_ERROR_DATA_NOT_VALID,Optional.empty());
        EnterprisesServiceImpl spy = Mockito.spy(enterprisesService);
        ResponseBase responseBaseObtained = spy.createEnterprise(requestEnterprises);

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(responseBaseObtained.getData(), responseBaseExpected.getData());
    }
    @Test
    void findOneEnterpriseSucceedWithRedisCache() {
        String doc = "20602356459";
        EnterprisesTypeEntity enterprisesTypeEntity = new EnterprisesTypeEntity(
                2,
                "02",
                "SAC",
                1
        );
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity(
                3,
                "06",
                "RUC",
                1
        );
        EnterprisesEntity enterprisesEntity = new EnterprisesEntity(
                1,
                "20602356459",
                "Los Pollos Gordos",
                "POLLOS GORDOS S.A.C.",
                1,
                enterprisesTypeEntity,
                documentsTypeEntity
        );
        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(enterprisesEntity));

        String dataRedis = Util.convertToJsonEntity(enterprisesEntity);
        Mockito.when(redisService.getValueByKey(Mockito.anyString())).thenReturn(dataRedis);

        ResponseBase responseBaseObtained = enterprisesService.findOneEnterprise(doc);

        Optional<?> dataObtained = responseBaseObtained.getData();
        EnterprisesEntity enterprisesEntity1 = (EnterprisesEntity) dataObtained.get();

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(enterprisesEntity1.getIdEnterprises(), enterprisesEntity.getIdEnterprises());
        assertEquals(enterprisesEntity1.getBusinessName(), enterprisesEntity.getBusinessName());
        assertEquals(enterprisesEntity1.getTradeName(), enterprisesEntity.getTradeName());
        assertEquals(enterprisesEntity1.getNumDocument(), enterprisesEntity.getNumDocument());
        assertEquals(enterprisesEntity1.getStatus(), enterprisesEntity.getStatus());
    }
    @Test
    void findOneEnterpriseError() {
        String doc = "20602356459";
        EnterprisesTypeEntity enterprisesTypeEntity = new EnterprisesTypeEntity(
                2,
                "02",
                "SAC",
                1
        );
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity(
                3,
                "06",
                "RUC",
                1
        );
        EnterprisesEntity enterprisesEntity = new EnterprisesEntity(
                1,
                "20602356459",
                "Los Pollos Gordos",
                "POLLOS GORDOS S.A.C.",
                1,
                enterprisesTypeEntity,
                documentsTypeEntity
        );
        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_NON_DATA, Optional.empty());

        ResponseBase responseBaseObtained = enterprisesService.findOneEnterprise(doc);

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(responseBaseObtained.getData(), responseBaseExpected.getData());
    }

    @Test
    void findAllEnterprisesSucceed() {
        EnterprisesTypeEntity enterprisesTypeEntity = new EnterprisesTypeEntity(
                2,
                "02",
                "SAC",
                1
        );
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity(
                3,
                "06",
                "RUC",
                1
        );
        EnterprisesEntity enterprisesEntity1 = new EnterprisesEntity(
                1,
                "20602356459",
                "Los Pollos Gordos",
                "POLLOS GORDOS S.A.C.",
                1,
                enterprisesTypeEntity,
                documentsTypeEntity
        );
        EnterprisesEntity enterprisesEntity2 = new EnterprisesEntity(
                2,
                "20602345987",
                "Computec",
                "COMPUTEC STORE S.A.C.",
                1,
                enterprisesTypeEntity,
                documentsTypeEntity
        );
        List<EnterprisesEntity> listEntities = List.of(enterprisesEntity1, enterprisesEntity2);
        Mockito.when(enterprisesRepository.findAll()).thenReturn(listEntities);
        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(listEntities));

        ResponseBase responseBaseObtained = enterprisesService.findAllEnterprises();

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(responseBaseObtained.getData(), responseBaseExpected.getData());
    }

    @Test
    void findAllEnterprisesError() {
        List<EnterprisesEntity> listEntities = new ArrayList<>();

        Mockito.when(enterprisesRepository.findAll()).thenReturn(listEntities);
        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_ZERO_ROWS, Optional.empty());

        ResponseBase responseBaseObtained = enterprisesService.findAllEnterprises();

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(responseBaseObtained.getData(), responseBaseExpected.getData());
    }

    @Test
    void updateEnterpriseSucceed() {
        boolean existsEnterprise = true;
        boolean validationEntity = true;
        int id = 2;

        RequestEnterprises requestEnterprises = new RequestEnterprises(
                "20602345987",
                "Computec Services and Stores",
                "COMPUTEC S.A.C.",
                2,
                3
        );

        EnterprisesTypeEntity enterprisesTypeEntity = new EnterprisesTypeEntity(
                2,
                "02",
                "SAC",
                1
        );
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity(
                3,
                "06",
                "RUC",
                1
        );

        EnterprisesEntity enterprisesEntity = new EnterprisesEntity(
                id,
                "20602345987",
                "Computec",
                "COMPUTEC STORE S.A.C.",
                1,
                enterprisesTypeEntity,
                documentsTypeEntity
        );
        EnterprisesEntity enterprisesUpdate = new EnterprisesEntity(
                id,
                "20602345987",
                "Computec Services and Stores",
                "COMPUTEC S.A.C.",
                1,
                enterprisesTypeEntity,
                documentsTypeEntity
        );

        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,Optional.of(enterprisesUpdate));
        EnterprisesServiceImpl spy = Mockito.spy(enterprisesService);

        Mockito.when(enterprisesRepository.existsById(anyInt())).thenReturn(existsEnterprise);
        Mockito.when(enterprisesRepository.findById(anyInt())).thenReturn(Optional.of(enterprisesEntity));
        Mockito.when(enterprisesValidations.validateInputUpdate(Mockito.any(RequestEnterprises.class))).thenReturn(validationEntity);
        Mockito.when(enterprisesTypeRepository.findById(anyInt())).thenReturn(Optional.of(enterprisesTypeEntity));
        Mockito.when(enterprisesRepository.save(Mockito.any(EnterprisesEntity.class))).thenReturn(enterprisesUpdate);

        ResponseBase responseBaseObtained = spy.updateEnterprise(id, requestEnterprises);

        Optional<?> dataObtained = responseBaseObtained.getData();
        EnterprisesEntity enterprisesEntity1 = (EnterprisesEntity) dataObtained.get();

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(enterprisesEntity1.getIdEnterprises(), enterprisesUpdate.getIdEnterprises());
        assertEquals(enterprisesEntity1.getBusinessName(), enterprisesUpdate.getBusinessName());
        assertEquals(enterprisesEntity1.getTradeName(), enterprisesUpdate.getTradeName());
        assertEquals(enterprisesEntity1.getNumDocument(), enterprisesUpdate.getNumDocument());
        assertEquals(enterprisesEntity1.getStatus(), enterprisesUpdate.getStatus());
    }

    @Test
    void updateEnterpriseError() {
        int id = 2;

        RequestEnterprises requestEnterprises = new RequestEnterprises(
                "20602345987",
                "Computec Services and Stores",
                "COMPUTEC S.A.C.",
                2,
                3
        );
        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_ERROR_EXIST, Constants.MESS_ERROR_NOT_UPDATE, Optional.empty());

        ResponseBase responseBaseObtained = enterprisesService.updateEnterprise(id, requestEnterprises);

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(responseBaseObtained.getData(), responseBaseExpected.getData());
    }

    @Test
    void deleteEnterpriseSucceed() {
        int id = 2;
        EnterprisesTypeEntity enterprisesTypeEntity = new EnterprisesTypeEntity(
                2,
                "02",
                "SAC",
                1
        );
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity(
                3,
                "06",
                "RUC",
                1
        );

        EnterprisesEntity enterprisesEntity = new EnterprisesEntity(
                id,
                "20602345987",
                "Computec",
                "COMPUTEC STORE S.A.C.",
                1,
                enterprisesTypeEntity,
                documentsTypeEntity
        );
        EnterprisesEntity enterprisesDelete = new EnterprisesEntity(
                id,
                "20602345987",
                "Computec",
                "COMPUTEC STORE S.A.C.",
                0,
                enterprisesTypeEntity,
                documentsTypeEntity
        );
        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_SUCCESS, Constants.MESS_SUCCESS, Optional.of(enterprisesDelete));

        Mockito.when(enterprisesRepository.findById(anyInt())).thenReturn(Optional.of(enterprisesEntity));
        Mockito.when(enterprisesRepository.save(Mockito.any(EnterprisesEntity.class))).thenReturn(enterprisesDelete);

        ResponseBase responseBaseObtained = enterprisesService.deleteEnterprise(id);

        Optional<?> dataObtained = responseBaseObtained.getData();
        EnterprisesEntity enterprisesEntity1 = (EnterprisesEntity) dataObtained.get();

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(enterprisesEntity1.getIdEnterprises(), enterprisesDelete.getIdEnterprises());
        assertEquals(enterprisesEntity1.getBusinessName(), enterprisesDelete.getBusinessName());
        assertEquals(enterprisesEntity1.getTradeName(), enterprisesDelete.getTradeName());
        assertEquals(enterprisesEntity1.getNumDocument(), enterprisesDelete.getNumDocument());
        assertEquals(enterprisesEntity1.getStatus(), enterprisesDelete.getStatus());
    }

    @Test
    void deleteEnterpriseError() {
        int id = 2;

        ResponseBase responseBaseExpected = new ResponseBase(Constants.CODE_ERROR_EXIST, Constants.MESS_NON_DATA, Optional.empty());

        Mockito.when(enterprisesRepository.findById(anyInt())).thenReturn(Optional.empty());

        ResponseBase responseBaseObtained = enterprisesService.deleteEnterprise(id);

        assertEquals(responseBaseObtained.getCode(), responseBaseExpected.getCode());
        assertEquals(responseBaseObtained.getMessage(), responseBaseExpected.getMessage());
        assertEquals(responseBaseObtained.getData(), responseBaseExpected.getData());
    }

    @Test
    void getExecutionSunat() {
        String numero = "20602356459";
        ResponseSunat responseSunat = new ResponseSunat(
                "POLLOS GORDOS S.A.C.",
                "6",
                "20602356459",
                "ACTIVO",
                "HABIDO",
                "AV. PETIT THOUARS NRO 9988 URB. VALLECITO ",
                "123456",
                "AV.",
                "PETIT THOUARS",
                "URB.",
                "VALLECITO",
                "9988",
                "-",
                "-",
                "-",
                "-",
                "-",
                "MIRAFLORES",
                "AREQUIPA",
                "AREQUIPA",
                true
        );
        Mockito.when(sunatClient.getInfoSunat(anyString(), anyString())).thenReturn(responseSunat);

        ResponseSunat responseSunatObtained = enterprisesService.getExecutionSunat(numero);

        assertEquals(responseSunat, responseSunatObtained);
    }
}