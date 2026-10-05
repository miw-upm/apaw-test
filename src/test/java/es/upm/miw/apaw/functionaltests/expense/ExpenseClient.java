package es.upm.miw.apaw.functionaltests.expense;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "expenses", url = "${test.api.apaw-practice}")
public interface ExpenseClient {

    @GetMapping("/expense/expenses")
    List<Expense> find(@SpringQueryMap ExpenseFindCriteria criteria);

    @PostMapping("/expense/expenses")
    Expense create(@RequestBody CreationExpense creation);
}