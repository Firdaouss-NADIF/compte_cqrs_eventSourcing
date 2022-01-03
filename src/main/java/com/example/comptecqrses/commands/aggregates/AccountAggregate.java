package com.example.comptecqrses.commands.aggregates;

import com.example.comptecqrses.commonapi.commands.CreateAccountCommand;
import com.example.comptecqrses.commonapi.commands.CreditAccountCommand;
import com.example.comptecqrses.commonapi.commands.DebitAccountCommand;
import com.example.comptecqrses.commonapi.enums.AccountStatus;
import com.example.comptecqrses.commonapi.events.AccountActivatedEvent;
import com.example.comptecqrses.commonapi.events.AccountCreatedEvent;
import com.example.comptecqrses.commonapi.events.AccountCreditedEvent;
import com.example.comptecqrses.commonapi.events.AccountDebitedEvent;
import com.example.comptecqrses.commonapi.exceptions.BalanceInSufficientException;
import com.example.comptecqrses.commonapi.exceptions.NegativeAmountException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class AccountAggregate {
    @AggregateIdentifier
    private String accountId;
    private double balance;
    private String currency;
    private AccountStatus accountStatus;

    public AccountAggregate() {
        // required by AXON
    }

    @CommandHandler  // listener on CreateAccountCommand
    public AccountAggregate(CreateAccountCommand command) {
        if(command.getInitialeBalance() <0) throw new RuntimeException("balance should not be negatif");
        AggregateLifecycle.apply(new AccountCreatedEvent(
            command.getId(),
            command.getInitialeBalance(),
            command.getCurrency(),
            AccountStatus.CREATED
        ));
    }
    @EventSourcingHandler
    public void on(AccountCreatedEvent event)
    {
       this.accountId = event.getId();
       this.balance = event.getInitialeBalance();
       this.currency = event.getCurrency();
       this.accountStatus = event.getAccountStatus();

       AggregateLifecycle.apply(new AccountActivatedEvent(
           event.getId(),
           AccountStatus.ACTIVATED
       ));
    }
    @EventSourcingHandler
    public void on(AccountActivatedEvent event)
    {
        this.accountStatus = event.getAccountStatus();

    }
    @CommandHandler  // listener on CreateAccountCommand
    public void handle(CreditAccountCommand command) {
        if(command.getAmount() <0) throw new NegativeAmountException("amount should not be negative");
        AggregateLifecycle.apply(new AccountCreditedEvent(
            command.getId(),
            command.getAmount(),
            command.getCurrency()
        ));
    }
    @EventSourcingHandler
    public void on(AccountCreditedEvent event)
    {
        this.balance += event.getAmount();
    }
    @CommandHandler
    public void handle(DebitAccountCommand command) {
        if(command.getAmount() <0) throw new NegativeAmountException("amount should not be negative");
        if(balance < command.getAmount() ) throw new BalanceInSufficientException("balance is InSufficient !");
        AggregateLifecycle.apply(new AccountDebitedEvent(
            command.getId(),
            command.getAmount(),
            command.getCurrency()
        ));
    }
    @EventSourcingHandler
    public void on(AccountDebitedEvent event)
    {
        this.balance -= event.getAmount();
    }

}
