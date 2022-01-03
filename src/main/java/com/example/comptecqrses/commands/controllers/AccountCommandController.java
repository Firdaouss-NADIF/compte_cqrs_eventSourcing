package com.example.comptecqrses.commands.controllers;

import com.example.comptecqrses.commonapi.commands.CreateAccountCommand;
import com.example.comptecqrses.commonapi.commands.CreditAccountCommand;
import com.example.comptecqrses.commonapi.commands.DebitAccountCommand;
import com.example.comptecqrses.commonapi.dtos.CreateAccountRequestDto;
import com.example.comptecqrses.commonapi.dtos.CreditAccountRequestDto;
import com.example.comptecqrses.commonapi.dtos.DebitAccountRequestDto;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@RestController
@RequestMapping("/commands/account")
public class AccountCommandController  {

    private CommandGateway commandGateway;
    private EventStore eventStore;

    public AccountCommandController(CommandGateway commandGateway, EventStore eventStore) {
        this.commandGateway = commandGateway;
        this.eventStore = eventStore;
    }

    @PostMapping("/create")
    public CompletableFuture<String> createAccount(@RequestBody CreateAccountRequestDto createAccountRequestDto)
    {
        CompletableFuture<String> commandResponse = commandGateway.send(new CreateAccountCommand(
            UUID.randomUUID().toString(),
            createAccountRequestDto.getInitialeBalance(),
            createAccountRequestDto.getCurrency()
        ));
      return commandResponse;
    }

    @PutMapping("/credit")
    public CompletableFuture<String> creditAccount(@RequestBody CreditAccountRequestDto creditAccountRequestDto)
    {
        CompletableFuture<String> commandResponse = commandGateway.send(new CreditAccountCommand(
            creditAccountRequestDto.getId(),
            creditAccountRequestDto.getAmount(),
            creditAccountRequestDto.getCurrency()
        ));
        return commandResponse;
    }

    @PutMapping("/debit")
    public CompletableFuture<String> debitAccount(@RequestBody DebitAccountRequestDto debitAccountRequestDto)
    {
        CompletableFuture<String> commandResponse = commandGateway.send(new DebitAccountCommand(
            debitAccountRequestDto.getId(),
            debitAccountRequestDto.getAmount(),
            debitAccountRequestDto.getCurrency()
        ));
        return commandResponse;
    }


    @GetMapping("/eventStore/{accountId}")
    public Stream eventStore(@PathVariable String accountId)
    {
       return eventStore.readEvents(accountId).asStream();
    }



     @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception)
     {
         ResponseEntity<String> responseEntity = new ResponseEntity<String>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

         return responseEntity;
     }

}
