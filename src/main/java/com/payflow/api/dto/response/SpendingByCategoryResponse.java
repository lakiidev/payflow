package com.payflow.api.dto.response;


public record SpendingByCategoryResponse(String transactionType,
                                         long totalCents,
                                         long count)
{
}
