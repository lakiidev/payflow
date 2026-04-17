package com.payflow.api.exception;

import com.payflow.api.dto.response.ErrorResponse;
import com.payflow.domain.model.token.InvalidRefreshTokenException;
import com.payflow.domain.model.transaction.CurrencyMismatchException;
import com.payflow.domain.model.transaction.InvalidWalletOperationException;
import com.payflow.domain.model.transaction.TransactionNotFoundException;
import com.payflow.domain.model.user.EmailAlreadyRegisteredException;
import com.payflow.domain.model.wallet.InsufficientBalanceException;
import com.payflow.domain.model.wallet.WalletAlreadyExistsException;
import com.payflow.domain.model.wallet.WalletAlreadyFrozenException;
import com.payflow.domain.model.wallet.WalletNotFoundException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleWalletNotFound(WalletNotFoundException ex) {
        return new ErrorResponse("WALLET_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return new ErrorResponse("CONCURRENT_MODIFICATION", "Resource was modified by another request, please retry");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        return new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handeDuplicateEmail(EmailAlreadyRegisteredException ex) {
        return new ErrorResponse("EMAIL_ALREADY_REGISTERED", ex.getMessage());
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleWalletAlreadyExists(WalletAlreadyExistsException ex) {
        return new ErrorResponse("WALLET_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalState(IllegalStateException ex) {
        return new ErrorResponse("INVALID_WALLET_STATE", ex.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleInsufficientBalance(InsufficientBalanceException ex) {
        return new ErrorResponse("INSUFFICIENT_BALANCE", ex.getMessage());
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleCurrencyMismatch(CurrencyMismatchException ex) {
        return new ErrorResponse("CURRENCY_MISMATCH", ex.getMessage());
    }

    @ExceptionHandler(WalletAlreadyFrozenException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleWalletAlreadyFrozen(WalletAlreadyFrozenException ex) {
        return new ErrorResponse("WALLET_ALREADY_FROZEN", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrity(DataIntegrityViolationException ex) {
        return new ErrorResponse("CONSTRAINT_VIOLATION", "Request violates a data constraint");
    }

    @ExceptionHandler(CannotAcquireLockException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleLockTimeout(CannotAcquireLockException ex) {
        return new ErrorResponse("LOCK_TIMEOUT", "Service is under high load, please retry");
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleSerializationFailure(PessimisticLockingFailureException ex) {
        return new ErrorResponse("SERIALIZATION_FAILURE", "Transaction could not be serialized after retries, please retry");
    }

    @ExceptionHandler(InvalidWalletOperationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleInvalidWalletOperation(InvalidWalletOperationException ex) {
        return new ErrorResponse("INVALID_WALLET_OPERATION", ex.getMessage());
    }
    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTransactionNotFound(TransactionNotFoundException ex) {
        return new ErrorResponse("TRANSACTION_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return new ErrorResponse("INVALID_REFRESH_TOKEN", ex.getMessage());
    }
}