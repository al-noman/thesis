package de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.controller;

import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.configuration.RabbitMQConfigConstants;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.converters.GenericMapper;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.dtos.BeverageDTO;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.dtos.CustomerOrderDTO;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.entities.BeverageEntity;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.exception.BadRequestParamValueException;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.exception.EntityNotFoundException;
import de.uniba.dsg.dsam.spring.boot.beveragestorespringboot.restful.backend.service.CrudService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/customer_order")
public class CustomerOrderController {

    private final CrudService<BeverageEntity> beverageService;
    private final GenericMapper<BeverageEntity, BeverageDTO> beverageConverter;
    private final RabbitMQConfigConstants configConstants;
    private final RabbitTemplate rabbitTemplate;

    public CustomerOrderController(CrudService<BeverageEntity> beverageService,
                                   GenericMapper<BeverageEntity, BeverageDTO> beverageConverter,
                                   RabbitMQConfigConstants configConstants,
                                   RabbitTemplate rabbitTemplate) {
        this.beverageService = beverageService;
        this.beverageConverter = beverageConverter;
        this.configConstants = configConstants;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<CustomerOrderDTO> addOne(@RequestBody @NotEmpty List<@Valid CustomerOrderDTO> dtos){

        dtos.forEach(customerOrderDTO -> {
            int beverageId = customerOrderDTO.getBeverageId();
            BeverageEntity beverageEntity = beverageService.getOne(beverageId)
                    .orElseThrow(() -> new EntityNotFoundException(beverageId));
            BeverageDTO beverageDTO = beverageConverter.convertEntityToDTO(beverageEntity);

            if (beverageDTO.getQuantity() < customerOrderDTO.getOrderAmount()){
                throw new BadRequestParamValueException("orderAmount", "customer order amount exceeds " +
                        "available beverage quantity");
            }

            beverageDTO.setQuantity(beverageDTO.getQuantity() - customerOrderDTO.getOrderAmount());
            customerOrderDTO.setBeverageDTO(beverageDTO);
            customerOrderDTO.setIssueDate(new Date());

            // Publishing to RabbitMQ
            CustomerOrderDTO receivedDto = (CustomerOrderDTO) rabbitTemplate.convertSendAndReceive(
                    configConstants.getExchange(),
                    configConstants.getRoutingKey(),
                    customerOrderDTO);
            customerOrderDTO.setId(receivedDto.getId());
            customerOrderDTO.setVersion(receivedDto.getVersion());

            this.beverageService.updateOne(this.beverageConverter.convertDTOToEntity(beverageDTO));
        });
        return dtos;
    }
}
