package es.upm.miw.apaw.functionaltests.training;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;


@FeignClient(name = "training-plans", url = "${test.api.apaw-practice}")
public interface TrainingPlanClient {
    
    @PostMapping("/training/training-plans") 
    TrainingPlan create(@RequestBody CreationTrainingPlan creation);

    @GetMapping("/training/training-plans") 
    List<TrainingPlan> find(@SpringQueryMap TrainingPlanFindCriteria criteria);
}
