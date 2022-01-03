package com.example.comptecqrses.query.services;


import com.example.comptecqrses.commonapi.enums.OperationType;
import com.example.comptecqrses.commonapi.events.AccountActivatedEvent;
import com.example.comptecqrses.commonapi.events.AccountCreatedEvent;
import com.example.comptecqrses.commonapi.events.AccountCreditedEvent;
import com.example.comptecqrses.commonapi.events.AccountDebitedEvent;
import com.example.comptecqrses.commonapi.queries.GetAccountByIdQuery;
import com.example.comptecqrses.commonapi.queries.GetAllAccountsQuery;
import com.example.comptecqrses.query.entities.Account;
import com.example.comptecqrses.query.entities.Operation;
import com.example.comptecqrses.query.repositories.AccountRepository;
import com.example.comptecqrses.query.repositories.OperationRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j   // for logging
@Transactional
public class AccountServiceHandler {
    private AccountRepository accountRepository;
    private OperationRepository operationRepository;

    public AccountServiceHandler(AccountRepository accountRepository, OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    @EventHandler
    public void on(AccountCreatedEvent event)
    {
        log.info("AccountCreatedEvent recieved!");
        Account account = new Account();
        account.setId(event.getId());
        account.setAccountStatus(event.getAccountStatus());
        account.setBalance(event.getInitialeBalance());
        account.setCurrency(event.getCurrency());

         accountRepository.save(account);
    }

    @EventHandler
    public void on(AccountActivatedEvent event)
    {
        log.info("AccountActivatedEvent recieved!");
        Account account = accountRepository.findById(event.getId()).get();
        account.setAccountStatus(event.getAccountStatus());
        accountRepository.save(account);
    }

    @EventHandler
    public void on(AccountCreditedEvent event)
    {
        log.info("AccountCreditedEvent recieved!");
        Account account = accountRepository.findById(event.getId()).get();
        Operation operation = new Operation();
        operation.setOperationType(OperationType.CREDIT);
        operation.setAmount(event.getAmount());
        operation.setDate(new Date());  // not this date -- should put the date of the operation
        operation.setAccount(account);
        operationRepository.save(operation);
        account.setBalance(account.getBalance() + event.getAmount());
        accountRepository.save(account);
    }


    @EventHandler
    public void on(AccountDebitedEvent event)
    {
        log.info("AccountDebitedEvent recieved!");
        Account account = accountRepository.findById(event.getId()).get();
        Operation operation = new Operation();
        operation.setOperationType(OperationType.DEBIT);
        operation.setAmount(event.getAmount());
        operation.setDate(new Date());  // not this date -- should put the date of the operation
        operation.setAccount(account);
        operationRepository.save(operation);
        account.setBalance(account.getBalance() - event.getAmount());
        accountRepository.save(account);
    }

    @QueryHandler
    public List<Account> on(GetAllAccountsQuery query)
    {
        return accountRepository.findAll();
    }

    @QueryHandler
    public Account on(GetAccountByIdQuery query)
    {
        return accountRepository.findById(query.getId()).get();
    }

}
