package com.codigo.msexamenexp.controller;


import com.codigo.msexamenexp.aggregates.request.RequestEnterprises;
import com.codigo.msexamenexp.aggregates.response.ResponseBase;
import com.codigo.msexamenexp.service.EnterprisesService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/enterprises")
public class EnterprisesController {
    private final EnterprisesService enterprisesService;

    public EnterprisesController(EnterprisesService enterprisesService) {
        this.enterprisesService = enterprisesService;
    }

    @GetMapping("{numero}")
    public ResponseBase getInfoSunat(@PathVariable String numero) {
        return enterprisesService.getInfoSunat(numero);
    }

    @PostMapping
    public ResponseBase createEnterprise(@RequestBody RequestEnterprises requestEnterprises){
        return enterprisesService.createEnterprise(requestEnterprises);
    }
    @GetMapping("dat/{doc}")
    public ResponseBase findOne(@PathVariable String doc){
        return enterprisesService.findOneEnterprise(doc);
    }
    @GetMapping()
    public ResponseBase findAll(){
        return enterprisesService.findAllEnterprises();
    }
    @PatchMapping("/{id}")
    public ResponseBase updateEnterprises(@PathVariable int id, @RequestBody RequestEnterprises requestEnterprises){
        return enterprisesService.updateEnterprise(id,requestEnterprises);
    }
    @DeleteMapping("/{id}")
    public ResponseBase deleteEnterprises(@PathVariable int id){
        return enterprisesService.deleteEnterprise(id);
    }
}
